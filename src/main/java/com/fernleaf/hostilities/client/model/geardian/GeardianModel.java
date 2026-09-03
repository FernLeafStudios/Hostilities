package com.fernleaf.hostilities.client.model.geardian;

import com.fernleaf.fernframe.proprio.animation.TransitionAnimationSystem;
import com.fernleaf.fernframe.proprio.animation.TransitionEasing;
import com.fernleaf.hostilities.Hostilities;
import com.fernleaf.hostilities.client.animations.GeardianAnimations;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GeardianModel<T extends Geardian> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hostilities.MODID, "geardian"), "main");

    private final ModelPart root;
    public final ModelPart body;
    public final ModelPart lowerHalf;
    public final ModelPart bellDress;
    public final ModelPart feet;
    public final ModelPart leftLeg;
    public final ModelPart rightLeg;
    public final ModelPart topHalf;
    public final ModelPart head;
    public final ModelPart rightEye;
    public final ModelPart leftEye;
    public final ModelPart hair;
    public final ModelPart arms;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart glaive;

    public GeardianModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.lowerHalf = this.body.getChild("lower_half");
        this.bellDress = this.lowerHalf.getChild("bell_dress");
        this.feet = this.lowerHalf.getChild("feet");
        this.leftLeg = this.feet.getChild("left_leg");
        this.rightLeg = this.feet.getChild("right_leg");
        this.topHalf = this.body.getChild("top_half");
        this.head = this.topHalf.getChild("head");
        this.rightEye = this.head.getChild("right_eye");
        this.leftEye = this.head.getChild("left_eye");
        this.hair = this.head.getChild("hair");
        this.arms = this.topHalf.getChild("arms");
        this.rightArm = this.arms.getChild("right_arm");
        this.leftArm = this.arms.getChild("left_arm");
        ModelPart group = this.leftArm.getChild("group");
        this.glaive = group.getChild("glaive");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-2.5F, 5.5F, -0.5F));
        PartDefinition lower_half = body.addOrReplaceChild("lower_half", CubeListBuilder.create(), PartPose.offset(0.0F, 3.5F, 0.0F));

        lower_half.addOrReplaceChild("bell_dress", CubeListBuilder.create().texOffs(13, 0).addBox(-5.5F, -2.0F, -3.5F, 11.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 25).addBox(-6.5F, 13.0F, -4.5F, 13.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.0F, 0.0F));

        PartDefinition feet = lower_half.addOrReplaceChild("feet", CubeListBuilder.create(), PartPose.offset(2.0F, 6.0F, 0.0F));
        feet.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(65, 78).addBox(-2.5F, -1.0F, -1.5F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.75F, 0.0F, 0.0F));
        feet.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(17, 80).addBox(-1.5F, -1.0F, -1.5F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.75F, 0.0F, 0.0F));

        PartDefinition top_half = body.addOrReplaceChild("top_half", CubeListBuilder.create().texOffs(0, 98).addBox(-2.5F, -9.0F, -2.5F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 56).addBox(-4.5F, -7.0F, -1.5F, 9.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -4.5F, 0.0F));

        PartDefinition head = top_half.addOrReplaceChild("head", CubeListBuilder.create().texOffs(52, 0).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(87, 0).addBox(-1.5F, 5.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.0F, 0.5F));

        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(17, 73).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.75F, -3.5F, -0.48F, 0.0F, 0.0F));
        head.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(67, 54).addBox(-12.5F, -11.75F, -0.75F, 13.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(52, 18).addBox(-4.5F, -4.0F, -1.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, -3.5F));
        head.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(63, 18).addBox(-13.75F, -2.25F, -4.25F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(67, 67).addBox(-13.5F, -10.0F, -4.0F, 13.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(14.25F, -1.75F, -0.25F));
        head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(46, 38).addBox(-5.5F, -4.5F, -7.5F, 11.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(13, 25).addBox(-5.5F, -1.5F, -2.5F, 11.0F, 24.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.5F, 2.5F));

        PartDefinition arms = top_half.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(6.5F, 2.5F, 0.5F));
        arms.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(27, 56).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 19.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, -8.0F, 0.0F));

        PartDefinition left_arm = arms.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));
        left_arm.addOrReplaceChild("group2", CubeListBuilder.create().texOffs(0, 73).addBox(-2.0F, -9.5F, 0.0F, 4.0F, 19.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, -2.0F));

        PartDefinition group = left_arm.addOrReplaceChild("group", CubeListBuilder.create(), PartPose.offset(0.0F, 14.5F, 0.5F));
        PartDefinition glaive = group.addOrReplaceChild("glaive", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 1.5708F, 0.0F, 1.5708F));

        glaive.addOrReplaceChild("resize", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -22.0F, -1.5F, 3.0F, 52.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(34, 84).addBox(-11.0F, -16.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(82, 78).addBox(-9.5F, -29.0F, -0.5F, 6.0F, 15.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(46, 52).addBox(-9.5F, -45.0F, 0.0F, 9.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Geardian entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // MUST reset pose every frame before applying keyframe transformations
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // 1. Walking & Idle
        this.animateWalk(GeardianAnimations.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(entity.idleAnimationState, GeardianAnimations.idle, ageInTicks);
        this.animate(entity.sleepAnimationState, GeardianAnimations.sleep, ageInTicks);

        // 2. Attacks (Evaluated AFTER idle so attack keyframes override idle rotations)
        this.animate(entity.sweepAnimationState, GeardianAnimations.sweep, ageInTicks);
        this.animate(entity.slamAnimationState, GeardianAnimations.slam, ageInTicks);
        this.animate(entity.chargedSweepAnimationState, GeardianAnimations.charged_sweep, ageInTicks);
    }
}