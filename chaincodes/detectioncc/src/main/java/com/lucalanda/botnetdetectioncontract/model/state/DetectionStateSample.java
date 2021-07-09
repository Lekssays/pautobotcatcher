package com.lucalanda.botnetdetectioncontract.model.state;

import main.common.Host;
import main.model.DetectionData;

import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DetectionStateSample {

    private final DetectionData detectionData;

    public DetectionStateSample(DetectionData detectionData) {
        this.detectionData = detectionData;
    }

    public String getHostHashCodes() {
        List<Integer> hashCodes = detectionData.getHosts().stream().map(Host::realHashCode).collect(toList());

        return hashCodes.toString();
    }

    public String getCommunicationClustersLength() {
        int result = 0;
        for (Host h : detectionData.getHosts()) {
            result += h.getCommunicationClusters().length;
        }

        return String.valueOf(result);
    }

    public HashMap<Integer, Integer> getHostP2PContactsNumber() {
        HashMap<Integer, Integer> result = new HashMap<>();

        for (Host h : detectionData.getHosts()) {
            result.put(h.getIp(), h.getP2PContacts().size());
        }

        return result;
    }
}
