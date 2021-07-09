package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlowCompressedData;
import com.lucalanda.botnetdetectioncontract.model.state.NetworkFlowState;
import com.owlike.genson.Genson;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.lucalanda.botnetdetectioncontract.TestUtils.*;
import static main.common.IPV4Utils.convertIP;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NetworkFlowStateSerializerTest {

    private static final NetworkFlow[] networkFlows = new NetworkFlow[]{
            new NetworkFlow("123.0.0.1", "126.0.0.1", TCP, 5, 3),
            new NetworkFlow("123.0.0.1", "130.1.1.2", UDP, 3, 20),
            new NetworkFlow("126.0.0.1", "130.1.1.2", TCP, 1000, 50),
            new NetworkFlow("129.0.1.1", "2.2.2.2", ICMP, 10, 3)
    };

    private static final String txCreatorPublicKey = "public key";
    private static final String txCreatorMspId = "Org1MSP";

    private static final NetworkFlowStateSerializer serializer = new NetworkFlowStateSerializer();

    @Test
    public void serialize_works_correctly() {
        String actual = serializer.serializeState(networkFlows, txCreatorPublicKey, txCreatorMspId);

        int[] expectedIpsList = new int[]{
                convertIP("123.0.0.1"),
                convertIP("126.0.0.1"),
                convertIP("130.1.1.2"),
                convertIP("129.0.1.1"),
                convertIP("2.2.2.2")
        };

        String expectedIpsListString = Arrays.toString(expectedIpsList).replaceAll(" ", "");

        String expected = "{\"creatorMspId\":\"Org1MSP\",\"creatorPublicKey\":\"public key\"," +
                "\"networkFlowCompressedData\":" +
                "{\"bytesPerPacketValuesList\":[5,3,20,1000,50,10]," +
                "\"ipsList\":" + expectedIpsListString + "," +
                "\"protocolsList\":\"AAEC\"," +
                "\"recordsList\":[\"0,1,0,0,1\",\"0,2,1,1,2\",\"1,2,0,3,4\",\"3,4,2,5,1\"]}}";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void serialized_string_contains_correct_network_flows_serialization() {
        String serializedState = serializer.serializeState(networkFlows, txCreatorPublicKey, txCreatorMspId);

        NetworkFlowCompressedData compressedData = new NetworkFlowCompressor().compress(networkFlows);
        String serializedCompressedNetworkFlowData = new Genson().serialize(compressedData);

        assertThat(serializedState).contains(serializedCompressedNetworkFlowData);
    }

    @Test
    public void deserialize_works_correctly() {
        int[] expectedIpsList = new int[]{
                convertIP("123.0.0.1"),
                convertIP("126.0.0.1"),
                convertIP("130.1.1.2"),
                convertIP("129.0.1.1"),
                convertIP("2.2.2.2")
        };
        String ipsListString = Arrays.toString(expectedIpsList).replaceAll(" ", "");

        String serializedState = "{\"creatorMspId\":\"Org1MSP\",\"creatorPublicKey\":\"public key\"," +
                "\"networkFlowCompressedData\":" +
                "{\"bytesPerPacketValuesList\":[5,3,20,1000,50,10]," +
                "\"ipsList\":" + ipsListString + "," +
                "\"protocolsList\":\"AAEC\"," +
                "\"recordsList\":[\"0,1,0,0,1\",\"0,2,1,1,2\",\"1,2,0,3,4\",\"3,4,2,5,1\"]}}";

        NetworkFlowState actual = serializer.deserializeState(serializedState);

        NetworkFlowCompressedData expectedNetworkFlowCompressedData = new NetworkFlowCompressor().compress(networkFlows);
        NetworkFlowState expected = new NetworkFlowState(expectedNetworkFlowCompressedData, txCreatorPublicKey, txCreatorMspId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void serialize_and_deserialize_work_correctly() {
        NetworkFlow[] networkFlows = getRandomNetworkFlows(10000);

        String serialized = serializer.serializeState(networkFlows, txCreatorPublicKey, txCreatorMspId);

        NetworkFlowState unserialized = serializer.deserializeState(serialized);

        assertThat(unserialized.getNetworkFlows()).isEqualTo(networkFlows);
        assertThat(unserialized.getCreatorMspId()).isEqualTo(txCreatorMspId);
        assertThat(unserialized.getCreatorPublicKey()).isEqualTo(txCreatorPublicKey);
    }

}