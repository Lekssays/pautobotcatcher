package com.lucalanda.botnetdetectioncontract;

import main.common.ProtocolUtils;

import java.io.*;
import java.util.*;

public class Util {
    private static final String INT_REGEX = "^\\d+$";

    public static boolean isInteger(String s) {
        return s.matches(INT_REGEX);
    }

    public static String leftPad(String input, int length, String fill) {
        String pad = String.format("%" + length + "s", "").replace(" ", fill) + input.trim();
        return pad.substring(pad.length() - length);
    }

    public static String repeat(String s, int repetitions) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < repetitions; i++) {
            sb.append(s);
        }

        return sb.toString();
    }

    public static List<String> splitStringBySize(String str, int size) {
        ArrayList<String> split = new ArrayList<>();
        for (int i = 0; i <= str.length() / size; i++) {
            split.add(str.substring(i * size, Math.min((i + 1) * size, str.length())));
        }
        return split;
    }

    public static byte[][] splitByteArrayBySize(byte[] array, int chunkSize) {
        int numOfChunks = (int) Math.ceil((float) array.length / (float) chunkSize);
        byte[][] result = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; i++) {
            result[i] = Arrays.copyOfRange(array, i * chunkSize, Math.min(array.length, (i + 1) * chunkSize));
        }

        return result;
    }

    public static String[] stringListToArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static int[] intListToArray(List<Integer> list) {
        int[] result = new int[list.size()];
        int i = 0;
        for(Integer f : list) {
            result[i++] = f;
        }

        return result;
    }


    public static <T> T clone(T object) throws IOException, ClassNotFoundException {
        return deserializeObject(serializeObject(object));
    }

    public static <T> byte[] serializeObject(T obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static <T> T deserializeObject(byte[] serializedObj) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(serializedObj);
        ObjectInputStream is = new ObjectInputStream(in);
        return (T) is.readObject();
    }

    public static short[] shortListToArray(List<Short> list) {
        short[] result = new short[list.size()];
        int i = 0;
        for(Short s : list) {
            result[i++] = s;
        }

        return result;
    }

    public static byte[] byteListToArray(List<Byte> list) {
        byte[] result = new byte[list.size()];
        int i = 0;
        for(Byte b : list) {
            result[i++] = b;
        }

        return result;
    }

    public static boolean[] booleanListToArray(List<Boolean> list) {
        boolean[] result = new boolean[list.size()];
        int i = 0;
        for(Boolean b : list) {
            result[i++] = b;
        }

        return result;
    }

    public static float[] floatListToArray(List<Float> list) {
        float[] result = new float[list.size()];
        int i = 0;
        for(Float f : list) {
            result[i++] = f;
        }

        return result;
    }

    public static <T> Set<T> buildSet(T... params) {
        return new HashSet<>(Arrays.asList(params));
    }

    public static byte parseNetworkFlowProtocol(String protocol) {
        return ProtocolUtils.convertProtocol(protocol);
    }

}
