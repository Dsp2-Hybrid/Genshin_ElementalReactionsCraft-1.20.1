package com.dsp2.genshinerc.common.registry;

import com.dsp2.genshinerc.GenshinERC;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> PYRO     = key("pyro");
    public static final ResourceKey<DamageType> HYDRO    = key("hydro");
    public static final ResourceKey<DamageType> ELECTRO  = key("electro");
    public static final ResourceKey<DamageType> CRYO     = key("cryo");
    public static final ResourceKey<DamageType> ANEMO    = key("anemo");
    public static final ResourceKey<DamageType> GEO      = key("geo");
    public static final ResourceKey<DamageType> DENDRO   = key("dendro");
    public static final ResourceKey<DamageType> PHYSICAL = key("physical");
    public static final ResourceKey<DamageType> TRUE     = key("true");

    private ModDamageTypes() {}

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(GenshinERC.MODID, name));
    }
}
