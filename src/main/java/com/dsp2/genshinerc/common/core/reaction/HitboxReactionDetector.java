package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.WeakHashMap;

public final class HitboxReactionDetector {
    private HitboxReactionDetector() {}
    private static final WeakHashMap<LivingEntity, ElementType> SWING = new WeakHashMap<>();
    public static void tagNextSwing(LivingEntity a, ElementType e) { if (a != null && e != null) SWING.put(a, e); }
    public static void detectHitbox(LivingEntity a, Vec3 start, Vec3 end, double radius) {
        ElementType el = SWING.get(a); if (el == null) return;
        AABB box = new AABB(start, end).inflate(radius);
        List<LivingEntity> targets = a.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != a && e.isAlive() && !e.isSpectator());
        for (LivingEntity t : targets) if (box.inflate(radius).contains(closest(t.getBoundingBox().getCenter(), start, end)))
            ElementReactions.trigger(t, a, el, 2.0f);
    }
    private static Vec3 closest(Vec3 p, Vec3 s, Vec3 e) {
        Vec3 seg = e.subtract(s);
        double t = Math.max(0, Math.min(1, p.subtract(s).dot(seg) / seg.lengthSqr()));
        return s.add(seg.scale(t));
    }
    public static void clearSwing(LivingEntity a) { SWING.remove(a); }
}