package mat.easieranvils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.config.EasierOptions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.function.Supplier;

public class ChangeColoredRenameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("coloredrename").requires((player)->{
                            return player.hasPermissionLevel(2);
                        })
                .then(CommandManager.argument("value",BoolArgumentType.bool())
                        .executes(ChangeColoredRenameCommand::changeColoredRename))));
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("coloredrename")
                        .executes(ChangeColoredRenameCommand::getColoredRename)));
    }

    private static int changeColoredRename(CommandContext<ServerCommandSource> context) {
        boolean coloredRename = BoolArgumentType.getBool(context, "value");

        try {
            EasierOptions.COLORED_RENAME = coloredRename;
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Turned " + (coloredRename ? "on" : "off") + " colored item rename").formatted(coloredRename ? Formatting.GREEN : Formatting.RED);
                }
            }, true);
            EasierOptions.refreshConfigs();
        } catch (IOException e) {
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Failed to save to configs file").formatted(Formatting.RED);
                }
            }, false);

        }

        return 1;
    }
    private static int getColoredRename(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new Supplier<Text>() {
            @Override
            public Text get() {
                return Text.literal("§eColored renames are currently "+ (EasierOptions.COLORED_RENAME ? "§aenabled §ewith custom symbol §a"+EasierOptions.COLORED_CHAR : "§cdisabled"));
            }
        }, false);
        return 1;
    }
}
