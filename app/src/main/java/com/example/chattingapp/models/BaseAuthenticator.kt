package com.example.chattingapp.models

import android.net.Uri
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.utilities.Resource
import com.example.chattingapp.utilities.Screen
import com.example.chattingapp.utilities.ScreenState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface BaseAuthenticator {

  suspend   fun signupWithEmailandPassword(username: String, email: String, password: String):Flow<Resource<FirebaseUser>>

  suspend   fun signInWithEmailandPassword(email: String, password: String):Flow<Resource<FirebaseUser>>

  suspend fun signUnWithPhonenumber(number:String,name:String,image:String,verificationId:String):Flow<Resource<FirebaseUser>>

  suspend fun selectProfilePhoto(imageUri:Uri):Flow<Resource<String>>

  suspend  fun retrieveAllUsers(currentUserId: String):Flow<ScreenState<List<Users>>>

  suspend fun retrieveSearchUsers(str:String,userId:String):Flow<ScreenState<List<Users>>>

  suspend fun sendMessages(senderId: String, userIdVisit: String, message: String,date: String,url:String)

  suspend fun sendMultipleChats(date: String, list: List<Chat>)

suspend fun saveCalls(sender:String,receiver:String,senderName:String,receiverName:String,type:String,time:Long)

  suspend fun retrieveUser(id:String):Flow<Screen<Users>>

  suspend fun uploadImageToFirebaseStorage(senderId:String,receiverId:String,imageUri:Uri,date:String)

  suspend fun uploadStatusImagesToFirebaseStorage(userId:String,username:String,
                                                  photoUrls:List<Uri>,adapter:RecentUpdatesAdapter)

  suspend fun uploadAudioFileToFirestore(senderId: String, receiverId: String,audioFile: String,date: String)

  suspend fun uploadVideoFileToFirestore(senderId:String,receiverId:String,videoUri:Uri,date: String)

  suspend fun retrieveAllChats(senderId: String,receiverId: String):Flow<ScreenState<List<ChatDay>>>

  suspend fun retrieveAllStatus(userId:String):Flow<ScreenState<List<Status>>>

  suspend fun retrieveAllViewedStatus(userId:String):Flow<ScreenState<List<Status>>>

  suspend fun retrieveCurrentUserStatus(id:String):Flow<Screen<Status>>

  suspend fun updateClickedStatus(userId:String,currentUserId:String)

  suspend fun deleteStatus(id:String)

  suspend fun retrieveLastMessage(userId:String,list:List<Users>):Flow<ScreenState<List<LastChatModel>>>

  suspend fun retrieveChatsLists(id:String):Flow<ScreenState<List<ChatsList>>>

  suspend fun getAllChatUsers(list:List<ChatsList>):Flow<ScreenState<List<Users>>>

  suspend fun getUnreadMsgs(id:String):Flow<Screen<Int>>

  suspend fun getAllCalls(senderId:String):Flow<ScreenState<List<Calls>>>
}