package mat.easieranvils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.config.EasierOptions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.function.Supplier;

public class ChangeAnvilWorkaroundCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("anvilworkaround").requires((player)->{
                            return player.hasPermissionLevel(2);
                        })
                .then(CommandManager.argument("value",BoolArgumentType.bool())
                        .executes(ChangeAnvilWorkaroundCommand::changeAnvilWorkaround))));
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("anvilworkaround")
                        .executes(ChangeAnvilWorkaroundCommand::getAnvilWorkaround)));
    }

    private static int changeAnvilWorkaround(CommandContext<ServerCommandSource> context) {
        boolean anvilWorkaround = BoolArgumentType.getBool(context, "value");

        try {
            EasierOptions.ANVIL_WORKAROUND = anvilWorkaround;
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Turned " + (anvilWorkaround ? "on" : "off") + " anvil workaround").formatted(anvilWorkaround ? Formatting.GREEN : Formatting.RED);
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
    private static int getAnvilWorkaround(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new Supplier<Text>() {
            @Override
            public Text get() {
                return Text.literal("§eAnvil Workaround is currently "+ (EasierOptions.ANVIL_WORKAROUND ? "§aenabled" : "§cdisabled"));
            }
        }, false);
        return 1;
    }
}
