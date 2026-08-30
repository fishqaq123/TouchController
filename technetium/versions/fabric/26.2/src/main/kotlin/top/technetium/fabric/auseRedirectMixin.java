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
import top.technetium.ui.BedrockMenu;

/**
 * 重定向 TouchController 顶栏「返回主菜单」(polling) 按钮的动作。
 *
 * 目标与依据(已从 TouchController 源码核实):
 * - 顶栏 pause 按钮(BuiltinWidgets.pause)触发 WidgetTriggerAction.Game(GameActions.gameMenu)
 *   → GameAction::openGameMenu → GameActionImpl.openGameMenu() → client.pauseGame(false)(原版 Esc)。
 * -「openGameMenu」仅被触摸控件(BuiltinWidgets.kt / ConfigProperties.kt)调用,不会被实体键盘 Esc
 *   触发(MC 的 Esc 直接调 Minecraft.pauseGame,不走这里)。
 * 因此在此重定向,恰好实现「双轨互斥」:
 *   - 按 TC 顶栏按钮  → 打开我们的仿基岩主菜单;
 *   - 虚拟键盘按 Esc  → 仍进原版暂停菜单(不受影响)。
 *
 * 注意:GameActionImpl 是 Kotlin object(实现接口 GameAction),Mixin 目标类名
 *       top.fifthlight.touchcontroller.gal.action.v26_2.GameActionImpl。
 *       Kotlin object 可能被编译成 INSTANCE 单例;若 @Inject 到普通方法不生效,
 *       可改用 @Mixin(interface GameAction) 的 @Redirect openGameMenu,或用 @Overwrite。
 *       以下先用 @Inject(HEAD, cancellable)+手动打开仿基岩菜单 + 取消原实现。
 */
@Mixin(GameActionImpl.class)
public abstract class BuiltinWidgetsPauseRedirectMixin {
    @Inject(method = "openGameMenu", at = @At("HEAD"), cancellable = true)
    private void technetium$onPauseButton(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        // 打开我们的仿基岩主菜单(combine 渲染)。
        // 用一个布尔开关防止递归:mixin 目标不是 openGameMenu 自身,不会递归。
        BedrockMenu.openFor(client);
        ci.cancel(); // 阻止原版 pauseGame(false) —— 这样按钮不再进原版暂停菜单
    }
}
