package com.lucalanda.botnetdetectioncontract.model.state;

import com.lucalanda.botnetdetectioncontract.NetworkFlowCompressor;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlowCompressedData;
import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;

public class NetworkFlowState {
    private final NetworkFlowCompressor networkFlowCompressor;

    private final NetworkFlowCompressedData networkFlowCompressedData;
    private final String creatorPublicKey;
    private final String creatorMspId;

    public NetworkFlowState(@JsonProperty("networkFlowCompressedData") NetworkFlowCompressedData networkFlowCompressedData,
                            @JsonProperty("creatorPublicKey") String txCreatorPublicKey,
                            @JsonProperty("creatorMspId") String txCreatorMspId) {
        this.networkFlowCompressedData = networkFlowCompressedData;
        this.creatorPublicKey = txCreatorPublicKey;
        this.creatorMspId = txCreatorMspId;

        this.networkFlowCompressor = new NetworkFlowCompressor();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NetworkFlowState)) return false;
        NetworkFlowState stateObj = (NetworkFlowState) obj;

        return networkFlowCompressedData.equals(stateObj.networkFlowCompressedData) &&
                creatorPublicKey.equals(stateObj.creatorPublicKey) &&
                creatorMspId.equals(stateObj.creatorMspId);
    }

    @JsonIgnore
    public NetworkFlow[] getNetworkFlows() {
        return networkFlowCompressor.decompress(networkFlowCompressedData);
    }

    public NetworkFlowCompressedData getNetworkFlowCompressedData() {
        return networkFlowCompressedData;
    }

    public String getCreatorPublicKey() {
        return creatorPublicKey;
    }

    public String getCreatorMspId() {
        return creatorMspId;
    }
}
