package com.fbchat.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val GRAPH_API = "https://graph.facebook.com/v18.0/"

data class FBUser(val id: String, val name: String, val email: String?,
                  val picture: FBPic?)
data class FBPic(val data: FBPicData?)
data class FBPicData(val url: String?)
data class FBFriends(val data: List<FBFriend>?)
data class FBFriend(val id: String, val name: String, val picture: FBPic?)
data class FBComments(val data: List<FBComment>?)
data class FBComment(val id: String, val message: String?,
                     val from: FBFrom?, val created_time: String?)
data class FBFrom(val id: String, val name: String)
data class FBPost(val id: String)

interface FBService {
    @GET("me")
    suspend fun getProfile(@Query("fields") f: String = "id,name,email,picture",
                           @Query("access_token") t: String): Response<FBUser>
    @GET("me/friends")
    suspend fun getFriends(@Query("fields") f: String = "id,name,picture",
                           @Query("access_token") t: String): Response<FBFriends>
    @POST("{id}/feed")
    suspend fun createPost(@Path("id") id: String,
                           @Query("message") m: String,
                           @Query("access_token") t: String): Response<FBPost>
    @POST("{id}/comments")
    suspend fun sendMsg(@Path("id") id: String, @Query("message") m: String,
                        @Query("access_token") t: String): Response<FBPost>
    @GET("{id}/comments")
    suspend fun getMsgs(@Path("id") id: String,
                        @Query("fields") f: String = "id,message,from,created_time",
                        @Query("access_token") t: String): Response<FBComments>
}

object Api {
    val service: FBService = Retrofit.Builder()
        .baseUrl(GRAPH_API).addConverterFactory(GsonConverterFactory.create())
        .build().create(FBService::class.java)
}
