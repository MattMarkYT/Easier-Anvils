package mat.easieranvils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.config.EasierOptions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.function.Supplier;

public class ChangeXpCapCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("xpcap").requires((player)->{
                            return player.hasPermissionLevel(2);
                        })
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1,127))
                        .executes(ChangeXpCapCommand::changeXpCap))));
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("xpcap")
                        .executes(ChangeXpCapCommand::getXpCap)));
    }

    private static int changeXpCap(CommandContext<ServerCommandSource> context) {
        int xpCap = IntegerArgumentType.getInteger(context, "newxpcap");


        EasierOptions.XP_CAP = xpCap;
        context.getSource().sendFeedback(new Supplier<Text>() {
            @Override
            public Text get() {
                return Text.literal("Changed the XP Cap to " + xpCap).formatted(Formatting.GREEN);
            }
            }, true);
        try {
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
    private static int getXpCap(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new Supplier<Text>() {
            @Override
            public Text get() {
                return Text.literal("§eXp Cap is currently §a"+ EasierOptions.XP_CAP);
            }
        }, false);
        return 1;
    }
}
