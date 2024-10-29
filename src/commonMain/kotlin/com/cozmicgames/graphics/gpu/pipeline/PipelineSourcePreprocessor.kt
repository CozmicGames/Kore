package com.cozmicgames.graphics.gpu.pipeline

import com.cozmicgames.utils.StringTokenizer
import com.cozmicgames.utils.collections.DynamicStack
import com.cozmicgames.utils.extensions.removeBlankLines
import com.cozmicgames.utils.extensions.removeComments

object PipelineSourcePreprocessor {
    private fun tokenize(expression: String): List<String> {
        val tokenizer = StringTokenizer(expression)
        return buildList {
            while (tokenizer.hasMoreTokens) {
                println("A")
                add(tokenizer.nextToken())
            }
        }
    }

    private fun precedence(operator: String): Int {
        return when (operator) {
            "||" -> 1
            "&&" -> 2
            "!" -> 3
            else -> 0
        }
    }

    private fun applyOperator(operator: String, right: Boolean, left: Boolean = false): Boolean {
        return when (operator) {
            "&&" -> left && right
            "||" -> left || right
            "!" -> !right
            else -> throw IllegalArgumentException("Unknown operator $operator")
        }
    }

    private fun isComparison(token: String): Boolean {
        return token.contains("==") || token.contains("!=")
    }

    private fun evaluateExpression(expression: String): Boolean {
        val tokens = tokenize(expression)
        val valuesStack = DynamicStack<Boolean>()
        val operatorsStack = DynamicStack<String>()

        for (token in tokens) {
            when (token) {
                "true" -> valuesStack.push(true)
                "false" -> valuesStack.push(false)
                "&&", "||" -> {
                    while (!operatorsStack.isEmpty && precedence(operatorsStack.peek()) >= precedence(token))
                        valuesStack.push(applyOperator(operatorsStack.pop(), valuesStack.pop(), valuesStack.pop()))

                    operatorsStack.push(token)
                }

                "!" -> operatorsStack.push(token)
                else -> {
                    if (isComparison(token)) {
                        val lhs = token.substringBefore("==").trim()
                        val rhs = token.substringAfter("==").trim()
                        valuesStack.push(lhs == rhs)
                    }
                }
            }
        }

        while (!operatorsStack.isEmpty)
            valuesStack.push(applyOperator(operatorsStack.pop(), valuesStack.pop(), valuesStack.pop()))

        return valuesStack.pop()
    }

    private fun evaluateCondition(condition: String, defines: Map<String, String>): Boolean {
        var processedCondition = condition
        for ((key, value) in defines)
            processedCondition = processedCondition.replace(key, value)

        return evaluateExpression(processedCondition)
    }

    private fun processIncludes(source: String): String {
        val lines = source.removeComments().removeBlankLines().lines()
        return buildString {
            lines.forEach {
                if (it.removeComments().trim().startsWith("#include")) {
                    val i0 = it.indexOfAny(charArrayOf('"', '<')) + 1
                    val i1 = it.indexOfAny(charArrayOf('"', '>'), i0)
                    val name = it.substring(i0, i1)
                    appendLine(processIncludes(PipelineLibrary.getInclude(name)))
                } else
                    appendLine(it)
            }
        }
    }

    fun preprocess(source: String): String {
        val defines = mutableMapOf<String, String>()
        val conditionsStack = mutableListOf<Boolean>()
        var skipBlock = false

        fun updateSkipBlock() {
            skipBlock = conditionsStack.any { !it }
        }

        fun replaceDefines(line: String): String {
            var processedLine = line
            for ((key, value) in defines)
                processedLine = processedLine.replace(key, value)

            return processedLine
        }

        val lines = processIncludes(source).lines()

        return lines.joinToString("\n")

        val output = ArrayList<String>()

        for (line in lines) {
            val trimmedLine = line.trim()

            when {
                trimmedLine.startsWith("#define") -> {
                    val tokens = line.split(" ")
                    if (tokens.size >= 2) {
                        val key = tokens[1]
                        val value = if (tokens.size > 2) tokens.subList(2, tokens.size).joinToString(" ") else ""
                        defines[key] = value
                    }
                }

                trimmedLine.startsWith("#undef") -> {
                    val tokens = tokenize(line)
                    if (tokens.size >= 2) {
                        val key = tokens[1]
                        defines.remove(key)
                    }
                }

                trimmedLine.startsWith("#ifdef") -> {
                    val tokens = tokenize(line)
                    if (tokens.size == 2) {
                        val key = tokens[1]
                        val conditionResult = defines.containsKey(key)
                        conditionsStack.add(conditionResult)
                        updateSkipBlock()
                    }
                }

                trimmedLine.startsWith("#ifndef") -> {
                    val tokens = tokenize(line)
                    if (tokens.size == 2) {
                        val key = tokens[1]
                        val conditionResult = !defines.containsKey(key)
                        conditionsStack.add(conditionResult)
                        updateSkipBlock()
                    }
                }

                trimmedLine.startsWith("#if") -> {
                    val condition = line.removePrefix("#if").trim()
                    val result = evaluateCondition(condition, defines)
                    conditionsStack.add(result)
                    updateSkipBlock()
                }

                trimmedLine.startsWith("#else") -> {
                    if (conditionsStack.isNotEmpty()) {
                        val topCondition = conditionsStack.removeAt(conditionsStack.size - 1)
                        conditionsStack.add(!topCondition)
                        updateSkipBlock()
                    }
                }

                trimmedLine.startsWith("#elif") -> {
                    if (conditionsStack.isNotEmpty()) {
                        val previousCondition = conditionsStack.removeAt(conditionsStack.size - 1)
                        if (!previousCondition) {
                            val condition = line.removePrefix("#elif").trim()
                            val result = evaluateCondition(condition, defines)
                            conditionsStack.add(result)
                        } else
                            conditionsStack.add(false)
                        updateSkipBlock()
                    }
                }

                trimmedLine.startsWith("#endif") -> {
                    if (conditionsStack.isNotEmpty()) {
                        conditionsStack.removeAt(conditionsStack.size - 1)
                        updateSkipBlock()
                    }
                }

                trimmedLine.startsWith("#error") -> {
                    if (skipBlock)
                        continue

                    val message = line.removePrefix("#error").trim()
                    throw IllegalStateException("Preprocessor error: $message")
                }

                else -> {
                    if (!skipBlock)
                        output.add(replaceDefines(line))
                }
            }
        }

        return output.joinToString("\n")
    }
}