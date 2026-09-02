/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import java.util.concurrent.CompletableFuture

/**
 * 命令补全提供者 - 封装 MC 原版 Brigadier 命令补全 API。
 *
 * 通过 ClientPacketListener 获取命令调度器，使用 ClientSuggestionProvider 提供上下文，
 * 返回当前输入对应的命令建议列表。
 */
object CommandSuggestionProvider {

    /**
     * 获取命令建议。
     *
     * @param input 用户当前输入（可能以 / 开头）
     * @return 建议文本列表（如 ["/tp", "/time"]）
     */
    fun getSuggestions(input: String): List<String> {
        if (input.isEmpty()) return emptyList()
        
        val client = Minecraft.getInstance()
        val connection = client.connection ?: return emptyList()
        
        // 创建 ClientSuggestionProvider 作为命令上下文
        val source = ClientSuggestionProvider(connection, client)
        
        // 获取命令调度器并解析输入
        val dispatcher = connection.commands
        val parse: ParseResults<ClientSuggestionProvider> = dispatcher.parse(input, source)
        
        // 获取建议（Brigadier 返回 CompletableFuture，使用 join 阻塞等待）
        val suggestionsFuture: CompletableFuture<Suggestions> = dispatcher.getCompletionSuggestions(parse)
        val suggestions: Suggestions = suggestionsFuture.join()
        
        // 提取建议文本
        return suggestions.list.map { it.text }
    }

    /**
     * 获取带替换范围的建议。
     * 返回 (建议文本, 替换起始位置, 替换结束位置) 三元组。
     */
    fun getSuggestionEntries(input: String): List<SuggestionEntry> {
        if (input.isEmpty()) return emptyList()
        
        val client = Minecraft.getInstance()
        val connection = client.connection ?: return emptyList()
        
        val source = ClientSuggestionProvider(connection, client)
        val dispatcher = connection.commands
        val parse = dispatcher.parse(input, source)
        
        val suggestionsFuture = dispatcher.getCompletionSuggestions(parse)
        val suggestions = suggestionsFuture.join()
        
        val rangeStart = suggestions.range.start
        val rangeEnd = suggestions.range.end
        
        return suggestions.list.map { suggestion ->
            SuggestionEntry(
                text = suggestion.text,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
            )
        }
    }

    /**
     * 将建议应用到输入文本。
     *
     * @param input 当前输入
     * @param suggestion 建议文本
     * @param rangeStart 替换起始位置
     * @param rangeEnd 替换结束位置
     * @return 应用建议后的完整文本
     */
    fun applySuggestion(input: String, suggestion: String, rangeStart: Int, rangeEnd: Int): String {
        return input.substring(0, rangeStart) + suggestion + input.substring(rangeEnd)
    }
}

/**
 * 带替换范围的命令建议条目。
 */
data class SuggestionEntry(
    val text: String,
    val rangeStart: Int,
    val rangeEnd: Int,
)