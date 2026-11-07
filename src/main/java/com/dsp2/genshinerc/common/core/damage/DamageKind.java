package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import com.dsp2.genshinerc.common.registry.ModDamageTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public enum DamageKind {
    PHYSICAL("physical", ModDamageTypes.PHYSICAL, ModAttributes.PHYSICAL_RESISTANCE),
    PYRO("pyro", ModDamageTypes.PYRO, ModAttributes.PYRO_RESISTANCE),
    HYDRO("hydro", ModDamageTypes.HYDRO, ModAttributes.HYDRO_RESISTANCE),
    ELECTRO("electro", ModDamageTypes.ELECTRO, ModAttributes.ELECTRO_RESISTANCE),
    CRYO("cryo", ModDamageTypes.CRYO, ModAttributes.CRYO_RESISTANCE),
    ANEMO("anemo", ModDamageTypes.ANEMO, ModAttributes.ANEMO_RESISTANCE),
    GEO("geo", ModDamageTypes.GEO, ModAttributes.GEO_RESISTANCE),
    DENDRO("dendro", ModDamageTypes.DENDRO, ModAttributes.DENDRO_RESISTANCE),
    TRUE("true", ModDamageTypes.TRUE, null);

    private final String indicatorKey;
    private final ResourceKey<DamageType> typeKey;
    @Nullable private final RegistryObject<Attribute> resistance;

    DamageKind(String indicatorKey, ResourceKey<DamageType> typeKey,
               @Nullable RegistryObject<Attribute> resistance) {
        this.indicatorKey = indicatorKey;
        this.typeKey = typeKey;
        this.resistance = resistance;
    }

    public String indicatorKey() { return indicatorKey; }
    public ResourceKey<DamageType> typeKey() { return typeKey; }
    public boolean isTrue() { return this == TRUE; }

    @Nullable
    public Attribute resistanceAttribute() { return resistance == null ? null : resistance.get(); }

    @Nullable
    public RegistryObject<Attribute> resistanceObject() { return resistance; }

    public static DamageKind fromElement(ElementType type) {
        return switch (type) {
            case PYRO, BURNING -> PYRO;
            case HYDRO -> HYDRO;
            case ELECTRO -> ELECTRO;
            case CRYO, FROZEN -> CRYO;
            case DENDRO, QUICKEN -> DENDRO;
            case ANEMO -> ANEMO;
            case GEO -> GEO;
        };
    }
}
