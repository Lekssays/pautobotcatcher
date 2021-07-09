package com.lucalanda.botnetdetectioncontract;

import com.lucalanda.botnetdetectioncontract.model.NetworkFlow;
import gnu.trove.set.hash.TIntHashSet;
import main.common.CommunicationCluster;
import main.common.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static main.common.IPV4Utils.convertIP;
import static main.common.ProtocolUtils.convertProtocol;

public class TestUtils {
    private static Random random = new Random(42);

    public static final byte TCP = convertProtocol("tcp");
    public static final byte UDP = convertProtocol("udp");
    public static final byte ICMP = convertProtocol("icmp");

    private static byte[] protocols = new byte[]{TCP, UDP, ICMP};


    public static NetworkFlow[] getRandomNetworkFlows(int nRecords) {
        ArrayList<NetworkFlow> networkFlows = new ArrayList<>();

        for (int i = 0; i < nRecords; i++) {
            String ipSource = getRandomIpString();
            String ipDestination = getRandomIpString();
            int bytesPerPacketIn = getRandomBytesPerPacketValue();
            int bytesPerPacketOut = getRandomBytesPerPacketValue();
            byte protocol = getRandomProtocol();

            networkFlows.add(new NetworkFlow(ipSource, ipDestination, protocol, bytesPerPacketIn, bytesPerPacketOut));
        }

        return networkFlows.toArray(new NetworkFlow[nRecords]);
    }

    public static int getRandomBytesPerPacketValue() {
        return random.nextInt(1000);
    }

    public static int getRandomIp() {
        return convertIP(getRandomIpString());
    }

    public static String getRandomIpString() {
        return random.nextInt(100) + "." + random.nextInt(100) + "." + random.nextInt(100) + "." + random.nextInt(100);
    }

    public static byte getRandomProtocol() {
        return protocols[random.nextInt(protocols.length)];
    }

    public static List<Host> getRandomHosts(int hostsNumber,
                                            int minContactsNumber, int maxContactsNumber,
                                            int minClustersNumber, int maxClustersNumber,
                                            int minContactsPerClusterNumber, int maxContactsPerClusterNumber) {
        List<Host> result = new ArrayList<>(hostsNumber);

        for (int i = 0; i < hostsNumber; i++) {
            boolean isP2P = random.nextBoolean();
            int ip = getRandomIp();

            int contactsNumber = maxContactsNumber == 0 ? 0 : random.nextInt(maxContactsNumber) + minContactsNumber;
            Set<Integer> contacts = range(0, contactsNumber)
                    .mapToObj(_i -> getRandomIp()).collect(toSet());

            int clustersNumber = maxClustersNumber == 0 ? 0 : random.nextInt(maxClustersNumber) + minClustersNumber;
            Set<CommunicationCluster> clusters = range(0, clustersNumber)
                    .mapToObj(_i -> {
                        int minContacts = Math.min(contacts.size(), minContactsPerClusterNumber);
                        int maxContacts = Math.min(contacts.size(), maxContactsPerClusterNumber);

                        CommunicationCluster cluster = getRandomCommunicationCluster(contacts, minContacts, maxContacts);

                        return cluster;
                    }).collect(toSet());

            result.add(new Host(isP2P, ip, clusters));
        }

        return result;
    }

    public static CommunicationCluster getRandomCommunicationCluster(Set<Integer> contacts, int minContactsPerClusterNumber, int maxContactsPerClusterNumber) {
        int contactsNumber = 0;

        if (maxContactsPerClusterNumber > 0) {
            contactsNumber = random.nextInt(maxContactsPerClusterNumber);
            contactsNumber = Math.max(contactsNumber, minContactsPerClusterNumber);
        }

        List<Integer> contactsList = new ArrayList<>(contacts);
        TIntHashSet contactsSet = new TIntHashSet(contactsList.subList(0, contactsNumber));

        byte protocol = getRandomProtocol();
        int bytesPerPacketIn = getRandomBytesPerPacketValue();
        int bytesPerPacketOut = getRandomBytesPerPacketValue();

        return new CommunicationCluster(contactsSet.toArray(), protocol, bytesPerPacketIn, bytesPerPacketOut, random.nextBoolean());
    }
}
