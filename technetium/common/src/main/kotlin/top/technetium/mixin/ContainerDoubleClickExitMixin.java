/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.technetium.ui.ContainerExitHint;

/**
 * 容器界面双击「蒙版空白处」退出。
 *
 * 需求:
 *  - 仅对:箱子(含陷阱箱/潜影盒/木桶)、工作台、背包、锻造台、铁砧、村民交易 → 这些是 AbstractContainerScreen;
 *    (命令方块/结构方块是普通 Screen,由另一 Mixin 处理)
 *  - 双击位置必须在容器面板之外(即灰色蒙版区域);
 *  - 鼠标(手上)未选中任何物品(menu.getCarried() 为空);
 *  - 第一次点击 → 在鼠标位置显示「再次点击以退出」提示(3 秒);
 *  - 3 秒内再次点击(同样条件) → 关闭容器界面回到游戏;
 *  - 超过 3 秒 → 点击无效(需重新双击)。
 *
 * 其他模组 UI:若点击被别的元素消费(即它们的处理先返回 true),我们的注入在 HEAD 会先拿到,
 * 因此只在「面板外 + 无物品 + 白名单容器」时才拦截,否则放行,天然不与其它模组 UI 冲突。
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerDoubleClickExitMixin {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow @Final protected AbstractContainerMenu menu;

    @Unique
    private static long technetium$lastClickTime = 0L;
    @Unique
    private static double technetium$lastClickX = 0;
    @Unique
    private static double technetium$lastClickY = 0;

    /** 3 秒窗口(毫秒)。 */
    @Unique
    private static final long TECHNETIUM_DOUBLE_CLICK_WINDOW_MS = 3000L;

    // 注入到 HEAD:判断"面板外空白处 + 手上无物品 + 白名单",满足才拦截(返回 true)。
    // 面板内(含 slot、按钮)一律不拦截 → 原版与其他模组 UI 正常处理。
    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
    private void technetium$onMouseClicked(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        // 只处理左键
        if (event.button() != 0) {
            return;
        }
        double mx = event.x();
        double my = event.y();

        // 1. 必须在容器面板之外(即灰色蒙版区域)
        boolean insidePanel = mx >= leftPos && mx <= leftPos + imageWidth
                && my >= topPos && my <= topPos + imageHeight;
        if (insidePanel) {
            return;
        }

        // 2. 手上(鼠标)不能选中任何物品
        if (!menu.getCarried().isEmpty()) {
            return;
        }

        // 3. 白名单容器(仅这些界面生效)
        if (!technetium$isSupportedContainer()) {
            return;
        }

        // 4. 3 秒窗口内的双击 → 关闭;否则记录并显示提示
        long now = System.currentTimeMillis();
        boolean withinWindow = (now - technetium$lastClickTime) <= TECHNETIUM_DOUBLE_CLICK_WINDOW_MS;
        double dx = Math.abs(mx - technetium$lastClickX);
        double dy = Math.abs(my - technetium$lastClickY);
        boolean nearSameSpot = dx <= 40 && dy <= 40;

        if (withinWindow && nearSameSpot) {
            // 双击成功 → 不直接关容器,而是切到「透明守护层」接住这次点击(避免穿透到世界开箱子),
            // 该层约 0.08 秒后自己关闭回游戏。
            technetium$lastClickTime = 0L;
            ContainerExitHint.hide();
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            client.setScreen(new top.technetium.ui.TransparentGuardScreen(client, client.font));
            cir.setReturnValue(true);
        } else {
            // 第一次点击 → 记录 + 在鼠标处显示提示
            technetium$lastClickTime = now;
            technetium$lastClickX = mx;
            technetium$lastClickY = my;
            ContainerExitHint.show(mx, my);
            cir.setReturnValue(true);
        }
    }

    /** 判断当前容器界面是否在白名单内(仅列出的这些)。 */
    @Unique
    private boolean technetium$isSupportedContainer() {
        Object self = this;
        String name = self.getClass().getName();
        // 箱子/陷阱箱/木桶 → ContainerScreen;潜影盒 → ShulkerBoxScreen;工作台 → CraftingScreen;
        // 背包 → InventoryScreen;铁砧 → AnvilScreen;锻造台 → SmithingScreen;村民交易 → MerchantScreen
        return name.endsWith("ContainerScreen")
                || name.endsWith("ShulkerBoxScreen")
                || name.endsWith("CraftingScreen")
                || name.endsWith("InventoryScreen")
                || name.endsWith("AnvilScreen")
                || name.endsWith("SmithingScreen")
                || name.endsWith("MerchantScreen");
    }
}
