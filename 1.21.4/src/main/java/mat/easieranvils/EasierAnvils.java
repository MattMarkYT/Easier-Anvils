package mat.easieranvils;

import mat.easieranvils.commands.*;
import mat.easieranvils.config.EasierOptions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasierAnvils implements ModInitializer {
	public static final String MOD_ID = "easier-anvils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Easier Anvils");
		EasierOptions.registerConfigs();
		registerCommands();

	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register(ChangeXpCapCommand::register);
		CommandRegistrationCallback.EVENT.register(ChangeColoredRenameCommand::register);
		CommandRegistrationCallback.EVENT.register(ChangeColoredCharCommand::register);
		CommandRegistrationCallback.EVENT.register(ChangeAnvilWorkaroundCommand::register);
		CommandRegistrationCallback.EVENT.register(ChangeAnvilRepairCommand::register);
		CommandRegistrationCallback.EVENT.register(UserHelpCommand::register);

	}
}