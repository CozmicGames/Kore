package com.cozmicgames.utils.extensions

import kotlin.random.Random

fun Random.nextBoolean(probability: Float = 0.5f) = nextFloat() <= probability