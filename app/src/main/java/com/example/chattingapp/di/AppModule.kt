package com.example.chattingapp.di

import android.content.Context
import com.example.chattingapp.models.BaseAuthenticator
import com.example.chattingapp.use_cases.BaseAuthenticatorImpl
import com.example.chattingapp.repositories.UserRepository
import com.example.chattingapp.repositories.UserRepositoryImpl
import com.example.chattingapp.utilities.Constants.SHARED_PREFERENCES_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Provides
    fun provideContext(@ApplicationContext context:Context) : Context = context.applicationContext

    @Provides
    fun provideGson(): Gson = Gson()

    //@Singleton
    //@Provides
    //fun getFirebaseUser():String
    //      = FirebaseAuth.getInstance().currentUser!!.uid

@Singleton
@Provides
fun getFirebaseAuth():FirebaseAuth = FirebaseAuth.getInstance()



    // @Singleton
    //@Provides
    //fun getFirebaseUserDatabaseReference(): DatabaseReference =
    //FirebaseDatabase.getInstance().reference

    @Singleton
    @Provides
    fun getBaseAuthenticator(mAuth: FirebaseAuth):BaseAuthenticator {
        return BaseAuthenticatorImpl(mAuth)
    }

    @Singleton
    @Provides
    fun getUserRepository(authenticator: BaseAuthenticator):UserRepository{
        return UserRepositoryImpl(authenticator)
    }


}