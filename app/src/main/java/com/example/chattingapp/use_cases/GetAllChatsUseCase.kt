package com.example.chattingapp.use_cases

import com.example.chattingapp.repositories.UserRepository
import javax.inject.Inject

class GetAllChatsUseCase @Inject constructor(private val rep:UserRepository) {
    suspend fun getAllChats(senderId: String, receiverId: String) =
        rep.retrieveAllChats(senderId, receiverId)

}