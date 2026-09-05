package com.fernleaf.hostilities.server.entity.hounden.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.hounden.Hounden;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HoundenScratchBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public HoundenScratchBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override public int getWindupTicks() { return 6; }
        @Override public int getActiveTicks() { return 8; }
        @Override public int getRecoveryTicks() { return 4; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null
                    && target.isAlive()
                    && !owner.isSitting()
                    && !owner.isPerformingAction()
                    && owner.distanceToSqr(target) <= 9.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity hounden, LivingEntity target) {
            hounden.triggerAnimation(Hounden.ANIM_SCRATCH, getTotalDuration());
            alignFacing(hounden, target);
            lockRotation(hounden);
        }

        @Override
        public void onWindupTick(HostilitiesEntity hounden, LivingEntity target, int elapsedTicks) {
            if (elapsedTicks <= 2) {
                alignFacing(hounden, target);
            }
            lockRotation(hounden);
        }

        @Override
        public void onExecute(HostilitiesEntity hounden, LivingEntity target, int activeTicksElapsed) {
            lockRotation(hounden);

            // Explosive burst forward
            if (activeTicksElapsed == 1) {
                Vec3 look = hounden.getLookAngle();
                hounden.setDeltaMovement(look.x * 1.2D, 0.15D, look.z * 1.2D);
                hounden.hasImpulse = true;
            }

            // Rapid double-scratch hits at tick 2 and tick 5
            if (activeTicksElapsed == 2 || activeTicksElapsed == 5) {
                Vec3 look = hounden.getLookAngle();
                Vec3 center = hounden.position().add(look.scale(1.0D));

                AABB scratchBox = new AABB(
                        center.x - 0.75D, hounden.getY(), center.z - 0.75D,
                        center.x + 0.75D, hounden.getY() + 1.8D, center.z + 0.75D
                );

                List<LivingEntity> targets = hounden.level().getEntitiesOfClass(
                        LivingEntity.class, scratchBox, e -> e != hounden && !hounden.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.hurt(hounden.damageSources().mobAttack(hounden), 4.0F);
                }
            }
        }

        @Override
        public void onRecoveryTick(HostilitiesEntity hounden, LivingEntity target, int elapsed) {
            lockRotation(hounden);
        }

        @Override
        public void onStop(HostilitiesEntity hounden, LivingEntity target) {
            hounden.setAnimationId(Hounden.ANIM_NONE);
        }

        private void alignFacing(HostilitiesEntity hounden, LivingEntity target) {
            if (target == null) return;
            double dx = target.getX() - hounden.getX();
            double dz = target.getZ() - hounden.getZ();
            float targetYRot = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            hounden.setYRot(targetYRot);
            hounden.yBodyRot = targetYRot;
            hounden.yHeadRot = targetYRot;
            hounden.getLookControl().setLookAt(target, 90.0F, 90.0F);
        }

        private void lockRotation(HostilitiesEntity hounden) {
            hounden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            hounden.getNavigation().stop();

            hounden.setYRot(hounden.yRotO);
            hounden.setXRot(hounden.xRotO);
            hounden.yBodyRot = hounden.yBodyRotO;
            hounden.yHeadRot = hounden.yHeadRotO;
            hounden.getLookControl().setLookAt(
                    hounden.getX() + hounden.getLookAngle().x,
                    hounden.getEyeY(),
                    hounden.getZ() + hounden.getLookAngle().z
            );
        }
    }
}