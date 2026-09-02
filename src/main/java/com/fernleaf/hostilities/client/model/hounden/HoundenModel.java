package com.fernleaf.hostilities.client.model.hounden;

import com.fernleaf.fernframe.proprio.animation.AnimationTransition;
import com.fernleaf.fernframe.proprio.animation.TransitionAnimator;
import com.fernleaf.fernframe.proprio.animation.TransitionEasing;
import com.fernleaf.hostilities.Hostilities;
import com.fernleaf.hostilities.client.animations.HoundenAnimations;
import com.fernleaf.hostilities.server.entity.hounden.Hounden;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HoundenModel<T extends Hounden> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Hostilities.MODID, "hounden"), "main");

	private final ModelPart root;
	public final ModelPart bone;
	public final ModelPart hounden;
	public final ModelPart torso;
	public final ModelPart arms;
	public final ModelPart rightArm;
	public final ModelPart leftArm;
	public final ModelPart head;
	public final ModelPart braids;
	public final ModelPart leftBraid;
	public final ModelPart rightBraid;
	public final ModelPart legs;
	public final ModelPart rightLeg;
	public final ModelPart leftLeg;
	public final ModelPart skirt;

	// Transition definitions matching GeardianModel pattern
	private final AnimationTransition idleTransition = new AnimationTransition(5, TransitionEasing.SMOOTH);
	private final AnimationTransition sitTransition = new AnimationTransition(8, TransitionEasing.SMOOTH);
	private final AnimationTransition lungeTransition = new AnimationTransition(4, TransitionEasing.BEZIER);
	private final AnimationTransition scareTransition = new AnimationTransition(6, TransitionEasing.BEZIER);

	public HoundenModel(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
		this.hounden = this.bone.getChild("hounden");
		this.torso = this.hounden.getChild("torso");
		this.arms = this.torso.getChild("arms");
		this.rightArm = this.arms.getChild("right_arm");
		this.leftArm = this.arms.getChild("left_arm");
		this.head = this.hounden.getChild("head");
		this.braids = this.head.getChild("braids");
		this.leftBraid = this.braids.getChild("left_braid");
		this.rightBraid = this.braids.getChild("right_braid");
		this.legs = this.hounden.getChild("legs");
		this.rightLeg = this.legs.getChild("right_leg");
		this.leftLeg = this.legs.getChild("left_leg");
		this.skirt = this.legs.getChild("skirt");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Keep 'bone' at the base root (0, 0, 0)
		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		// Transferred the offset (1.0F, 14.0F, 0.0F) to 'hounden' so position keyframes transform correctly relative to 'bone'
		PartDefinition hounden = bone.addOrReplaceChild("hounden", CubeListBuilder.create(), PartPose.offset(1.0F, 14.0F, 0.0F));

		PartDefinition torso = hounden.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(25, 20).addBox(-3.0F, -1.0F, -1.0F, 4.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

		PartDefinition arms = torso.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(-4.0F, -1.0F, 0.0F));

		arms.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(9, 29).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		arms.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 0.0F, 0.0F));

		PartDefinition head = hounden.addOrReplaceChild("head", CubeListBuilder.create().texOffs(25, 0).addBox(-3.0F, -7.0F, -1.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(25, 11).addBox(-4.0F, -7.75F, -1.5F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12).addBox(-6.0F, -10.0F, 0.5F, 12.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(46, 7).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.0F, -7.75F, -0.5F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(30, 10).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -8.0F, -1.0F));

		PartDefinition braids = head.addOrReplaceChild("braids", CubeListBuilder.create(), PartPose.offset(-1.25F, -2.0F, 2.75F));

		braids.addOrReplaceChild("left_braid", CubeListBuilder.create().texOffs(44, 46).addBox(-2.0F, 3.5F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(18, 32).addBox(-1.5F, -0.5F, -1.5F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 43).addBox(-2.0F, 4.5F, -2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(46, 0).addBox(-2.0F, 8.5F, -2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(36, 32).addBox(-2.0F, 0.5F, -2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(49, 27).addBox(-2.0F, 7.5F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(33, 46).addBox(-2.0F, -0.5F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(4.25F, 1.0F, -0.25F));

		braids.addOrReplaceChild("right_braid", CubeListBuilder.create().texOffs(38, 20).addBox(-2.0F, 0.5F, -2.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 50).addBox(-3.0F, 7.5F, -0.5F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(44, 11).addBox(-2.0F, 8.5F, -2.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(27, 32).addBox(-0.5F, -0.5F, -2.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(36, 39).addBox(-2.0F, 4.5F, -2.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(38, 27).addBox(-3.0F, -0.5F, -0.5F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(51, 32).addBox(-3.0F, 3.5F, -0.5F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, 1.0F, 0.25F));

		PartDefinition legs = hounden.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(-1.0F, 1.0F, 0.0F));

		legs.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 46).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.0F));

		legs.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(15, 46).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

		legs.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 22).addBox(-6.0F, -1.0F, -1.0F, 10.0F, 4.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(1.0F, 1.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		// Head tracking
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);

		// Locomotion loops
		this.animate(entity.walkAnimationState, HoundenAnimations.walk, ageInTicks);
		this.animate(entity.runAnimationState, HoundenAnimations.run, ageInTicks);

		// Action states
		TransitionAnimator.animateWithTransition(this, entity.idleAnimationState, HoundenAnimations.idle, ageInTicks, this.idleTransition);
		TransitionAnimator.animateWithTransition(this, entity.sitAnimationState, HoundenAnimations.sit, ageInTicks, this.sitTransition);
		TransitionAnimator.animateWithTransition(this, entity.lungeAnimationState, HoundenAnimations.lunge, ageInTicks, this.lungeTransition);
		TransitionAnimator.animateWithTransition(this, entity.scareAnimationState, HoundenAnimations.scare, ageInTicks, this.scareTransition);
	}
}