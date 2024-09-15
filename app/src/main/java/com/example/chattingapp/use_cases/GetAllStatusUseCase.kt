package com.example.chattingapp.use_cases

import com.example.chattingapp.repositories.UserRepository
import javax.inject.Inject

class GetAllStatusUseCase @Inject constructor(private val userRepository: UserRepository) {

suspend fun getAllStatus(userId:String) = userRepository.retrieveAllStatus(userId)

    suspend fun getAllViewedStatus(userId:String)= userRepository.retrieveAllViewedStatus(userId)

}