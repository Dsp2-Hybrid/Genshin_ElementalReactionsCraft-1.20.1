package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class ReactionParticles {
    private ReactionParticles() {}
    private static ServerLevel L(LivingEntity e) { return e.level() instanceof ServerLevel sl ? sl : null; }
    public static void spawnOverload(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.FLAME,e.getX(),e.getY()+0.5,e.getZ(),20,0.5,0.5,0.5,0.05); sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+0.5,e.getZ(),15,0.5,0.5,0.5,0.1); }
    public static void spawnSuperconduct(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SNOWFLAKE,e.getX(),e.getY()+0.5,e.getZ(),15,0.5,0.5,0.5,0.05); sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+0.5,e.getZ(),10,0.5,0.5,0.5,0.08); }
    public static void spawnElectroCharged(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+0.5,e.getZ(),8,0.3,0.3,0.3,0.1); sl.sendParticles(ParticleTypes.FALLING_WATER,e.getX(),e.getY()+0.5,e.getZ(),5,0.3,0.3,0.3,0.05); }
    public static void spawnFreeze(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SNOWFLAKE,e.getX(),e.getY()+0.5,e.getZ(),25,0.5,0.5,0.5,0.02); }
    public static void spawnShatter(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.ITEM_SNOWBALL,e.getX(),e.getY()+0.5,e.getZ(),30,0.5,0.5,0.5,0.15); }
    public static void spawnMelt(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.DRIPPING_WATER,e.getX(),e.getY()+1.0,e.getZ(),15,0.3,0.3,0.3,0.05); sl.sendParticles(ParticleTypes.SMOKE,e.getX(),e.getY()+0.5,e.getZ(),8,0.3,0.3,0.3,0.02); }
    public static void spawnVaporize(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.CLOUD,e.getX(),e.getY()+0.5,e.getZ(),20,0.5,0.5,0.5,0.08); }
    public static void spawnSwirl(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SWEEP_ATTACK,e.getX(),e.getY()+0.5,e.getZ(),5,0.5,0.5,0.5,0.0); }
    public static void spawnCrystallize(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.END_ROD,e.getX(),e.getY()+0.5,e.getZ(),12,0.4,0.4,0.4,0.05); }
    public static void spawnBloom(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,e.getX(),e.getY()+0.5,e.getZ(),15,0.5,0.5,0.5,0.05); }
    public static void spawnBurning(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SMALL_FLAME,e.getX(),e.getY()+0.5,e.getZ(),3,0.2,0.2,0.2,0.01); }
}