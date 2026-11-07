package com.dsp2.genshinerc.common.registry;

import com.dsp2.genshinerc.GenshinERC;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GenshinERC.MODID);

    public static final RegistryObject<MobEffect> PYRO_ATTACHMENT     = register("pyro_attachment", 0xFF6600);
    public static final RegistryObject<MobEffect> HYDRO_ATTACHMENT    = register("hydro_attachment", 0x00CCFF);
    public static final RegistryObject<MobEffect> ELECTRO_ATTACHMENT  = register("electro_attachment", 0xCC66FF);
    public static final RegistryObject<MobEffect> CRYO_ATTACHMENT     = register("cryo_attachment", 0xB4FFFE);
    public static final RegistryObject<MobEffect> DENDRO_ATTACHMENT   = register("dendro_attachment", 0x55A601);
    public static final RegistryObject<MobEffect> FROZEN_ATTACHMENT   = register("frozen_attachment", 0xB4FFFE);
    public static final RegistryObject<MobEffect> QUICKEN_ATTACHMENT  = register("quicken_attachment", 0x55A601);
    public static final RegistryObject<MobEffect> BURNING_ATTACHMENT  = register("burning_attachment", 0xFF6600);

    private ModEffects() {}

    private static RegistryObject<MobEffect> register(String name, int color) {
        return REGISTRY.register(name, () -> new ElementEffect(color));
    }
}
