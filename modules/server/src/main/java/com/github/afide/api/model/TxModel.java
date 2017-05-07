package com.github.afide.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * The tx model interface.
 * @author tglaeser
 */
public abstract class TxModel {

    protected static final Logger logger = LoggerFactory.getLogger(TxModel.class);

    public volatile boolean stop = false;

    public abstract boolean validate(byte[] tx);

    public abstract boolean deliver(byte[] tx);

    public abstract byte[] commit();

    public abstract long size();

    public boolean setOption(String key, String value) {
        try {
            Field field = this.getClass().getField(key);
            if (field.getType().equals(boolean.class)) {
                if ("on".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
                    field.setBoolean(this, true);
                    return true;
                } else if ("off".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    field.setBoolean(this, false);
                    return true;
                }
            }
        } catch (NoSuchFieldException e) {
            logger.error("Field with name '{}' does not exist: {}", key, e);
            return false;
        } catch (IllegalAccessException e) {
            logger.error("Cannot access field with name '{}': {}", key, e);
            return false;
        }
        return false;
    }
}