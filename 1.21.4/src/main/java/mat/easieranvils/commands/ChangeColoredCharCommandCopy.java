package mat.easieranvils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.config.EasierOptions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

public class ChangeColoredCharCommandCopy {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("easieranvils").requires((player)->{
            return player.hasPermissionLevel(2);
                }).then(CommandManager.literal("coloredrenamechar").executes(ChangeColoredCharCommandCopy::changeColoredRename)));
    }

    private static int changeColoredRename(CommandContext<ServerCommandSource> context) {
        char prevColoredChar = EasierOptions.COLORED_CHAR;
        int result = EasierOptions.registerConfigs();

        if (result == 1) {
            if (prevColoredChar != EasierOptions.COLORED_CHAR) {
                context.getSource().sendFeedback(new Supplier<Text>() {
                    @Override
                    public Text get() {
                        return Text.literal("Colored item character set to " + EasierOptions.COLORED_CHAR).formatted(Formatting.GREEN);
                    }
                }, true);
            }
            else{
                context.getSource().sendFeedback(new Supplier<Text>() {
                    @Override
                    public Text get() {
                        return Text.literal("To change this, you have to change the config file and run this command again.\n" +
                                "It's located in the config folder of this instance");
                    }
                }, true);
            }
        }
        else if (result == -1) {
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Must be one valid vanilla character and not a letter. Reverted to &...").formatted(Formatting.RED);
                }
            }, true);
        }
        else {
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Must be one valid vanilla character and not a letter. Reverted to & but failed to save to config file").formatted(Formatting.RED);
                }
            }, true);
        }


        return 1;
    }
}
