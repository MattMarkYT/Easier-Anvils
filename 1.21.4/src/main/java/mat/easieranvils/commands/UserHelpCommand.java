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

public class UserHelpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

        dispatcher.register(CommandManager.literal("easieranvils").requires((player)->{
                    return player.hasPermissionLevel(0);
                    }).then(CommandManager.literal("help")
                        .executes(UserHelpCommand::printHelp)));
    }

    private static int printHelp(CommandContext<ServerCommandSource> context) {
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("How to use Easier Anvils:\n\n"+
                            
                            "§eAnvil Repair - Right click an anvil with an iron block\n"+
                            "§fCurrently: "+(EasierOptions.ANVIL_REPAIR?"§aEnabled": "§cDisabled")+
                            "\n§fCurrent Xp Cap: §a"+ EasierOptions.XP_CAP+
                            
                            "\n\n§eAnvil Xp Cap Workaround - To workaround the xp cap, you don't use the anvil screen. "+
                            "Use your main hand as slot 1 and your offhand as slot 2. "+
                            "Punch the anvil to check the price. Interact with the anvil to combine the items.\n"+
                            "§fCurrently: "+(EasierOptions.ANVIL_WORKAROUND?"§aEnabled": "§cDisabled")+

                            "\n\n§eColored Rename - At the beginning of the item name, put the custom symbol, then the color code. Look up Minecraft color codes online to find out how to change\n" +
                            "§fCurrently: "+(EasierOptions.COLORED_RENAME?"§aEnabled": "§cDisabled")+
                            "\n§fCustom symbol: §a"+EasierOptions.COLORED_CHAR);
                }}, false);
        return 1;
    }

}
