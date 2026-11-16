package com.ordana.immersive_weathering.items;

import com.ordana.immersive_weathering.util.Varnish;
import com.ordana.immersive_weathering.util.WoodWeathering;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port addition: protects wooden blocks from rotting and moss. Several uses per bottle.
 */
public class VarnishItem extends Item {

    public VarnishItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!WoodWeathering.isWeatherableWood(state.getBlock())) return InteractionResult.PASS;
        if (level instanceof ServerLevel serverLevel) {
            Player player = context.getPlayer();
            if (!Varnish.add(level, pos)) {
                if (player != null) player.sendOverlayMessage(Component.translatable("message.immersive_weathering.already_varnished"));
                return InteractionResult.FAIL;
            }
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1f, 0.8f);
            serverLevel.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    12, 0.4, 0.4, 0.4, 0.5);
            if (player != null) context.getItemInHand().hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
        }
        return InteractionResult.SUCCESS;
    }
}
