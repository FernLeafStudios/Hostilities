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

public class DaturenaJabCrossBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public DaturenaJabCrossBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 6; }   // 0.0s - 0.3s

        @Override
        public int getActiveTicks() { return 8; }   // 0.3s - 0.7s (Jab @ 0.3s, Cross @ 0.5s)

        @Override
        public int getRecoveryTicks() { return 6; } // 0.7s - 1.0s (Matches 1.0s animation total)

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null
                    && target.isAlive()
                    && !owner.isSitting()
                    && !owner.isPerformingAction()
                    && owner.distanceToSqr(target) <= 12.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity daturena, LivingEntity target) {
            daturena.triggerAnimation(Daturena.ANIM_JAB_CROSS, getTotalDuration());
            lockRotation(daturena);
        }

        @Override
        public void onWindupTick(HostilitiesEntity daturena, LivingEntity target, int elapsedTicks) {
            lockRotation(daturena);
        }

        @Override
        public void onExecute(HostilitiesEntity daturena, LivingEntity target, int activeTicksElapsed) {
            lockRotation(daturena);

            // Jab @ Active Tick 1, Cross @ Active Tick 5
            if (activeTicksElapsed == 1 || activeTicksElapsed == 5) {
                Vec3 look = daturena.getLookAngle();

                double stepForce = (activeTicksElapsed == 1) ? 0.35D : 0.5D;
                daturena.setDeltaMovement(new Vec3(look.x * stepForce, daturena.getDeltaMovement().y, look.z * stepForce));
                daturena.hasImpulse = true;

                double frontX = daturena.getX() + look.x * 1.8D;
                double frontZ = daturena.getZ() + look.z * 1.8D;

                AABB attackBox = new AABB(
                        frontX - 1.2D, daturena.getY(), frontZ - 1.2D,
                        frontX + 1.2D, daturena.getY() + 3.0D, frontZ + 1.2D
                );

                List<LivingEntity> targets = daturena.level().getEntitiesOfClass(
                        LivingEntity.class, attackBox, e -> e != daturena && !daturena.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.invulnerableTime = 0;
                    entity.hurt(daturena.damageSources().mobAttack(daturena), 10.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(look.x * 0.8D, 0.1D, look.z * 0.8D));
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