package com.fernleaf.hostilities.client.model.daturena;

import com.fernleaf.hostilities.Hostilities;
import com.fernleaf.hostilities.client.animations.DaturenaAnimations;
import com.fernleaf.hostilities.server.entity.daturena.Daturena;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DaturenaModel<T extends Daturena> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hostilities.MODID, "daturena"), "main");

	private final ModelPart root;
	private final ModelPart bone;
	private final ModelPart abdomen;
	private final ModelPart flippers;
	private final ModelPart right_flipper;
	private final ModelPart left_flipper;
	private final ModelPart torso;
	private final ModelPart ribbon;
	private final ModelPart head;
	private final ModelPart face;
	private final ModelPart helmet;
	private final ModelPart right_arm;
	private final ModelPart right_forearm;
	private final ModelPart right_claws;
	private final ModelPart left_arm;
	private final ModelPart left_forearm;
	private final ModelPart left_claws;

	public DaturenaModel(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
		this.abdomen = this.bone.getChild("abdomen");
		this.flippers = this.abdomen.getChild("flippers");
		this.right_flipper = this.flippers.getChild("right_flipper");
		this.left_flipper = this.flippers.getChild("left_flipper");
		this.torso = this.bone.getChild("torso");
		this.ribbon = this.torso.getChild("ribbon");
		this.head = this.torso.getChild("head");
		this.face = this.head.getChild("face");
		this.helmet = this.head.getChild("helmet");
		this.right_arm = this.torso.getChild("right_arm");
		this.right_forearm = this.right_arm.getChild("right_forearm");
		this.right_claws = this.right_forearm.getChild("right_claws");
		this.left_arm = this.torso.getChild("left_arm");
		this.left_forearm = this.left_arm.getChild("left_forearm");
		this.left_claws = this.left_forearm.getChild("left_claws");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 8.0F));

		PartDefinition abdomen = bone.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 0).addBox(-5.75F, 0.5F, -5.0F, 12.0F, 9.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, -9.5F, -11.0F));

		PartDefinition flippers = abdomen.addOrReplaceChild("flippers", CubeListBuilder.create(), PartPose.offset(-0.25F, 7.0F, 25.0F));

		PartDefinition right_flipper = flippers.addOrReplaceChild("right_flipper", CubeListBuilder.create(), PartPose.offset(-9.0F, 1.0F, 3.5F));

		right_flipper.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(49, 68).mirror().addBox(-4.5F, -1.5F, -7.5F, 9.0F, 3.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.4363F, 0.0F));

		PartDefinition left_flipper = flippers.addOrReplaceChild("left_flipper", CubeListBuilder.create(), PartPose.offset(9.0F, 1.0F, 4.5F));

		left_flipper.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 76).mirror().addBox(-4.5F, -1.5F, -7.5F, 9.0F, 3.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.4363F, 0.0F));

		PartDefinition torso = bone.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 42).addBox(-6.0F, -22.0F, -5.0F, 12.0F, 21.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(98, 79).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, -11.0F));

		torso.addOrReplaceChild("ribbon", CubeListBuilder.create().texOffs(110, 28).addBox(-5.0F, 1.0F, 0.0F, 10.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -6.0F));

		PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create().texOffs(110, 11).addBox(-3.0F, -10.0F, -1.0F, 6.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -21.0F, 0.0F));

		head.addOrReplaceChild("face", CubeListBuilder.create().texOffs(49, 42).addBox(-3.0F, -3.0F, -12.5F, 6.0F, 6.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, -0.5F));

		head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(88, 87).addBox(-4.0F, 1.0F, -6.0F, 12.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(89, 0).addBox(-4.0F, 13.0F, 1.0F, 12.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(49, 87).addBox(-4.0F, 1.0F, 1.0F, 12.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(25, 107).addBox(-4.0F, -1.0F, -6.0F, 12.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(89, 11).addBox(2.0F, -14.0F, -2.0F, 0.0F, 15.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -21.0F, 0.0F));

		PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(100, 58).addBox(-5.0F, -2.5F, -3.0F, 5.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -19.5F, 3.0F));

		PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(88, 100).addBox(-19.5F, 1.5F, -3.0F, 5.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(14.5F, 9.0F, 0.0F));

		right_forearm.addOrReplaceChild("right_claws", CubeListBuilder.create().texOffs(25, 95).addBox(-0.5F, 0.0F, -3.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-19.0F, 14.5F, 0.5F));

		PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(100, 37).addBox(0.0F, -2.5F, -3.0F, 5.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -19.5F, 3.0F));

		PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(0, 95).mirror().addBox(14.5F, 1.5F, -3.0F, 5.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-14.5F, 9.0F, 0.0F));

		left_forearm.addOrReplaceChild("left_claws", CubeListBuilder.create().texOffs(25, 95).mirror().addBox(-2.5F, 1.0F, -3.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(19.0F, 13.5F, 0.5F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(Daturena entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.bashAnimationState, DaturenaAnimations.bash, ageInTicks);
		this.animate(entity.threePunchComboAnimationState, DaturenaAnimations.three_combo_punch, ageInTicks);
		this.animate(entity.jabCrossAnimationState, DaturenaAnimations.jab_cross, ageInTicks);
	}
}