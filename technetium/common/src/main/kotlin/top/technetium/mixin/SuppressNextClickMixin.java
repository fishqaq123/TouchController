/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.technetium.ui.ContainerExitHint;

/**
 * 抑制「双击退出容器」之后的紧接着一次左键事件。
 *
 * 原因:双击蒙版空白处退出容器后,如果鼠标那里正好是箱子/可交互方块,
 * 这一次点击会穿透到游戏世界 → 立刻又把箱子打开。故退出时打标记,在这里吃掉那一次左键。
 */
@Mixin(MouseHandler.class)
public abstract class SuppressNextClickMixin {
    /** 诊断日志(定位后移除)。 */
    @org.spongepowered.asm.mixin.Unique
    private static final org.slf4j.Logger TECHNETIUM_LOGGER =
            org.slf4j.LoggerFactory.getLogger("Technetium");

    @Inject(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", at = @At("HEAD"), cancellable = true)
    private void technetium$suppressClick(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        TECHNETIUM_LOGGER.info("[TC-SDIAG] onButton btn={} action={} suppress={}",
                buttonInfo.button(), action, ContainerExitHint.peekSuppress());
        // 只抑制左键
        if (buttonInfo.button() != 0) {
            return;
        }
        if (ContainerExitHint.consumeSuppressClick()) {
            TECHNETIUM_LOGGER.info("[TC-SDIAG] suppressed left click (action={})", action);
            ci.cancel();
        }
    }
}
