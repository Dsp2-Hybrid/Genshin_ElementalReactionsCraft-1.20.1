package com.dsp2.genshinerc.common.world.entity.rig;
import java.util.Map;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;
public class UnitRig extends Armature {
    public final Joint root, torso, head, armR, armL;
    public UnitRig(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
        super(name, jointNumber, rootJoint, jointMap);
        this.root = this.getOrLogException(jointMap, "Root");
        this.torso = this.getOrLogException(jointMap, "Torso");
        this.head = this.getOrLogException(jointMap, "Head");
        this.armR = this.getOrLogException(jointMap, "Arm_R");
        this.armL = this.getOrLogException(jointMap, "Arm_L");
    }
}