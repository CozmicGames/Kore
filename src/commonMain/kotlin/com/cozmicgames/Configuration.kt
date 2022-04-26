package com.cozmicgames

import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.boolean
import com.cozmicgames.utils.int
import com.cozmicgames.utils.string

class Configuration : Properties() {
    /**
     * The width of the client area.
     */
    var width by int { 800 }

    /**
     * The height of the client area.
     */
    var height by int { 600 }

    /**
     * The title of the application.
     */
    var title by string { "Kore Application" }

    /**
     * Whether the application should be fullscreen.
     */
    var fullscreen by boolean { false }

    /**
     * Whether the application should be run in VSync mode.
     */
    var vsync by boolean { false }

    /**
     * The target frame rate of the application.
     */
    var framerate by int { 60 }

    /**
     * Whether the application should be run in debug mode.
     */
    var debug by boolean { false }
}

/**
 * Creates a new [Configuration] instance.
 *
 * @param block The block to configure the [Configuration] instance.
 *
 * @return The new [Configuration] instance.
 */
fun configuration(block: Configuration.() -> Unit): Configuration {
    val configuration = Configuration()
    block(configuration)
    return configuration
}
