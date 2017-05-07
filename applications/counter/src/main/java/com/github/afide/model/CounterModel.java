package com.github.afide.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afide.api.model.TxModel;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * A tx model implementation for long values where each new value must be equal or larger than the tx count. In serial
 * mode every new value must be consecutive compared to the value from the previous transaction. The app state is
 * simply represented by the tx count.
 * @author tglaeser
 */
public class CounterModel extends TxModel {

    /* If true, new value of tx n must be consecutive compared to the value from tx n-1. */
    public boolean serial = false;

    @JsonProperty private long txCount = 0;

    public CounterModel(boolean serial) {
        this.serial = serial;
    }

    @Override public String toString() {
        return "0x" + String.format("%02X", txCount);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CounterModel count = (CounterModel) o;

        return txCount == count.txCount;
    }

    @Override public int hashCode() {
        return (int) (txCount ^ (txCount >>> 32));
    }

    @Override public boolean validate(byte[] tx) {
        if (tx.length == 0) {
            logger.warn("Received empty tx value");
            return false;
        } else if (tx.length <= Long.BYTES) {
            long txValue = new BigInteger(1, tx).longValueExact();
            if ((serial && txValue != txCount) || txValue < txCount) {
                logger.warn("Invalid transaction value; expected {}, got {}", txCount, txValue);
                return false;
            }
        } else {
            logger.warn("Received bad tx value");
            return false;
        }
        logger.info("Received tx value is valid");
        return true;
    }

    @Override public boolean deliver(byte[] tx) {
        if (validate(tx)) {
            txCount += 1;
            logger.info("New tx count is now {}", txCount);
            return true;
        }
        return false;
    }

    @Override public byte[] commit() {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(txCount);
        buf.flip();
        return buf.array();
    }

    @Override public long size() {
        return txCount;
    }
}