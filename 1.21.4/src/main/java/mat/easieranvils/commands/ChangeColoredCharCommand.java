package mat.easieranvils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.config.EasierOptions;
import mat.easieranvils.screenhandlers.ChangeCharScreen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ChangeColoredCharCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("coloredrenamechar").requires((player)->{
                    return player.hasPermissionLevel(2);
                }).executes(ChangeColoredCharCommand::changeColoredRename)));
    }

    private static int changeColoredRename(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getPlayer() != null){
            ChangeCharScreen.settings(context.getSource().getPlayer(), context);
        }
        return 1;
    }
}
