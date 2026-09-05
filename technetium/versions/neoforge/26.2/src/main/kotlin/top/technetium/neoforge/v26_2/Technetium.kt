/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.neoforge.v26_2

import net.minecraft.client.Minecraft
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent
import org.slf4j.LoggerFactory

/**
 * Technetium — NeoForge 26.2 客户端入口。
 *
 * 结构参考 TouchController 的 neoforge 入口:
 *   touchcontroller/versions/neoforge/26.2/TouchController.kt
 *
 * 阶段:本类是「Fabric → NeoForge」过渡的第一个构建点。
 *  - 与 Fabric 版共用 common/(仿基岩菜单 UI + Mixin 重定向)。
 *  - only loader 壳不同:这里用 NeoForge 的 @Mod + @EventBusSubscriber。
 *
 * 注意:modid 需与 resources/neoforge.mods.toml 里的 [[mods]].modId 一致(technetium)。
 */
@Mod("technetium")
@EventBusSubscriber(modid = "technetium")
class Technetium(modEventBus: IEventBus) {
    private val logger = LoggerFactory.getLogger(Technetium::class.java)

    init {
        modEventBus.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        logger.info("Technetium (NeoForge) loaded")
        // 核心功能(仿基岩菜单 + Mixin 重定向)由 common 层的 Mixin 完成,此处只需初始化兜底。
    }

    companion object {
        @JvmStatic
        @SubscribeEvent
        private fun onClientStarted(event: ClientStartedEvent) {
            val client = Minecraft.getInstance()
            // 预留:游戏启动后的初始化挂点。
        }
    }
}