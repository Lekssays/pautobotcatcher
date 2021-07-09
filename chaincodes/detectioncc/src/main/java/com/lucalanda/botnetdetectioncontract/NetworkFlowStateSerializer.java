package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlowCompressedData;
import com.lucalanda.botnetdetectioncontract.model.state.NetworkFlowState;
import com.owlike.genson.Genson;

public class NetworkFlowStateSerializer {

    private final Genson genson;
    private final NetworkFlowCompressor networkFlowCompressor;

    public NetworkFlowStateSerializer() {
        this.genson = new Genson();
        this.networkFlowCompressor = new NetworkFlowCompressor();
    }

    public String serializeState(NetworkFlow[] networkFlows, String txCreatorPublicKey, String txCreatorMspId) {
        NetworkFlowCompressedData networkFlowCompressedData = networkFlowCompressor.compress(networkFlows);

        NetworkFlowState state = new NetworkFlowState(networkFlowCompressedData, txCreatorPublicKey, txCreatorMspId);
        return genson.serialize(state);
    }

    public NetworkFlowState deserializeState(String serializedState) {
        return genson.deserialize(serializedState, NetworkFlowState.class);
    }

}
