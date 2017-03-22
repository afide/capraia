package com.github.afide.api.app;

import com.github.afide.api.model.TxModel;
import com.github.jtendermint.jabci.api.ABCIAPI;
import com.github.jtendermint.jabci.socket.TSocket;
import com.github.jtendermint.jabci.types.Types;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic app implementation.
 * @author tglaeser
 */
public abstract class Application implements ABCIAPI {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    protected static String version;

    private TxModel txModel;

    private transient long lastBlockHeight = 0;
    private transient byte[] lastBlockHash = new byte[]{};
    private transient byte[] lastAppHash = new byte[]{};

    protected Application(TxModel txModel) {
        this.txModel = txModel;
        logger.info("Starting application...");

        TSocket socket = new TSocket();
        socket.registerListener(this);

        new Thread(socket::start).start();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException  e) {
                Thread.currentThread().interrupt();
                logger.info("...finished application: {}", e);
            }
        }
    }

    private static String byteArrayToHexSring(byte[] bytes) {
        return "0x" + byteArrayToHex(bytes);
    }

    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    @Override public Types.ResponseEcho requestEcho(Types.RequestEcho req) {
        logger.debug("Request echo");
        return Types.ResponseEcho.newBuilder().setMessage(req.getMessage()).build();
    }

    @Override public Types.ResponseSetOption requestSetOption(Types.RequestSetOption req) {
        logger.debug("Request set option");
        if (txModel.setOption(req.getKey(), req.getValue())) {
            return Types.ResponseSetOption.newBuilder().build();
        } else {
            return Types.ResponseSetOption.newBuilder().setLog("Got a bad value or cannot access field named '" + req.getKey() + "'").build();
        }
    }

    @Override public Types.ResponseInfo requestInfo(Types.RequestInfo req) {
        logger.debug("Request info");
        String data = new Gson().toJson(this);
        Types.ResponseInfo.Builder responseBuilder = Types.ResponseInfo.newBuilder();
        responseBuilder.setVersion(version).setLastBlockHeight(lastBlockHeight).setLastBlockAppHash(ByteString.copyFrom(lastAppHash));
        responseBuilder.setData(data);
        return responseBuilder.build();
    }

    @Override public Types.ResponseCheckTx requestCheckTx(Types.RequestCheckTx req) {
        logger.debug("Request check tx");
        String message;
        byte[] tx = req.getTx().toByteArray();
        logger.info("Received tx value: {}", byteArrayToHexSring(tx));
        if (txModel.validate(tx)) {
            message = "Sending OK";
            return Types.ResponseCheckTx.newBuilder().setCode(Types.CodeType.OK).setLog(message).build();
        } else {
            message = "Invalid nonce; expected " + txModel + ", got " + byteArrayToHexSring(tx);
            return Types.ResponseCheckTx.newBuilder().setCode(Types.CodeType.BadNonce).setLog(message).build();
        }
    }

    @Override public Types.ResponseDeliverTx receivedDeliverTx(Types.RequestDeliverTx req) {
        logger.debug("Received deliver tx");
        String message;
        byte[] tx = req.getTx().toByteArray();
        logger.debug("Received tx value: " + byteArrayToHexSring(tx));
        if (txModel.deliver(tx)) {
            message = "Delivered";
            return Types.ResponseDeliverTx.newBuilder().setCode(Types.CodeType.OK).setLog(message).build();
        } else {
            message = "Invalid nonce; expected " + txModel + ", got " + byteArrayToHexSring(tx);
            return Types.ResponseDeliverTx.newBuilder().setCode(Types.CodeType.BadNonce).setLog(message).build();
        }
    }

    @Override public Types.ResponseQuery requestQuery(Types.RequestQuery req) {
        logger.debug("Request query");
        String message;
        String query = req.getQuery().toStringUtf8();

        switch (query) {
            case "hash":
                message = "Current hash txModel is now: " + lastBlockHeight;
                logger.debug(message);
                return Types.ResponseQuery.newBuilder().setCode(Types.CodeType.OK).setLog(message).build();
            case "tx":
                message = "Current tx is now: " + txModel;
                logger.debug(message);
                return Types.ResponseQuery.newBuilder().setCode(Types.CodeType.OK).setLog(message).build();
            default:
                message = "Invalid nonce; expected 'hash' or 'tx', got '" + query + "'";
                return Types.ResponseQuery.newBuilder().setCode(Types.CodeType.BadNonce).setLog(message).build();
        }
    }

    @Override public Types.ResponseCommit requestCommit(Types.RequestCommit req) {
        logger.debug("Request commit");

        if (txModel.size() == 0L) {
            return Types.ResponseCommit.newBuilder().setCode(Types.CodeType.OK).build();
        } else {
            lastAppHash = txModel.commit();
            return Types.ResponseCommit.newBuilder().setCode(Types.CodeType.OK).setData(ByteString.copyFrom(lastAppHash)).build();
        }
    }

    @Override public Types.ResponseInitChain requestInitChain(Types.RequestInitChain req) {
        logger.debug("Request init chain");
        return null;
    }

    @Override public Types.ResponseFlush requestFlush(Types.RequestFlush req) {
        logger.debug("Request flush");
        return Types.ResponseFlush.newBuilder().build();
    }

    @Override public Types.ResponseBeginBlock requestBeginBlock(Types.RequestBeginBlock req) {
        logger.debug("Request begin block");

        lastBlockHash = req.getHash().toByteArray();
        logger.debug("hash={}", byteArrayToHex(lastBlockHash));
        return Types.ResponseBeginBlock.newBuilder().build();
    }

    @Override public Types.ResponseEndBlock requestEndBlock(Types.RequestEndBlock req) {
        logger.debug("Request end block");

        lastBlockHeight = req.getHeight();
        logger.debug("hash={} height={}", byteArrayToHex(lastBlockHash), lastBlockHeight);
        return Types.ResponseEndBlock.newBuilder().build();
    }
}