/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Properties props;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static void loadConfig(String path) {
        Class<Config> clazz = Config.class;
        synchronized (Config.class) {
            if (props != null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
            props = new Properties();
            try {
                InputStream in = Config.class.getResourceAsStream(path);
                if (in == null) {
                    throw new RuntimeException("Cannot open resource " + path);
                }
                props.load(in);
                in.close();
            }
            catch (IOException excep) {
                throw new RuntimeException("Unable to load config properties from " + path + ": " + excep);
            }
            return;
        }
    }

    public static String getRaw(String key) {
        return props.getProperty(key);
    }

    protected static int getIntRaw(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    protected static long getLongRaw(String key) {
        return Long.parseLong(props.getProperty(key));
    }

    protected static boolean getBoolRaw(String key) {
        return Boolean.valueOf(props.getProperty(key));
    }

    public static Properties getAllRaw() {
        return props;
    }
}
