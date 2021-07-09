/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lucalanda.botnetdetectioncontract.model;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;
import main.common.ProtocolUtils;

import java.util.ArrayList;
import java.util.Objects;

import static com.lucalanda.botnetdetectioncontract.Util.isInteger;
import static com.lucalanda.botnetdetectioncontract.Util.parseNetworkFlowProtocol;
import static java.lang.Integer.parseInt;
import static main.common.IPV4Utils.convertIP;

public final class NetworkFlow {

    private final int ipSource;

    private final int ipDestination;

    private final int bytesPerPacketIn;

    private final int bytesPerPacketOut;

    private final byte protocol;

    public static NetworkFlow[] createInstancesFromCsv(String dataCsv) {
        return createInstancesFromCsv(dataCsv, ",");
    }

    public static NetworkFlow[] createInstancesFromCsv(String dataCsv, String separator) {
        ArrayList<NetworkFlow> result = new ArrayList<>();

        String[] lines = dataCsv.split("\n");

        for (String line : lines) {
            String[] args = line.split(separator);
            String[] trimmedArgs = new String[args.length];

            for (int i = 0; i < args.length; i++) {
                trimmedArgs[i] = args[i].trim();
            }

            if (validArgsSet(trimmedArgs)) {
                result.add(buildNetworkFlow(trimmedArgs));
            }
        }

        return result.toArray(new NetworkFlow[result.size()]);
    }

    @JsonCreator
    public NetworkFlow(@JsonProperty("ipSource") final int ipSource,
                       @JsonProperty("ipDestination") final int ipDestination,
                       @JsonProperty("protocol") final byte protocol,
                       @JsonProperty("bytesPerPacketIn") final int bytesPerPacketIn,
                       @JsonProperty("bytesPerPacketOut") final int bytesPerPacketOut) {
        this.ipSource = ipSource;
        this.ipDestination = ipDestination;
        this.protocol = protocol;
        this.bytesPerPacketIn = bytesPerPacketIn;
        this.bytesPerPacketOut = bytesPerPacketOut;
    }

    public NetworkFlow(@JsonProperty("ipSource") final String ipSource,
                       @JsonProperty("ipDestination") final String ipDestination,
                       @JsonProperty("protocol") final byte protocol,
                       @JsonProperty("bytesPerPacketIn") final int bytesPerPacketIn,
                       @JsonProperty("bytesPerPacketOut") final int bytesPerPacketOut) {

        this(convertIP(ipSource), convertIP(ipDestination), protocol, bytesPerPacketIn, bytesPerPacketOut);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetworkFlow && this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIpSource(), getIpDestination(), getBytesPerPacketIn(), getBytesPerPacketOut(), getProtocol());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + " [ipSource=" + ipSource + ", ipDestination=" + ipDestination + ", protocol=" + protocol
                + ", bytesPerPacketIn=" + bytesPerPacketIn + ", bytesPerPacketOut=" + bytesPerPacketOut + "]";
    }

    public int getIpSource() {
        return ipSource;
    }

    public int getIpDestination() {
        return ipDestination;
    }

    public int getBytesPerPacketIn() {
        return bytesPerPacketIn;
    }

    public int getBytesPerPacketOut() {
        return bytesPerPacketOut;
    }

    public byte getProtocol() {
        return protocol;
    }

    private static NetworkFlow buildNetworkFlow(String[] args) throws IllegalArgumentException {
        return new NetworkFlow(args[0], args[1], parseNetworkFlowProtocol(args[2]), parseInt(args[3]), parseInt(args[4]));
    }

    private static boolean validArgsSet(String[] args) {
        if (args.length != 5) {
            return false;
        }

        for (String arg : args) {
            if (arg.length() == 0) {
                return false;
            }
        }

        String protocol = args[2].toLowerCase();
        if(!ProtocolUtils.isProtocolAccepted(protocol)) return false;

        return isInteger(args[3]) && isInteger(args[4]);
    }
}
