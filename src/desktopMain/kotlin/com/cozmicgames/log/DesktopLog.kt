package com.cozmicgames.log

import com.cozmicgames.utils.Reflection
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread
import kotlin.reflect.KClass

class DesktopLog : Log {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SS")

    init {
        val fileOutputStream = FileOutputStream(File("log.txt"), false)
        val originalOutStream = System.out
        val outPrintStream = PrintStream(object : OutputStream() {
            override fun write(b: Int) {
                originalOutStream.write(b)
                fileOutputStream.write(b)
            }
        })

        System.setOut(outPrintStream)
        System.setErr(outPrintStream)

        Runtime.getRuntime().addShutdownHook(thread(false) {
            outPrintStream.close()
        })
    }

    private fun getTimeInfo(): String {
        val date = Date.from(Instant.now())
        return dateFormat.format(date)
    }

    override fun debug(caller: KClass<*>, message: String) {
        val className = Reflection.getClassName(caller)
        val timeInfo = getTimeInfo()
        println("[DEBUG] $className ($timeInfo): $message")
    }

    override fun info(caller: KClass<*>, message: String) {
        val className = Reflection.getClassName(caller)
        val timeInfo = getTimeInfo()
        println("[INFO] $className ($timeInfo): $message")
    }

    override fun error(caller: KClass<*>, message: String) {
        val className = Reflection.getClassName(caller)
        val timeInfo = getTimeInfo()
        println("[ERROR] $className ($timeInfo): $message")
    }

    override fun fail(caller: KClass<*>, message: String) {
        val className = Reflection.getClassName(caller)
        val timeInfo = getTimeInfo()
        println("[FAIL] $className ($timeInfo): $message")
        throw RuntimeException()
    }
}