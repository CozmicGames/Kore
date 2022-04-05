package com.cozmicgames

import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.boolean
import com.cozmicgames.utils.int
import com.cozmicgames.utils.string

class Configuration : Properties() {
    var width by int { 800 }
    var height by int { 600 }
    var title by string { "Kore Application" }
    var fullscreen by boolean { false }
    var vsync by boolean { false }
    var framerate by int { 60 }
    var debug by boolean { false }
}

fun configuration(block: Configuration.() -> Unit): Configuration {
    val configuration = Configuration()
    block(configuration)
    return configuration
}
