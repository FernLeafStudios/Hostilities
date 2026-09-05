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

public class DaturenaThreePunchComboBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public DaturenaThreePunchComboBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 12; }  // 0.0s - 0.6s

        @Override
        public int getActiveTicks() { return 48; }  // 0.6s - 3.0s (Hits @ 0.6s, 1.5s, 2.7s)

        @Override
        public int getRecoveryTicks() { return 12; } // 3.0s - 3.6s

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null && target.isAlive() && !owner.isSitting() && owner.distanceToSqr(target) <= 16.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity daturena, LivingEntity target) {
            daturena.triggerAnimation(Daturena.ANIM_THREE_PUNCH_COMBO, getTotalDuration());
            lockRotation(daturena);
        }

        @Override
        public void onWindupTick(HostilitiesEntity daturena, LivingEntity target, int elapsedTicks) {
            lockRotation(daturena);
        }

        @Override
        public void onExecute(HostilitiesEntity daturena, LivingEntity target, int activeTicksElapsed) {
            lockRotation(daturena);

            // Punch 1 @ 0.6s (Tick 1), Punch 2 @ 1.5s (Tick 19), Punch 3 @ 2.7s (Tick 43)
            if (activeTicksElapsed == 1 || activeTicksElapsed == 19 || activeTicksElapsed == 43) {
                Vec3 look = daturena.getLookAngle();

                // Forward Step on each hit trigger
                boolean isFinisher = (activeTicksElapsed == 43);
                double stepForce = isFinisher ? 0.85D : 0.5D;

                daturena.setDeltaMovement(new Vec3(look.x * stepForce, daturena.getDeltaMovement().y, look.z * stepForce));
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

                float damage = isFinisher ? 18.0F : 12.0F;
                double pushForce = isFinisher ? 2.0D : 0.8D;
                double liftForce = isFinisher ? 0.5D : 0.15D;

                for (LivingEntity entity : targets) {
                    entity.invulnerableTime = 0;
                    entity.hurt(daturena.damageSources().mobAttack(daturena), damage);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(look.x * pushForce, liftForce, look.z * pushForce));
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