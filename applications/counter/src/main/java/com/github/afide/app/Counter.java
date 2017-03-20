package com.github.afide.app;

import com.github.afide.api.app.Application;
import com.github.afide.model.CounterModel;
import com.github.jtendermint.jabci.api.ABCIAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The counter app implementation.
 * @author tglaeser
 */
public class Counter extends Application implements ABCIAPI {

    static { version = "0.1"; }

    private Counter(boolean serial) { super(new CounterModel(serial)); }

    public static void main(String[] args) {
        Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);

        new Counter(true);
    }
}