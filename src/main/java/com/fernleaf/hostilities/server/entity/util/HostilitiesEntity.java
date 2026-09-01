package com.fernleaf.hostilities.server.entity.util;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class HostilitiesEntity extends TamableAnimal implements OwnableEntity {

    // --- Synced Data Accessors ---
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(HostilitiesEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Integer> DATA_COMMAND_STATE =
            SynchedEntityData.defineId(HostilitiesEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ACTION_STATE =
            SynchedEntityData.defineId(HostilitiesEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_CURRENT_PHASE =
            SynchedEntityData.defineId(HostilitiesEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_COMBO_COUNTER =
            SynchedEntityData.defineId(HostilitiesEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ANIMATION_ID =
            SynchedEntityData.defineId(HostilitiesEntity.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos stationPost = null;

    protected int actionTicks = 0;
    protected int phaseInvulnerabilityTicks = 0;

    public enum CommandState {
        WANDER(0), SIT(1), FOLLOW(2), WORK(3);
        public final int id;
        CommandState(int id) { this.id = id; }
        public static CommandState byId(int id) {
            for (CommandState state : values()) if (state.id == id) return state;
            return WANDER;
        }
    }

    public enum ActionState {
        IDLE(0), LIGHT_ATTACKS(1), HEAVY_ATTACKS(2), SPECIAL_MOVES(3), TASKS(4);
        public final int id;
        ActionState(int id) { this.id = id; }
        public static ActionState byId(int id) {
            for (ActionState state : values()) if (state.id == id) return state;
            return IDLE;
        }
    }

    // Shared Base Brain Modules
    protected static final List<SensorType<? extends Sensor<? super HostilitiesEntity>>> SENSOR_TYPES = List.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY
    );

    protected static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.HOME
    );

    protected HostilitiesEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_COMMAND_STATE, CommandState.WANDER.id);
        builder.define(DATA_ACTION_STATE, ActionState.IDLE.id);
        builder.define(DATA_CURRENT_PHASE, 1);
        builder.define(DATA_COMBO_COUNTER, 0);
        builder.define(DATA_ANIMATION_ID, 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Brain<HostilitiesEntity> getBrain() {
        return (Brain<HostilitiesEntity>) super.getBrain();
    }

    @Override
    protected Brain.@NotNull Provider<?> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    // Force subclasses (like Geardian) to supply their own Brain goals
    @Override
    protected abstract @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic);

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("hostilitiesBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    // --- Station Post Accessors ---
    @Nullable
    public BlockPos getStationPost() {
        return this.stationPost;
    }

    public void setStationPost(@Nullable BlockPos pos) {
        this.stationPost = pos;
        if (pos != null) {
            this.getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level().dimension(), pos));
        } else {
            this.getBrain().eraseMemory(MemoryModuleType.HOME);
        }
    }

    // --- Action State Management ---
    public int getActionTicks() { return this.actionTicks; }
    public ActionState getActionState() { return ActionState.byId(this.entityData.get(DATA_ACTION_STATE)); }

    public void setActionState(ActionState state, int durationTicks) {
        this.entityData.set(DATA_ACTION_STATE, state.id);
        this.actionTicks = durationTicks;
    }

    public boolean isPerformingAction() { return this.actionTicks > 0 || getActionState() != ActionState.IDLE; }
    public int getComboCounter() { return this.entityData.get(DATA_COMBO_COUNTER); }
    public void incrementCombo() { this.entityData.set(DATA_COMBO_COUNTER, getComboCounter() + 1); }
    public void resetCombo() { this.entityData.set(DATA_COMBO_COUNTER, 0); }
    public int getAnimationId() { return this.entityData.get(DATA_ANIMATION_ID); }

    public void triggerAnimation(int animId, int durationTicks) {
        this.entityData.set(DATA_ANIMATION_ID, animId);
        this.actionTicks = durationTicks;
    }

    // --- Phase System ---
    public int getCurrentPhase() { return this.entityData.get(DATA_CURRENT_PHASE); }

    public void setPhase(int phase) {
        int oldPhase = getCurrentPhase();
        int newPhase = Math.min(Math.max(phase, 1), 4);
        if (oldPhase != newPhase) {
            this.entityData.set(DATA_CURRENT_PHASE, newPhase);
            onPhaseTransition(oldPhase, newPhase);
        }
    }

    protected void checkPhaseTransitions() {
        float healthPct = this.getHealth() / this.getMaxHealth();
        if (healthPct <= 0.25F && getCurrentPhase() < 4) setPhase(4);
        else if (healthPct <= 0.50F && getCurrentPhase() < 3) setPhase(3);
        else if (healthPct <= 0.75F && getCurrentPhase() < 2) setPhase(2);
    }

    protected void onPhaseTransition(int oldPhase, int newPhase) {
        setActionState(ActionState.IDLE, 0);
        this.phaseInvulnerabilityTicks = 30;
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0D, getZ(), 10, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    // --- Taming & Command Persistence ---
    @Override
    @Nullable
    public UUID getOwnerUUID() { return this.entityData.get(DATA_OWNER_UUID).orElse(null); }
    public void setOwnerUUID(@Nullable UUID uuid) { this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid)); }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        UUID uuid = getOwnerUUID();
        return uuid == null ? null : this.level().getPlayerByUUID(uuid);
    }

    public boolean isTamed() { return getOwnerUUID() != null; }
    public boolean isOwner(Entity entity) { return entity != null && entity.getUUID().equals(getOwnerUUID()); }

    public CommandState getCommandState() { return CommandState.byId(this.entityData.get(DATA_COMMAND_STATE)); }

    public void setCommandState(CommandState state) {
        this.entityData.set(DATA_COMMAND_STATE, state.id);
        boolean sitting = (state == CommandState.SIT);
        super.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);
        this.getNavigation().stop();
        if (sitting) {
            this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    public boolean isSitting() { return isTamed() && getCommandState() == CommandState.SIT; }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.actionTicks > 0) {
                this.actionTicks--;
                if (this.actionTicks == 0) {
                    this.entityData.set(DATA_ACTION_STATE, ActionState.IDLE.id);
                    this.entityData.set(DATA_ANIMATION_ID, 0);
                }
            }
            if (this.phaseInvulnerabilityTicks > 0) this.phaseInvulnerabilityTicks--;
            checkPhaseTransitions();
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.phaseInvulnerabilityTicks > 0) return false;
        if (source.getEntity() instanceof Player player && isOwner(player) && !player.isSecondaryUseActive()) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CommandState", getCommandState().id);
        tag.putInt("ActionState", getActionState().id);
        tag.putInt("CurrentPhase", getCurrentPhase());
        tag.putInt("ComboCounter", getComboCounter());
        if (getOwnerUUID() != null) tag.putUUID("Owner", getOwnerUUID());
        if (this.stationPost != null) tag.put("StationPost", NbtUtils.writeBlockPos(this.stationPost));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CommandState")) setCommandState(CommandState.byId(tag.getInt("CommandState")));
        if (tag.contains("ActionState")) this.entityData.set(DATA_ACTION_STATE, tag.getInt("ActionState"));
        if (tag.contains("CurrentPhase")) this.entityData.set(DATA_CURRENT_PHASE, tag.getInt("CurrentPhase"));
        if (tag.contains("ComboCounter")) this.entityData.set(DATA_COMBO_COUNTER, tag.getInt("ComboCounter"));
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
        if (tag.contains("StationPost")) setStationPost(NbtUtils.readBlockPos(tag, "StationPost").orElse(null));
    }

    @Override public boolean isFood(@NotNull ItemStack stack) { return false; }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob partner) { return null; }
}