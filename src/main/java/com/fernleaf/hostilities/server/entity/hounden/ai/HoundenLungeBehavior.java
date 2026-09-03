package com.fernleaf.hostilities.server.entity.hounden.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.hounden.Hounden;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HoundenLungeBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public HoundenLungeBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override public int getWindupTicks() { return 8; }
        @Override public int getActiveTicks() { return 10; }
        @Override public int getRecoveryTicks() { return 15; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            if (target == null || !target.isAlive() || owner.isSitting()) return false;
            double distSqr = owner.distanceToSqr(target);
            return distSqr <= 36.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity hounden, LivingEntity target) {
            hounden.triggerAnimation(Hounden.ANIM_LUNGE, getTotalDuration());
            lockPosition(hounden);
        }

        @Override
        public void onWindupTick(HostilitiesEntity hounden, LivingEntity target, int elapsedTicks) {
            lockPosition(hounden);
            hounden.getLookControl().setLookAt(target, 45.0F, 45.0F);
        }

        @Override
        public void onExecute(HostilitiesEntity hounden, LivingEntity target, int activeTicksElapsed) {
            if (activeTicksElapsed == 1 && target != null) {
                // Apply impulse on first active frame after windup locks release
                Vec3 dir = target.position().subtract(hounden.position()).normalize();
                hounden.setDeltaMovement(dir.x * 1.2D, 0.35D, dir.z * 1.2D);
                hounden.hasImpulse = true;
            }

            AABB hitBox = hounden.getBoundingBox().inflate(1.2D);
            List<LivingEntity> targets = hounden.level().getEntitiesOfClass(
                    LivingEntity.class, hitBox, e -> e != hounden && !hounden.isAlliedTo(e)
            );

            for (LivingEntity entity : targets) {
                entity.hurt(hounden.damageSources().mobAttack(hounden), 7.0F);
            }
        }

        @Override public void onRecoveryTick(HostilitiesEntity hounden, LivingEntity target, int elapsed) {}
        @Override public void onStop(HostilitiesEntity hounden, LivingEntity target) {}

        private void lockPosition(HostilitiesEntity hounden) {
            hounden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            hounden.getNavigation().stop();
            hounden.setDeltaMovement(0.0D, hounden.getDeltaMovement().y, 0.0D);
        }
    }
}