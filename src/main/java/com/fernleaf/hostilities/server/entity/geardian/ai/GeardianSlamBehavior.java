package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GeardianSlamBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianSlamBehavior() {
        super(new Attack());
    }

    private static class Attack implements TelegraphedAttack<HostilitiesEntity> {
        @Override
        public int getWindupTicks() { return 30; }

        @Override
        public int getActiveTicks() { return 5; }

        @Override
        public int getRecoveryTicks() { return 40; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            return target != null && target.isAlive() && !owner.isSitting() && owner.distanceToSqr(target) <= 36.0D;
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            geardian.triggerAnimation(Geardian.ANIM_SLAM, getTotalDuration());
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
                AABB slamBox = geardian.getBoundingBox().inflate(2.5D, 1.0D, 2.5D);
                List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                        LivingEntity.class, slamBox, e -> e != geardian && !geardian.isAlliedTo(e)
                );
                for (LivingEntity entity : targets) {
                    entity.hurt(geardian.damageSources().mobAttack(geardian), 14.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
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