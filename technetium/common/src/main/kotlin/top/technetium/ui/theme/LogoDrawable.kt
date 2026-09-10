/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui.theme

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
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
 * combine 的 BlitRenderState/submitElement/GuiElementUtil 是 internal/private,跨 module 用不了,
 * 故在此复刻一份最小实现(照 combine 的 TextureImpl.BlitRenderState)。
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
                u0 = 0f,
                u1 = 1f,
                v0 = 0f,
                v1 = 1f,
                color = tint.value,
                screenRectangle = guiGraphics.peekScissorStackSafe(),
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

/** 计算 bounds(照 combine 的 GuiElementUtil.getBounds)。 */
private fun computeBounds(
    x0: Float, y0: Float, x1: Float, y1: Float,
    pose: Matrix3x2f, screenRectangle: ScreenRectangle?,
): ScreenRectangle {
    val translated = ScreenRectangle(
        x0.toInt(),
        y0.toInt(),
        ceil(x1 - x0).toInt(),
        ceil(y1 - y0).toInt(),
    ).transformMaxBounds(pose)
    return screenRectangle?.intersection(translated) ?: translated
}

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
        screenRectangle: ScreenRectangle?,
    ) : this(
        pipeline = pipeline,
        textureSetup = textureSetup,
        pose = pose,
        x0 = x0,
        y0 = y0,
        x1 = x1,
        y1 = y1,
        u0 = u0,
        u1 = u1,
        v0 = v0,
        v1 = v1,
        color = color,
        scissorArea = screenRectangle,
        bounds = computeBounds(x0, y0, x1, y1, pose, screenRectangle),
    )

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(color)
    }

    override fun pipeline(): RenderPipeline = pipeline
    override fun textureSetup(): TextureSetup = textureSetup
    override fun scissorArea(): ScreenRectangle? = scissorArea
    override fun bounds(): ScreenRectangle? = bounds
}
