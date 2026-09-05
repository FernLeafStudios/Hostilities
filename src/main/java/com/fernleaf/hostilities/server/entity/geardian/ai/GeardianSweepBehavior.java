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

public class GeardianSweepBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianSweepBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 16; }   // 0.80s

        @Override
        public int getActiveTicks() { return 4; }    // 0.20s (0.80s - 1.00s)

        @Override
        public int getRecoveryTicks() { return 10; } // 0.50s (Total: 1.5s / 30 ticks)

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null && target.isAlive() && !owner.isSitting() && owner.distanceToSqr(target) <= 20.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            geardian.triggerAnimation(Geardian.ANIM_SWEEP, getTotalDuration());
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
                Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
                Vec3 center = geardian.position().add(look.scale(2.0D)); // Center of the 4-block forward reach

                // Base box at center: 3 wide (1.5 extend), 2 tall (0 to 2), 4 deep (2.0 extend)
                AABB sweepBox = new AABB(
                        center.x - 1.5D, geardian.getY(), center.z - 1.5D,
                        center.x + 1.5D, geardian.getY() + 2.0D, center.z + 1.5D
                ).expandTowards(look.scale(2.0D));

                List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                        LivingEntity.class, sweepBox, e -> e != geardian && !geardian.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.hurt(geardian.damageSources().mobAttack(geardian), 8.0F);
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