import com.cozmicgames.core.Application
import com.cozmicgames.core.ApplicationInfo
import com.cozmicgames.core.DesktopPlatform
import com.cozmicgames.core.Kore
import com.cozmicgames.core.configuration
import com.cozmicgames.core.graphics
import com.cozmicgames.core.graphics.Primitive
import com.cozmicgames.core.graphics.rhi.GPUDevice
import com.cozmicgames.core.graphics.rhi.GPUGraphicsPipeline
import com.cozmicgames.core.graphics.rhi.GPUStaticBuffer
import com.cozmicgames.core.memory.Memory
import com.cozmicgames.core.memory.of
import com.cozmicgames.core.memory.usedMemory
import com.cozmicgames.core.memoryAccess
import com.cozmicgames.core.utils.Color

fun main() {
    Kore.start(object : Application {
        override val info = ApplicationInfo {
            applicationName = ""
        }

        private var t = 0.0f

        private lateinit var d: GPUDevice
        private lateinit var p: GPUGraphicsPipeline
        private lateinit var b: GPUStaticBuffer

        override fun onCreate() {
            d = Kore.graphics.createDevice(true)
            Kore.graphics.setActiveDevice(d)

            d.debugHandler = object : GPUDevice.DebugHandler {
                override fun onDebugMessage(severity: GPUDevice.DebugHandler.Severity, message: String) {
                    println("[$severity] $message")
                }
            }

            val shader = """
                #section vertex
                
                layout(binding = 0) buffer Data {
                    vec4 data[];
                };
                
                void main() {
                    vec4 value = data[gl_VertexIndex];
                    gl_Position = vec4(value.x, value.y, value.z, 1.0);
                }
                
                #section fragment
                
                layout(location = 0) out vec4 outColor;
                
                void main() {
                    outColor = vec4(1.0, 0.0, 0.0, 1.0);
                }
                
            """.trimIndent()

            val sc = Kore.graphics.createShaderCompiler(true)
            val ss = sc.compile(shader).getOrThrow()
            sc.dispose()

            val s = d.createShader(ss)
            p = d.createGraphicsPipeline {
                setShader(s)
                setCullState {
                    front = false
                    back = false
                }
            }

            val m = Memory.of(
                -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.0f, 1.0f,
                0.0f, 0.5f, 0.0f, 1.0f
            )

            b = d.createStaticBuffer()
            b.setSize(m.size)
            b.setData(m)

            s.resources.forEach {
                println("${it.name} - ${it.type} - ${it.binding}")
            }
        }

        override fun onFrame(delta: Float) {
            t += delta

            if (t >= 1.0f) {
                //println(Kore.graphics.statistics.averageFramesPerSecond)
                println(Kore.memoryAccess.usedMemory)
                t = 0.0f
            }

            val cmd = d.beginFrame()
            cmd.beginMainRenderPass(Color.LIME, 1.0f)
            cmd.setViewport(0, 0, Kore.graphics.width, Kore.graphics.height)
            cmd.setPipeline(p)
            cmd.setBuffer(p.shader.resources.find { it.name == "Data" }!!.binding, b)
            cmd.draw(Primitive.TRIANGLES, 3)

            cmd.endMainRenderPass()
            d.endFrame()
        }
    }, configuration {
        framerate = 0
        vsync = false
        debug = true
    }) {
        DesktopPlatform()
    }
}