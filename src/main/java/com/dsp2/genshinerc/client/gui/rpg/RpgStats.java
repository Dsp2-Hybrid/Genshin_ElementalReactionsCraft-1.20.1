package com.dsp2.genshinerc.client.gui.rpg;

import com.dsp2.genshinerc.client.render.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.bar.shield.ClientShieldState;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.List;

public final class RpgStats {
    private static final int STAMINA_RGB   = 0x3FC1C9;
    private static final int HUNGER_RGB    = 0xC8843A;
    private static final int OXYGEN_RGB    = 0x6EC6FF;
    private static final int TOUGHNESS_RGB = 0xB0BEC5;

    public static final RpgStat SHIELD = new RpgStat("shield",
            RpgStat.source(
                    p -> { ClientShieldState s = ClientCombatBars.shield(p.getId()); return s != null && s.largestAmount() > 0f; },
                    p -> { ClientShieldState s = ClientCombatBars.shield(p.getId()); return s == null ? 0f : s.largestAmount(); },
                    p -> { ClientShieldState s = ClientCombatBars.shield(p.getId()); return Math.max(p.getMaxHealth(), s == null ? 0f : s.maxAmount()); }),
            (p, f) -> { ClientShieldState s = ClientCombatBars.shield(p.getId()); return s == null ? 0xFFFFFF : s.color(BarConfig.get()); },
            true, false, null);

    public static final RpgStat HEALTH = new RpgStat("health",
            RpgStat.source(p -> true, p -> p.getHealth(), p -> p.getMaxHealth()),
            (p, f) -> HealthGradient.sample(f),
            true, true, new int[]{16, 0, 52, 0});

    public static final RpgStat SHIELD_TOUGHNESS = new RpgStat("shield_toughness",
            RpgStat.NONE, (p, f) -> TOUGHNESS_RGB, true, false, null);

    public static final RpgStat STAMINA = new RpgStat("stamina",
            RpgStat.source(p -> efStamina(p, true) > 0f, p -> efStamina(p, false), p -> efStamina(p, true)),
            (p, f) -> STAMINA_RGB, false, false, null);

    public static final RpgStat HUNGER = new RpgStat("hunger",
            RpgStat.source(p -> true, p -> p.getFoodData().getFoodLevel(), p -> 20f),
            (p, f) -> HUNGER_RGB, true, false, new int[]{16, 27, 52, 27});

    public static final RpgStat OXYGEN = new RpgStat("oxygen",
            RpgStat.source(p -> p.getAirSupply() < p.getMaxAirSupply(), p -> Math.max(0, p.getAirSupply()), p -> p.getMaxAirSupply()),
            (p, f) -> OXYGEN_RGB, false, false, new int[]{16, 18});

    private static final List<RpgStat> ALL = List.of(SHIELD, HEALTH, SHIELD_TOUGHNESS, STAMINA, HUNGER, OXYGEN);

    private RpgStats() {}

    public static List<RpgStat> all() { return ALL; }

    private static float efStamina(Player p, boolean max) {
        Object patch = EpicFightCapabilities.getUnparameterizedEntityPatch(p, PlayerPatch.class).orElse(null);
        if (patch instanceof PlayerPatch<?> pp) return max ? pp.getMaxStamina() : pp.getStamina();
        return 0f;
    }
}