package com.cozmicgames.graphics.gpu.pipeline

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.Files
import com.cozmicgames.files.readToString
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.concurrency.concurrentHashMapOf
import com.cozmicgames.utils.extensions.removeBlankLines
import com.cozmicgames.utils.extensions.removeComments

object ProgramLibrary {
    private const val STANDARD = """
        #ifndef __STANDARD_SHADER_LIBRARY_INCLUDED
        #define __STANDARD_SHADER_LIBRARY_INCLUDED
        
        #ifndef PI
            #define PI 3.14159265358979323846264
        #endif
        
        #ifndef E
            #define E 2.718281828459045235360287
        #endif
        
        float saturate(float value) {
            return clamp(value, 0.0, 1.0);
        }
        
        vec2 saturate(vec2 value) {
            return clamp(value, vec2(0.0), vec2(1.0));
        }
        
        vec3 saturate(vec3 value) {
            return clamp(value, vec3(0.0), vec3(1.0));
        }
        
        vec4 saturate(vec4 value) {
            return clamp(value, vec4(0.0), vec4(1.0));
        }
        
        float map(float value, float sourceMin, float sourceMax, float destMin, float destMax) {
            float amount = (saturate(value) - sourceMin) / (sourceMax - sourceMin);
            return destMin + amount * destMax;
        }
        
        vec2 map(vec2 value, vec2 sourceMin, vec2 sourceMax, vec2 destMin, vec2 destMax) {
            vec2 amount = (saturate(value) - sourceMin) / (sourceMax - sourceMin);
            return destMin + amount * destMax;
        }
        
        vec3 map(vec3 value, vec3 sourceMin, vec3 sourceMax, vec3 destMin, vec3 destMax) {
            vec3 amount = (saturate(value) - sourceMin) / (sourceMax - sourceMin);
            return destMin + amount * destMax;
        }
        
        vec4 map(vec4 value, vec4 sourceMin, vec4 sourceMax, vec4 destMin, vec4 destMax) {
            vec4 amount = (saturate(value) - sourceMin) / (sourceMax - sourceMin);
            return destMin + amount * destMax;
        }
        
        int randomInt(int seed) {
            seed = (seed << 13) ^ seed;
            return (seed * (seed * seed * 15731 + 789221) + 1376312589) & 0x7fffffff;
        }
        
        float randomFloat(int seed) {
            return float(randomInt(seed));
        }
        
        int mask(int value) {
            return (1 << value) - 1;
        }
        
        void packInt(inout int data, int value, int offset, int size) {
            int mask = mask(size);
            int clear = data & ~(mask << offset);
            data = clear | ((value & mask) << offset);
        }
        
        int unpackInt(int data, int offset, int size) {
            return (data >> offset) & mask(size);
        }
        
        ivec2 unpackIVec2(int data, int offset, int size) {
            ivec2 result;
            result.x = unpackInt(data, offset, size);
            result.y = unpackInt(data, offset + size, size);
            return result;
        }
        
        ivec3 unpackIVec3(int data, int offset, int size) {
            ivec3 result;
            result.x = unpackInt(data, offset, size);
            result.y = unpackInt(data, offset + size, size);
            result.z = unpackInt(data, offset + size * 2, size);
            return result;
        }
        
        ivec4 unpackIVec4(int data, int offset, int size) {
            ivec4 result;
            result.x = unpackInt(data, offset, size);
            result.y = unpackInt(data, offset + size, size);
            result.z = unpackInt(data, offset + size * 2, size);
            result.w = unpackInt(data, offset + size * 3, size);
            return result;
        }
        
        float unpackFloat(int data, int offset, int size) {
            return float(unpackInt(data, offset, size));
        }
        
        vec2 unpackVec2(int data, int offset, int size) {
            vec2 result;
            result.x = unpackFloat(data, offset, size);
            result.y = unpackFloat(data, offset + size, size);
            return result;
        }
        
        vec3 unpackVec3(int data, int offset, int size) {
            vec3 result;
            result.x = unpackFloat(data, offset, size);
            result.y = unpackFloat(data, offset + size, size);
            result.z = unpackFloat(data, offset + size * 2, size);
            return result;
        }
        
        vec4 unpackVec4(int data, int offset, int size) {
            vec4 result;
            result.x = unpackFloat(data, offset, size);
            result.y = unpackFloat(data, offset + size, size);
            result.z = unpackFloat(data, offset + size * 2, size);
            result.w = unpackFloat(data, offset + size * 3, size);
            return result;
        }
        
        bool isOdd(int value) {
            return (value % 2) == 1;
        }
        
        bool isEven(int value) {
            return (value % 2) == 0;
        }
        
        bool isPowerOfTwo(int value) {
            return value != 0 && (value & (value - 1)) == 0;
        }
    
        int nextPowerOfTwo(int value) {
            value--;
            value |= value << 1;
            value |= value << 2;
            value |= value << 4;
            value |= value << 8;
            value |= value << 16;
            value++;
            return value;
        }
        
        int prevPowerOfTwo(int value) {
            return isPowerOfTwo(value) ? value : nextPowerOfTwo(value) >> 1;
        }
    
        vec2 packSignedVector(vec2 vector) {
            return vector * 0.5 + 0.5;
        }
        
        vec3 packSignedVector(vec3 vector) {
            return vector * 0.5 + 0.5;
        }
        
        vec4 packSignedVector(vec4 vector) {
            return vector * 0.5 + 0.5;
        }
        
        vec2 unpackSignedVector(vec2 vector) {
            return vector * 2.0 - 1.0;
        }
        
        vec3 unpackSignedVector(vec3 vector) {
            return vector * 2.0 - 1.0;
        }
        
        vec4 unpackSignedVector(vec4 vector) {
            return vector * 2.0 - 1.0;
        }

        #endif
    """

    private val includes = concurrentHashMapOf<String, () -> String>()

    init {
        addInclude("standard") { STANDARD }
    }

    fun addInclude(name: String, source: () -> String) {
        includes[name] = source
    }

    fun addInclude(file: String, type: Files.Type, charset: Charset = Charsets.UTF8) {
        addInclude(file) { Kore.files.readToString(file, type, charset) }
    }

    fun getInclude(name: String): String {
        val include = includes[name]
        return if (include == null)
            "#error Unable to locate include: $name"
        else
            include()
    }

    fun process(source: String): String {
        val lines = source.removeComments().removeBlankLines().lines()
        return buildString {
            lines.forEach {
                if (it.removeComments().trim().startsWith("#include")) {
                    val i0 = it.indexOf('"') + 1
                    val i1 = it.indexOf('"', i0)
                    val name = it.substring(i0, i1)
                    appendLine(process(getInclude(name)))
                } else
                    appendLine(it)
            }
        }
    }
}