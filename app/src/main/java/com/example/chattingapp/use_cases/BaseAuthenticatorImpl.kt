package com.example.chattingapp.use_cases



import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bumptech.glide.load.HttpException
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.models.BaseAuthenticator
import com.example.chattingapp.models.Calls
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.example.chattingapp.models.ChatsList
import com.example.chattingapp.models.LastChatModel
import com.example.chattingapp.models.Status
import com.example.chattingapp.models.Users
import com.example.chattingapp.utilities.Resource
import com.example.chattingapp.utilities.Screen
import com.example.chattingapp.utilities.ScreenState
import com.example.chattingapp.utilities.State
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BaseAuthenticatorImpl @Inject constructor(private val mAuth:FirebaseAuth ):BaseAuthenticator {

    private var firebaseUserId: String = ""
    private val db = FirebaseFirestore.getInstance()
    //  private  val mAuth:FirebaseAuth = FirebaseAuth.getInstance()

    private var mUsers = ArrayList<Users>()
    private var mArrayList = ArrayList<Chat>()
    private var list = ArrayList<Chat>()

    private var listener: OnDataChangeListener? = null


    override suspend fun signupWithEmailandPassword(
        username: String,
        email: String, password: String
    ): Flow<Resource<FirebaseUser>> = flow {

        emit(Resource.Loading())
        try {
            val result = mAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {

                firebaseUserId = it.uid

                val userHashMap = HashMap<String, Any>()
                userHashMap["uid"] = firebaseUserId
                userHashMap["username"] = username
                userHashMap["email"] = email
                userHashMap["password"] = password
                userHashMap["profile"] = ""
                userHashMap["status"] = "offline"
                userHashMap["cover"] = ""
                userHashMap["search"] = username.toLowerCase(Locale.ROOT)
                userHashMap["facebook"] = "https://m.facebook.com"
                userHashMap["instagram"] = "https://m.instagram.com"
                userHashMap["website"] = "https://www.google.com"
                userHashMap["event"] = ""
                userHashMap["data"] = ""

                db.collection("Users")
                    .document(firebaseUserId).set(userHashMap)
                emit(Resource.Success(it))
            }


        } catch (e: HttpException) {
            emit(Resource.Error(message = e.message ?: "Unknown error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.message ?: "Check your internet connection "))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "some error occurred"))
        }


    }

    override suspend fun signInWithEmailandPassword(
        email: String,
        password: String
    ): Flow<Resource<FirebaseUser>> = flow {

        emit(Resource.Loading())

        try {
            val result = mAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {

                firebaseUserId = it.uid
                emit(Resource.Success(data = it))
            }


        } catch (e: HttpException) {
            emit(Resource.Error(message = e.message ?: "Unknown error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.message ?: "Check your internet connection "))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "some error occurred"))
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override suspend fun signUnWithPhonenumber(
        number: String,
        name: String,
        image: String, verificationId:String
    ): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId,number)
         val result =    mAuth.signInWithCredential(credential).await()

                    result.user?.let { firebaseUser ->

                        firebaseUserId = firebaseUser.uid
                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserId
                        userHashMap["username"] = name
                        userHashMap["number"] = number
                        userHashMap["profile"] = image
                        userHashMap["status"] = "offline"
                        userHashMap["cover"] = ""
                        userHashMap["search"] = name.toLowerCase(Locale.ROOT)
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com"
                        userHashMap["event"] = ""
                        userHashMap["data"] = ""

                        db.collection("Users")
                            .document(firebaseUserId).set(userHashMap)
                        emit(Resource.Success(firebaseUser))
                    }

        }catch (e: HttpException) {
            emit(Resource.Error(message = e.message ?: "Unknown error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.message ?: "Check your internet connection "))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "some error occurred"))
        }


    }

    override suspend fun selectProfilePhoto(imageUri: Uri): Flow<Resource<String>> = flow  {

        try {
            emit(Resource.Loading(null))

            val storageReference = FirebaseStorage.getInstance().reference
                .child("Profile Images")
            val messageKey = db.collection("ProfileImages").document().id
            val filePath = storageReference.child("$messageKey.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(imageUri)

            val downloadUrl = suspendCoroutine<String> { continuation ->
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result.toString()

                        continuation.resume(url)
                    } else {
                        task.exception?.let { exception ->
                            continuation.resumeWithException(exception)
                        }
                    }
                }
            }

            emit(Resource.Success(downloadUrl))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred"))
        }

      /*  try {
            emit(Resource.Loading(null))

            val storageReference = FirebaseStorage.getInstance().reference
                .child("Profile Images")
            val messageKey = db.collection("ProfileImages").document().id
            val filePath = storageReference.child("$messageKey.jpg")

            // Upload the file and await completion
          //  val uploadTaskSnapshot = filePath.putFile(imageUri).await()

            // Get the download URL once the upload is successful
            val downloadUrl = filePath.downloadUrl.await().toString()

            // Emit success with the download URL
            emit(Resource.Success(downloadUrl))

        } catch (e: Exception) {
            // Emit an error with a localized message
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred"))
        }*/

    }

    override suspend fun retrieveAllUsers(currentUserId: String): Flow<ScreenState<List<Users>>> =
        flow {


            emit(ScreenState.Loading())
            try {
                val snapshot = db.collection("Users").orderBy("search").get().await()

                snapshot?.let {
                    mUsers.clear()
                    for (document in it.documents) {
                        val user = document.toObject(Users::class.java)
                        if (user!!.uid != currentUserId) {
                            mUsers.add(user)
                        }
                    }
                    emit(ScreenState.Success(data = mUsers))
                }
            } catch (e: HttpException) {
                emit(ScreenState.Error(message = e.message ?: "Unknown error"))
            } catch (e: IOException) {
                emit(ScreenState.Error(message = e.message ?: "Check your internet connection "))
            } catch (e: Exception) {
                emit(ScreenState.Error(message = e.message ?: "some error occurred"))
            }


        }

    override suspend fun retrieveSearchUsers(
        str: String, userId: String
    ): Flow<ScreenState<List<Users>>> = flow {

        val list = ArrayList<Users>()
        emit(State.Loading())

        try {

            val snapshot = db.collection("Users")
                .orderBy("search").startAt(str).endAt(str + "\uf8ff").get().await()
            snapshot?.let {
                for (document in it.documents) {
                    val user = document.toObject(Users::class.java)
                    if (!(user!!.uid.equals(userId))) {
                        list.add(user)
                    }
                }
                emit(State.Success(data = list))
            }
        } catch (e: HttpException) {
            emit(State.Error(message = e.message ?: "Unknown error"))
        } catch (e: IOException) {
            emit(State.Error(message = e.message ?: "Check your internet connection "))
        } catch (e: Exception) {
            emit(State.Error(message = e.message ?: "some error occurred"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override suspend fun sendMessages(
        senderId: String,
        userIdVisit: String,
        message: String,
        date: String,url:String
    ){

        try {
            ScreenState.Loading(null)

            val messageKey = db.collection("ChatsDates").document().id
                  val msgHashMap = HashMap<String, Any>()
                  val chatLList = mutableListOf<Chat>()

                  msgHashMap["sender"] = senderId
                  msgHashMap["message"] = message
                  msgHashMap["receiver"] = userIdVisit
                  msgHashMap["isSeen"] = false
                  msgHashMap["url"] = url
                  msgHashMap["messageKey"] = messageKey
                  msgHashMap["time"] =System.currentTimeMillis()
                  msgHashMap["clickedNum"] = 0



                  val chat:Chat = Chat(senderId,message, userIdVisit,false,"",messageKey,System.currentTimeMillis(),0,false)

                  val hashMap = HashMap<String,Any>()

                  chatLList.add(chat)
                  hashMap["chatDay"] = System.currentTimeMillis()
                  hashMap["chats"] = chatLList

                  db.collection("ChatsDates").document(date).get().addOnCompleteListener {
            if(it.isSuccessful){
                val doc = it.result
               if(doc != null && doc.exists()) {

                   val existingChats = doc.get("chats") as? List<HashMap<String, Any>> ?: emptyList()
                   val updatedChats = existingChats + chatLList
                   doc.reference.update("chats", updatedChats).addOnCompleteListener { /* Update complete */ }


               }else{
                   db.collection("ChatsDates").document(date).set(hashMap).addOnCompleteListener {

                   }
               }


            }

                  }
        //    db.collection("Chats").document(messageKey).set(msgHashMap).addOnCompleteListener {
                //ScreenState.Success(messageKey)
                db.collection("ChatsList").document(senderId)
                    .collection("Messages").document(userIdVisit).get().addOnSuccessListener {
                        if (!it.exists()) {
                            db.collection("ChatsList").document(senderId)
                                .collection("Messages").document(userIdVisit)
                                .set(hashMapOf("id" to userIdVisit))
                        }
                        db.collection("ChatsList").document(userIdVisit)
                            .collection("Messages").document(senderId)
                            .set(hashMapOf("id" to senderId))
                    }


            //}


            } catch (e: Exception) {
                // ScreenState.Error(e.localizedMessage ?: "An Error Occurred")
            }




    }

    override suspend fun sendMultipleChats(date: String, listChats: List<Chat>) {
        try {
            ScreenState.Loading(null)


            val msgHashMap = HashMap<String, Any>()
            val chatLList = mutableListOf<Chat>()



            for(msg in listChats){
                val messageKey = db.collection("ChatsDates").document().id
                val chat:Chat = Chat(msg.sender,msg.message, msg.receiver,msg.isSeen,msg.url,messageKey,System.currentTimeMillis(),msg.clickedNum,msg.isSelected)
                 chatLList.add(chat)
            }





            val hashMap = HashMap<String,Any>()


            hashMap["chatDay"] = System.currentTimeMillis()
            hashMap["chats"] = chatLList

            db.collection("ChatsDates").document(date).get().addOnCompleteListener {
                if(it.isSuccessful){
                    val doc = it.result
                    if(doc != null && doc.exists()) {

                        val existingChats = doc.get("chats") as? List<HashMap<String, Any>> ?: emptyList()
                        val updatedChats = existingChats + chatLList
                        doc.reference.update("chats", updatedChats).addOnCompleteListener { /* Update complete */ }


                    }else{
                        db.collection("ChatsDates").document(date).set(hashMap).addOnCompleteListener {

                        }
                    }


                }

            }
            //    db.collection("Chats").document(messageKey).set(msgHashMap).addOnCompleteListener {
            //ScreenState.Success(messageKey)
            for(msgs in chatLList){
                db.collection("ChatsList").document(msgs.sender)
                    .collection("Messages").document(msgs.receiver).get().addOnSuccessListener {
                        if (!it.exists()) {
                            db.collection("ChatsList").document(msgs.sender)
                                .collection("Messages").document(msgs.receiver)
                                .set(hashMapOf("id" to msgs.receiver))
                        }
                        db.collection("ChatsList").document(msgs.receiver)
                            .collection("Messages").document(msgs.sender)
                            .set(hashMapOf("id" to msgs.sender))
                    }
            }



            //}


        } catch (e: Exception) {
            // ScreenState.Error(e.localizedMessage ?: "An Error Occurred")
        }



    }

    override suspend fun saveCalls(
        sender: String,
        receiver: String,
        senderName: String,
        receiverName: String,
        type: String,
        time: Long
    ) {

        try {
            ScreenState.Loading(null)

            val callKey = db.collection("Calls").document().id
           val hashMap = HashMap<String,Any>()
            hashMap["sender"] = sender
            hashMap["receiver"] = receiver
            hashMap["senderName"] = senderName
            hashMap["receiverName"] = receiverName
            hashMap["type"] = type
            hashMap["time"] = time
            hashMap["callKey"] = callKey

                db.collection("Calls").document(callKey).set(hashMap).addOnCompleteListener {
            //ScreenState.Success(messageKey)



            }


        } catch (e: Exception) {
            //ScreenState.Error(e.localizedMessage ?: "An Error Occurred")
        }


    }


    override suspend fun retrieveUser(id: String): Flow<Screen<Users>> = callbackFlow {

    /*    emit(Screen.Loading())
        try {
            val snapshot = db.collection("Users").document(id).get().await()

            snapshot?.let {
                val user = it.toObject(Users::class.java)
                emit(Screen.Success(user))
            }
        } catch (e: HttpException) {
            emit(Screen.Error(message = e.message ?: "Unknown error"))
        } catch (e: IOException) {
            emit(Screen.Error(message = e.message ?: "Check your internet connection "))
        } catch (e: Exception) {
            emit(Screen.Error(message = e.message ?: "some error occurred"))
        }*/
        try {
            trySend(Screen.Loading(null))
            val query = db.collection("Users").document(id)
            val listener = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(Screen.Error(exception.localizedMessage ?: "An Error Occurred"))
                    return@addSnapshotListener
                }
                snapshot?.let {
                    if (it.exists()) {

                            val userStatus = it.toObject(Users::class.java)

                            trySend(Screen.Success(userStatus))


                    } else {
                        trySend(Screen.Success(null))
                    }
                    // adapter.notifyDataSetChanged()
                }
            }
            awaitClose {
                listener.remove()
                channel.close()
            }
        } catch (exception: Exception) {
            trySend(Screen.Error(exception.localizedMessage ?: "An Error Occurred"))
        }

    }

    override suspend fun uploadImageToFirebaseStorage(
        senderId: String,
        receiverId: String,
        imageUri: Uri,
        date:String
    ) {

        val storageReference = FirebaseStorage.getInstance().reference
            .child("Chat Images")
        val messageKey = db.collection("ChatsImages").document().id
        val filePath = storageReference.child("$messageKey.jpg")

        val uploadTask: StorageTask<*>
        uploadTask = filePath.putFile(imageUri)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation filePath.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                val messageHashMap = HashMap<String, Any>()
                messageHashMap["sender"] = senderId
                messageHashMap["message"] = "sent you an image."
                messageHashMap["receiver"] = receiverId
                messageHashMap["isSeen"] = false
                messageHashMap["url"] = url
                messageHashMap["messageId"] = messageKey

                val chatsList = mutableListOf<Chat>()

                val chat: Chat = Chat(
                    senderId,
                    "sent you an image.",
                    receiverId,
                    false,
                    url,
                    messageKey,
                    System.currentTimeMillis(),
                    0,false
                )
                val hashMap = HashMap<String, Any>()

                chatsList.add(chat)
                hashMap["chatDay"] = System.currentTimeMillis()
                hashMap["chats"] = chatsList

                        db.collection("ChatsDates").document(date).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val doc = it.result
                        if (doc != null && doc.exists()) {

                            val existingChats =
                                doc.get("chats") as? List<HashMap<String, Any>> ?: emptyList()
                            val updatedChats = existingChats + chatsList
                            doc.reference.update("chats", updatedChats)
                                .addOnCompleteListener { /* Update complete */ }


                        } else {
                            db.collection("ChatsDates").document(date).set(hashMap)
                                .addOnCompleteListener {

                                }
                        }


                    }
                }
                ScreenState.Success(true)
                db.collection("ChatsList").document(senderId)
                    .collection("Messages").document(receiverId).get()
                    .addOnSuccessListener {
                        if (!it.exists()) {
                            db.collection("ChatsList").document(senderId)
                                .collection("Messages").document(receiverId)
                                .set(hashMapOf("id" to receiverId))
                        }
                        db.collection("ChatsList").document(receiverId)
                            .collection("Messages").document(senderId)
                            .set(hashMapOf("id" to senderId))
                    }
              //  db.collection("Chats").document(messageKey).set(messageHashMap)
                    //   .addOnCompleteListener {



                  //  }

            }
        }
    }


    override suspend fun uploadStatusImagesToFirebaseStorage(
        userId: String,
        username: String,
        photoUrls: List<Uri>,
        adapter: RecentUpdatesAdapter
    ) {

        val storageReference = FirebaseStorage.getInstance().reference.child("Status Images")

        val messageKey = db.collection("StatusImages").document().id

        val urls = mutableListOf<String>()

        // Upload each image and get their download URLs
        for ((index, imageUri) in photoUrls.withIndex()) {
            val filePath = storageReference.child("$messageKey-$index.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(imageUri)

            try {
                val taskSnapshot = uploadTask.await() // Wait for the upload to complete
                val downloadUrl = taskSnapshot.storage.downloadUrl.await() // Get download URL

                urls.add(downloadUrl.toString())
            } catch (e: Exception) {
                // Handle the exception, for example, log an error
                e.printStackTrace()
            }
        }

        val statusHashmap = HashMap<String, Any>()
        statusHashmap["userId"] = userId
        statusHashmap["userName"] = username
        statusHashmap["clicked"] = false
        statusHashmap["time"] = System.currentTimeMillis()
        statusHashmap["imageUrls"] = urls

        // Check if userId exists in "Status" collection
        db.collection("Status").document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result

                if (document != null && document.exists()) {

                    // If userId exists, update the existing document
                    val existingUrls = document["imageUrls"] as? List<String> ?: emptyList()


                    //  statusHashmap["time"] = System.currentTimeMillis()
                    //   statusHashmap["imageUrls"] = urls

                    urls.addAll(existingUrls) // Add existing URLs to the new ones
                    statusHashmap["time"] = System.currentTimeMillis()
                    statusHashmap["imageUrls"] = urls



                    db.collection("Status").document(userId).update(statusHashmap)
                        .addOnCompleteListener {
                            // Handle completion if needed
                            ///    adapter.notifyDataSetChanged()
                        }
                } else {
                    // If userId does not exist, create a new document
                    //  statusHashmap["time"] = System.currentTimeMillis()
                    db.collection("Status").document(userId).set(statusHashmap)
                        .addOnCompleteListener {
                            // Handle completion if needed
                        }
                }
            } else {
                // Handle the exception, for example, log an error
                task.exception?.printStackTrace()
            }
        }


    }

    override suspend fun uploadAudioFileToFirestore(
        senderId: String,
        receiverId: String,
        audioFile: String,
        date: String
    ) {

        val chatsList = mutableListOf<Chat>()
   val audioId = db.collection("ChatsDates").document().id
        val chat: Chat = Chat(
            senderId,
            "sent you an audio.",
            receiverId,
            false,
            audioFile,
            audioId,
            System.currentTimeMillis(),
            0,false
        )
        val hashMap = HashMap<String, Any>()

        chatsList.add(chat)
        hashMap["chatDay"] = System.currentTimeMillis()
        hashMap["chats"] = chatsList

        db.collection("ChatsDates").document(date).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val doc = it.result
                if (doc != null && doc.exists()) {

                    val existingChats =
                        doc.get("chats") as? List<HashMap<String, Any>> ?: emptyList()
                    val updatedChats = existingChats + chatsList
                    doc.reference.update("chats", updatedChats)
                        .addOnCompleteListener { /* Update complete */ }


                } else {
                    db.collection("ChatsDates").document(date).set(hashMap)
                        .addOnCompleteListener {

                        }
                }


            }
        }




        //   db.collection("Chats").document(messageKey).set(msgHashMap).addOnCompleteListener {
        ScreenState.Success(true)
        db.collection("ChatsList").document(senderId)
            .collection("Messages").document(receiverId).get().addOnSuccessListener {
                if (!it.exists()) {
                    db.collection("ChatsList").document(senderId)
                        .collection("Messages").document(receiverId)
                        .set(hashMapOf("id" to receiverId))
                }
                db.collection("ChatsList").document(receiverId)
                    .collection("Messages").document(senderId)
                    .set(hashMapOf("id" to senderId))
            }


    }



    override suspend fun uploadVideoFileToFirestore(
        senderId: String,
        receiverId: String,
        videoUri: Uri,date: String
    ) {
        val storageReference = FirebaseStorage.getInstance().reference
            .child("Chat Videos")
        val messageKey = db.collection("ChatsVideo").document().id
        val filePath = storageReference.child("$messageKey.mp4")

        val uploadTask: StorageTask<*>
        uploadTask = filePath.putFile(videoUri)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation filePath.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                val chatsList = mutableListOf<Chat>()

                val chat: Chat = Chat(
                    senderId,
                    "sent you a video.",
                    receiverId,
                    false,
                    url,
                    messageKey,
                    System.currentTimeMillis(),
                    0,false
                )
                val hashMap = HashMap<String, Any>()

                chatsList.add(chat)
                hashMap["chatDay"] = System.currentTimeMillis()
                hashMap["chats"] = chatsList

                db.collection("ChatsDates").document(date).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val doc = it.result
                        if (doc != null && doc.exists()) {

                            val existingChats =
                                doc.get("chats") as? List<HashMap<String, Any>> ?: emptyList()
                            val updatedChats = existingChats + chatsList
                            doc.reference.update("chats", updatedChats)
                                .addOnCompleteListener { /* Update complete */ }


                        } else {
                            db.collection("ChatsDates").document(date).set(hashMap)
                                .addOnCompleteListener {

                                }
                        }


                    }
                }
                ScreenState.Success(true)
                db.collection("ChatsList").document(senderId)
                    .collection("Messages").document(receiverId).get()
                    .addOnSuccessListener {
                        if (!it.exists()) {
                            db.collection("ChatsList").document(senderId)
                                .collection("Messages").document(receiverId)
                                .set(hashMapOf("id" to receiverId))
                        }
                        db.collection("ChatsList").document(receiverId)
                            .collection("Messages").document(senderId)
                            .set(hashMapOf("id" to senderId))
                    }
            }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("SuspiciousIndentation")
    override suspend fun retrieveAllChats(
        senderId: String,
        receiverId: String): Flow<ScreenState<List<ChatDay>>> = callbackFlow {

        var updated:Boolean = false

       try {
            trySend(ScreenState.Loading(null))
            val query = db.collection("ChatsDates")
            val listener = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
                    return@addSnapshotListener
                }

                val chatsList = mutableListOf<ChatDay>()


                snapshot?.let { documents ->

                    chatsList.clear()

                    for (document in documents) {
                        val list = mutableListOf<Chat>()
                        val chatDay = document.toObject(ChatDay::class.java)

                       for(chat in chatDay.chats){
                            if(chat.receiver == senderId && chat.sender == receiverId ||
                                chat.receiver == receiverId && chat.sender == senderId){
                                list.add(chat)
                            }
                           if(chat.receiver == senderId
                               && chat.sender == receiverId){
                               chat.isSeen = true
                               // Update the chat in Firestore

                           }


                        }
                        db.collection("ChatsDates").document(document.id)
                            .update("chats", chatDay.chats)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                            }



                        chatDay.chats = list


                        chatsList.add(chatDay)


                    }
                    if (chatsList.isNotEmpty()) {

                        trySend(ScreenState.Success(chatsList))
                        Log.i("new list", chatsList.toString())

                    }

                   }

                trySend(ScreenState.Success(chatsList))



                }

            awaitClose {
                listener.remove()
                channel.close()
            }
        } catch (exception: Exception) {
            trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
        }



}

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun retrieveAllStatus(userId: String): Flow<ScreenState<List<Status>>> =
        callbackFlow {
            val statusList = mutableListOf<Status>()

            try {
                trySend(ScreenState.Loading(null))
                val query = db.collection("Status").whereEqualTo("clicked", false)
                val listener = query.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(
                            ScreenState.Error(
                                exception.localizedMessage ?: "An Error Occurred"
                            )
                        )
                        return@addSnapshotListener
                    }
                    snapshot?.let { documents ->
                        statusList.clear()
                        for (document in documents) {
                            val userStatus = document.toObject(Status::class.java)
                            if (userStatus.userId != userId) {
                                statusList.add(userStatus)
                            }
                        }
                        if (statusList.isEmpty()) {
                            trySend(ScreenState.Success(null))
                        } else {
                            trySend(ScreenState.Success(statusList))
                        }
                    }
                }
                awaitClose {
                    listener.remove()
                    channel.close()
                }
            } catch (exception: Exception) {
                trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun retrieveAllViewedStatus(userId: String): Flow<ScreenState<List<Status>>> =
        callbackFlow {
            try {
                trySend(ScreenState.Loading(null))
                val query = db.collection("Status").whereEqualTo("clicked", true)
                val listener = query.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(
                            ScreenState.Error(
                                exception.localizedMessage ?: "An Error Occurred"
                            )
                        )
                        return@addSnapshotListener
                    }
                    snapshot?.let { documents ->
                        val statusList = mutableListOf<Status>()
                        for (document in documents) {
                            val userStatus = document.toObject(Status::class.java)
                            if (userStatus.userId != userId) {
                                statusList.add(userStatus)
                            }
                        }
                        if (statusList.isEmpty()) {
                            trySend(ScreenState.Success(null))
                        } else {
                            trySend(ScreenState.Success(statusList))
                        }

                    }
                }
                awaitClose {
                    listener.remove()
                    channel.close()
                }
            } catch (exception: Exception) {
                trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun retrieveCurrentUserStatus(
        id: String
    ): Flow<Screen<Status>> = callbackFlow {
        try {
            trySend(Screen.Loading(null))
            val query = db.collection("Status").whereEqualTo("userId", id)
            val listener = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(Screen.Error(exception.localizedMessage ?: "An Error Occurred"))
                    return@addSnapshotListener
                }
                snapshot?.let {
                    if (!it.isEmpty) {
                        for (document in it) {
                            val userStatus = document.toObject(Status::class.java)

                            trySend(Screen.Success(userStatus))

                        }
                    } else {
                        trySend(Screen.Success(null))
                    }
                    // adapter.notifyDataSetChanged()
                }
            }
            awaitClose {
                listener.remove()
                channel.close()
            }
        } catch (exception: Exception) {
            trySend(Screen.Error(exception.localizedMessage ?: "An Error Occurred"))
        }


    }

    override suspend fun updateClickedStatus(userId: String, currentUserId: String) {
        val urls = mutableListOf<String>()
        urls.add(currentUserId)
        val statusHashmap = HashMap<String, Any>()
        statusHashmap["viewedUsers"] = urls
        db.collection("Status").document(userId).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val document = it.result

                if (document != null && document.exists()) {

                    // If userId exists, update the existing document
                    val existingUrls = document["viewedUsers"] as? List<String> ?: emptyList()
                    urls.addAll(existingUrls) // Add existing URLs to the new ones
                    statusHashmap["clicked"] = true
                    statusHashmap["viewedUsers"] = urls

                    db.collection("Status").document(userId).update(statusHashmap)
                        .addOnCompleteListener {
                            // Handle completion if needed
                        }
                }
            } else {

            }
        }


    }

    override suspend fun deleteStatus(id: String) {
        db.collection("Status").document(id).delete().addOnFailureListener {

        }
            .addOnFailureListener {

            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun retrieveLastMessage(
        userId: String,
        list: List<Users>
    ): Flow<ScreenState<List<LastChatModel>>> = callbackFlow {

        try {
            trySend(ScreenState.Loading(null))
            val query = db.collection("ChatsDates")
            val listener = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
                    return@addSnapshotListener
                }
                val chatsLists = mutableListOf<LastChatModel>()
                snapshot?.documents?.forEach { document ->
                    // Get the date and chats list from the document
                    for(user in list){
                        val date = document.id
                        val chats = document.toObject(ChatDay::class.java)?.chats ?: emptyList()
                        val latestMessageSentByYou = chats.filter { chat ->
                            chat.receiver == userId && chat.sender == user.uid ||
                                    chat.sender == userId && chat.receiver == user.uid}
                            .maxByOrNull { chat -> chat.time }
                        val lastChat =LastChatModel(user.uid,
                            user.username,latestMessageSentByYou!!.message,
                            latestMessageSentByYou.time,latestMessageSentByYou.isSeen)
                        chatsLists.add(lastChat)
                    }


                    // Find the latest message sent by you


                    // Do something with the latest message

                    trySend(ScreenState.Success(chatsLists))

                }


            }

            awaitClose {
                listener.remove()
                channel.close()
            }
        } catch (exception: Exception) {
            trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
        }
    }

    override suspend fun retrieveChatsLists(id: String): Flow<ScreenState<List<ChatsList>>> =
        callbackFlow {

            try {
                trySend(ScreenState.Loading(null))
                val query = db.collection("ChatsList").document(id).collection("Messages")
                val listener = query.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(
                            ScreenState.Error(
                                exception.localizedMessage ?: "An Error Occurred"
                            )
                        )
                        return@addSnapshotListener
                    }
                    val list = mutableListOf<ChatsList>()
                    snapshot?.let { documents ->
                      //  list.clear()
                        for (document in documents) {
                            val chatList = document.toObject(ChatsList::class.java)
                            list.add(chatList)
                        }
                        if (list.isEmpty()) {
                            trySend(ScreenState.Success(null))
                        } else {
                            trySend(ScreenState.Success(list))
                        }

                    }
                }
                awaitClose {
                    listener.remove()
                    channel.close()
                }
            } catch (exception: Exception) {
                trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
            }
        }

    override suspend fun getAllChatUsers(chatUsers: List<ChatsList>,searchQuery:String): Flow<ScreenState<List<Users>>> =
        callbackFlow {


            try {

                trySend(ScreenState.Loading(null))
                val query = if (searchQuery.isNotEmpty()) {
                    db.collection("Users")
                        .orderBy("search") // Replace "name" with the actual field you want to search by
                        .startAt(searchQuery)
                        .endAt(searchQuery + "\uf8ff")
                } else {
                    db.collection("Users")
                }
                val listener = query.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(
                            ScreenState.Error(
                                exception.localizedMessage ?: "An Error Occurred"
                            )
                        )
                        return@addSnapshotListener
                    }
                    val list = mutableListOf<Users>()
                    snapshot?.let { documents ->
                      //  list.clear()
                        for (document in documents) {
                            val user = document.toObject(Users::class.java)
                            for (eachList in chatUsers) {
                                if (user.uid == eachList.id) {
                                    list.add(user)
                                }
                            }
                        }
                        if (list.isEmpty()) {
                            trySend(ScreenState.Success(null))
                        } else {
                            trySend(ScreenState.Success(list))
                        }

                    }
                }
                awaitClose {
                    listener.remove()
                    channel.close()
                }
            } catch (exception: Exception) {
                trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
            }
        }

    override suspend fun getUnreadMsgs(id: String): Flow<Screen<Int>> = callbackFlow {
        var unreadMsgs = 0
        try {
            trySend(Screen.Loading(null))
            val query = db.collection("ChatsDates")
            val listener = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(Screen.Error(exception.localizedMessage ?: "An Error Occurred"))
                    return@addSnapshotListener
                }
                unreadMsgs = 0
                snapshot?.let { documents ->

                    for (document in documents) {
                        val chatDay = document.toObject(ChatDay::class.java)
                        for (chat in chatDay.chats) {
                            if (chat.receiver == id && !chat.isSeen) {
                                unreadMsgs += 1
                            }
                        }
                    }

                    trySend(Screen.Success(unreadMsgs))

                }



            }

        awaitClose {
            listener.remove()
            channel.close()
        }
    } catch (exception: Exception)
    {
        trySend(Screen.Error(exception.localizedMessage ?: "An Error Occurred"))
    }

}

    override suspend fun getAllCalls(
        id: String
    ): Flow<ScreenState<List<Calls>>> = callbackFlow {
        try {
            trySend(ScreenState.Loading(null))
            val query = db.collection("Calls")
            val listener = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
                    return@addSnapshotListener
                }

                val callsList = mutableListOf<Calls>()


                snapshot?.let { documents ->

                    callsList.clear()

                    for (document in documents) {
                       // val list = mutableListOf<Calls>()
                        val call = document.toObject(Calls::class.java)
                      if(call.sender == id ||
                          call.receiver == id ) {

                          callsList.add(call)

                      }

                    }
                    if (callsList.isNotEmpty()) {

                        trySend(ScreenState.Success(callsList))
                        Log.i("new list", callsList.toString())

                    }

                }




            }

            awaitClose {
                listener.remove()
                channel.close()
            }
        } catch (exception: Exception) {
            trySend(ScreenState.Error(exception.localizedMessage ?: "An Error Occurred"))
        }


    }
}




/*    val snapshot = db.collection("Chats").get().await()
            snapshot?.let {
                mArrayList.clear()
                for (document in it.documents) {
                    val chat = document.toObject(Chat::class.java)
             //   if(chat!!.receiver == senderId && chat.sender == receiverId || chat!!.receiver == receiverId && chat.sender == senderId) {
                    mArrayList.add(chat!!)
                    if (chat.receiver == firebaseUser.uid && chat.sender == userIdVisit) {
                        val hashMap = HashMap<String, Any?>()
                        hashMap["isSeen"] = true

                    }



            }
                emit(ScreenState.Success(data = mArrayList))
        }
        }catch (e:HttpException){
            emit(ScreenState.Error(message = e.message ?: "Unknown error"))
        } catch (e:IOException){
            emit(ScreenState.Error(message = e.message ?: "Check your internet connection "))
        } catch (e:Exception){
            emit(ScreenState.Error(message = e.message ?: "some error occurred"))
        }*/



interface OnDataChangeListener{
    fun onDataChange(data:List<Chat>)
}