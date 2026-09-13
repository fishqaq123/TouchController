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

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        String text = "再次点击以退出";

        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        int centerX = (int) ContainerExitHint.getX();
        int centerY = (int) ContainerExitHint.getY();

        // 先不旋转/缩放,直接以鼠标位置为左上角画白字(验证渲染通路)
        graphics.text(font, text, centerX, centerY, 0xFFFFFF, true);
    }
}
