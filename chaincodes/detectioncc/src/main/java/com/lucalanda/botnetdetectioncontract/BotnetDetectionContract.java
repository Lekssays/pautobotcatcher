package com.lucalanda.botnetdetectioncontract;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.state.NetworkFlowState;
import com.lucalanda.botnetdetectioncontract.model.state.DetectionStateSample;
import main.PeerHunter;
import main.model.DetectionData;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.io.IOException;
import java.util.*;

import static com.lucalanda.botnetdetectioncontract.Util.parseNetworkFlowProtocol;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toCollection;

public final class BotnetDetectionContract {

    private static final String DETECTION_STATE_KEY = "detection-state";
    private static final String DETECTION_STATE_METADATA_KEY = "detection-state-metadata";

    private final NetworkFlowStateSerializer networkFlowStateSerializer = new NetworkFlowStateSerializer();
    private final DetectionStateHandler detectionStateHandler;

    public BotnetDetectionContract(int maxLedgerStateLength) {
        this.detectionStateHandler = new DetectionStateHandler(DETECTION_STATE_KEY, DETECTION_STATE_METADATA_KEY);
    }

    private enum ContractErrors {
        INVALID_NETWORK_FLOW_KEY_DATA,
        ORG_MSP_ID_NOT_FOUND
    }

    private static final int MAX_TIMESTAMP_LENGTH = 20;
    private static final int MAX_ORG_ID_LENGTH = 3;

    private static final Map<String, String> mspIdOrgIdMap = new HashMap<String, String>() {{
        put("Org1MSP", "1");
        put("Org2MSP", "2");
        put("Org3MSP", "3");
    }};

    // for debugging purposes
    private static final List<String> addedNetworkFlowsStates = new ArrayList<>();

    public void initialize(ChaincodeStub stub) throws IOException {
        detectionStateHandler.updateDetectionState(stub, DetectionData.getEmptyData());

        //example of key-level endorsement policy on detection state, it should be applied to every detection-state chunk
        detectionStateHandler.setEndorsementPolicyForDetectionData(stub);

        System.out.println("BotnetDetectionContract initialized");
    }

    public Set<String> performBotnetDetection(final ChaincodeStub stub, final String startRange, final String endRange) throws IOException, ClassNotFoundException {
        NetworkFlow[] networkFlows = getNetworkFlowsByRange(stub, startRange, endRange);

        DetectionData currentDetectionData = detectionStateHandler.getDetectionData(stub);

        DetectionData newDetectionData = new PeerHunter(currentDetectionData).computeDetectionData(convertNetworkFlows(networkFlows));
        detectionStateHandler.updateDetectionState(stub, newDetectionData);

        new Thread(System::gc).start();

        return newDetectionData.getBotIps();
    }

    public Set<String> getBotIps(final ChaincodeStub stub) throws IOException, ClassNotFoundException {
        return detectionStateHandler.getBotIpsFromLedger(stub);
    }

    public DetectionStateSample getDetectionState(final ChaincodeStub stub) throws IOException, ClassNotFoundException {
        return new DetectionStateSample(detectionStateHandler.getDetectionData(stub));
    }

    // DEBUGGING purposes
    public void clearState(final ChaincodeStub stub) throws IOException {
        for(String state: addedNetworkFlowsStates) {
            stub.delState(state);
        }

        addedNetworkFlowsStates.clear();

        detectionStateHandler.updateDetectionState(stub, DetectionData.getEmptyData());
    }

    public NetworkFlow[] addSingleNetworkFlow(final ChaincodeStub stub, final String timestamp,
                                              final String ipSource, final String ipDestination,
                                              final String protocol, final String bytesPerPacketIn, final String bytesPerPacketOut) {

        String[] errors = validateKeyData(timestamp);

        if (errors.length > 0) {
            throw new RuntimeException(String.join(",", errors) + " - " + ContractErrors.INVALID_NETWORK_FLOW_KEY_DATA.toString());
        }

        String orgId = getTxCreatorOrgId(stub);

        NetworkFlow networkFlow = new NetworkFlow(ipSource, ipDestination, parseNetworkFlowProtocol(protocol), parseInt(bytesPerPacketIn), parseInt(bytesPerPacketOut));
        NetworkFlow[] networkFlows = new NetworkFlow[]{networkFlow};

        String key = createNetworkFlowsKey(timestamp, orgId);
        String state = createNetworkFlowsState(stub, networkFlows);

        stub.putStringState(key, state);

        addedNetworkFlowsStates.add(key);

        return networkFlows;
    }

