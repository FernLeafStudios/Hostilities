package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.fernframe.allyrally.attack.TelegraphedAttack;
import com.fernleaf.fernframe.allyrally.attack.TelegraphedAttackBehavior;
import com.fernleaf.fernframe.allyrally.entity.AllyRallyBossEntity;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GeardianSweepBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianSweepBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 12; } // 0.6s windup

        @Override
        public int getActiveTicks() { return 7; }  // Active window 0.6s to 0.95s

        @Override
        public int getRecoveryTicks() { return 20; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null && target.isAlive() && !owner.isSitting() && owner.distanceToSqr(target) <= 20.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            geardian.setActionState(AllyRallyBossEntity.ActionState.LIGHT_ATTACKS, getTotalDuration());
            geardian.triggerAnimation(Geardian.ANIM_SWEEP, getTotalDuration());
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
                AABB sweepBox = geardian.getBoundingBox().inflate(2.0D, 1.0D, 2.0D);
                List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                        LivingEntity.class, sweepBox, e -> e != geardian && !geardian.isAlliedTo(e)
                );

                for (LivingEntity entity : targets) {
                    entity.hurt(geardian.damageSources().mobAttack(geardian), 8.0F);
                }
                geardian.incrementCombo();
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