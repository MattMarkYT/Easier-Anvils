package mat.easieranvils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.EasierAnvils;
import mat.easieranvils.config.EasierOptions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.function.Supplier;

public class ChangeAnvilRepairCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("anvilrepair").requires((player)->{
                            return player.hasPermissionLevel(2);
                        })
                .then(CommandManager.argument("value",BoolArgumentType.bool())
                        .executes(ChangeAnvilRepairCommand::changeAnvilRepair))));
        dispatcher.register(CommandManager.literal("easieranvils")
                .then(CommandManager.literal("anvilrepair")
                        .executes(ChangeAnvilRepairCommand::getAnvilRepair)));
    }

    private static int changeAnvilRepair(CommandContext<ServerCommandSource> context) {
        boolean anvilRepair = BoolArgumentType.getBool(context, "value");

        try {
            EasierOptions.ANVIL_REPAIR = anvilRepair;
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Turned " + (anvilRepair ? "on" : "off") + " anvil repair").formatted(anvilRepair ? Formatting.GREEN : Formatting.RED);
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
    private static int getAnvilRepair(CommandContext<ServerCommandSource> context) {
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("§eAnvil Repair is currently "+ (EasierOptions.ANVIL_REPAIR ? "§aenabled" : "§cdisabled"));
                }
            }, false);
        return 1;
    }

}
