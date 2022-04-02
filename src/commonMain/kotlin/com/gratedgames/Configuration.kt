package com.gratedgames

import com.gratedgames.utils.Properties
import com.gratedgames.utils.boolean
import com.gratedgames.utils.int
import com.gratedgames.utils.string

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
