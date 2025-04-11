package mat.easieranvils.config;

import com.mojang.datafixers.util.Pair;
import mat.easieranvils.EasierAnvils;

import java.io.IOException;

public class EasierOptions {
    public static SimpleConfig CONFIG;
    private static EasierConfigProvider configs;

    public static int XP_CAP = 55;
    public static boolean COLORED_RENAME = true;
    public static char COLORED_CHAR = '&';
    public static boolean ANVIL_WORKAROUND = true;
    public static boolean ANVIL_REPAIR = true;

    public static int registerConfigs() {
        configs = new EasierConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(EasierAnvils.MOD_ID).provider(configs).request();

        assignConfigs();
        if (!isValidChar(COLORED_CHAR)) {
            try {
                refreshConfigs();
                return -1;
            } catch (IOException e) {
                EasierAnvils.LOGGER.error("An error occurred while refreshing config");
                EasierAnvils.LOGGER.error(e.getLocalizedMessage());
                return -2;
            }
        }
        return 1;
    }
    public static void refreshConfigs() throws IOException {
        if (!isValidChar(COLORED_CHAR)) {
            EasierAnvils.LOGGER.warn("Invalid character for colored rename. Resetting to default &");
            COLORED_CHAR = '&';
        }
        configs.resetConfigsList();
        createConfigs();

        CONFIG.refreshConfig();

    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("xp_cap", XP_CAP));
        configs.addKeyValuePair(new Pair<>("colored_rename", COLORED_RENAME));
        configs.addKeyValuePair(new Pair<>("colored_char", String.valueOf(COLORED_CHAR)));
        configs.addKeyValuePair(new Pair<>("anvil_workaround", ANVIL_WORKAROUND));
        configs.addKeyValuePair(new Pair<>("anvil_repair", ANVIL_REPAIR));
    }

    private static void assignConfigs() {
        XP_CAP = CONFIG.getOrDefault("xp_cap", 55);
        COLORED_RENAME = CONFIG.getOrDefault("colored_rename", true);
        COLORED_CHAR = CONFIG.getOrDefault("colored_char", "&").charAt(0);
        ANVIL_WORKAROUND = CONFIG.getOrDefault("anvil_workaround", true);
        ANVIL_REPAIR = CONFIG.getOrDefault("anvil_repair", true);

    }

    public static boolean isValidChar(char c) {
        return c != 167 && c >= ' ' && !(c > '0' && c < '9') && !(c > 'A' && c < 'Z') && !(c >= 'a' && c <= 'z');
    }
}
