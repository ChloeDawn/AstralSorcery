/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.CropHelper;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectAevitas
 * Created by HellFirePvP
 * Date: 27.07.2019 / 21:54
 */
public class CEffectAevitas extends CEffectAbstractList<CropHelper.GrowablePlant> {

    public static AevitasConfig CONFIG = new AevitasConfig();

    public CEffectAevitas(@Nullable ILocatable origin) {
        super(origin, ConstellationsAS.aevitas, CONFIG.maxAmount.get(), (world, pos, state) -> CropHelper.wrapPlant(world, pos) != null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(World world, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (rand.nextBoolean()) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(
                            pos.getX() + rand.nextFloat() * 5 * (rand.nextBoolean() ? 1 : -1) + 0.5,
                            pos.getY() + rand.nextFloat() * 2 + 0.5,
                            pos.getZ() + rand.nextFloat() * 5 * (rand.nextBoolean() ? 1 : -1) + 0.5))
                    .setGravityStrength(-0.008F)
                    .setScaleMultiplier(0.45F)
                    .color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_AEVITAS))
                    .setMaxAge(35);
        }
    }

    @Override
    public boolean playEffect(World world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        boolean changed = false;
        CropHelper.GrowablePlant plant = getRandomElementChanced();
        if(plant != null) {
            if (MiscUtils.isChunkLoaded(world, new ChunkPos(plant.getPos()))) {
                if (properties.isCorrupted()) {
                    if(world instanceof ServerWorld) {
                        if (BlockUtils.breakBlockWithoutPlayer(((ServerWorld) world), plant.getPos())) {
                            changed = true;
                        }
                    } else {
                        if (world.removeBlock(plant.getPos(), false)) {
                            changed = true;
                        }
                    }
                } else {
                    if (!plant.isValid(world, true)) {
                        removeElement(plant.getPos());
                        changed = true;
                    } else {
                        if (plant.tryGrow(world, rand)) {
                            //TODO pkt effects
                            //PktParticleEvent ev = new PktParticleEvent(PktParticleEvent.ParticleEventType.CE_CROP_INTERACT, plant.getPos());
                            //PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(world, plant.getPos(), 8));
                            changed = true;
                        }
                    }
                }
            }
        }

        if(findNewPosition(world, pos, properties)) changed = true;
        if(findNewPosition(world, pos, properties)) changed = true;

        int amplifier = CONFIG.potionAmplifier.get();
        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, BOX.offset(pos).grow(properties.getSize()));
        for (LivingEntity entity : entities) {
            if (entity.isAlive()) {
                if (properties.isCorrupted()) {
                    //TODO potions
                    //entity.addPotionEffect(new PotionEffect(RegistryPotions.potionBleed, 200, potionAmplifier * 2));
                    entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 200, amplifier * 3));
                    entity.addPotionEffect(new EffectInstance(Effects.HUNGER, 200, amplifier * 4));
                    entity.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 200, amplifier * 2));
                } else {
                    entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, 200, amplifier));
                }
            }
        }

        return changed;
    }

    @Nullable
    @Override
    public CropHelper.GrowablePlant recreateElement(CompoundNBT tag, BlockPos pos) {
        return CropHelper.fromNBT(tag, pos);
    }

    @Nullable
    @Override
    public CropHelper.GrowablePlant createElement(World world, BlockPos pos) {
        return CropHelper.wrapPlant(world, pos);
    }

    @Override
    public ConstellationEffectProperties createProperties(int mirrors) {
        return new ConstellationEffectProperties(CONFIG.range.get() + mirrors * CONFIG.rangePerLens.get());
    }

    private static class AevitasConfig extends CountConfig {

        private final int defaultPotionAmplifier = 1;

        public ForgeConfigSpec.IntValue potionAmplifier;

        public AevitasConfig() {
            super("aevitas", 16D, 0D, 200);
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.potionAmplifier = cfgBuilder
                    .comment("Set the amplifier for the potion effects this ritual provides.")
                    .translation(translationKey("potionAmplifier"))
                    .defineInRange("potionAmplifier", this.defaultPotionAmplifier, 0, 10);
        }
    }
}
