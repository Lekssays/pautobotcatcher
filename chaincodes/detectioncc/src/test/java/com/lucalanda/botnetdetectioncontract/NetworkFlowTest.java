package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static com.lucalanda.botnetdetectioncontract.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public final class NetworkFlowTest {

    private final String validCsvData = "123,456,tcp,0,0\n789,987,udp,1,1";

    @Test
    public void createInstancesFromCsv_returns_array_of_instances_from_csv() {
        NetworkFlow[] expected = new NetworkFlow[]{
                new NetworkFlow("123", "456", TCP, 0, 0),
                new NetworkFlow("789", "987", UDP, 1, 1),
        };

        NetworkFlow[] actual = NetworkFlow.createInstancesFromCsv(validCsvData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void createInstancesFromCsv_discards_invalid_instances() {
        String invalidCsv = "123,456,icmp,0,0\n789,987,tcp,1\n123,345,man,1,2";

        NetworkFlow[] expected = new NetworkFlow[]{
                new NetworkFlow("123", "456", ICMP, 0, 0)
        };

        NetworkFlow[] actual = NetworkFlow.createInstancesFromCsv(invalidCsv);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void createInstancesFromCsv_trims_fields() {
        String csv = "123  , 456, tcp  ,  0,0  \n   789,  987     ,udp  , 1,1  ";

        NetworkFlow[] expected = new NetworkFlow[]{
                new NetworkFlow("123", "456", TCP, 0, 0),
                new NetworkFlow("789", "987", UDP, 1, 1)
        };

        NetworkFlow[] actual = NetworkFlow.createInstancesFromCsv(csv);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void instances_are_serialized_and_deserialized_correctly() {
        NetworkFlow[] expected = new NetworkFlow[]{
                new NetworkFlow("123", "456", TCP, 0, 0),
                new NetworkFlow("789", "987", UDP, 1, 1)
        };

        Genson g = new Genson();

        String x = g.serialize(expected);

        List<NetworkFlow> deserialized = g.deserialize(x, new GenericType<List<NetworkFlow>>() {
        });

        NetworkFlow[] array = deserialized.toArray(new NetworkFlow[deserialized.size()]);

        assertThat(array).isEqualTo(expected);
    }

    @Test
    public void conversionTimeTest() {
        Genson g = new Genson();

        String csv = getCsvSample(10000);

        long startTime = new Date().getTime();

        NetworkFlow[] networkFlows = NetworkFlow.createInstancesFromCsv(csv);

        long instantiationTime = new Date().getTime() - startTime;

        g.serialize(networkFlows);

        long serializationTime = new Date().getTime() - (startTime + instantiationTime);

        assertThat(instantiationTime - startTime).isLessThan(100);
        assertThat(serializationTime - instantiationTime).isLessThan(100);
    }

    private String getCsvSample(int nLines) {
        String csv = "";
        for(int i = 0; i < nLines; i++) {
            String line = i + "," + i + "," + "1000,1000,tcp" + "\n";
            csv += line;
        }
        return csv;
    }

}
