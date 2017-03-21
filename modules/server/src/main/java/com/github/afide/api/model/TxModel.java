package com.github.afide.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * The tx model interface.
 * @author tglaeser
 */
public abstract class TxModel {

    protected final static Logger logger = LoggerFactory.getLogger(TxModel.class);

    public abstract boolean validate(byte[] tx);

    public abstract boolean deliver(byte[] tx);

    public abstract byte[] commit();

    public abstract long size();

    public boolean setOption(String key, String value) {
        try {
            Field field = this.getClass().getDeclaredField(key);
            if (field.getType().equals(boolean.class)) {
                if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
                    field.setBoolean(this, true);
                    return true;
                } else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
                    field.setBoolean(this, false);
                    return true;
                }
            }
        } catch (NoSuchFieldException e) {
            logger.error("Field with name '{}' does not exist.", key);
            return false;
        } catch (IllegalAccessException e) {
            logger.error("Cannot access field with name '{}'.", key);
            return false;
        }
        return false;
    }
}