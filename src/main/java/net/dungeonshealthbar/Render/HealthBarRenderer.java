package net.dungeonshealthbar.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;

public class HealthBarRenderer {

    private static HealthBarRenderer instance;
    private final EntityRenderDispatcher dispatcher;
    private final Tessellator tessellator;
    private final BufferBuilder buffer;
    private final LightmapTextureManager lightManager;

    public static HealthBarRenderer getInstance(){
        if (instance == null) {
            instance = new HealthBarRenderer();
        }
        return instance;
    }

    private HealthBarRenderer(){
        MinecraftClient client = MinecraftClient.getInstance();

        this.dispatcher = client.getEntityRenderDispatcher();
        this.tessellator = Tessellator.getInstance();
        this.buffer = tessellator.getBuffer();
        this.lightManager = client.gameRenderer.getLightmapTextureManager();

    }

    public <T extends LivingEntity> void RenderHealthBar(T livingEntity, MatrixStack matrixStack, int light) {

        if(!shouldRender(livingEntity)) return;

        float yPosition = livingEntity.getHeight() + 0.3f;

        matrixStack.push();
        matrixStack.translate(0, yPosition, 0);
        matrixStack.multiply(dispatcher.getRotation());
        matrixStack.scale(-0.025f, 0.025f, 0.025f);
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        matrixStack.pop();

        Color friendlyFill = new Color(0, 255, 128,255);
        Color hostileFill = new Color(209, 63, 71, 255);

        Color fillColor;

        if (livingEntity instanceof HostileEntity) {
            fillColor = hostileFill;
        } else {
            fillColor = friendlyFill;
        }

        Color backgroundColor = fillColor.darker().darker().darker().darker();

        int width = 25;
        int height = 2;

        float fillAmount = livingEntity.getHealth() / livingEntity.getMaxHealth();

        renderBackground(positionMatrix, backgroundColor,  width, height, fillAmount, light);
        renderFill(positionMatrix, fillColor, width, height, fillAmount, light);

    }

    private boolean shouldRender(LivingEntity entity){
        double maxDistance = 16 * 16;
        double distance = dispatcher.getSquaredDistanceToCamera(entity);

        if (distance > maxDistance) return false;

        if (entity.getHealth() == entity.getMaxHealth()) return false;
        if (entity.getHealth() == 0) return false;

        return true;
    }

    private void renderBackground(Matrix4f positionMatrix, Color color, int width, int height, float fillAmount, int light) {
        drawRectangle(positionMatrix, width, height, fillAmount, 1f, color, light);
    }

    private void renderFill(Matrix4f positionMatrix, Color color, int width, int height, float fillAmount, int light) {
        drawRectangle(positionMatrix, width, height, 0f, fillAmount, color, light);
    }

    private void drawRectangle(Matrix4f positionMatrix, int width, int height, float minXOffset, float maxXOffset, Color color, int light){

        light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        float xOffset = width / 2f;
        float yOffset = height / 2f;

        float minX = minXOffset * width - xOffset;
        float maxX = maxXOffset * width - xOffset;
        float minY = -yOffset;
        float maxY = height - yOffset;

        float r = color.getRed()/255f;
        float g = color.getGreen()/255f;
        float b = color.getBlue()/255f;
        float a = color.getAlpha()/255f;

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_LIGHT);
        buffer.vertex(positionMatrix, minX, minY, 0f).color(r, g, b, a).light(light).next();
        buffer.vertex(positionMatrix, maxX, minY, 0f).color(r, g, b, a).light(light).next();
        buffer.vertex(positionMatrix, maxX, maxY, 0f).color(r, g, b, a).light(light).next();
        buffer.vertex(positionMatrix, minX, maxY, 0f).color(r, g, b, a).light(light).next();

        RenderSystem.setShader(GameRenderer::getPositionColorLightmapShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        lightManager.enable();

        tessellator.draw();

        RenderSystem.disableDepthTest();
        lightManager.disable();

    }


}
