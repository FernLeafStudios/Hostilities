package com.fernleaf.hostilities.server.entity.daturena.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.daturena.Daturena;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DaturenaBashBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public DaturenaBashBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 10; }  // 0.0s - 0.5s

        @Override
        public int getActiveTicks() { return 10; }  // 0.5s - 1.0s

        @Override
        public int getRecoveryTicks() { return 5; } // 1.0s - 1.25s (Matches 1.25s animation total)

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null
                    && target.isAlive()
                    && !owner.isSitting()
                    && !owner.isPerformingAction()
                    && owner.distanceToSqr(target) <= 16.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity daturena, LivingEntity target) {
            daturena.triggerAnimation(Daturena.ANIM_BASH, getTotalDuration());
            lockRotation(daturena);
        }

        @Override
        public void onWindupTick(HostilitiesEntity daturena, LivingEntity target, int elapsedTicks) {
            lockRotation(daturena);
        }

        @Override
        public void onExecute(HostilitiesEntity daturena, LivingEntity target, int activeTicksElapsed) {
            lockRotation(daturena);

            if (activeTicksElapsed == 1) {
                Vec3 look = daturena.getLookAngle();

                daturena.setDeltaMovement(new Vec3(look.x * 1.8D, daturena.getDeltaMovement().y, look.z * 1.8D));
                daturena.hasImpulse = true;

                double frontX = daturena.getX() + look.x * 2.0D;
                double frontZ = daturena.getZ() + look.z * 2.0D;

                AABB attackBox = new AABB(
                        frontX - 1.5D, daturena.getY(), frontZ - 1.5D,
                        frontX + 1.5D, daturena.getY() + 3.0D, frontZ + 1.5D
                );

                List<LivingEntity> targets = daturena.level().getEntitiesOfClass(
                        LivingEntity.class, attackBox, e -> e != daturena && !daturena.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.invulnerableTime = 0;
                    entity.hurt(daturena.damageSources().mobAttack(daturena), 18.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(look.x * 1.5D, 0.4D, look.z * 1.5D));
                }
            }
        }

        @Override
        public void onRecoveryTick(HostilitiesEntity daturena, LivingEntity target, int recoveryTicksElapsed) {
            lockRotation(daturena);
        }

        @Override
        public void onStop(HostilitiesEntity daturena, LivingEntity target) {
            daturena.triggerAnimation(Daturena.ANIM_NONE, 0);
            if (daturena instanceof Daturena clientDaturena) {
                clientDaturena.stopActionAnimations();
            }
        }

        private void lockRotation(HostilitiesEntity daturena) {
            daturena.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            daturena.getNavigation().stop();

            daturena.setYRot(daturena.yRotO);
            daturena.setXRot(daturena.xRotO);
            daturena.yBodyRot = daturena.yBodyRotO;
            daturena.yHeadRot = daturena.yHeadRotO;
            daturena.getLookControl().setLookAt(daturena.getX() + daturena.getLookAngle().x, daturena.getEyeY(), daturena.getZ() + daturena.getLookAngle().z);
        }
    }
}