/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * 透明守护层:双击退出容器时的"垫底"界面。
 *
 * 作用:双击蒙版空白处退出容器后,如果鼠标那正好是箱子/可交互方块,
 * 点击会穿透到世界 → 立刻又打开箱子。故退出时不直接回游戏,而是切到这个透明层,
 * 由它接住那次点击(不传给世界),约 0.08 秒后自己关闭回游戏。
 */
public class TransparentGuardScreen extends Screen {
    /** 已存在的 tick 数;20 tick/秒 → 2 tick ≈ 0.1s(接近 0.08s)。 */
    private int technetium$ticks = 0;

    public TransparentGuardScreen() {
        super(Component.literal(""));
    }

    /** 不加任何控件,保持透明。 */
    @Override
    protected void init() {
    }

    /** 接住点击,不让它传给世界。 */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
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
