package com.lucalanda.botnetdetectioncontract.model.state;

import java.io.Serializable;

public class DetectionStateMetadata implements Serializable {

    static final long serialVersionUID = 42L;

    private String[] hostKeys;
    private int stateHashCode;

    public DetectionStateMetadata(String[] hostKeys, int stateHashCode) {
        this.hostKeys = hostKeys;
        this.stateHashCode = stateHashCode;
    }

    public String[] getHostKeys() {
        return hostKeys;
    }

    public int getStateHashCode() {
        return stateHashCode;
    }
}
