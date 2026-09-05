package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GeardianChargedSweepBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianChargedSweepBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 30; }   // 1.50s

        @Override
        public int getActiveTicks() { return 10; }   // 0.50s (1.50s - 2.00s)

        @Override
        public int getRecoveryTicks() { return 10; } // 0.50s (Total: 2.5s / 50 ticks)

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            if (target == null || !target.isAlive() || owner.isSitting()) return false;
            double distSqr = owner.distanceToSqr(target);
            return distSqr > 16.0D && distSqr <= 30.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            geardian.triggerAnimation(Geardian.ANIM_CHARGED_SWEEP, getTotalDuration());
            lockPositionAndRotation(geardian);
        }

        @Override
        public void onWindupTick(HostilitiesEntity geardian, LivingEntity target, int elapsedTicks) {
            lockPositionAndRotation(geardian);
        }

        @Override
        public void onExecute(HostilitiesEntity geardian, LivingEntity target, int activeTicksElapsed) {
            lockPositionAndRotation(geardian);

            if (activeTicksElapsed == 1) {
                Vec3 look = geardian.getLookAngle();
                Vec3 center = geardian.position().add(look.scale(2.0D));

                AABB sweepBox = new AABB(
                        center.x - 1.5D, geardian.getY(), center.z - 1.5D,
                        center.x + 1.5D, geardian.getY() + 2.0D, center.z + 1.5D
                ).expandTowards(look.scale(2.0D));

                List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                        LivingEntity.class, sweepBox, e -> e != geardian && !geardian.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.hurt(geardian.damageSources().mobAttack(geardian), 18.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            look.x * 1.8D, 0.2D, look.z * 1.8D
                    ));
                }
            }
        }

        @Override
        public void onRecoveryTick(HostilitiesEntity geardian, LivingEntity target, int recoveryTicksElapsed) {
            lockPositionAndRotation(geardian);
        }

        @Override
        public void onStop(HostilitiesEntity geardian, LivingEntity target) {}

        private void lockPositionAndRotation(HostilitiesEntity geardian) {
            geardian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            geardian.getNavigation().stop();
            geardian.setDeltaMovement(0.0D, geardian.getDeltaMovement().y, 0.0D);

            geardian.setYRot(geardian.yRotO);
            geardian.setXRot(geardian.xRotO);
            geardian.yBodyRot = geardian.yBodyRotO;
            geardian.yHeadRot = geardian.yHeadRotO;
            geardian.getLookControl().setLookAt(geardian.getX() + geardian.getLookAngle().x, geardian.getEyeY(), geardian.getZ() + geardian.getLookAngle().z);
        }
    }
}