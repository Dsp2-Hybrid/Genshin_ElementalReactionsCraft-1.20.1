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
    public static final RegistryObject<Attribute> ELEMENTAL_MASTERY = REGISTRY.register("elemental_mastery", () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".elemental_mastery", 0.0, 0.0, 100000.0).setSyncable(true));
    public static final RegistryObject<Attribute> DEFENSE = REGISTRY.register("defense", () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".defense", 0.0, 0.0, 100000.0).setSyncable(true));
    public static final RegistryObject<Attribute> ELEMENTAL_TOUGHNESS = REGISTRY.register("elemental_toughness", () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + ".elemental_toughness", 0.0, 0.0, 100000.0).setSyncable(true));
    public static final RegistryObject<Attribute> PYRO_DAMAGE_BONUS = bonus("pyro_damage_bonus");
    public static final RegistryObject<Attribute> HYDRO_DAMAGE_BONUS = bonus("hydro_damage_bonus");
    public static final RegistryObject<Attribute> ELECTRO_DAMAGE_BONUS = bonus("electro_damage_bonus");
    public static final RegistryObject<Attribute> CRYO_DAMAGE_BONUS = bonus("cryo_damage_bonus");
    public static final RegistryObject<Attribute> ANEMO_DAMAGE_BONUS = bonus("anemo_damage_bonus");
    public static final RegistryObject<Attribute> GEO_DAMAGE_BONUS = bonus("geo_damage_bonus");
    public static final RegistryObject<Attribute> DENDRO_DAMAGE_BONUS = bonus("dendro_damage_bonus");
    public static final RegistryObject<Attribute> PHYSICAL_DAMAGE_BONUS = bonus("physical_damage_bonus");
    public static final List<RegistryObject<Attribute>> RESISTANCES = List.of(PYRO_RESISTANCE, HYDRO_RESISTANCE, ELECTRO_RESISTANCE, CRYO_RESISTANCE, ANEMO_RESISTANCE, GEO_RESISTANCE, DENDRO_RESISTANCE, PHYSICAL_RESISTANCE);
    public static final List<RegistryObject<Attribute>> DAMAGE_BONUSES = List.of(PYRO_DAMAGE_BONUS, HYDRO_DAMAGE_BONUS, ELECTRO_DAMAGE_BONUS, CRYO_DAMAGE_BONUS, ANEMO_DAMAGE_BONUS, GEO_DAMAGE_BONUS, DENDRO_DAMAGE_BONUS, PHYSICAL_DAMAGE_BONUS);
    public static final List<RegistryObject<Attribute>> LIVING_ATTRIBUTES = Stream.concat(Stream.concat(RESISTANCES.stream(), DAMAGE_BONUSES.stream()), Stream.of(ELEMENTAL_MASTERY, DEFENSE, ELEMENTAL_TOUGHNESS)).toList();
    private ModAttributes() {}
    private static RegistryObject<Attribute> resistance(String name) { return REGISTRY.register(name, () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + "." + name, 0.0, -1.0, 1.0).setSyncable(true)); }
    private static RegistryObject<Attribute> bonus(String name) { return REGISTRY.register(name, () -> new RangedAttribute("attribute.name." + GenshinERC.MODID + "." + name, 0.0, -10.0, 100.0).setSyncable(true)); }
}