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

public class GeardianHeavyThrustBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianHeavyThrustBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 20; }   // 1.00s (Windup + Active = 36 ticks)

        @Override
        public int getActiveTicks() { return 16; }   // 0.80s (1.00s - 1.80s)

        @Override
        public int getRecoveryTicks() { return 64; } // 3.20s (Total: 5.0s / 100 ticks)

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null && target.isAlive() && !owner.isSitting() && owner.distanceToSqr(target) <= 25.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            geardian.triggerAnimation(Geardian.ANIM_HEAVY_THRUST, getTotalDuration());
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
                Vec3 startPos = geardian.position().add(0, 0.5D, 0);

                AABB thrustBox = new AABB(
                        startPos.x - 0.5D, geardian.getY(), startPos.z - 0.5D,
                        startPos.x + 0.5D, geardian.getY() + 2.0D, startPos.z + 0.5D
                ).expandTowards(look.x * 4.0D, 0.0D, look.z * 4.0D);

                List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                        LivingEntity.class, thrustBox, e -> e != geardian && !geardian.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.hurt(geardian.damageSources().mobAttack(geardian), 22.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            look.x * 2.2D, 0.3D, look.z * 2.2D
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