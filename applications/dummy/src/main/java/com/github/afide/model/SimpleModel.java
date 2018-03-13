package com.github.afide.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afide.api.model.TxModel;
import com.github.jtendermint.merkletree.IMerkleTree;
import com.github.jtendermint.merkletree.MerkleTree;
import com.github.jtendermint.merkletree.byteable.types.IByteable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;

/**
 * A tx model implementation for storing simple values or name/value pairs in a Merkle tree which represents the app
 * state.
 * @author tglaeser
 */
public class SimpleModel<T extends IByteable> extends TxModel {

    /**
     * If set to true, the state across app restarts shall be persisted.
     */
    @JsonProperty protected boolean persist;
    /**
     * If true, the tree was not extended but updated.
     */
    @JsonProperty protected boolean updated;
    /**
     * The Merkle tree.
     */
    protected IMerkleTree<T> tree;

    public SimpleModel(boolean persist) {
        this.persist = persist;
        this.tree = new MerkleTree<T>() {};
    }

    @Override public String toString() {
        return tree.toPrettyString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleModel tree1 = (SimpleModel) o;

        return tree.equals(tree1.tree);
    }

    @Override public int hashCode() {
        return tree.hashCode();
    }

    @Override public boolean validate(byte[] tx) {
        if (tx.length == 0) {
            logger.warn("Received empty tx value");
            return false;
        }
        try {
            IByteable value = getBytable(tx);
            if (null == value) {
                logger.warn("Invalid transaction value, bytable value is null");
                return false;
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Validated tx value {}", value.toPrettyString());
                }
                return true;
            }
        } catch (ArithmeticException e) {
            logger.warn("Invalid transaction value, got exception: {}", e);
            return false;
        }
    }

    @Override public boolean deliver(byte[] tx) {
        if (validate(tx)) {
            updated = tree.add(getBytable(tx));
            if (logger.isInfoEnabled()) {
                logger.info("New tree is now {}", tree.toPrettyString());
            }
            return true;
        }
        return false;
    }

    @Override public byte[] commit() {
        return tree.getRootHash();
    }

    @Override public long size() {
        return (long) tree.size();
    }

    @SuppressWarnings("unchecked") private T getBytable(byte[] tx) {
        Class<T> classOfT = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            return (T) MethodHandles.lookup().findConstructor(classOfT, MethodType.methodType(void.class, byte[].class)).invoke(tx);
        } catch (Throwable e) {
            logger.warn("Unable to instantiate bytable type, got exception: {}", e);
        }
        return null;
    }
}