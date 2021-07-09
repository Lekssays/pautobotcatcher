package com.lucalanda.botnetdetectioncontract.model;

import com.owlike.genson.annotation.JsonProperty;

import java.util.Arrays;


public class NetworkFlowCompressedData {
    private final int[] ipsList;
    private final int[] bytesPerPacketValuesList;
    private final byte[] protocolsList;

    private final String[] recordsList;

    public NetworkFlowCompressedData(@JsonProperty("ipsList") int[] ipsList,
                                     @JsonProperty("bytesPerPacketValuesList") int[] bytesPerPacketValuesList,
                                     @JsonProperty("protocolsList") byte[] protocolsList,
                                     @JsonProperty("recordsList") String[] recordsList) {
        this.ipsList = ipsList;
        this.bytesPerPacketValuesList = bytesPerPacketValuesList;
        this.protocolsList = protocolsList;
        this.recordsList = recordsList;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NetworkFlowCompressedData)) return false;
        NetworkFlowCompressedData objData = (NetworkFlowCompressedData) obj;

        return Arrays.equals(ipsList, objData.ipsList) &&
                Arrays.equals(bytesPerPacketValuesList, objData.bytesPerPacketValuesList) &&
                Arrays.equals(protocolsList, objData.protocolsList) &&
                Arrays.equals(recordsList, objData.recordsList);
    }

    public int[] getIpsList() {
        return ipsList;
    }

    public int[] getBytesPerPacketValuesList() {
        return bytesPerPacketValuesList;
    }

    public byte[] getProtocolsList() {
        return protocolsList;
    }

    public String[] getRecordsList() {
        return recordsList;
    }
}
