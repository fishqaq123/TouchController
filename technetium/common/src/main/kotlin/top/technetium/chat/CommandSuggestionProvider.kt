/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientSuggestionProvider

/**
 * 命令补全提供者 - 通过反射调用 MC 原版 Brigadier 命令补全 API。
 *
 * 由于 Brigadier 库不在 Technetium 的编译类路径中（TC 不提供 brigadier 的 maven 依赖），
 * 这里使用反射避免直接引用 com.mojang.brigadier.* 类型。
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
        return getSuggestionEntries(input).map { it.text }
    }

    /**
     * 获取带替换范围的建议。
     * 返回 (建议文本, 替换起始位置, 替换结束位置) 三元组。
     */
    fun getSuggestionEntries(input: String): List<SuggestionEntry> {
        if (input.isEmpty()) return emptyList()
        
        val client = Minecraft.getInstance()
        val connection = client.connection ?: return emptyList()
        
        // 创建 ClientSuggestionProvider 作为命令上下文
        // MC 26.2 需要三个参数
        val source = ClientSuggestionProvider(connection, client, 2)
        
        try {
            // 获取 dispatcher（通过反射，避免编译期依赖 Brigadier 类型）
            val commandsField = connection.javaClass.getMethod("getCommands")
            val dispatcher = commandsField.invoke(connection)
            
            // 通过反射调用 parse
            val dispatcherClass = dispatcher.javaClass
            val parseMethod = dispatcherClass.getMethod("parse", String::class.java, source.javaClass)
            val parse = parseMethod.invoke(dispatcher, input, source)
            
            // 通过反射调用 getCompletionSuggestions
            val suggestionsMethod = dispatcherClass.getMethod("getCompletionSuggestions", parse.javaClass)
            val suggestionsFuture = suggestionsMethod.invoke(dispatcher, parse) as java.util.concurrent.CompletableFuture<*>
            val suggestions = suggestionsFuture.join()
            
            // 通过反射获取 suggestions.range（用于替换范围）
            val rangeMethod = suggestions.javaClass.getMethod("getRange")
            val range = rangeMethod.invoke(suggestions)
            val rangeStart = range.javaClass.getMethod("getStart").invoke(range) as Int
            val rangeEnd = range.javaClass.getMethod("getEnd").invoke(range) as Int
            
            // 通过反射获取 suggestions.list（建议列表）
            val listMethod = suggestions.javaClass.getMethod("getList")
            val list = listMethod.invoke(suggestions) as List<*>
            
            // 提取建议文本
            return list.map { obj ->
                val textMethod = obj.javaClass.getMethod("getText")
                val text = textMethod.invoke(obj) as String
                SuggestionEntry(
                    text = text,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                )
            }
        } catch (e: Exception) {
            return emptyList()
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