    public NetworkFlow[] addMultipleNetworkFlows(final ChaincodeStub stub, final String timestamp, final String csvData) {
        String[] errors = validateKeyData(timestamp);

        if (errors.length > 0) {
            throw new RuntimeException(String.join(",", errors) + " - " + ContractErrors.INVALID_NETWORK_FLOW_KEY_DATA.toString());
        }

        String orgId = getTxCreatorOrgId(stub);

        NetworkFlow[] networkFlows = NetworkFlow.createInstancesFromCsv(csvData);

        String key = createNetworkFlowsKey(timestamp, orgId);
        String state = createNetworkFlowsState(stub, networkFlows);

        stub.putStringState(key, state);

        addedNetworkFlowsStates.add(key);

        return networkFlows;
    }

    public NetworkFlow[] getNetworkFlowsByRange(final ChaincodeStub stub, final String startTimestamp, final String endTimestamp) {
        ArrayList<NetworkFlow> networkFlows = new ArrayList<>();

        String startRange = Util.leftPad(startTimestamp, MAX_TIMESTAMP_LENGTH, "0") + "-" + Util.repeat("0", MAX_ORG_ID_LENGTH);
        String endRange = Util.leftPad(endTimestamp, MAX_TIMESTAMP_LENGTH, "0") + "-" + Util.repeat("9", MAX_ORG_ID_LENGTH);

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startRange, endRange);

        for (KeyValue result : results) {
            NetworkFlowState state = networkFlowStateSerializer.deserializeState(result.getStringValue());
            networkFlows.addAll(Arrays.asList(state.getNetworkFlows()));
        }

        return networkFlows.toArray(new NetworkFlow[networkFlows.size()]);
    }

    public NetworkFlow[] getNetworkFlowsForKey(final ChaincodeStub stub, final String key) {
        String state = stub.getStringState(key);

        if (state.isEmpty()) {
            throw new IllegalArgumentException("No record found found for key " + key);
        }

        return networkFlowStateSerializer.deserializeState(state).getNetworkFlows();
    }


    private ArrayList<main.common.NetworkFlow> convertNetworkFlows(NetworkFlow[] contractNetworkFlow) {
        return Arrays.stream(contractNetworkFlow)
                .map(nf -> new main.common.NetworkFlow(
                        nf.getIpSource(), nf.getIpDestination(), nf.getProtocol(),
                        nf.getBytesPerPacketIn(), nf.getBytesPerPacketOut())
                ).collect(toCollection(ArrayList::new));
    }

    private String createNetworkFlowsKey(String timestamp, String orgId) {
        String _timestamp = Util.leftPad(timestamp, MAX_TIMESTAMP_LENGTH, "0");
        String _orgId = Util.leftPad(orgId, MAX_ORG_ID_LENGTH, "0");

        return _timestamp + "-" + _orgId;
    }

    private String createNetworkFlowsState(ChaincodeStub stub, NetworkFlow[] networkFlows) {
        return networkFlowStateSerializer.serializeState(networkFlows, getTxCreatorPublicKey(stub), getTxCreatorMspId(stub));
    }

    private String[] validateKeyData(String timestamp) {
        ArrayList<String> errors = new ArrayList<>();

        if (timestamp.length() > MAX_TIMESTAMP_LENGTH) {
            errors.add("timestamp \"" + timestamp + "\" exceeds length limit of " + MAX_TIMESTAMP_LENGTH);
        }

        if (!Util.isInteger(timestamp)) {
            errors.add("timestamp \"" + timestamp + "\" is not a positive integer");
        }

        return errors.toArray(new String[errors.size()]);
    }

    private String getTxCreatorOrgId(ChaincodeStub stub) {
        String txCreatorMspId = getTxCreatorMspId(stub);
        String result = mspIdOrgIdMap.get(txCreatorMspId);

        if(result == null) {
            throw new RuntimeException("Org mspId " + txCreatorMspId + " not recognized - " + ContractErrors.ORG_MSP_ID_NOT_FOUND.toString());
        }
        return result;
    }

    private String getTxCreatorPublicKey(ChaincodeStub stub) {
        try {
            byte[] creator = stub.getCreator();
            Identities.SerializedIdentity identity = Identities.SerializedIdentity.parseFrom(creator);

            return identity.getIdBytes().toStringUtf8();
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    private String getTxCreatorMspId(ChaincodeStub stub) {
        try {
            byte[] creator = stub.getCreator();
            Identities.SerializedIdentity identity = Identities.SerializedIdentity.parseFrom(creator);

            return identity.getMspid();
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }


}
