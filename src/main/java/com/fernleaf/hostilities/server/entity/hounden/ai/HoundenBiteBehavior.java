package com.fernleaf.hostilities.server.entity.hounden.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.hounden.Hounden;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HoundenBiteBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public HoundenBiteBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override public int getWindupTicks() { return 10; }
        @Override public int getActiveTicks() { return 12; }
        @Override public int getRecoveryTicks() { return 5; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null
                    && target.isAlive()
                    && !owner.isSitting()
                    && !owner.isPerformingAction()
                    && owner.distanceToSqr(target) <= 16.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity hounden, LivingEntity target) {
            hounden.triggerAnimation(Hounden.ANIM_BITE, getTotalDuration());
            alignFacing(hounden, target);
            lockRotation(hounden);
        }

        @Override
        public void onWindupTick(HostilitiesEntity hounden, LivingEntity target, int elapsedTicks) {
            if (elapsedTicks <= 3) {
                alignFacing(hounden, target);
            }
            lockRotation(hounden);
        }

        @Override
        public void onExecute(HostilitiesEntity hounden, LivingEntity target, int activeTicksElapsed) {
            lockRotation(hounden);

            // Fast horizontal launch (farther than lunge, flat arc)
            if (activeTicksElapsed == 1) {
                Vec3 look = hounden.getLookAngle();
                hounden.setDeltaMovement(look.x * 1.8D, 0.1D, look.z * 1.8D);
                hounden.hasImpulse = true;
            }

            // Hitbox exactly 1 block in front
            if (activeTicksElapsed >= 4 && activeTicksElapsed <= 8) {
                Vec3 look = hounden.getLookAngle();
                Vec3 center = hounden.position().add(look.scale(1.0D));

                AABB biteBox = new AABB(
                        center.x - 0.75D, hounden.getY(), center.z - 0.75D,
                        center.x + 0.75D, hounden.getY() + 1.8D, center.z + 0.75D
                );

                List<LivingEntity> targets = hounden.level().getEntitiesOfClass(
                        LivingEntity.class, biteBox, e -> e != hounden && !hounden.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    if (entity.hurt(hounden.damageSources().mobAttack(hounden), 7.0F)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1), hounden);
                    }
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