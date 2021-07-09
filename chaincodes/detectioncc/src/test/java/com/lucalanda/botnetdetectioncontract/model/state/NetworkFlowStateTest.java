package com.lucalanda.botnetdetectioncontract.model.state;

import com.lucalanda.botnetdetectioncontract.NetworkFlowCompressor;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.lucalanda.botnetdetectioncontract.model.NetworkFlowCompressedData;
import org.junit.jupiter.api.Test;

import static com.lucalanda.botnetdetectioncontract.TestUtils.TCP;
import static com.lucalanda.botnetdetectioncontract.TestUtils.UDP;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NetworkFlowStateTest {
    private static final NetworkFlow[] networkFlows = new NetworkFlow[]{
            new NetworkFlow("123.0.0.1", "126.0.0.1", TCP, 5, 3),
            new NetworkFlow("123.0.0.1", "130.1.1.2", UDP, 3, 20),
            new NetworkFlow("126.0.0.1", "130.1.1.2", TCP, 1000, 50),
            new NetworkFlow("129.0.1.1", "2.2.2.2", UDP, 10, 3)
    };

    private static final NetworkFlowCompressor serializer = new NetworkFlowCompressor();

    @Test
    public void equals_works_correctly() {
        NetworkFlowCompressedData networkFlowCompressedData1 = serializer.compress(networkFlows);
        NetworkFlowCompressedData networkFlowCompressedData2 = serializer.compress(networkFlows);

        NetworkFlowCompressedData networkFlowCompressedData3 = serializer.compress(new NetworkFlow[]{
                networkFlows[0], networkFlows[1], networkFlows[2]
        });
        NetworkFlowCompressedData networkFlowCompressedData4 = serializer.compress(new NetworkFlow[]{
                networkFlows[0], networkFlows[1], networkFlows[2],
                new NetworkFlow(networkFlows[3].getIpSource(), networkFlows[3].getIpDestination(), networkFlows[3].getProtocol(),
                        networkFlows[3].getBytesPerPacketIn(), networkFlows[3].getBytesPerPacketOut() + 1)
        });

        assertThat(networkFlowCompressedData1).isEqualTo(networkFlowCompressedData2);
        assertThat(networkFlowCompressedData1).isNotEqualTo(networkFlowCompressedData3);
        assertThat(networkFlowCompressedData1).isNotEqualTo(networkFlowCompressedData4);
    }
}
