package com.lucalanda.botnetdetectioncontract;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lucalanda.botnetdetectioncontract.Util.buildSet;

public class BotnetDetectionChaincode extends CustomChaincodeBase {

    private final Genson g = new Genson();

    private static final int MAX_GRPC_INBOUND_MESSAGE_SIZE = 524288000;
    private static final int MAX_LEDGER_STATE_LENGTH = 20971520;
    private final BotnetDetectionContract contract;

    private static final String PERFORM_BOTNET_DETECTION_FUNCTION = "performBotnetDetection";
    private static final String GET_DETECTION_STATE_FUNCTION = "getDetectionState";
    private static final String CLEAR_STATE_FUNCTION = "clearState";
    private static final String GET_BOT_IPS_FUNCTION = "getBotIps";
    private static final String ADD_SINGLE_NETWORK_FLOW_FUNCTION = "addSingleNetworkFlow";
    private static final String ADD_MULTIPLE_NETWORK_FLOWS_FUNCTION = "addMultipleNetworkFlows";
    private static final String GET_NETWORK_FLOWS_BY_RANGE = "getNetworkFlowsByRange";
    private static final String GET_NETWORK_FLOWS_FOR_KEY = "getNetworkFlowsForKey";

    private static final Map<String, Integer> functionArgumentsSize = new HashMap<String, Integer>() {{
        put(PERFORM_BOTNET_DETECTION_FUNCTION, 2);
        put(GET_DETECTION_STATE_FUNCTION, 0);
        put(CLEAR_STATE_FUNCTION, 0);
        put(GET_BOT_IPS_FUNCTION, 0);
        put(ADD_SINGLE_NETWORK_FLOW_FUNCTION, 6);
        put(ADD_MULTIPLE_NETWORK_FLOWS_FUNCTION, 2);
        put(GET_NETWORK_FLOWS_BY_RANGE, 2);
        put(GET_NETWORK_FLOWS_FOR_KEY, 1);
    }};

    public BotnetDetectionChaincode() {
        super(MAX_GRPC_INBOUND_MESSAGE_SIZE);
        this.contract = new BotnetDetectionContract(MAX_LEDGER_STATE_LENGTH);
    }

    @Override
    public Response InitLedger(ChaincodeStub chaincodeStub) {
        List<String> args = chaincodeStub.getStringArgs();

        if(args.size() == 1 && args.get(0).equalsIgnoreCase("BotnetDetectionContract:initialize")) {
            try {
                contract.initialize(chaincodeStub);
            } catch (IOException e) {
                e.printStackTrace();
                return new Response(Response.Status.INTERNAL_SERVER_ERROR, "Chaincode initialization", e.getMessage().getBytes());
            }
            System.out.println("BotnetDetectionContract has been initialized");
        }

        return new Response(Response.Status.SUCCESS, "Chaincode initialization", "Chaincode has been successfully initialized".getBytes());
    }

    @Override
    public Response invoke(ChaincodeStub chaincodeStub) {
        List<String> args = chaincodeStub.getStringArgs();
        String function = args.get(0);

        String error = checkInvocationError(function, args);
        if (error != null) {
            return new Response(Response.Status.INTERNAL_SERVER_ERROR, "Invalid contract invocation", error.getBytes());
        }

        try {
            return executeInvokedFunction(chaincodeStub, args, function);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new Response(Response.Status.INTERNAL_SERVER_ERROR, "Invalid contract invocation", e.getMessage().getBytes());
        }
    }

    private Response executeInvokedFunction(ChaincodeStub chaincodeStub, List<String> args, String function) throws IOException, ClassNotFoundException {
        String response;
        switch (function) {
            case PERFORM_BOTNET_DETECTION_FUNCTION:
                response = g.serialize(contract.performBotnetDetection(chaincodeStub, args.get(1), args.get(2)));
                break;
            case GET_BOT_IPS_FUNCTION:
                response = g.serialize(contract.getBotIps(chaincodeStub));
                break;
            case GET_DETECTION_STATE_FUNCTION:
                response = g.serialize(contract.getDetectionState(chaincodeStub));
                break;
            case CLEAR_STATE_FUNCTION:
                contract.clearState(chaincodeStub);
                response = "detection state cleared";
                break;
            case ADD_SINGLE_NETWORK_FLOW_FUNCTION:
                response = g.serialize(contract.addSingleNetworkFlow(chaincodeStub, args.get(1), args.get(2), args.get(3), args.get(4),
                        args.get(5), args.get(6)));
                break;
            case ADD_MULTIPLE_NETWORK_FLOWS_FUNCTION:
                response = g.serialize(contract.addMultipleNetworkFlows(chaincodeStub, args.get(1), args.get(2)));
                break;
            case GET_NETWORK_FLOWS_BY_RANGE:
                response = g.serialize(contract.getNetworkFlowsByRange(chaincodeStub, args.get(1), args.get(2)));
                break;
            case GET_NETWORK_FLOWS_FOR_KEY:
                response = g.serialize(contract.getNetworkFlowsForKey(chaincodeStub, args.get(1)));
                break;
            default:
                throw new IllegalArgumentException("Unrecognized function invoked \"" + function + "\"");
        }

        return new Response(Response.Status.SUCCESS, "Invocation finished correctly", response.getBytes());
    }

    private String checkInvocationError(String function, List<String> args) {
        Set<String> allowedFunctions = buildSet(
                PERFORM_BOTNET_DETECTION_FUNCTION,
                GET_DETECTION_STATE_FUNCTION,
                CLEAR_STATE_FUNCTION,
                GET_BOT_IPS_FUNCTION,
                ADD_SINGLE_NETWORK_FLOW_FUNCTION,
                ADD_MULTIPLE_NETWORK_FLOWS_FUNCTION,
                GET_NETWORK_FLOWS_BY_RANGE,
                GET_NETWORK_FLOWS_FOR_KEY
        );

        if (!allowedFunctions.contains(function)) {
            return "Invalid function \"" + function + "\" was invoked." + "\n" +
                    "Allowed functions: " + allowedFunctions.toString();
        }

        int expectedArgsSize = functionArgumentsSize.get(function) + 1;
        if (args.size() != expectedArgsSize) {
            return "Wrong number of arguments for function \"" + function + "\": expected: " + expectedArgsSize + ", actual: " + args.size();
        }

        return null;
    }

    public static void main(String[] args) {
        new BotnetDetectionChaincode().start(args);
    }
}
