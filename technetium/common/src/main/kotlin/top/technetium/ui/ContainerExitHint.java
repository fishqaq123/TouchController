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

    private ContainerExitHint() {
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
}
