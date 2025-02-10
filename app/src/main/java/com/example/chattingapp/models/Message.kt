package com.example.chattingapp.models

data class Message(
    val content: String,
    val isSender: Boolean
)

enum class MessagesType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO
}
