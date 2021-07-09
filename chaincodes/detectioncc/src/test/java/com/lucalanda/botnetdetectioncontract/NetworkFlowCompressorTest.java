package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlowCompressedData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.lucalanda.botnetdetectioncontract.TestUtils.*;
import static main.common.IPV4Utils.convertIP;
import static org.assertj.core.api.Assertions.assertThat;

class NetworkFlowCompressorTest {

    NetworkFlowCompressor compressor = new NetworkFlowCompressor();

    @Test
    public void compress_works() {
        NetworkFlow[] networkFlows = new NetworkFlow[]{
                new NetworkFlow("123.0.0.1", "126.0.0.1", TCP, 5, 3),
                new NetworkFlow("123.0.0.1", "130.1.1.2", UDP, 3, 20),
                new NetworkFlow("126.0.0.1", "130.1.1.2", TCP, 1000, 50),
                new NetworkFlow("129.0.1.1", "2.2.2.2", ICMP, 10, 3)
        };

        Integer[] expectedBytesPerPacketValuesList = new Integer[]{5,3,20,1000,50,10};
        int[] expectedIpsList = new int[]{
                convertIP("123.0.0.1"),
                convertIP("126.0.0.1"),
                convertIP("130.1.1.2"),
                convertIP("129.0.1.1"),
                convertIP("2.2.2.2")
        };

        byte[] expectedProtocolsList = new byte[]{TCP, UDP, ICMP};
        String[] expectedRecordsList = new String[]{"0,1,0,0,1","0,2,1,1,2","1,2,0,3,4","3,4,2,5,1"};

        NetworkFlowCompressedData compressedData = compressor.compress(networkFlows);

        assertThat(compressedData.getBytesPerPacketValuesList()).isEqualTo(expectedBytesPerPacketValuesList);
        assertThat(compressedData.getIpsList()).isEqualTo(expectedIpsList);
        assertThat(compressedData.getProtocolsList()).isEqualTo(expectedProtocolsList);
        assertThat(compressedData.getRecordsList()).isEqualTo(expectedRecordsList);
    }

    @Test
    public void decompress_works() {
        int[] bytesPerPacketValuesList = new int[]{5,3,20,1000,50,10};
        int[] ipsList = new int[]{convertIP("123.0.0.1"), convertIP("126.0.0.1"),
                convertIP("130.1.1.2"), convertIP("129.0.1.1"), convertIP("2.2.2.2")};

        byte[] protocolsList = new byte[]{TCP, UDP};
        String[] recordsList = new String[]{"0,1,0,0,1","0,2,1,1,2","1,2,0,3,4","3,4,1,5,1"};

        NetworkFlowCompressedData compressedData = new NetworkFlowCompressedData(ipsList, bytesPerPacketValuesList, protocolsList, recordsList);

        NetworkFlow[] expected = new NetworkFlow[]{
                new NetworkFlow("123.0.0.1", "126.0.0.1", TCP, 5, 3),
                new NetworkFlow("123.0.0.1", "130.1.1.2", UDP, 3, 20),
                new NetworkFlow("126.0.0.1", "130.1.1.2", TCP, 1000, 50),
                new NetworkFlow("129.0.1.1", "2.2.2.2", UDP, 10, 3)
        };

        Assertions.assertThat(compressor.decompress(compressedData)).isEqualTo(expected);
    }

    @Test
    public void compress_and_decompress_work() {
        NetworkFlow[] originalRecords = getRandomNetworkFlows(10000);

        NetworkFlowCompressedData compressedData = compressor.compress(originalRecords);

        NetworkFlow[] decompressed = compressor.decompress(compressedData);

        assertThat(decompressed).isEqualTo(originalRecords);
    }

    @Test
    public void compress_with_no_records_works() {
        NetworkFlow[] records = new NetworkFlow[0];

        int[] expectedBytesPerPacketValuesList = new int[0];
        String[] expectedIpsList = new String[0];
        String[] expectedProtocolsList = new String[0];
        String[] expectedRecordsList = new String[0];

        NetworkFlowCompressedData compressedData = compressor.compress(records);

        assertThat(compressedData.getBytesPerPacketValuesList()).isEqualTo(expectedBytesPerPacketValuesList);
        assertThat(compressedData.getIpsList()).isEqualTo(expectedIpsList);
        assertThat(compressedData.getProtocolsList()).isEqualTo(expectedProtocolsList);
        assertThat(compressedData.getRecordsList()).isEqualTo(expectedRecordsList);
    }

    @Test
    public void decompress_with_no_records_works() {
        int[] bytesPerPacketValuesList = new int[0];
        int[] ipsList = new int[0];
        byte[] protocolsList = new byte[0];
        String[] recordsList = new String[0];

        NetworkFlowCompressedData emptyCompressedData = new NetworkFlowCompressedData(ipsList, bytesPerPacketValuesList, protocolsList, recordsList);

        NetworkFlow[] expected = new NetworkFlow[0];

        Assertions.assertThat(compressor.decompress(emptyCompressedData)).isEqualTo(expected);
    }

}