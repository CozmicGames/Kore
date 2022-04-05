package com.cozmicgames.input

interface Key {
    companion object {
        fun of(c: Char) = when (c.lowercaseChar()) {
            ' ' -> Keys.KEY_SPACE
            '.' -> Keys.KEY_PERIOD
            ',' -> Keys.KEY_COMMA
            '-' -> Keys.KEY_MINUS
            '0' -> Keys.KEY_0
            '1' -> Keys.KEY_1
            '2' -> Keys.KEY_2
            '3' -> Keys.KEY_3
            '4' -> Keys.KEY_4
            '5' -> Keys.KEY_5
            '6' -> Keys.KEY_6
            '7' -> Keys.KEY_7
            '8' -> Keys.KEY_8
            '9' -> Keys.KEY_9
            'a' -> Keys.KEY_A
            'b' -> Keys.KEY_B
            'c' -> Keys.KEY_C
            'd' -> Keys.KEY_D
            'e' -> Keys.KEY_E
            'f' -> Keys.KEY_F
            'g' -> Keys.KEY_G
            'h' -> Keys.KEY_H
            'i' -> Keys.KEY_I
            'j' -> Keys.KEY_J
            'k' -> Keys.KEY_K
            'l' -> Keys.KEY_L
            'm' -> Keys.KEY_M
            'n' -> Keys.KEY_N
            'o' -> Keys.KEY_O
            'p' -> Keys.KEY_P
            'q' -> Keys.KEY_Q
            'r' -> Keys.KEY_R
            's' -> Keys.KEY_S
            't' -> Keys.KEY_T
            'u' -> Keys.KEY_U
            'v' -> Keys.KEY_V
            'w' -> Keys.KEY_W
            'x' -> Keys.KEY_X
            'y' -> Keys.KEY_Y
            'z' -> Keys.KEY_Z
            else -> null
        }
    }

    val ordinal: Int
    val isDisplayable: Boolean
    val char: Char
    val hasUpper: Boolean
}

enum class Keys(override val isDisplayable: Boolean = false, override val char: Char = '?', override val hasUpper: Boolean = false) : Key {
    KEY_ENTER,
    KEY_BACKSPACE,
    KEY_TAB,
    KEY_SHIFT,
    KEY_CONTROL,
    KEY_ALT,
    KEY_PAUSE,
    KEY_CAPSLOCK,
    KEY_ESCAPE,
    KEY_SPACE(true, ' '),
    KEY_PAGE_UP,
    KEY_PAGE_DOWN,
    KEY_END,
    KEY_HOME,
    KEY_LEFT,
    KEY_UP,
    KEY_RIGHT,
    KEY_DOWN,
    KEY_COMMA(true, ','),
    KEY_MINUS(true, '-'),
    KEY_PERIOD(true, '.'),
    KEY_0(true, '0'),
    KEY_1(true, '1'),
    KEY_2(true, '2'),
    KEY_3(true, '3'),
    KEY_4(true, '4'),
    KEY_5(true, '5'),
    KEY_6(true, '6'),
    KEY_7(true, '7'),
    KEY_8(true, '8'),
    KEY_9(true, '9'),
    KEY_SEMICOLON(true, ';'),
    KEY_A(true, 'a', true),
    KEY_B(true, 'b', true),
    KEY_C(true, 'c', true),
    KEY_D(true, 'd', true),
    KEY_E(true, 'e', true),
    KEY_F(true, 'f', true),
    KEY_G(true, 'g', true),
    KEY_H(true, 'h', true),
    KEY_I(true, 'i', true),
    KEY_J(true, 'j', true),
    KEY_K(true, 'k', true),
    KEY_L(true, 'l', true),
    KEY_M(true, 'm', true),
    KEY_N(true, 'n', true),
    KEY_O(true, 'o', true),
    KEY_P(true, 'p', true),
    KEY_Q(true, 'q', true),
    KEY_R(true, 'r', true),
    KEY_S(true, 's', true),
    KEY_T(true, 't', true),
    KEY_U(true, 'u', true),
    KEY_V(true, 'v', true),
    KEY_W(true, 'w', true),
    KEY_X(true, 'x', true),
    KEY_Y(true, 'y', true),
    KEY_Z(true, 'z', true),
    KEY_DELETE,
    KEY_F1,
    KEY_F2,
    KEY_F3,
    KEY_F4,
    KEY_F5,
    KEY_F6,
    KEY_F7,
    KEY_F8,
    KEY_F9,
    KEY_F10,
    KEY_F11,
    KEY_F12;

    companion object {
        val count = values().size
    }
}