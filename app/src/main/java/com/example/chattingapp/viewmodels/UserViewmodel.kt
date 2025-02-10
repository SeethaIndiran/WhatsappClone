package com.example.chattingapp.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatsList
import com.example.chattingapp.models.Users
import com.example.chattingapp.repositories.UserRepository
import com.example.chattingapp.use_cases.GetAllChatsUseCase
import com.example.chattingapp.use_cases.GetAllStatusUseCase
import com.example.chattingapp.utilities.AllChatsListStatus
import com.example.chattingapp.utilities.AuthState
import com.example.chattingapp.utilities.CallsState
import com.example.chattingapp.utilities.ChatState
import com.example.chattingapp.utilities.CurrentUserStatus
import com.example.chattingapp.utilities.LastMessageChat
import com.example.chattingapp.utilities.Resource
import com.example.chattingapp.utilities.Screen
import com.example.chattingapp.utilities.ScreenState
import com.example.chattingapp.utilities.SearchUserState
import com.example.chattingapp.utilities.State
import com.example.chattingapp.utilities.StatusState
import com.example.chattingapp.utilities.UnreadMsgState
import com.example.chattingapp.utilities.UserState
import com.example.chattingapp.utilities.ViewedStatusState
import com.example.chattingapp.utilities.ImageState
import com.example.chattingapp.utilities.visitUserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewmodel @Inject constructor(private val repository: UserRepository,
    private val useCase: GetAllChatsUseCase,
    private val statusUsecase:GetAllStatusUseCase): ViewModel() {

   private val _userData = MutableStateFlow(AuthState())
    val userData : StateFlow<AuthState> = _userData

    private val _users = MutableStateFlow(UserState())
    val users : StateFlow<UserState> = _users

    private val _searchUsers = MutableStateFlow(SearchUserState())
    val searchUsers : StateFlow<SearchUserState> = _searchUsers

    private val _singleUser = MutableStateFlow(visitUserState())
    val singleUser : StateFlow<visitUserState> = _singleUser

    private val _url = MutableStateFlow(ImageState())
    val url : StateFlow<ImageState> = _url

    private val _singleStatus  = MutableStateFlow(CurrentUserStatus())
    val singleStatus:StateFlow<CurrentUserStatus> = _singleStatus

    private val _lastMsg  = MutableStateFlow(LastMessageChat())
    val lastMsg:StateFlow<LastMessageChat> = _lastMsg

    private val _chats = MutableStateFlow(ChatState())
    val chats : StateFlow<ChatState> = _chats

    private val _calls = MutableStateFlow(CallsState())
    val calls : StateFlow<CallsState> = _calls

    private val _status = MutableStateFlow(StatusState())
    val status : StateFlow<StatusState> = _status

    private val _statusViewed = MutableStateFlow(ViewedStatusState())
    val statusViewed : StateFlow<ViewedStatusState> = _statusViewed

    private val _chatsList = MutableStateFlow(AllChatsListStatus())
    val chatsList : StateFlow<AllChatsListStatus> = _chatsList

    private val _unreadMsg = MutableStateFlow(UnreadMsgState())
    val unreadMsg : StateFlow<UnreadMsgState> = _unreadMsg

    var _result = MutableLiveData<ScreenState<List<Chat>>>()
    var result  : LiveData<ScreenState<List<Chat>>> = _result

      fun signUp(username:String, email:String, password:String) {

             viewModelScope.launch {
                 repository.signupWithEmailandPassword(username, email, password).onEach {
                     when(it){
                         is Resource.Success ->{
                             _userData.value = AuthState(data = it.data)
                         }
                         is Resource.Error ->{
                             _userData.value = AuthState(error = it.message!!)
                         }
                         is Resource.Loading ->{
                             _userData.value = AuthState(isLoading = true)
                         }
                     }
                 }.launchIn(viewModelScope)
             }



    }
      fun signIn(email: String, password: String){

          viewModelScope.launch {
              repository.signInWithEmailandPassword( email, password).onEach {
                  when(it){
                      is Resource.Success ->{
                          _userData.value = AuthState(data = it.data)
                      }
                      is Resource.Error ->{
                          _userData.value = AuthState(error = it.message!!)
                      }
                      is Resource.Loading ->{
                          _userData.value = AuthState(isLoading = true)
                      }
                  }
              }.launchIn(viewModelScope)
          }

      }

    fun signUpWithPhonenumber(number:String, name:String, image: String, verificationId:String){
        viewModelScope.launch {
            repository.signUnWithPhonenumber(number, name, image, verificationId).onEach {
                when(it){
                    is Resource.Success ->{
                        _userData.value = AuthState(data = it.data)
                    }
                    is Resource.Error ->{
                        _userData.value = AuthState(error = it.message!!)
                    }
                    is Resource.Loading ->{
                        _userData.value = AuthState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun selectProfilePhoto(imageUri:Uri){
        viewModelScope.launch {
            repository.selectProfilePhoto(imageUri).onEach {
                when(it){
                    is Resource.Success -> {
                        _url.value = ImageState(imageUrl = it.data)
                    }
                    is Resource.Loading ->{
                        _url.value = ImageState(isLoading = true)
                    }
                    is Resource.Error->{
                        _url.value = ImageState(error = it.message.toString())
                    }
                }
            }
        }
    }


     fun getAllUsers(currentUserId:String){
        viewModelScope.launch {
            repository.retrieveAllUsers(currentUserId).onEach {

                when(it){
                    is ScreenState.Success ->{
                        _users.value = UserState(data = it.data!!)
                    }
                    is ScreenState.Error ->{
                        _users.value = UserState(error = it.message!!)
                    }
                    is ScreenState.Loading ->{
                        _users.value = UserState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getAllSearchUsers(str:String,currentUserId:String){
        viewModelScope.launch {
            repository.retrieveSearchUsers(str,currentUserId).onEach {

                when(it){
                    is State.Success ->{
                        _searchUsers.value = SearchUserState(data = it.data!!)
                    }
                    is State.Error ->{
                        _searchUsers.value = SearchUserState(error = it.message!!)
                    }
                    is State.Loading ->{
                        _searchUsers.value = SearchUserState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun sendMessage(senderId: String, userIdVisit: String, message: String,date:String,url:String){
        viewModelScope.launch {
            repository.sendMessages(senderId, userIdVisit, message,date,url)
        }
    }

    fun sendMultipleChats(date: String, listChats: List<Chat>){
        viewModelScope.launch {
            repository.sendMultipleChats(date, listChats)
        }
    }

    fun saveCalls(senderId: String,receiverId: String,senderName:String,receiverName:String,type:String,time:Long){
        viewModelScope.launch {
            repository.saveCalls(senderId,receiverId,senderName,receiverName,type,time)
        }
    }

    fun retrieveUser(id:String){
        viewModelScope.launch {
            repository.retrieveUser(id).onEach {
                when(it){
                  is  Screen.Success ->{
                        _singleUser.value = visitUserState(userVisit = it.data!!)
                    }
                    is Screen.Error ->{
                        _singleUser.value = visitUserState(error = it.message.toString())
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun uploadImageToFirebaseStorage(senderId: String,receiverId:String,imageUri:Uri,date: String){
        viewModelScope.launch {
            repository.uploadImageToFirebaseStorage(senderId, receiverId, imageUri,date)
        }
    }
    fun uploadAudioFileToFirebaseStorage(senderId: String,receiverId:String,audioFile: String,date: String){
        viewModelScope.launch {
            repository.uploadAudioFileToFirestore(senderId,receiverId, audioFile,date )
        }
    }
    fun uploadVideoFileToFirebaseStorage(senderId: String,receiverId:String,videoUri:Uri,date: String){
        viewModelScope.launch {
            repository.uploadVideoFileToFirestore(senderId,receiverId, videoUri,date )
        }
    }
    fun uploadStatusImagesToFirebaseStorage(userId: String,username:String,imageUrls:List<Uri>,adapter: RecentUpdatesAdapter){
        viewModelScope.launch {
            repository.uploadStatusImagesToFirebaseStorage(userId,username,imageUrls,adapter )
        }
    }

    fun getAllChats(senderId: String,receiverId: String){
                             viewModelScope.launch {
                                 repository.retrieveAllChats(senderId, receiverId).collectLatest {
                                     when(it){
                                         is ScreenState.Loading ->{

                                         }
                                         is ScreenState.Success ->{
                                             _chats.value = ChatState(data = it.data!!)
                                         }
                                         is ScreenState.Error ->{
                                             _chats.value = ChatState(error = it.message.toString())
                                         }
                                     }
                                 }
                             }

    }

    fun getAllStatus(userId:String){
        viewModelScope.launch {
            statusUsecase.getAllStatus(userId).collectLatest {
                when(it){
                    is ScreenState.Loading ->{

                    }
                    is ScreenState.Success->{
                          if(it.data != null){
                              _status.value = StatusState(data = it.data)
                          }else{
                              _status.value = StatusState(data = null)
                          }


                    }
                    is ScreenState.Error->{
                        _status.value = StatusState(error = it.message.toString())
                    }
                }
            }
        }
    }

    fun getAllViewedStatus(userId:String){
        viewModelScope.launch {
            statusUsecase.getAllViewedStatus(userId).collectLatest {
                when(it){
                    is ScreenState.Loading ->{

                    }
                    is ScreenState.Success->{
                        if(it.data != null){
                            _statusViewed.value = ViewedStatusState(data = it.data)
                        }else{
                            _statusViewed.value = ViewedStatusState(data = null)
                        }
                    }
                    is ScreenState.Error->{
                        _statusViewed.value = ViewedStatusState(error = it.message.toString())
                    }
                }
            }
        }
    }
    fun updateClickedStatus(id:String,currentUserId: String) =
        viewModelScope.launch {
            repository.updateClickedStatus(id,currentUserId)
        }

    fun deleteStatus(id:String) =
        viewModelScope.launch {
            repository.deleteStatus(id)
        }

    fun retrieveCurrentUserStatus(id:String){
        viewModelScope.launch {
            repository.retrieveCurrentUserStatus(id).onEach {
                when(it){
                    is  Screen.Success ->{
                        if(it.data != null){
                            _singleStatus.value = CurrentUserStatus(status = it.data)
                        }else{
                            _singleStatus.value = CurrentUserStatus(status = null)
                        }
                    }
                    is Screen.Error ->{
                       // _singleUser.value = visitUserState(error = it.message.toString())
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun retrieveLastMessage(userId:String,list: List<Users>){
        viewModelScope.launch {
            repository.retrieveLastMessage(userId, list).collectLatest {
                when(it){
                    is ScreenState.Success ->{
                       if(it.data!= null){
                           _lastMsg.value = LastMessageChat(chatDayList = it.data)
                       }else{
                           _lastMsg.value = LastMessageChat(chatDayList = null)
                       }
                    }
                    is ScreenState.Error ->{

                    }
                    is ScreenState.Loading ->{

                    }
                }
            }
        }
    }

    fun getAllChatsList(userId:String){
        viewModelScope.launch {
            repository.retrieveAllChatsList(userId).collectLatest {
                when(it){
                    is ScreenState.Loading ->{

                    }
                    is ScreenState.Success->{
                        if(it.data != null){
                            _chatsList.value = AllChatsListStatus(data = it.data)
                        }else{
                            _chatsList.value = AllChatsListStatus(data = null)
                        }
                    }
                    is ScreenState.Error->{
                        _chatsList.value = AllChatsListStatus(error = it.message.toString())
                    }
                }
            }
        }
    }

    fun getAllChatUsers(list:List<ChatsList>,searchQuery:String){
        viewModelScope.launch {
            repository.retrieveAllChatUsers(list,searchQuery).collectLatest {
                when(it){
                    is ScreenState.Loading ->{

                    }
                    is ScreenState.Success->{
                        if(it.data != null){
                            _users.value = UserState(data = it.data)
                        }else{
                            _users.value = UserState(data = null)
                        }
                    }
                    is ScreenState.Error->{
                        _users.value = UserState(error = it.message.toString())
                    }
                }
            }
        }
    }

    fun getUnreadMessages(id:String){
        viewModelScope.launch {
            repository.getUnreadMsgs(id).collectLatest {
                when(it){
                    is Screen.Loading ->{

                    }
                    is Screen.Success ->{
                        _unreadMsg.value = UnreadMsgState(unreadMsgs = it.data)
                    }
                    is Screen.Error->{
                        _unreadMsg.value = UnreadMsgState(error = it.message.toString())
                    }
                }
            }
        }
    }

    fun getAllCalls(id:String){
        viewModelScope.launch {
            repository.getAllCalls(id  ).collectLatest {
                when(it){
                    is ScreenState.Loading ->{

                    }
                    is ScreenState.Success ->{
                        _calls.value = CallsState(data = it.data!!)
                    }
                    is ScreenState.Error ->{
                        _calls.value = CallsState(error = it.message.toString())
                    }
                }
            }
        }

    }
}