package com.dsp2.genshinerc.common.world.entity.unit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
public abstract class CombatUnit extends Monster {
    private String tacticCursor = "";
    public CombatUnit(EntityType<? extends Monster> type, Level level) { super(type, level); this.xpReward = 10; }
    public static AttributeSupplier.Builder baseStats() {
        return Monster.createMonsterAttributes()
                .add(EpicFightAttributes.WEIGHT.get())
                .add(EpicFightAttributes.ARMOR_NEGATION.get())
                .add(EpicFightAttributes.IMPACT.get())
                .add(EpicFightAttributes.MAX_STRIKES.get())
                .add(EpicFightAttributes.STUN_ARMOR.get());
    }
    public String tacticCursor() { return tacticCursor; }
    public void setTacticCursor(String s) { this.tacticCursor = s == null ? "" : s; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putString("TacticCursor", tacticCursor); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); if (tag.contains("TacticCursor")) this.tacticCursor = tag.getString("TacticCursor"); }
}