/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.technetium.ui.ContainerExitHint;

/**
 * 渲染「再次点击以退出」提示:显示在鼠标点击位置(非弹窗),以该点为中心随机旋转约 15°,
 * 字体小,3 秒后消失。
 *
 * 挂载点:AbstractContainerScreen.extractRenderState(GuiGraphicsExtractor, int, int, float)。 
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerExitHintRendererMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("TAIL")
    )
    private void technetium$renderExitHint(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci
    ) {
        if (!ContainerExitHint.isVisible()) {
            return;
        }

        // === 临时诊断日志(定位问题后删除) ===
        System.out.println("[TC-DIAG] renderExitHint called: x=" + ContainerExitHint.getX()
                + " y=" + ContainerExitHint.getY()
                + " rot=" + ContainerExitHint.getRotationDeg());

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        String text = "再次点击以退出";

        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        float centerX = (float) ContainerExitHint.getX();
        float centerY = (float) ContainerExitHint.getY();
        float rotationRad = (float) Math.toRadians(ContainerExitHint.getRotationDeg());

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY);
        pose.rotate(rotationRad);
        pose.scale(0.75f, 0.75f);  // 字体小
        graphics.text(font, text, -textWidth / 2, -textHeight / 2, 0xFFFFFF, true);
        pose.popMatrix();
    }
}
