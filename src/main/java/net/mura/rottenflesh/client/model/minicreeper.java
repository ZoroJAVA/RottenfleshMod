package net.mura.rottenflesh.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.7 - 1.12
public class minicreeper extends ModelBase {
    private final ModelRenderer corps;
    private final ModelRenderer cube_r1;
    private final ModelRenderer pied1;
    private final ModelRenderer p3;
    private final ModelRenderer p2;
    private final ModelRenderer p4;
    private final ModelRenderer Head;
    private final ModelRenderer cube_r2;

    public minicreeper() {
        textureWidth = 32;
        textureHeight = 32;

        corps = new ModelRenderer(this);
        corps.setRotationPoint(0.0F, 24.0F, 0.0F);


        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, -2.0F, -1.0F);
        corps.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, -1.5708F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 8, 0.0F, -5.0F, -2.0F, 2, 5, 4, 0.0F, false));

        pied1 = new ModelRenderer(this);
        pied1.setRotationPoint(0.0F, 24.0F, 0.0F);
        pied1.cubeList.add(new ModelBox(pied1, 12, 8, -2.0F, -2.0F, 0.0F, 2, 2, 2, 0.0F, false));

        p3 = new ModelRenderer(this);
        p3.setRotationPoint(-1.0F, 24.0F, -1.0F);
        p3.cubeList.add(new ModelBox(p3, 12, 12, 1.0F, -2.0F, 1.0F, 2, 2, 2, 0.0F, false));

        p2 = new ModelRenderer(this);
        p2.setRotationPoint(0.0F, 24.0F, 0.0F);
        p2.cubeList.add(new ModelBox(p2, 16, 0, -2.0F, -2.0F, -2.0F, 2, 2, 2, 0.0F, false));

        p4 = new ModelRenderer(this);
        p4.setRotationPoint(0.0F, 24.0F, 0.0F);
        p4.cubeList.add(new ModelBox(p4, 16, 4, 0.0F, -2.0F, -2.0F, 2, 2, 2, 0.0F, false));

        Head = new ModelRenderer(this);
        Head.setRotationPoint(0.0F, 17.0F, 0.0F);


        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(1.0F, 0.0F, -1.0F);
        Head.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 1.5708F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 0, -3.0F, -4.0F, -3.0F, 4, 4, 4, 0.0F, false));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        // f = limbSwing (avance dans le cycle de marche), f1 = limbSwingAmount (0 = immobile, 1 = marche a fond)
        // Balancement diagonal : deux pattes opposees avancent ensemble, les deux autres reculent en meme temps
        float swing = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
        float swingOpposite = MathHelper.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;

        setRotationAngle(pied1, swing, 0.0F, 0.0F);
        setRotationAngle(p4, swing, 0.0F, 0.0F);
        setRotationAngle(p3, swingOpposite, 0.0F, 0.0F);
        setRotationAngle(p2, swingOpposite, 0.0F, 0.0F);

        corps.render(f5);
        pied1.render(f5);
        p3.render(f5);
        p2.render(f5);
        p4.render(f5);
        Head.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}