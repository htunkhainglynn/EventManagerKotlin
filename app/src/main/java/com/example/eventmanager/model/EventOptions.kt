package com.example.eventmanager.model

enum class EventType(val label: String) {
    Conference("Conference"),
    Workshop("Workshop"),
    Seminar("Seminar"),
    Social("Social"),
    Other("Other")
}

enum class EventStatus(val label: String) {
    Upcoming("Upcoming"),
    Completed("Completed"),
    Cancelled("Cancelled")
}

enum class SessionType(val label: String) {
    Workshop("Workshop"),
    Presentation("Presentation"),
    PanelDiscussion("Panel Discussion"),
    Networking("Networking"),
    Keynote("Keynote"),
    Others("Others")
}
