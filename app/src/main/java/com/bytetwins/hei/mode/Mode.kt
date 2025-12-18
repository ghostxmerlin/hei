package com.bytetwins.hei.mode

enum class HeiMode(val id: String) {
    CREATE("create"),
    EMPATHY("empathy"),
    EXECUTE("execute"),
    LOGIC("logic");

    companion object {
        fun fromId(id: String?): HeiMode = when (id) {
            CREATE.id -> CREATE
            EMPATHY.id -> EMPATHY
            EXECUTE.id -> EXECUTE
            LOGIC.id -> LOGIC
            else -> LOGIC
        }
    }
}
