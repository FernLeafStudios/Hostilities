package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GeardianChargedSweepBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianChargedSweepBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 34; }

        @Override
        public int getActiveTicks() { return 3; }

        @Override
        public int getRecoveryTicks() { return 45; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            if (target == null || !target.isAlive() || owner.isSitting()) return false;
            double distSqr = owner.distanceToSqr(target);
            return distSqr > 16.0D && distSqr <= 30.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            geardian.triggerAnimation(Geardian.ANIM_CHARGED_SWEEP, getTotalDuration());
            lockPosition(geardian);
        }

        @Override
        public void onWindupTick(HostilitiesEntity geardian, LivingEntity target, int elapsedTicks) {
            lockPosition(geardian);
            geardian.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        @Override
        public void onExecute(HostilitiesEntity geardian, LivingEntity target, int activeTicksElapsed) {
            lockPosition(geardian);
            geardian.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (activeTicksElapsed == 1) {
                AABB sweepBox = geardian.getBoundingBox().inflate(3.0D, 1.0D, 3.0D);
                List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                        LivingEntity.class, sweepBox, e -> e != geardian && !geardian.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.hurt(geardian.damageSources().mobAttack(geardian), 18.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            geardian.getLookAngle().x * 1.8D, 0.2D, geardian.getLookAngle().z * 1.8D
                    ));
                }
            }
        }

        @Override
        public void onRecoveryTick(HostilitiesEntity geardian, LivingEntity target, int recoveryTicksElapsed) {}

        @Override
        public void onStop(HostilitiesEntity geardian, LivingEntity target) {}

        private void lockPosition(HostilitiesEntity geardian) {
            geardian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            geardian.getNavigation().stop();
            geardian.setDeltaMovement(0.0D, geardian.getDeltaMovement().y, 0.0D);
        }
    }
}