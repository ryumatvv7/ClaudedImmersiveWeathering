package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModSoundEvents {
    public static void init() {
    }

    public static Supplier<SoundEvent> ICICLE_CRACK = registerSoundEvent("icicle_crack");

    @NotNull
    private static Supplier<SoundEvent> registerSoundEvent(@NotNull String name) {
        Identifier id = ImmersiveWeathering.res(name);
        return com.ordana.immersive_weathering.platform.Reg.sound(id);
    }
}
