package com.lucalanda.botnetdetectioncontract;


import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.impl.ChaincodeSupportStream;
import org.hyperledger.fabric.shim.impl.Handler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class CustomChaincodeBase implements Chaincode {
    public static final String CORE_CHAINCODE_LOGGING_SHIM = "CORE_CHAINCODE_LOGGING_SHIM";
    public static final String CORE_CHAINCODE_LOGGING_LEVEL = "CORE_CHAINCODE_LOGGING_LEVEL";
    private static Log logger = LogFactory.getLog(org.hyperledger.fabric.shim.ChaincodeBase.class);
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 7051;
    private String host = "127.0.0.1";
    private int port = 7051;
    private boolean tlsEnabled = false;
    private String tlsClientKeyPath;
    private String tlsClientCertPath;
    private String tlsClientRootCertPath;
    private String id;
    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";
    private static final String CORE_PEER_ADDRESS = "CORE_PEER_ADDRESS";
    private static final String CORE_PEER_TLS_ENABLED = "CORE_PEER_TLS_ENABLED";
    private static final String CORE_PEER_TLS_ROOTCERT_FILE = "CORE_PEER_TLS_ROOTCERT_FILE";
    private static final String ENV_TLS_CLIENT_KEY_PATH = "CORE_TLS_CLIENT_KEY_PATH";
    private static final String ENV_TLS_CLIENT_CERT_PATH = "CORE_TLS_CLIENT_CERT_PATH";

    private final int maxGRPCMessageInboundSize;

    public CustomChaincodeBase(int maxGRPCMessageInboundSize) {
        this.maxGRPCMessageInboundSize = maxGRPCMessageInboundSize;
    }

    public abstract Response init(ChaincodeStub var1);

    public abstract Response invoke(ChaincodeStub var1);

    public void start(String[] args) {
        try {
            this.processEnvironmentOptions();
            this.processCommandLineOptions(args);
            this.initializeLogging();
            this.validateOptions();
            ChaincodeID chaincodeId = ChaincodeID.newBuilder().setName(this.id).build();
            ManagedChannelBuilder<?> channelBuilder = this.newChannelBuilder();
            Handler handler = new Handler(chaincodeId, this);
            new ChaincodeSupportStream(channelBuilder, handler::onChaincodeMessage, handler::nextOutboundChaincodeMessage);
        } catch (Exception var5) {
            logger.fatal("Chaincode could not start", var5);
        }

    }

    void initializeLogging() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS:%1$tL %4$-7.7s %2$s %5$s%6$s%n");
        Logger rootLogger = Logger.getLogger("");
        java.util.logging.Handler[] var2 = rootLogger.getHandlers();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            java.util.logging.Handler handler = var2[var4];
            handler.setLevel(Level.ALL);
            handler.setFormatter(new SimpleFormatter() {
                public synchronized String format(LogRecord record) {
                    return super.format(record).replaceFirst(".*SEVERE\\s*\\S*\\s*\\S*", "\u001b[1;31m$0\u001b[0m").replaceFirst(".*WARNING\\s*\\S*\\s*\\S*", "\u001b[1;33m$0\u001b[0m").replaceFirst(".*CONFIG\\s*\\S*\\s*\\S*", "\u001b[35m$0\u001b[0m").replaceFirst(".*FINE\\s*\\S*\\s*\\S*", "\u001b[36m$0\u001b[0m").replaceFirst(".*FINER\\s*\\S*\\s*\\S*", "\u001b[36m$0\u001b[0m").replaceFirst(".*FINEST\\s*\\S*\\s*\\S*", "\u001b[36m$0\u001b[0m");
                }
            });
        }

        Level chaincodeLogLevel = this.mapLevel(System.getenv("CORE_CHAINCODE_LOGGING_LEVEL"));
        Package chaincodePackage = this.getClass().getPackage();
        if (chaincodePackage != null) {
            Logger.getLogger(chaincodePackage.getName()).setLevel(chaincodeLogLevel);
        } else {
            Logger.getLogger("").setLevel(chaincodeLogLevel);
        }

        Level shimLogLevel = this.mapLevel(System.getenv("CORE_CHAINCODE_LOGGING_SHIM"));
        Logger.getLogger(org.hyperledger.fabric.shim.ChaincodeBase.class.getPackage().getName()).setLevel(shimLogLevel);
    }

    private Level mapLevel(String level) {
        if (level != null) {
            byte var3 = -1;
            switch(level.hashCode()) {
                case -1986360616:
                    if (level.equals("NOTICE")) {
                        var3 = 4;
                    }
                    break;
                case -1560189025:
                    if (level.equals("CRITICAL")) {
                        var3 = 0;
                    }
                    break;
                case 2251950:
                    if (level.equals("INFO")) {
                        var3 = 3;
                    }
                    break;
                case 64921139:
                    if (level.equals("DEBUG")) {
                        var3 = 5;
                    }
                    break;
                case 66247144:
                    if (level.equals("ERROR")) {
                        var3 = 1;
                    }
                    break;
                case 1842428796:
                    if (level.equals("WARNING")) {
                        var3 = 2;
                    }
            }

            switch(var3) {
                case 0:
                case 1:
                    return Level.SEVERE;
                case 2:
                    return Level.WARNING;
                case 3:
                    return Level.INFO;
                case 4:
                    return Level.CONFIG;
                case 5:
                    return Level.FINEST;
            }
        }

        return Level.INFO;
    }

    void validateOptions() {
        if (this.id == null) {
            throw new IllegalArgumentException(String.format("The chaincode id must be specified using either the -i or --i command line options or the %s environment variable.", "CORE_CHAINCODE_ID_NAME"));
        } else {
            if (this.tlsEnabled) {
                if (this.tlsClientCertPath == null) {
                    throw new IllegalArgumentException(String.format("Client key certificate chain (%s) was not specified.", "CORE_TLS_CLIENT_CERT_PATH"));
                }

                if (this.tlsClientKeyPath == null) {
                    throw new IllegalArgumentException(String.format("Client key (%s) was not specified.", "CORE_TLS_CLIENT_KEY_PATH"));
                }

                if (this.tlsClientRootCertPath == null) {
                    throw new IllegalArgumentException(String.format("Peer certificate trust store (%s) was not specified.", "CORE_PEER_TLS_ROOTCERT_FILE"));
                }
            }

        }
    }

    void processCommandLineOptions(String[] args) {
        Options options = new Options();
        options.addOption("a", "peer.address", true, "Address of peer to connect to");
        options.addOption((String)null, "peerAddress", true, "Address of peer to connect to");
        options.addOption("i", "id", true, "Identity of chaincode");

        try {
            CommandLine cl = (new DefaultParser()).parse(options, args);
            if (cl.hasOption("peerAddress") || cl.hasOption('a')) {
                String hostAddrStr;
                if (cl.hasOption('a')) {
                    hostAddrStr = cl.getOptionValue('a');
                } else {
                    hostAddrStr = cl.getOptionValue("peerAddress");
                }

                String[] hostArr = hostAddrStr.split(":");
                if (hostArr.length != 2) {
                    String msg = String.format("peer address argument should be in host:port format, current %s in wrong", hostAddrStr);
                    logger.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                this.port = Integer.valueOf(hostArr[1].trim());
                this.host = hostArr[0].trim();
            }

            if (cl.hasOption('i')) {
                this.id = cl.getOptionValue('i');
            }
        } catch (Exception var7) {
            logger.warn("cli parsing failed with exception", var7);
        }

        logger.info("<<<<<<<<<<<<<CommandLine options>>>>>>>>>>>>");
        logger.info("CORE_CHAINCODE_ID_NAME: " + this.id);
        logger.info("CORE_PEER_ADDRESS: " + this.host + ":" + this.port);
        logger.info("CORE_PEER_TLS_ENABLED: " + this.tlsEnabled);
        logger.info("CORE_PEER_TLS_ROOTCERT_FILE" + this.tlsClientRootCertPath);
        logger.info("CORE_TLS_CLIENT_KEY_PATH" + this.tlsClientKeyPath);
        logger.info("CORE_TLS_CLIENT_CERT_PATH" + this.tlsClientCertPath);
    }

    void processEnvironmentOptions() {
        if (System.getenv().containsKey("CORE_CHAINCODE_ID_NAME")) {
            this.id = System.getenv("CORE_CHAINCODE_ID_NAME");
        }

        if (System.getenv().containsKey("CORE_PEER_ADDRESS")) {
            String[] hostArr = System.getenv("CORE_PEER_ADDRESS").split(":");
            if (hostArr.length == 2) {
                this.port = Integer.valueOf(hostArr[1].trim());
                this.host = hostArr[0].trim();
            } else {
                String msg = String.format("peer address argument should be in host:port format, ignoring current %s", System.getenv("CORE_PEER_ADDRESS"));
                logger.error(msg);
            }
        }

        this.tlsEnabled = Boolean.parseBoolean(System.getenv("CORE_PEER_TLS_ENABLED"));
        if (this.tlsEnabled) {
            this.tlsClientRootCertPath = System.getenv("CORE_PEER_TLS_ROOTCERT_FILE");
            this.tlsClientKeyPath = System.getenv("CORE_TLS_CLIENT_KEY_PATH");
            this.tlsClientCertPath = System.getenv("CORE_TLS_CLIENT_CERT_PATH");
        }

        logger.info("<<<<<<<<<<<<<Enviromental options>>>>>>>>>>>>");
        logger.info("CORE_CHAINCODE_ID_NAME: " + this.id);
        logger.info("CORE_PEER_ADDRESS: " + this.host);
        logger.info("CORE_PEER_TLS_ENABLED: " + this.tlsEnabled);
        logger.info("CORE_PEER_TLS_ROOTCERT_FILE" + this.tlsClientRootCertPath);
        logger.info("CORE_TLS_CLIENT_KEY_PATH" + this.tlsClientKeyPath);
        logger.info("CORE_TLS_CLIENT_CERT_PATH" + this.tlsClientCertPath);
    }

    ManagedChannelBuilder<?> newChannelBuilder() throws IOException {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(this.host, this.port);
        logger.info("Configuring channel connection to peer.");

        builder.maxInboundMessageSize(maxGRPCMessageInboundSize);
        if (this.tlsEnabled) {
            builder.negotiationType(NegotiationType.TLS);
            builder.sslContext(this.createSSLContext());
        } else {
            builder.usePlaintext(true);
        }

        return builder;
    }

    SslContext createSSLContext() throws IOException {
        byte[] ckb = Files.readAllBytes(Paths.get(this.tlsClientKeyPath));
        byte[] ccb = Files.readAllBytes(Paths.get(this.tlsClientCertPath));
        return GrpcSslContexts.forClient().trustManager(new File(this.tlsClientRootCertPath)).keyManager(new ByteArrayInputStream(Base64.getDecoder().decode(ccb)), new ByteArrayInputStream(Base64.getDecoder().decode(ckb))).build();
    }

    protected static Response newSuccessResponse(String message, byte[] payload) {
        return new Response(Status.SUCCESS, message, payload);
    }

    protected static Response newSuccessResponse() {
        return newSuccessResponse((String)null, (byte[])null);
    }

    protected static Response newSuccessResponse(String message) {
        return newSuccessResponse(message, (byte[])null);
    }

    protected static Response newSuccessResponse(byte[] payload) {
        return newSuccessResponse((String)null, payload);
    }

    protected static Response newErrorResponse(String message, byte[] payload) {
        return new Response(Status.INTERNAL_SERVER_ERROR, message, payload);
    }

    protected static Response newErrorResponse() {
        return newErrorResponse((String)null, (byte[])null);
    }

    protected static Response newErrorResponse(String message) {
        return newErrorResponse(message, (byte[])null);
    }

    protected static Response newErrorResponse(byte[] payload) {
        return newErrorResponse((String)null, payload);
    }

    protected static Response newErrorResponse(Throwable throwable) {
        return newErrorResponse(throwable.getMessage(), printStackTrace(throwable));
    }

    private static byte[] printStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        } else {
            StringWriter buffer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(buffer));
            return buffer.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    String getHost() {
        return this.host;
    }

    int getPort() {
        return this.port;
    }

    boolean isTlsEnabled() {
        return this.tlsEnabled;
    }

    String getTlsClientKeyPath() {
        return this.tlsClientKeyPath;
    }

    String getTlsClientCertPath() {
        return this.tlsClientCertPath;
    }

    String getTlsClientRootCertPath() {
        return this.tlsClientRootCertPath;
    }

    String getId() {
        return this.id;
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
