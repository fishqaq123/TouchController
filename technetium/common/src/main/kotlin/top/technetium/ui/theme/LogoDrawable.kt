/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui.theme

import net.minecraft.client.renderer.RenderPipelines
import top.fifthlight.combine.backend.minecraft.render.v26_2.CanvasImpl
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.data.IntRect

/**
 * 绘制 MINECRAFT logo 的自定义 Drawable。
 *
 * 用 LogoTexture 注册的纹理 id,在 combine 的 Canvas 底层 GuiGraphics 上原生 blit。
 * 全程不用 textureManager.getTexture("technetium:logo/minecraft")(那会 Missing resource)。
 */
class LogoDrawable : Drawable {
    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) {
        val guiGraphics = (canvas as? CanvasImpl)?.guiGraphics ?: return
        val location = LogoTexture.getLocation()
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            location,
            dstRect.offset.x,
            dstRect.offset.y,
            0f,
            0f,
            dstRect.size.width,
            dstRect.size.height,
            1024,
            256,
            tint.value,
        )
    }
}
