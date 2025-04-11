package mat.easieranvils.screenhandlers;

import com.mojang.brigadier.context.CommandContext;
import mat.easieranvils.EasierAnvils;
import mat.easieranvils.config.EasierOptions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EggItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.function.Supplier;

public class ChangeCharScreen {
    private ChangeCharScreen() {}

    final static ItemStack egg = new ItemStack(Items.EGG,1);

    public static void settings(ServerPlayerEntity p, CommandContext<ServerCommandSource> context){
        final AnvilScreenHandler[] screen = {null};
        egg.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Rename Me!"));
        p.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            screen[0] = new AnvilScreenHandler(syncId, inventory); return screen[0];
        }, Text.literal("DO NOT INSERT ITEMS")));
        ScreenHandlerListener s = new ScreenHandlerListener() {
            ItemStack prevItemStack = null;
            String newRenameColorChar = "&";
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
                String stackName = stack.getName().getString();
                EasierAnvils.LOGGER.info("Slot: "+slotId+". Name: "+(stack.isEmpty() ? "Stack is Empty" : ("Stack is "+stackName)));
                if (slotId == 2) {
                    if (stack.isOf(Items.EGG) && !stackName.equals("Egg")) {
                        newRenameColorChar = stackName;
                        prevItemStack = stack;
                    }
                    else if (stack.isEmpty() && prevItemStack != null){
                        //handler.setCursorStack(ItemStack.EMPTY);
                        //handler.updateToClient();
                        p.closeHandledScreen();
                        changeColoredRename(newRenameColorChar, p, context);
                    }
                }
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        };
        if (screen[0] != null) {
            screen[0].addListener(s);
            screen[0].setStackInSlot(0,0,egg);
        }
    }
    public static void changeColoredRename(String newRenameColorChar, ServerPlayerEntity player, CommandContext<ServerCommandSource> context){
        if (newRenameColorChar.length() == 1 && EasierOptions.isValidChar(newRenameColorChar.charAt(0))){
            EasierOptions.COLORED_CHAR = newRenameColorChar.charAt(0);
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Colored item character set to " + EasierOptions.COLORED_CHAR).formatted(Formatting.GREEN);
                }
            }, false);
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
        }
        else {
            context.getSource().sendFeedback(new Supplier<Text>() {
                @Override
                public Text get() {
                    return Text.literal("Must be one valid vanilla character and not a letter. Reverted to &...").formatted(Formatting.RED);
                }
            }, false);
        }
    }
}

