package mat.easieranvils.mixin;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.StringHelper.isValidChar;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
	@Shadow @Final private Property levelCost;
	@Shadow private int repairItemUsage;

	@Shadow @Final private static Logger LOGGER;

	public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
    }
	/*
  	Based on Yarn mappings
 	*/
	/**
	 * Redirect's the stripInvalidChars call in anvil's sanitize method to replace ampersand with NAK (aka double s aka format code symbol)
	 */
	@Redirect(method="sanitize",at= @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;stripInvalidChars(Ljava/lang/String;)Ljava/lang/String;"))
	private static String stripInvalidChars(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		char[] var3 = string.toCharArray();
		int var4 = var3.length;

		for(int var5 = 0; var5 < var4; ++var5) {
			char c = var3[var5];
			if (isValidChar(c)) {
				if (c == '&')
					stringBuilder.append('§');
				else
					stringBuilder.append(c);
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Overwrites on TakeOutput
	 */
	public void onTakeOutput(PlayerEntity player, ItemStack stack) {
        LOGGER.info("Cost is {}", this.levelCost.get());
		if (this.input.getStack(1) != ItemStack.EMPTY){
			LOGGER.info("BRUH IF");
			if (!player.getAbilities().creativeMode) {
				player.addExperienceLevels(-this.levelCost.get());
			}

			this.input.setStack(0, ItemStack.EMPTY);
			if (this.repairItemUsage > 0) {
				ItemStack itemStack = this.input.getStack(1);
				if (!itemStack.isEmpty() && itemStack.getCount() > this.repairItemUsage) {
					itemStack.decrement(this.repairItemUsage);
					this.input.setStack(1, itemStack);
				} else {
					this.input.setStack(1, ItemStack.EMPTY);
				}
			} else {
				this.input.setStack(1, ItemStack.EMPTY);
			}

			this.levelCost.set(0);
			this.context.run((world, pos) -> {
				BlockState blockState = world.getBlockState(pos);
				if (!player.isInCreativeMode() && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
					BlockState blockState2 = AnvilBlock.getLandingState(blockState);
					if (blockState2 == null) {
						world.removeBlock(pos, false);
						world.syncWorldEvent(1029, pos, 0);
					} else {
						world.setBlockState(pos, blockState2, 2);
						world.syncWorldEvent(1030, pos, 0);
					}
				} else {
					world.syncWorldEvent(1030, pos, 0);
				}

			});
		}
		else{
			LOGGER.info("BRUH ELSE");
			player.addExperienceLevels(0);
			this.input.setStack(0, ItemStack.EMPTY);
			this.levelCost.set(0);
			this.context.run((world, pos) -> {
				BlockState blockState = world.getBlockState(pos);
				world.syncWorldEvent(1030, pos, 0);
			});
		}
	}

	@Inject(method="updateResult",at=@At("TAIL"))
	public void updateResultMixin(CallbackInfo ci){
		if (!this.input.getStack(0).isEmpty() && this.input.getStack(1) == ItemStack.EMPTY && !this.output.getStack(0).isEmpty() && this.levelCost.get() > 1) {
			this.levelCost.set(1);
		}
	}
}