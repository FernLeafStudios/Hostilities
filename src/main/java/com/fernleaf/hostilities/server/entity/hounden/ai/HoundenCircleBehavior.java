package com.fernleaf.hostilities.server.entity.hounden.ai;

import com.fernleaf.fernframe.allyrally.entity.AllyRallyBossEntity;
import com.fernleaf.fernframe.mathbath.entity.OrbitMath;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

public class HoundenCircleBehavior extends Behavior<HostilitiesEntity> {
    private float circleAngle = 0.0f;
    private final double radius;
    private final double speed;

    public HoundenCircleBehavior(double radius, double speed) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.radius = radius;
        this.speed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, HostilitiesEntity owner) {
        if (owner.isSitting() || owner.getActionState() != AllyRallyBossEntity.ActionState.IDLE) {
            return false;
        }

        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && owner.distanceToSqr(target) <= 100.0D;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull HostilitiesEntity owner, long gameTime) {
        return checkExtraStartConditions(level, owner);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, HostilitiesEntity owner, long gameTime) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) return;

        this.circleAngle += 0.05f;
        OrbitMath.applyOrbitMotion(
                owner,
                target.position(),
                this.circleAngle,
                this.radius,
                1.0f,
                this.speed,
                0.15d,
                0.2f,
                target.getY()
        );
    }
}