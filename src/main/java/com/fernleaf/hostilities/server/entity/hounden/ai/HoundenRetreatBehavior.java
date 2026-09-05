package com.fernleaf.hostilities.server.entity.hounden.ai;

import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttack;
import com.fernleaf.fernframe.brawlcrawl.attack.TelegraphedAttackBehavior;
import com.fernleaf.hostilities.server.entity.hounden.Hounden;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

public class HoundenRetreatBehavior extends TelegraphedAttackBehavior<HostilitiesEntity> {

    public HoundenRetreatBehavior(double retreatForce) {
        super(new RetreatLeap(retreatForce));
    }

    private record RetreatLeap(double retreatForce) implements TelegraphedAttack<HostilitiesEntity> {

        @Override
            public int getWindupTicks() {
                return 3;
            }

            @Override
            public int getActiveTicks() {
                return 8;
            }

            @Override
            public int getRecoveryTicks() {
                return 4;
            }

            @Override
            public boolean canAttack(HostilitiesEntity owner, LivingEntity target) {
                if (target == null || !target.isAlive() || owner.isSitting()) {
                    return false;
                }
                // Trigger retreat when close to target (< 5 blocks)
                return owner.distanceToSqr(target) < 25.0D;
            }

            @Override
            public void onWindupStart(HostilitiesEntity hounden, LivingEntity target) {
                // Stop pathfinding momentum from overriding physical impulse
                hounden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                hounden.getNavigation().stop();

                // Trigger retreat animation across network for total action duration (15 ticks)
                hounden.triggerAnimation(Hounden.ANIM_RETREAT, getTotalDuration());
            }

            @Override
            public void onWindupTick(HostilitiesEntity hounden, LivingEntity target, int elapsedTicks) {
                if (target != null) {
                    hounden.getLookControl().setLookAt(target, 60.0F, 60.0F);
                }
            }

            @Override
            public void onExecute(HostilitiesEntity hounden, LivingEntity target, int activeTicksElapsed) {
                // Launch backwards on first active tick
                if (activeTicksElapsed == 1) {
                    Vec3 awayDir;
                    if (target != null) {
                        awayDir = hounden.position().subtract(target.position()).normalize();
                    } else {
                        awayDir = hounden.getLookAngle().scale(-1.0D);
                    }

                    hounden.setDeltaMovement(awayDir.x * this.retreatForce, 0.25D, awayDir.z * this.retreatForce);
                    hounden.hasImpulse = true;
                }

                if (target != null) {
                    hounden.getLookControl().setLookAt(target, 60.0F, 60.0F);
                }
            }

            @Override
            public void onRecoveryTick(HostilitiesEntity hounden, LivingEntity target, int recoveryTicksElapsed) {
                if (target != null) {
                    hounden.getLookControl().setLookAt(target, 30.0F, 30.0F);
                }
            }

            @Override
            public void onStop(HostilitiesEntity hounden, LivingEntity target) {
                // Reset animation ID back to default/idle on finish or early interrupt
                hounden.setAnimationId(Hounden.ANIM_NONE);
            }
        }
}