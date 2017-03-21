package com.github.afide.model;

import com.github.afide.api.model.TxModel;
import com.github.jtmsp.merkletree.IMerkleTree;
import com.github.jtmsp.merkletree.MerkleTree;
import com.github.jtmsp.merkletree.byteable.IByteable;

import java.lang.reflect.InvocationTargetException;
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
    protected boolean persist = false;
    /**
     * If true, the tree was not extended but updated.
     */
    protected boolean updated;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
            logger.info("Validated tx value {}", value.toPrettyString());
            return true;
        } catch (ArithmeticException e) {
            logger.warn("Invalid transaction value, got exception: {}", e.getLocalizedMessage());
            return false;
        }
    }

    @Override public boolean deliver(byte[] tx) {
        if (validate(tx)) {
            updated = tree.add(getBytable(tx));
            logger.info("New tree is now {}", tree.toPrettyString());
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

    private T getBytable(byte[] tx) {
        @SuppressWarnings("unchecked") Class<T> classOfT = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            return classOfT.getDeclaredConstructor(new Class[]{byte[].class}).newInstance((Object) tx);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.warn("Unable to instantiate bytable type, got exception: {}", e.getLocalizedMessage());
        }
        return null;
    }
}