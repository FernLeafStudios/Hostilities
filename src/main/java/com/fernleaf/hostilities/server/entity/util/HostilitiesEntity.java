package com.fernleaf.hostilities.server.entity.util;

import com.fernleaf.fernframe.allyrally.entity.AllyRallyBossEntity;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class HostilitiesEntity extends AllyRallyBossEntity {

    @Nullable
    private BlockPos stationPost = null;

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
    @SuppressWarnings("unchecked")
    public @NotNull Brain<HostilitiesEntity> getBrain() {
        return (Brain<HostilitiesEntity>) super.getBrain();
    }

    @Override
    protected Brain.@NotNull Provider<?> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected abstract @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic);

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("hostilitiesBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

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

    @Override
    public void setCommandState(CommandState state) {
        super.setCommandState(state);
        if (state == CommandState.SIT) {
            this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.stationPost != null) tag.put("StationPost", NbtUtils.writeBlockPos(this.stationPost));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("StationPost")) setStationPost(NbtUtils.readBlockPos(tag, "StationPost").orElse(null));
    }

    @Override public boolean isFood(@NotNull ItemStack stack) { return false; }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob partner) { return null; }
}