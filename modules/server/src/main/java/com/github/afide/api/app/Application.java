package com.github.afide.api.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.afide.api.model.TxModel;
import com.github.jtendermint.jabci.api.ABCIAPI;
import com.github.jtendermint.jabci.api.CodeType;
import com.github.jtendermint.jabci.socket.TSocket;
import com.github.jtendermint.jabci.types.RequestBeginBlock;
import com.github.jtendermint.jabci.types.RequestCheckTx;
import com.github.jtendermint.jabci.types.RequestCommit;
import com.github.jtendermint.jabci.types.RequestDeliverTx;
import com.github.jtendermint.jabci.types.RequestEcho;
import com.github.jtendermint.jabci.types.RequestEndBlock;
import com.github.jtendermint.jabci.types.RequestFlush;
import com.github.jtendermint.jabci.types.RequestInfo;
import com.github.jtendermint.jabci.types.RequestInitChain;
import com.github.jtendermint.jabci.types.RequestQuery;
import com.github.jtendermint.jabci.types.RequestSetOption;
import com.github.jtendermint.jabci.types.ResponseBeginBlock;
import com.github.jtendermint.jabci.types.ResponseCheckTx;
import com.github.jtendermint.jabci.types.ResponseCommit;
import com.github.jtendermint.jabci.types.ResponseDeliverTx;
import com.github.jtendermint.jabci.types.ResponseEcho;
import com.github.jtendermint.jabci.types.ResponseEndBlock;
import com.github.jtendermint.jabci.types.ResponseFlush;
import com.github.jtendermint.jabci.types.ResponseInfo;
import com.github.jtendermint.jabci.types.ResponseInitChain;
import com.github.jtendermint.jabci.types.ResponseQuery;
import com.github.jtendermint.jabci.types.ResponseSetOption;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * A generic app implementation.
 * @author tglaeser
 */
public abstract class Application implements ABCIAPI, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    protected static String version;

    @JsonProperty private TxModel txModel;

    private long lastBlockHeight = 0;
    private byte[] lastBlockHash = new byte[]{};
    private byte[] lastAppHash = new byte[]{};
    private TSocket tSocket = new TSocket();

    protected Application(TxModel txModel) { this.txModel = txModel; }

    private static String byteArrayToHexSring(byte[] bytes) {
        return "0x" + byteArrayToHex(bytes);
    }

    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    @Override public ResponseEcho requestEcho(RequestEcho req) {
        logger.debug("Request echo");
        return ResponseEcho.newBuilder().setMessage(req.getMessage()).build();
    }

    @Override public ResponseSetOption requestSetOption(RequestSetOption req) {
        logger.debug("Request set option");
        if (txModel.setOption(req.getKey(), req.getValue())) {
            return ResponseSetOption.newBuilder().setLog("Successfully updated field named '" + req.getKey() + "'").build();
        } else {
            return ResponseSetOption.newBuilder().setLog("Got a bad value or cannot access field named '" + req.getKey() + "'").build();
        }
    }

    @Override public ResponseInfo requestInfo(RequestInfo req) {
        logger.debug("Request info");
        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;
        try {
            data = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Unable to serialize data field, got exception: " + e);
        }
        ResponseInfo.Builder responseBuilder = ResponseInfo.newBuilder();
        responseBuilder.setVersion(version).setLastBlockHeight(lastBlockHeight).setLastBlockAppHash(ByteString.copyFrom(lastAppHash));
        responseBuilder.setData(data);
        return responseBuilder.build();
    }

    @Override public ResponseCheckTx requestCheckTx(RequestCheckTx req) {
        logger.debug("Request check tx");
        String message;
        byte[] tx = req.getTx().toByteArray();
        if (logger.isInfoEnabled()) {
            logger.info("Received tx value: {}", byteArrayToHexSring(tx));
        }
        if (txModel.validate(tx)) {
            message = "Sending OK";
            return ResponseCheckTx.newBuilder().setCode(CodeType.OK).setLog(message).build();
        } else {
            message = "Invalid nonce; expected " + txModel + ", got " + byteArrayToHexSring(tx);
            return ResponseCheckTx.newBuilder().setCode(CodeType.BadNonce).setLog(message).build();
        }
    }

    @Override public ResponseDeliverTx receivedDeliverTx(RequestDeliverTx req) {
        logger.debug("Received deliver tx");
        String message;
        byte[] tx = req.getTx().toByteArray();
        if (logger.isDebugEnabled()) {
            logger.debug("Received tx value: " + byteArrayToHexSring(tx));
        }
        if (txModel.deliver(tx)) {
            message = "Delivered";
            return ResponseDeliverTx.newBuilder().setCode(CodeType.OK).setLog(message).build();
        } else {
            message = "Invalid nonce; expected " + txModel + ", got " + byteArrayToHexSring(tx);
            return ResponseDeliverTx.newBuilder().setCode(CodeType.BadNonce).setLog(message).build();
        }
    }

    @Override public ResponseQuery requestQuery(RequestQuery req) {
        logger.debug("Request query");
        String message;
        String query = new String(req.getData().toByteArray(), Charset.forName("UTF-8"));

        switch (query) {
            case "hash":
                message = "Current hash txModel is now: " + lastBlockHeight;
                logger.debug(message);
                return ResponseQuery.newBuilder().setCode(CodeType.OK).setLog(message).build();
            case "tx":
                message = "Current tx is now: " + txModel;
                logger.debug(message);
                return ResponseQuery.newBuilder().setCode(CodeType.OK).setLog(message).build();
            default:
                message = "Invalid nonce; expected 'hash' or 'tx', got '" + query + "'";
                return ResponseQuery.newBuilder().setCode(CodeType.BadNonce).setLog(message).build();
        }
    }

    @Override public ResponseCommit requestCommit(RequestCommit req) {
        logger.debug("Request commit");

        if (txModel.size() == 0L) {
            return ResponseCommit.newBuilder().build();
        } else {
            lastAppHash = txModel.commit();
            return ResponseCommit.newBuilder().setData(ByteString.copyFrom(lastAppHash)).build();
        }
    }

    @Override public ResponseInitChain requestInitChain(RequestInitChain req) {
        logger.debug("Request init chain");
        return ResponseInitChain.newBuilder().build();
    }

    @Override public ResponseFlush requestFlush(RequestFlush req) {
        logger.debug("Request flush");
        return ResponseFlush.newBuilder().build();
    }

    @Override public ResponseBeginBlock requestBeginBlock(RequestBeginBlock req) {
        logger.debug("Request begin block");

        lastBlockHash = req.getHash().toByteArray();
        if (logger.isDebugEnabled()) {
            logger.debug("hash={}", byteArrayToHex(lastBlockHash));
        }
        return ResponseBeginBlock.newBuilder().build();
    }

    @Override public ResponseEndBlock requestEndBlock(RequestEndBlock req) {
        logger.debug("Request end block");

        lastBlockHeight = req.getHeight();
        if (logger.isDebugEnabled()) {
            logger.debug("hash={} height={}", byteArrayToHex(lastBlockHash), lastBlockHeight);
        }
        return ResponseEndBlock.newBuilder().build();
    }

    @Override public void run() {
        logger.info("Starting application...");
        tSocket.registerListener(this);
        tSocket.start();
    }

    protected void shutdown() {
        logger.info("Initiated shutdown...");
        tSocket.stop();
        logger.info("...waiting for clients to disconnect");
    }

    protected void keepalive() {
        while (!txModel.stop) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}