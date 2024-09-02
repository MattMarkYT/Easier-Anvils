package mat.easieranvils.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.Property;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

import static net.minecraft.block.AnvilBlock.getLandingState;
import static net.minecraft.screen.AnvilScreenHandler.getNextCost;


@Mixin(value = AnvilBlock.class)
public abstract class AnvilBlockMixin extends AbstractBlock {
    @Final
    @Shadow
    public static DirectionProperty FACING;
    @Unique
    private final Property levelCost = Property.create();
    @Unique
    private int repairItemUsage;
    @Unique
    private ItemStack output;

    public AnvilBlockMixin(Settings settings) {
        super(settings);
    }
/*
  Based on Yarn mappings
 */
    /**
     * Injects custom logic for anvil healing and alternate item combination at the top of the onUse method.
     * <p>Any return statements here are to cancel the anvil screen.</p>
     *
     */
    @Inject(method="onUse",at=@At("HEAD"), cancellable = true)
    public void onUseMixin(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        //Checking if the game is a client might not be necessary, but the original method checks, so it's there just in case.
        if (!world.isClient) {
            ItemStack mainhand = player.getMainHandStack();
            ItemStack offhand = player.getOffHandStack();

            //Anvil Healing
            if (mainhand.getItem().getTranslationKey().equals("block.minecraft.iron_block") || offhand.getItem().getTranslationKey().equals("block.minecraft.iron_block")) {
                BlockState blockState2 = this.inverseGetLandingState(world.getBlockState(pos));
                if (blockState2 != null) {
                    if (!player.isInCreativeMode()){
                        if (mainhand.getItem().getTranslationKey().equals("block.minecraft.iron_block")){
                            mainhand.decrement(1);
                        }
                        else{
                            offhand.decrement(1);
                        }
                    }
                    //Logic taken from AnvilScreenHandler in vanilla code to update anvil's health
                    world.setBlockState(pos, blockState2, 2);
                    world.syncWorldEvent(1030, pos, 0);
                    cir.setReturnValue(ActionResult.CONSUME);
                    return;
                }
            }
            //Alternate Item Combination
            //Series of checks to make sure player actually means to use this or else it could get annoying having to switch items to use the vanilla anvil
            else if (isValidMainHand(player) && (player.getOffHandStack().getItem().getTranslationKey().equals("item.minecraft.enchanted_book")||isHealingItem(player)||isCombinableItem(player))){
                //Runs anvil logic
                anvilWorkaround(mainhand,offhand,player);
                if (this.output.isEmpty()){
                    player.sendMessage(Text.of("§cInvalid combination"));
                    player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_BREAK,SoundCategory.PLAYERS,0.25F,1);
                }
                else{
                    if (player.isInCreativeMode() || player.experienceLevel >= this.levelCost.get()){
                        mainhand.applyChanges(output.getComponentChanges());
                        mainhand.setDamage(output.getDamage());
                        //If repairItemUsage is 0, that means player is not repairing tool with an ingredient (e.g. iron, diamond, etc.)
                        if (repairItemUsage == 0){
                            offhand.decrement(1);
                        }
                        else{
                            offhand.decrement(repairItemUsage);
                        }
                        player.addExperienceLevels(-this.levelCost.get());
                        player.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,SoundCategory.PLAYERS,0.4F,1);
                        player.sendMessage(Text.of("§aItems Combined"));
                        //Logic taken from AnvilScreenHandler in vanilla code to update anvil's health
                        if (player.getRandom().nextFloat() < 0.12F){
                            BlockState blockState2 = getLandingState(world.getBlockState(pos));
                            if (blockState2 == null) {
                                world.removeBlock(pos, false);
                                world.syncWorldEvent(1029, pos, 0);
                            } else {
                                world.setBlockState(pos, blockState2, 2);
                                world.syncWorldEvent(1030, pos, 0);
                            }
                        }
                        else{
                            world.syncWorldEvent(1030, pos, 0);
                        }
                    }
                    else{
                        player.sendMessage(Text.of("§cInsufficient xp"));
                        player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_BREAK,SoundCategory.PLAYERS,0.25F,1);
                    }
                }
                cir.setReturnValue(ActionResult.CONSUME);
                return;
            }
        }
    }

    /**
        It's the opposite of vanilla's getLandingState, it heals anvils instead of breaking
    */
    @Unique
    private BlockState inverseGetLandingState(BlockState fallingState) {
        if (fallingState.isOf(Blocks.DAMAGED_ANVIL)) {
            return (BlockState)Blocks.CHIPPED_ANVIL.getDefaultState().with(FACING, (Direction)fallingState.get(FACING));
        } else if (fallingState.isOf(Blocks.CHIPPED_ANVIL)) {
            return (BlockState)Blocks.ANVIL.getDefaultState().with(FACING, (Direction)fallingState.get(FACING));
        }
        else {
            return null;
        }
    }

    /**
     It's just the vanilla anvil calculations with minor changes (i.e. cost capped to 55, variable changes to work with mixin)
     */
    @Unique
    public void anvilWorkaround(ItemStack mainhand, ItemStack offhand, PlayerEntity player) {
        ItemStack itemStack = mainhand;
        this.levelCost.set(1);
        int i = 0;
        long l = 0L;
        int j = 0;
        if (!itemStack.isEmpty() && EnchantmentHelper.canHaveEnchantments(itemStack)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = offhand;
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(itemStack2));
            l += (long)(Integer)itemStack.getOrDefault(DataComponentTypes.REPAIR_COST, 0) + (long)(Integer)itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
            this.repairItemUsage = 0;
            int k;
            if (!itemStack3.isEmpty()) {
                boolean bl = itemStack3.contains(DataComponentTypes.STORED_ENCHANTMENTS);
                int m;
                int n;
                if (itemStack2.isDamageable() && itemStack2.getItem().canRepair(itemStack, itemStack3)) {
                    k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
                    if (k <= 0) {
                        this.output = ItemStack.EMPTY;
                        this.levelCost.set(0);
                        return;
                    }

                    for(m = 0; k > 0 && m < itemStack3.getCount(); ++m) {
                        n = itemStack2.getDamage() - k;
                        itemStack2.setDamage(n);
                        ++i;
                        k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
                    }

                    this.repairItemUsage = m;
                } else {
                    if (!bl && (!itemStack2.isOf(itemStack3.getItem()) || !itemStack2.isDamageable())) {
                        this.output = ItemStack.EMPTY;
                        this.levelCost.set(0);
                        return;
                    }

                    if (itemStack2.isDamageable() && !bl) {
                        k = itemStack.getMaxDamage() - itemStack.getDamage();
                        m = itemStack3.getMaxDamage() - itemStack3.getDamage();
                        n = m + itemStack2.getMaxDamage() * 12 / 100;
                        int o = k + n;
                        int p = itemStack2.getMaxDamage() - o;
                        if (p < 0) {
                            p = 0;
                        }

                        if (p < itemStack2.getDamage()) {
                            itemStack2.setDamage(p);
                            i += 2;
                        }
                    }

                    ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(itemStack3);
                    boolean bl2 = false;
                    boolean bl3 = false;
                    Iterator var26 = itemEnchantmentsComponent.getEnchantmentEntries().iterator();

                    while(var26.hasNext()) {
                        Object2IntMap.Entry<RegistryEntry<Enchantment>> entry = (Object2IntMap.Entry)var26.next();
                        RegistryEntry<Enchantment> registryEntry = (RegistryEntry)entry.getKey();
                        int q = builder.getLevel(registryEntry);
                        int r = entry.getIntValue();
                        r = q == r ? r + 1 : Math.max(r, q);
                        Enchantment enchantment = (Enchantment)registryEntry.value();
                        boolean bl4 = enchantment.isAcceptableItem(itemStack);
                        if (player.getAbilities().creativeMode || itemStack.isOf(Items.ENCHANTED_BOOK)) {
                            bl4 = true;
                        }

                        Iterator var20 = builder.getEnchantments().iterator();

                        while(var20.hasNext()) {
                            RegistryEntry<Enchantment> registryEntry2 = (RegistryEntry)var20.next();
                            if (!registryEntry2.equals(registryEntry) && !Enchantment.canBeCombined(registryEntry, registryEntry2)) {
                                bl4 = false;
                                ++i;
                            }
                        }

                        if (!bl4) {
                            bl3 = true;
                        } else {
                            bl2 = true;
                            if (r > enchantment.getMaxLevel()) {
                                r = enchantment.getMaxLevel();
                            }

                            builder.set(registryEntry, r);
                            int s = enchantment.getAnvilCost();
                            if (bl) {
                                s = Math.max(1, s / 2);
                            }

                            i += s * r;
                            if (itemStack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }

                    if (bl3 && !bl2) {
                        this.output = ItemStack.EMPTY;
                        this.levelCost.set(0);
                        return;
                    }
                }
            }

            int t = (int) MathHelper.clamp(l + (long)i, 0L, 55);
            this.levelCost.set(t);
            if (i <= 0) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (!itemStack2.isEmpty()) {
                k = (Integer)itemStack2.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
                if (k < (Integer)itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0)) {
                    k = (Integer)itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
                }

                if (j != i || j == 0) {
                    k = getNextCost(k);
                }

                itemStack2.set(DataComponentTypes.REPAIR_COST, k);
                EnchantmentHelper.set(itemStack2, builder.build());
            }

            this.output = itemStack2;
        }
    }

    /**
     From AbstractBlock class, adding this to AnvilBlock class overrides the inherited method. (I think?)
     <p>XP Cost Preview</p>
     */
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        //Runs same check from
        if (!world.isClient && !player.isInCreativeMode() && (isValidMainHand(player) && (player.getOffHandStack().getItem().getTranslationKey().equals("item.minecraft.enchanted_book")||isHealingItem(player)||isCombinableItem(player)))) {
            anvilWorkaround(player.getMainHandStack(),player.getOffHandStack(),player);
            if (!this.output.isEmpty()){
                if (player.experienceLevel >= this.levelCost.get()){
                    player.sendMessage(Text.of("§aThe combination will cost "+this.levelCost.get()+" xp levels"));
                }
                else {
                    player.sendMessage(Text.of("§cThe combination will cost "+this.levelCost.get()+" xp levels (Insufficient xp)"));
                }
            }
            else{
                player.sendMessage(Text.of("§cInvalid combination"));
            }
        }
    }

    /*
     The following 3 methods are used in checking if the player is intentionally trying to use the alternate item combination
     */
    @Unique
    public boolean isHealingItem(PlayerEntity player){
        return player.getMainHandStack().copy().getItem().canRepair(player.getMainHandStack(),player.getOffHandStack()) && (player.getMainHandStack().isDamaged());
    }

    @Unique
    public boolean isCombinableItem(PlayerEntity player){
        return player.getOffHandStack().isOf(player.getMainHandStack().getItem());
    }

    @Unique
    public boolean isValidMainHand(PlayerEntity player){
        return EnchantmentHelper.canHaveEnchantments(player.getMainHandStack()) && player.getMainHandStack().copy().isDamageable();
    }
}
