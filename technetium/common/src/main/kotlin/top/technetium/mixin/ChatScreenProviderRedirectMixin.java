/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.technetium.chat.screen.TechnetiumChatScreen;

/**
 * 重定向 TouchController 的 ChatScreenProvider，将聊天界面替换为 Technetium 版本。
 *
 * 目标：ChatScreenProviderFactoryImpl.openChatScreen()
 * 当用户点击 TC 的聊天按钮时，打开我们的 TechnetiumChatScreen（带命令补全 + Tab 虚拟按键），
 * 而不是 TC 的原版 ChatScreen。
 */
@Mixin(targets = "top.fifthlight.touchcontroller.common.ui.chat.screen.ChatScreenProviderFactoryImpl")
public abstract class ChatScreenProviderRedirectMixin {
    
    @Inject(method = "openChatScreen", at = @At("HEAD"), cancellable = true)
    private void technetium$openChatScreen(CallbackInfo ci) {
        // 打开我们的 Technetium 聊天界面
        Minecraft client = Minecraft.getInstance();
        TechnetiumChatScreen.openFor(client);
        ci.cancel(); // 阻止原 TC ChatScreen 打开
    }
}