package com.example.chattingapp.repositories

import android.net.Uri
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.models.BaseAuthenticator
import com.example.chattingapp.models.Calls
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.example.chattingapp.models.ChatsList
import com.example.chattingapp.models.LastChatModel
import com.example.chattingapp.models.Status
import com.example.chattingapp.models.Users
import com.example.chattingapp.utilities.LastMessageChat
import com.example.chattingapp.utilities.Resource
import com.example.chattingapp.utilities.Screen
import com.example.chattingapp.utilities.ScreenState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.Date
import javax.inject.Inject

 class UserRepositoryImpl @Inject constructor(private val authenticator: BaseAuthenticator):UserRepository {
    override suspend fun signupWithEmailandPassword(
        username: String,
        email: String,
        password: String
    ): Flow<Resource<FirebaseUser>> {
            return authenticator.signupWithEmailandPassword(username, email, password)
    }

    override suspend fun signInWithEmailandPassword(
        email: String,
        password: String
    ): Flow<Resource<FirebaseUser>> {
           return authenticator.signInWithEmailandPassword(email, password)
    }

     override suspend fun signUnWithPhonenumber(
         number: String,
         name: String,
         image: String,
         verificationId: String
     ): Flow<Resource<FirebaseUser>> {
         return authenticator.signUnWithPhonenumber(number, name, image, verificationId)
     }

     override suspend fun selectProfilePhoto(imageUri: Uri): Flow<Resource<String>> {
         return authenticator.selectProfilePhoto(imageUri)
     }

     override suspend fun retrieveAllUsers(currentUserId: String): Flow<ScreenState<List<Users>>> {
      return authenticator.retrieveAllUsers(currentUserId)
    }

    override suspend fun retrieveSearchUsers(
        str: String,
        userId: String
    ): Flow<ScreenState<List<Users>>> {
        return authenticator.retrieveSearchUsers(str,userId)
    }

     override suspend fun sendMessages(
         senderId: String,
         userIdVisit: String,
         message: String,
         date: String,url:String
     ) {
         return authenticator.sendMessages(senderId, userIdVisit, message,date,url)
     }

    override suspend fun sendMultipleChats(date: String, listChats: List<Chat>){
        return authenticator.sendMultipleChats(date,listChats)
    }

     override suspend fun saveCalls(
         sender: String,
         receiver: String,
         senderName: String,
         receiverName: String,
         type: String,
         time: Long
     ) {
         return authenticator.saveCalls(sender, receiver,senderName,receiverName, type, time)
     }

     override suspend fun retrieveUser(id: String): Flow<Screen<Users>> {
         return authenticator.retrieveUser(id)
     }

     override suspend fun uploadImageToFirebaseStorage(
         senderId: String,
         receiverId: String,
         imageUri: Uri,
         date: String
     ) {
         return authenticator.uploadImageToFirebaseStorage(senderId,receiverId, imageUri,date)
     }

     override suspend fun uploadAudioFileToFirestore(
         senderId: String,
         receiverId: String,
         audioFile: String,
         date: String
     ) {
         return authenticator.uploadAudioFileToFirestore(senderId, receiverId, audioFile,date)
     }

     override suspend fun uploadVideoFileToFirestore(
         senderId: String,
         receiverId: String,
         videoUri: Uri,date: String
     ) {
         return authenticator.uploadVideoFileToFirestore(senderId, receiverId, videoUri,date)
     }

     override suspend fun uploadStatusImagesToFirebaseStorage(
         userId: String,
         username: String,
         photoUrls: List<Uri>,
         adapter: RecentUpdatesAdapter
     ) {
         return authenticator.uploadStatusImagesToFirebaseStorage(userId, username, photoUrls,adapter)
     }

     override suspend fun retrieveAllChats(
         senderId: String,
         receiverId: String
     ): Flow<ScreenState<List<ChatDay>>> {
         return authenticator.retrieveAllChats(senderId, receiverId)
     }

     override suspend fun retrieveAllStatus(userId: String): Flow<ScreenState<List<Status>>> {
         return authenticator.retrieveAllStatus(userId)
     }

     override suspend fun retrieveAllViewedStatus(userId: String): Flow<ScreenState<List<Status>>> {
         return authenticator.retrieveAllViewedStatus(userId)
     }

     override suspend fun retrieveCurrentUserStatus(id: String): Flow<Screen<Status>> {
         return authenticator.retrieveCurrentUserStatus(id)
     }

     override suspend fun updateClickedStatus(id: String, currentUser: String) {
         return authenticator.updateClickedStatus(id,currentUser)
     }

     override suspend fun deleteStatus(id: String) {
         return authenticator.deleteStatus(id)
     }

     override suspend fun retrieveLastMessage(
         userId: String,
         list:List<Users>
     ): Flow<ScreenState<List<LastChatModel>>> {
         return authenticator.retrieveLastMessage(userId,list)
     }

     override suspend fun retrieveAllChatsList(id: String): Flow<ScreenState<List<ChatsList>>> {
         return authenticator.retrieveChatsLists(id)
     }

     override suspend fun retrieveAllChatUsers(list: List<ChatsList>): Flow<ScreenState<List<Users>>> {
         return authenticator.getAllChatUsers(list)
     }

     override suspend fun getUnreadMsgs(id: String): Flow<Screen<Int>> {
         return authenticator.getUnreadMsgs(id)
     }

     override suspend fun getAllCalls(
         id: String
     ): Flow<ScreenState<List<Calls>>> {
         return authenticator.getAllCalls(id)
     }
 }