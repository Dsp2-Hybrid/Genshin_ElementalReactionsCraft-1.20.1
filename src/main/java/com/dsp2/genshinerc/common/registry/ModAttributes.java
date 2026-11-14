package com.dsp2.genshinerc.common.registry;

import com.dsp2.genshinerc.GenshinERC;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.stream.Stream;

public final class ModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, GenshinERC.MODID);

    public static final RegistryObject<Attribute> PYRO_RESISTANCE = resistance("pyro_resistance");
    public static final RegistryObject<Attribute> HYDRO_RESISTANCE = resistance("hydro_resistance");
    public static final RegistryObject<Attribute> ELECTRO_RESISTANCE = resistance("electro_resistance");
    public static final RegistryObject<Attribute> CRYO_RESISTANCE = resistance("cryo_resistance");
    public static final RegistryObject<Attribute> ANEMO_RESISTANCE = resistance("anemo_resistance");
    public static final RegistryObject<Attribute> GEO_RESISTANCE = resistance("geo_resistance");
    public static final RegistryObject<Attribute> DENDRO_RESISTANCE = resistance("dendro_resistance");
    public static final RegistryObject<Attribute> PHYSICAL_RESISTANCE = resistance("physical_resistance");

    public static final RegistryObject<Attribute> ELEMENTAL_MASTERY = REGISTRY.register("elemental_mastery",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".elemental_mastery", 0.0, 0.0, 100000.0).setSyncable(true));
    public static final RegistryObject<Attribute> DEFENSE = REGISTRY.register("defense",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".defense", 0.0, 0.0, 100000.0).setSyncable(true));
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS = REGISTRY.register("elemental_toughness",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".elemental_toughness", 0.0, 0.0, 100000.0).setSyncable(true));

    public static final RegistryObject<Attribute> BASE_ATTACK = REGISTRY.register("base_attack",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".base_attack", 0.0, 0.0, 10000.0).setSyncable(true));
    public static final RegistryObject<Attribute> BONUS_ATTACK = REGISTRY.register("bonus_attack",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".bonus_attack", 0.0, 0.0, 10000.0).setSyncable(true));
    public static final RegistryObject<Attribute> FLAT_ATTACK_BONUS = REGISTRY.register("flat_attack_bonus",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".flat_attack_bonus", 0.0, 0.0, 10000.0).setSyncable(true));
    public static final RegistryObject<Attribute> FLAT_HP_BONUS = REGISTRY.register("flat_hp_bonus",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".flat_hp_bonus", 0.0, 0.0, 100000.0).setSyncable(true));
    public static final RegistryObject<Attribute> FLAT_DEFENSE_BONUS = REGISTRY.register("flat_defense_bonus",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".flat_defense_bonus", 0.0, 0.0, 10000.0).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_DEFENSE = REGISTRY.register("bonus_defense",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".bonus_defense", 0.0, 0.0, 10000.0).setSyncable(true));
    public static final RegistryObject<Attribute> SCALED_DEFENSE = REGISTRY.register("scaled_defense",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".scaled_defense", 0.0, 0.0, 10000.0).setSyncable(true));
    public static final RegistryObject<Attribute> INNATE_DEFENSE = REGISTRY.register("innate_defense",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".innate_defense", 0.0, 0.0, 10000.0).setSyncable(true));

    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_PYRO = toughness("elemental_toughness_pyro");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_HYDRO = toughness("elemental_toughness_hydro");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_DENDRO = toughness("elemental_toughness_dendro");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_ELECTRO = toughness("elemental_toughness_electro");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_ANEMO = toughness("elemental_toughness_anemo");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_CRYO = toughness("elemental_toughness_cryo");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_GEO = toughness("elemental_toughness_geo");
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS_PHYSICAL = toughness("elemental_toughness_physical");

    public static final RegistryObject<Attribute> CRIT_RATE = REGISTRY.register("crit_rate",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".crit_rate", 0.05, 0.0, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> CRIT_DAMAGE = REGISTRY.register("crit_damage",
            () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".crit_damage", 0.5, 0.0, 10.0).setSyncable(true));

    public static final RegistryObject<Attribute> PYRO_DAMAGE_BONUS = bonus("pyro_damage_bonus");
    public static final RegistryObject<Attribute> HYDRO_DAMAGE_BONUS = bonus("hydro_damage_bonus");
    public static final RegistryObject<Attribute> ELECTRO_DAMAGE_BONUS = bonus("electro_damage_bonus");
    public static final RegistryObject<Attribute> CRYO_DAMAGE_BONUS = bonus("cryo_damage_bonus");
    public static final RegistryObject<Attribute> ANEMO_DAMAGE_BONUS = bonus("anemo_damage_bonus");
    public static final RegistryObject<Attribute> GEO_DAMAGE_BONUS = bonus("geo_damage_bonus");
    public static final RegistryObject<Attribute> DENDRO_DAMAGE_BONUS = bonus("dendro_damage_bonus");
    public static final RegistryObject<Attribute> PHYSICAL_DAMAGE_BONUS = bonus("physical_damage_bonus");

    public static final List<RegistryObject<Attribute>> RESISTANCES = List.of(
            PYRO_RESISTANCE, HYDRO_RESISTANCE, ELECTRO_RESISTANCE, CRYO_RESISTANCE,
            ANEMO_RESISTANCE, GEO_RESISTANCE, DENDRO_RESISTANCE, PHYSICAL_RESISTANCE);
    public static final List<RegistryObject<Attribute>> DAMAGE_BONUSES = List.of(
            PYRO_DAMAGE_BONUS, HYDRO_DAMAGE_BONUS, ELECTRO_DAMAGE_BONUS, CRYO_DAMAGE_BONUS,
            ANEMO_DAMAGE_BONUS, GEO_DAMAGE_BONUS, DENDRO_DAMAGE_BONUS, PHYSICAL_DAMAGE_BONUS);
    public static final List<RegistryObject<Attribute>> LIVING_ATTRIBUTES = Stream.concat(
            Stream.concat(RESISTANCES.stream(), DAMAGE_BONUSES.stream()),
            Stream.of(ELEMENTAL_MASTERY, DEFENSE, ELEMENTAL_TOUGHNESS,
                    BASE_ATTACK, BONUS_ATTACK, FLAT_ATTACK_BONUS, FLAT_HP_BONUS, FLAT_DEFENSE_BONUS,
                    BONUS_DEFENSE, SCALED_DEFENSE, INNATE_DEFENSE,
                    ELEMENTAL_TOUGHNESS_PYRO, ELEMENTAL_TOUGHNESS_HYDRO, ELEMENTAL_TOUGHNESS_DENDRO,
                    ELEMENTAL_TOUGHNESS_ELECTRO, ELEMENTAL_TOUGHNESS_ANEMO, ELEMENTAL_TOUGHNESS_CRYO,
                    ELEMENTAL_TOUGHNESS_GEO, ELEMENTAL_TOUGHNESS_PHYSICAL,
                    CRIT_RATE, CRIT_DAMAGE)).toList();

    private ModAttributes() {}

    private static RegistryObject<Attribute> resistance(String name) {
        return REGISTRY.register(name, () -> new RangedAttribute(
                "attribute.name." + GenshinERC.MODID + "." + name, 0.0, -1.0, 1.0).setSyncable(true));
    }
    private static RegistryObject<Attribute> bonus(String name) {
        return REGISTRY.register(name, () -> new RangedAttribute(
                "attribute.name." + GenshinERC.MODID + "." + name, 0.0, -10.0, 100.0).setSyncable(true));
    }
    private static RegistryObject<Attribute> toughness(String name) {
        return REGISTRY.register(name, () -> new RangedAttribute(
                "attribute.name." + GenshinERC.MODID + "." + name, 0.0, 0.0, 10000.0).setSyncable(true));
    }
}