package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

public class GeardianStepBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public GeardianStepBehavior() {
        super(new StepDodge());
    }

    private static class StepDodge implements TelegraphedAttack<HostilitiesEntity> {
        private boolean isLeftStep = false;

        @Override
        public int getWindupTicks() { return 2; }

        @Override
        public int getActiveTicks() { return 8; }

        @Override
        public int getRecoveryTicks() { return 5; }

        @Override
        public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
            if (target == null || !target.isAlive() || owner.isSitting()) return false;
            return owner.distanceToSqr(target) <= 16.0D; // Within 4 blocks
        }

        @Override
        public void onWindupStart(HostilitiesEntity geardian, LivingEntity target) {
            // Stop navigation so pathfinding doesn't override movement momentum
            geardian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            geardian.getNavigation().stop();

            // Randomly decide left or right direction
            isLeftStep = geardian.getRandom().nextBoolean();
            int animId = isLeftStep ? Geardian.ANIM_LEFT_STEP : Geardian.ANIM_RIGHT_STEP;

            geardian.triggerAnimation(animId, getTotalDuration());
        }

        @Override
        public void onWindupTick(HostilitiesEntity geardian, LivingEntity target, int elapsedTicks) {}

        @Override
        public void onExecute(HostilitiesEntity geardian, LivingEntity target, int activeTicksElapsed) {
            if (activeTicksElapsed == 1) {
                // Enable i-frames at the start of execute phase
                geardian.setInvulnerable(true);

                Vec3 look = geardian.getLookAngle();
                Vec3 sideDir = new Vec3(-look.z, 0, look.x).normalize();

                if (!isLeftStep) {
                    sideDir = sideDir.scale(-1.0D);
                }

                // Impulse dodge force
                geardian.setDeltaMovement(sideDir.x * 1.1D, 0.1D, sideDir.z * 1.1D);
            }
        }

        @Override
        public void onRecoveryTick(HostilitiesEntity geardian, LivingEntity target, int recoveryTicksElapsed) {
            if (recoveryTicksElapsed == 1) {
                geardian.setInvulnerable(false);
            }
        }

        @Override
        public void onStop(HostilitiesEntity geardian, LivingEntity target) {
            // Fail-safe to clean up invulnerability if canceled mid-dash
            geardian.setInvulnerable(false);
        }
    }
}