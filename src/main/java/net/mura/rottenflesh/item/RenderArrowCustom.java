package net.mura.rottenflesh.item;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

public class RenderArrowCustom extends RenderArrow<EntityArrow> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/projectiles/arrow.png");

    public RenderArrowCustom(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityArrow entity) {
        return TEXTURE;
    }
}