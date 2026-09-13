/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui;

/**
 * 「再次点击以退出」提示的状态管理。
 *
 * 提示显示在鼠标点击的位置(不是弹窗),以该点为中心随机向左或右旋转约 15°,字体小,3 秒后消失。
 * 渲染由 ContainerExitHintRendererMixin 读取本状态完成。
 */
public final class ContainerExitHint {
    /** 提示是否可见。 */
    private static boolean visible = false;
    /** 显示位置(鼠标点击处)。 */
    private static double x = 0;
    private static double y = 0;
    /** 过期时间(毫秒时间戳)。 */
    private static long expireAt = 0L;
    /** 随机旋转角度(度,约 -15 或 +15)。 */
    private static float rotationDeg = 0f;

    /** 是否要抑制紧接着的一次鼠标左键事件(退出容器后,避免点到世界里的箱子)。 */
    private static boolean suppressNextClick = false;
    /** 抑制的过期时间(避免标志长期残留)。 */
    private static long suppressExpireAt = 0L;

    private ContainerExitHint() {
    }

    /** 标记:抑制紧接着的一次左键(退出容器后调用)。 */
    public static void suppressNextClick() {
        suppressNextClick = true;
        suppressExpireAt = System.currentTimeMillis() + 500L;
    }

    /** 只读查询:当前标志是否置位(不消费,用于诊断)。 */
    public static boolean peekSuppress() {
        return suppressNextClick && System.currentTimeMillis() <= suppressExpireAt;
    }

    /** 是否应抑制这次左键事件(会消费/清除标志)。 */
    public static boolean consumeSuppressClick() {
        if (!suppressNextClick) {
            return false;
        }
        if (System.currentTimeMillis() > suppressExpireAt) {
            suppressNextClick = false;
            return false;
        }
        suppressNextClick = false;
        return true;
    }

    /** 在指定位置显示提示(随机左/右旋转约 15°,3 秒后过期)。 */
    public static void show(double mouseX, double mouseY) {
        visible = true;
        x = mouseX;
        y = mouseY;
        expireAt = System.currentTimeMillis() + 3000L;
        // 随机向左或向右旋转约 15°
        rotationDeg = (Math.random() < 0.5) ? -15f : 15f;
    }

    /** 隐藏提示。 */
    public static void hide() {
        visible = false;
        expireAt = 0L;
    }

    /** 当前是否应显示(自动处理 3 秒过期)。 */
    public static boolean isVisible() {
        if (!visible) {
            return false;
        }
        if (System.currentTimeMillis() > expireAt) {
            hide();
            return false;
        }
        return true;
    }

    public static double getX() {
        return x;
    }

    public static double getY() {
        return y;
    }

    public static float getRotationDeg() {
        return rotationDeg;
    }

    /**
     * 切到「透明守护层」:接住退出容器时的那次点击(避免穿透到世界又打开箱子),约 0.08 秒后自动关闭。
     *
     * 守护层写成本类的静态内部类(同一编译单元),避免跨文件编译顺序问题。
     */
    public static void openGuardScreen() {
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        client.setScreen(new GuardScreen(client, client.font));
    }

    /** 透明守护层:不画内容(能看见下面的世界),接住点击,2 tick(≈0.1s)后自己关闭。 */
    public static class GuardScreen extends net.minecraft.client.gui.screens.Screen {
        private int technetium$ticks = 0;

        /** 26.2 的 Screen 构造器为 (Minecraft, Font, Component)。 */
        public GuardScreen(
                net.minecraft.client.Minecraft client,
                net.minecraft.client.gui.Font font) {
            super(client, font, net.minecraft.network.chat.Component.literal(""));
        }

        @Override
        protected void init() {
            // 不加任何控件,保持透明
        }

        @Override
        public boolean mouseClicked(
                net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
            // 接住点击,不让它传给世界
            return true;
        }

        @Override
        public void tick() {
            super.tick();
            technetium$ticks++;
            if (technetium$ticks >= 2) {
                this.onClose();
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
