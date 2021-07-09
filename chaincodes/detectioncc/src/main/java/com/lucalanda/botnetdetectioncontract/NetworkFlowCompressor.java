package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlowCompressedData;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TByteIntHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class NetworkFlowCompressor {

    public NetworkFlowCompressedData compress(NetworkFlow[] networkFlows) {
        TIntArrayList ipsList = new TIntArrayList();
        TIntIntHashMap ipIndexMap = new TIntIntHashMap();

        TIntArrayList bytesPerPacketValuesList = new TIntArrayList();
        TIntIntHashMap bytesPerPacketValueIndexMap = new TIntIntHashMap();

        TByteArrayList protocolsList = new TByteArrayList();
        TByteIntHashMap protocolIndexMap = new TByteIntHashMap();

        ArrayList<String> recordsList = new ArrayList<>();

        for (NetworkFlow n : networkFlows) {
            int ipSourceIndex = getIndexAndUpdateMapIfNeeded(ipsList, ipIndexMap, n.getIpSource());
            int ipDestinationIndex = getIndexAndUpdateMapIfNeeded(ipsList, ipIndexMap, n.getIpDestination());

            int protocolIndex = getIndexAndUpdateMapIfNeeded(protocolsList, protocolIndexMap, n.getProtocol());

            int bytesPerPacketInIndex = getIndexAndUpdateMapIfNeeded(bytesPerPacketValuesList, bytesPerPacketValueIndexMap, n.getBytesPerPacketIn());
            int bytesPerPacketOutIndex = getIndexAndUpdateMapIfNeeded(bytesPerPacketValuesList, bytesPerPacketValueIndexMap, n.getBytesPerPacketOut());

            recordsList.add(serializeCompressedRecord(ipSourceIndex, ipDestinationIndex, protocolIndex, bytesPerPacketInIndex, bytesPerPacketOutIndex));
        }

        return new NetworkFlowCompressedData(
                ipsList.toArray(),
                bytesPerPacketValuesList.toArray(),
                protocolsList.toArray(),
                Util.stringListToArray(recordsList)
        );
    }

    public NetworkFlow[] decompress(NetworkFlowCompressedData networkFlowCompressedData) {
        ArrayList<NetworkFlow> result = new ArrayList<>();

        for (String record : networkFlowCompressedData.getRecordsList()) {
            result.add(decompressRecord(networkFlowCompressedData, record));
        }

        return result.toArray(new NetworkFlow[result.size()]);
    }


    private NetworkFlow decompressRecord(NetworkFlowCompressedData compressedData, String record) {
        String[] tokens = record.split(",");

        int ipSource = compressedData.getIpsList()[parseInt(tokens[0])];
        int ipDestination = compressedData.getIpsList()[parseInt(tokens[1])];
        byte protocol = compressedData.getProtocolsList()[parseInt(tokens[2])];
        int bytesPerPacketIn = compressedData.getBytesPerPacketValuesList()[parseInt(tokens[3])];
        int bytesPerPacketOut = compressedData.getBytesPerPacketValuesList()[parseInt(tokens[4])];

        return new NetworkFlow(ipSource, ipDestination, protocol, bytesPerPacketIn, bytesPerPacketOut);
    }

    private String serializeCompressedRecord(int ipSourceIndex, int ipDestinationIndex, int protocolIndex, int bytesPerPacketInIndex, int bytesPerPacketOutIndex) {
        return ipSourceIndex + "," + ipDestinationIndex + "," + protocolIndex + "," + bytesPerPacketInIndex + "," + bytesPerPacketOutIndex;
    }

    private int getIndexAndUpdateMapIfNeeded(TIntArrayList valuesList, TIntIntHashMap indexMap, int value) {
        int index;
        if (indexMap.containsKey(value)) {
            index = indexMap.get(value);
        } else {
            index = valuesList.size();
            indexMap.put(value, index);
            valuesList.add(value);
        }

        return index;
    }

    private int getIndexAndUpdateMapIfNeeded(TByteArrayList valuesList, TByteIntHashMap indexMap, byte value) {
        int index;
        if (indexMap.containsKey(value)) {
            index = indexMap.get(value);
        } else {
            index = valuesList.size();
            indexMap.put(value, index);
            valuesList.add(value);
        }

        return index;
    }

}
