package com.gratedgames.utils.extensions

import kotlin.random.Random

inline fun Random.nextBoolean(probability: Float = 0.5f) = nextFloat() <= probability