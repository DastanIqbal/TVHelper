package com.dastanapps.poweroff.common

enum class RemoteEvent {

    // Touch
    MOUSE,
    SINGLE_TAP,
    SCROLL_UP,
    SCROLL_DOWN,
    SCROLL_LEFT,
    SCROLL_RIGHT,

    //Server
    STOP_SERVER,
    PING,
    PONG,

    // DPAD
    DPAD_LEFT,
    DPAD_RIGHT,
    DPAD_TOP,
    DPAD_BOTTOM,
    DPAD_CENTER,

    // Device
    WAKE_UP,
}