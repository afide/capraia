package com.github.afide.app;

import com.github.afide.api.app.Application;
import com.github.afide.model.SimpleModel;
import com.github.jtendermint.jabci.api.ABCIAPI;
import com.github.jtmsp.merkletree.byteable.ByteableLong;
import com.github.jtmsp.merkletree.byteable.ByteablePair;
import com.github.jtmsp.merkletree.byteable.ByteableString;
import com.github.jtmsp.merkletree.byteable.IByteable;
import com.google.devtools.common.options.EnumConverter;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Collections;

/**
 * The dummy app implementation.
 * @author tglaeser
 */
public class Dummy extends Application implements ABCIAPI {

    static { version = "0.1"; }

    private Dummy(SimpleModel<? extends IByteable> tree) {
        super(tree);
    }

    public static void main(String[] args) {
        OptionsParser parser = OptionsParser.newOptionsParser(DummyOptions.class);
        parser.parseAndExitUponError(args);
        DummyOptions options = parser.getOptions(DummyOptions.class);
        SimpleModel<? extends IByteable> tree = null;
        if (options == null || options.type == null || options.help) {
            printUsage(parser);
            return;
        } else if (options.version) {
            printVersion();
            return;
        } else if (options.type == ByteableTypeEnum.LONG) {
            tree = new SimpleModel<ByteableLong>(options.persist){};
        } else if (options.type == ByteableTypeEnum.STRING) {
            tree = new SimpleModel<ByteableString>(options.persist){};
        } else if (options.type == ByteableTypeEnum.PAIR) {
            tree = new SimpleModel<ByteablePair>(options.persist){};
        }
        Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        if (options.info) {
            rootLogger.setLevel(Level.INFO);
        } else if (options.debug) {
            rootLogger.setLevel(Level.DEBUG);
        }
        new Dummy(tree);
    }

    private static void printVersion() {
        System.out.println("Version: " + version);
    }

    private static void printUsage(OptionsParser parser) {
        System.out.println("Usage: java -jar dummy.jar OPTIONS");
        System.out.println(parser.describeOptions(Collections.emptyMap(), OptionsParser.HelpVerbosity.LONG));
    }

    private enum ByteableTypeEnum {
        LONG("long"), STRING("string"), PAIR("pair");

        private final String name;

        ByteableTypeEnum(String name) {
            this.name = name;
        }

        @Override public String toString() {
            return this.name;
        }
    }

    @SuppressWarnings("WeakerAccess") protected static class ByteableTypeEnumConverter extends EnumConverter<ByteableTypeEnum> {
        public ByteableTypeEnumConverter() {
            super(ByteableTypeEnum.class, "byteable type");
        }
    }

    @SuppressWarnings("WeakerAccess") protected static class DummyOptions extends OptionsBase {

        @Option(name = "help", abbrev = 'h', help = "Prints usage info.", defaultValue = "false") public boolean help;
        @Option(name = "version", help = "The application version.", defaultValue = "false") public boolean version;
        @Option(name = "info", abbrev = 'i', help = "Set log level to info.", defaultValue = "false") public boolean info;
        @Option(name = "debug", abbrev = 'd', help = "Log in debug mode.", defaultValue = "false") public boolean debug;
        @Option(name = "persist", help = "Persist the transactions.", category = "startup", defaultValue = "false") public boolean persist;
        @Option(name = "type", help = "The transaction type.", category = "startup", converter = ByteableTypeEnumConverter.class, defaultValue = "string") public ByteableTypeEnum type;

        public DummyOptions() { super(); }
    }
}