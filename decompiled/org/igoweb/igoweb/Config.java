/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb;

import java.util.Properties;

public class Config
extends org.igoweb.util.Config {
    public static final String COPYRIGHT = "\u00a9 William Shubert";
    public static final String VERSION_MAJOR = "version.major";
    public static final String VERSION_MINOR = "version.minor";
    public static final String VERSION_BUGFIX = "version.bugfix";
    public static final String VERSION_BETA = "version.beta";
    public static final String LOCALE_LIST = "localeList";
    public static final String DEFAULT_HOST = "defaultHost";
    public static final String DEFAULT_PORT = "defaultPort";
    public static final String JSON_HOST = "jsonHost";
    public static final String JSON_PORT = "jsonPort";
    public static final String LOAD_BALANCER = "loadBalancer";
    public static final String HTTP_MASTER_HOST = "httpMasterHost";
    public static final String OVERRIDE_HOST = "overrideHost";
    public static final String PREFS_SUBDIR = "prefsSubdir";
    public static final String DB_NAME = "dbName";
    public static final String DB_LOG_NAME = "dbLogName";
    public static final String DB_MASTER_HOST = "dbMasterHost";
    public static final String DB_REPL_HOST = "dbReplHost";
    public static final String DB_SERVER_USER = "dbServerUser";
    public static final String DB_SERVER_PASSWORD = "dbServerPassword";
    public static final String DB_JSP_USER = "dbJspUser";
    public static final String DB_JSP_PASSWORD = "dbJspPassword";
    public static final String DB_RANK_SYSTEM_USER = "dbRankSystemUser";
    public static final String DB_RANK_SYSTEM_PASSWORD = "dbRankSystemPassword";
    public static final String DB_TOURN_USER = "dbTournUser";
    public static final String DB_TOURN_PASSWORD = "dbTournPassword";
    public static final String DB_GAME_BACKUP_USER = "dbGameBackupUser";
    public static final String DB_GAME_BACKUP_PASSWORD = "dbGameBackupPassword";
    public static final String CRYPTO_MODULUS = "modulus";
    public static final String CRYPTO_PUBLIC = "public";
    public static final String CRYPTO_SECRET = "secret";
    public static final String SMTP_HOST = "smtpHost";
    public static final String WEB_HOST = "webHost";
    public static final String FILE_HOST = "fileHost";
    public static final String JSP_STATIC_PAGES = "jspStaticPages";
    public static final String LOG_DIR = "logDir";
    public static final String GRAPH_DIR = "graphDir";
    public static final String GAME_DIR = "gameDir";
    public static final String BACKUP_GAME_DIR = "backupGameDir";
    public static final String HTML_DIR = "htmlDir";
    public static final String WEB_HTML_DIR = "webHtmlDir";
    public static final String USER_AVATAR_DIR = "userAvatarDir";
    public static final String USER_AVATAR_PENDING_DIR = "userAvatarPendingDir";
    public static final String PLAYBACK_DIR = "playbackDir";
    public static final String PLAYBACK_CACHE_DIR = "playbackCacheDir";
    public static final String AD_DIR = "adDir";
    public static final String RMI_APPLICATION = "rmiApplication";
    public static final String RMI_MAIN_HOST = "rmiMainHost";
    public static final String RMI_WEB_HOST = "rmiWebHost";
    public static final String PAY_PAL_HOST = "payPalHost";
    public static final String CREDIT_CARD_TEST = "creditCardTest";
    public static final String DIRECT_CREDIT_CARD_PRESENT = "directCreditCardPresent";
    public static final String CREDIT_CARD_PROCESSING_URL = "creditCardProcessingUrl";
    public static final String CREDIT_CARD_LOGIN_ID = "creditCardLoginId";
    public static final String MASTER_LOCALE = "masterLocale";
    public static final String SERVER_THREAD_POOL_SIZE = "serverThreadPoolSize";
    public static final String SERVER_MAX_CLIENTS = "serverMaxClients";
    public static final String ADMIN_EMAIL = "adminEmail";
    public static final String SERVER_NUM_NETWORK_THREADS = "serverNumNetworkThreads";
    public static final String BUILD_TIMESTAMP = "buildTimestamp";
    public static final String ENABLE_CLEANUP = "enableCleanup";
    public static final String CLEANUP_PENDING_DURATION = "cleanupPendingInterval";
    public static final String CLEANUP_REGISTERED_DURATION = "cleanupRegisteredInterval";
    public static final String CLEANUP_LONG_LIVED_DURATION = "cleanupLongLivedInterval";
    public static final String ROOM_TIMEOUT_DURATION = "roomTimeoutDuration";
    public static final String LANDING_PAGE = "landingPage";

    public static String get(String key) {
        return Config.getRaw(key);
    }

    public static long getLong(String key) {
        return Config.getLongRaw(key);
    }

    public static int getInt(String key) {
        return Config.getIntRaw(key);
    }

    public static boolean getBoolean(String key) {
        return Config.getBoolRaw(key);
    }

    public static Properties getAll() {
        return Config.getAllRaw();
    }

    public static void privateNeeded() {
        if (Config.get(DB_NAME) == null) {
            throw new RuntimeException("Private configuration data is needed but not present");
        }
    }

    static {
        Config.loadConfig("/org/igoweb/igoweb/config.properties");
    }
}
