package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.state.DetectionStateMetadata;
import main.common.Host;
import main.model.DetectionData;
import main.model.MutualContactGraph;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.lucalanda.botnetdetectioncontract.Util.deserializeObject;
import static com.lucalanda.botnetdetectioncontract.Util.serializeObject;

public class DetectionStateHandler {
    private final String LEDGER_HOST_STATE_KEY_PREFIX = "HOST-";
    private final String LEDGER_MUTUAL_CONTACT_GRAPH_KEY = "MCG";
    private final String LEDGER_BOTS_SET_KEY = "BOTS";

    private final String detectionStateKey;
    private final String detectionStateMetaDataKey;

    private DetectionData detectionDataBackup;
    private int detectionDataBackupHashCode;
    private DetectionData detectionData;
    private int detectionDataHashCode;


    public DetectionStateHandler(String detectionStateKey, String detectionStateMetaDataKey) {
        this.detectionStateKey = detectionStateKey;
        this.detectionStateMetaDataKey = detectionStateMetaDataKey;

        // TODO fix
        //  set both backup and detection data
        this.updateDetectionDataCache(DetectionData.getEmptyData());
        this.updateDetectionDataCache(DetectionData.getEmptyData());
    }

    public void updateDetectionState(ChaincodeStub stub, DetectionData newDetectionData) throws IOException {
        List<Host> oldHostsList = detectionData.getHosts();
        List<Host> newHostsList = newDetectionData.getHosts();
        boolean[] hostIsNewOrUpdated = new boolean[newHostsList.size()];

        for (int i = 0; i < oldHostsList.size(); i++) {
            hostIsNewOrUpdated[i] = oldHostsList.get(i).realHashCode() != newHostsList.get(i).realHashCode();
        }

        for (int i = oldHostsList.size(); i < newHostsList.size(); i++) {
            hostIsNewOrUpdated[i] = true;
        }

        String[] hostKeys = new String[newHostsList.size()];

        for (int i = 0; i < newHostsList.size(); i++) {
            Host host = newHostsList.get(i);
            String hostStateKey = LEDGER_HOST_STATE_KEY_PREFIX + host.getIp();
            hostKeys[i] = hostStateKey;

            if (hostIsNewOrUpdated[i]) {
                byte[] serializedHost = serializeObject(host);
                stub.putState(hostStateKey, serializedHost);
            }
        }

        updateDetectionDataCache(newDetectionData);

        byte[] serializedMutualContactGraph = serializeObject(newDetectionData.getGraph());
        stub.putState(LEDGER_MUTUAL_CONTACT_GRAPH_KEY, serializedMutualContactGraph);

        byte[] serializedBotsSet = serializeObject(newDetectionData.getBotIps());
        stub.putState(LEDGER_BOTS_SET_KEY, serializedBotsSet);

        // TODO make more explicit: it's only working because updateDetectionDataCache computes and updates instance's detectionDataHashCode
        byte[] serializedDetectionStateMetaData = serializeObject(new DetectionStateMetadata(hostKeys, detectionDataHashCode));
        stub.putState(detectionStateMetaDataKey, serializedDetectionStateMetaData);
    }

    public DetectionData getDetectionData(ChaincodeStub stub) throws IOException, ClassNotFoundException {
        DetectionStateMetadata metadata = getDetectionStateMetaData(stub);
        int stateHashCode = metadata.getStateHashCode();

        if (detectionDataHashCode != stateHashCode) {
            if (detectionDataBackupHashCode == stateHashCode) {
                detectionDataHashCode = detectionDataBackupHashCode;
                detectionData = detectionDataBackup;
            } else {
                detectionData = loadDetectionDataFromLedger(stub, metadata);
            }
        }

        detectionDataHashCode = stateHashCode;

        return Util.clone(detectionData);
    }

    public DetectionData loadDetectionDataFromLedger(ChaincodeStub stub, DetectionStateMetadata metadata) throws IOException, ClassNotFoundException {
        String[] hostKeys = metadata.getHostKeys();
        ArrayList<Host> currentHosts = new ArrayList<>(hostKeys.length);

        for (int i = 0; i < hostKeys.length; i++) {
            byte[] serializedHost = stub.getState(hostKeys[i]);
            currentHosts.add(i, deserializeObject(serializedHost));
        }

        byte[] serializedMutualContactGraph = stub.getState(LEDGER_MUTUAL_CONTACT_GRAPH_KEY);
        MutualContactGraph mutualContactGraph = deserializeObject(serializedMutualContactGraph);

        Set<String> botsSet = getBotIpsFromLedger(stub);

        return new DetectionData(currentHosts, mutualContactGraph, botsSet);
    }

    public Set<String> getBotIpsFromLedger(ChaincodeStub stub) throws IOException, ClassNotFoundException {
        byte[] serializedBotsSet = stub.getState(LEDGER_BOTS_SET_KEY);
        return deserializeObject(serializedBotsSet);
    }

    public void setEndorsementPolicyForDetectionData(ChaincodeStub stub) {
        setEndorsementPolicyForKey(stub, detectionStateKey);
    }

    public void setEndorsementPolicyForKey(ChaincodeStub stub, String key) {
        stub.setStateValidationParameter(key, getDetectionStateEndorsementPolicy());
    }

    private void updateDetectionDataCache(DetectionData newDetectionData) {
        this.detectionDataBackup = detectionData;
        this.detectionDataBackupHashCode = detectionDataHashCode;
        this.detectionData = newDetectionData;
        this.detectionDataHashCode = hashCodeForDetectionData(newDetectionData);
    }

    private int hashCodeForDetectionData(DetectionData detectionData) {
        int result = detectionData.getBotIps().hashCode();
        result = 31 * result + detectionData.getGraph().hashCode();

        for(Host h: detectionData.getHosts()) {
            result = 31 * result + h.realHashCode();
        }

        return result;
    }

    private DetectionStateMetadata getDetectionStateMetaData(ChaincodeStub stub) throws IOException, ClassNotFoundException {
        byte[] detectionStateMetaData = stub.getState(detectionStateMetaDataKey);

        return deserializeObject(detectionStateMetaData);
    }

    private byte[] getDetectionStateEndorsementPolicy() {
        StateBasedEndorsement stateBasedEndorsement = StateBasedEndorsementFactory
                .getInstance()
                .newStateBasedEndorsement(new byte[0]);

        stateBasedEndorsement
                .addOrgs(StateBasedEndorsement.RoleType.RoleTypeMember, "Org1MSP", "Org2MSP", "Org3MSP");

        return stateBasedEndorsement.policy();
    }
}
