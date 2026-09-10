/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui.theme

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.render.v26_2.CanvasImpl
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SubmittableGuiGraphics
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import kotlin.math.ceil

/**
 * 绘制 MINECRAFT logo 的自定义 Drawable。
 *
 * 用 LogoTexture 注册的纹理 id,直接提交 BlitRenderState(UV 全图 + 绘制到 dstRect,可缩放)。
 * 全程不用 textureManager.getTexture("technetium:logo/minecraft")(那会 Missing resource)。
 *
 * 说明:combine 的 BlitRenderState/GuiElementUtil/submitElement 是 internal/private,
 * 无法跨 module 使用,故在此自行复刻一份最小实现。
 */
class LogoDrawable : Drawable {
    override val size: IntSize = IntSize(1024, 256)
    override val padding: IntPadding = IntPadding.ZERO

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) {
        val guiGraphics = (canvas as? CanvasImpl)?.guiGraphics ?: return
        val location = LogoTexture.getLocation()
        val texture = Minecraft.getInstance().textureManager.getTexture(location)
        guiGraphics.submitElement(
            BlitState(
                pipeline = RenderPipelines.GUI_TEXTURED,
                textureSetup = TextureSetup.singleTexture(texture.textureView, texture.sampler),
                pose = Matrix3x2f(guiGraphics.pose()),
                x0 = dstRect.offset.x.toFloat(),
                y0 = dstRect.offset.y.toFloat(),
                x1 = (dstRect.offset.x + dstRect.size.width).toFloat(),
                y1 = (dstRect.offset.y + dstRect.size.height).toFloat(),
                // UV 全图(与绘制尺寸分离,故可缩放显示整张图)
                u0 = 0f,
                u1 = 1f,
                v0 = 0f,
                v1 = 1f,
                color = tint.value,
                scissorArea = guiGraphics.peekScissorStackSafe(),
            )
        )
    }
}

/** 复刻 combine 的 GuiGraphicsExtractor.submitElement 扩展。 */
private fun net.minecraft.client.gui.GuiGraphicsExtractor.submitElement(
    guiElementRenderState: GuiElementRenderState,
) = (this as SubmittableGuiGraphics).`combine$addGuiElement`(guiElementRenderState)

/** 复刻 combine 的 GuiGraphicsExtractor.peekScissorStack 扩展。 */
private fun net.minecraft.client.gui.GuiGraphicsExtractor.peekScissorStackSafe(): ScreenRectangle? =
    (this as SubmittableGuiGraphics).`combine$peekScissorStack`()

/** 复刻 combine 的 BlitRenderState(UV 与绘制区域分离,支持缩放)。 */
private data class BlitState(
    val pipeline: RenderPipeline,
    val textureSetup: TextureSetup,
    val pose: Matrix3x2f,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val u0: Float,
    val u1: Float,
    val v0: Float,
    val v1: Float,
    val color: Int,
    val scissorArea: ScreenRectangle?,
    val bounds: ScreenRectangle?,
) : GuiElementRenderState {
    constructor(
        pipeline: RenderPipeline,
        textureSetup: TextureSetup,
        pose: Matrix3x2f,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        u0: Float,
        u1: Float,
        v0: Float,
        v1: Float,
        color: Int,
        scissorArea: ScreenRectangle?,
    ) : this(
        pipeline, textureSetup, pose, x0, y0, x1, y1, u0, u1, v0, v1, color,
        scissorArea,
        bounds = run {
            val translated = ScreenRectangle(
                x0.toInt(),
                y0.toInt(),
                ceil(x1 - x0).toInt(),
                ceil(y1 - y0).toInt(),
            ).transformMaxBounds(pose)
            scissorArea?.intersection(translated) ?: translated
        },
    )

    override fun pipeline(): RenderPipeline = pipeline
    override fun textureSetup(): TextureSetup = textureSetup
    override fun scissorArea(): ScreenRectangle? = scissorArea
    override fun bounds(): ScreenRectangle? = bounds
}
