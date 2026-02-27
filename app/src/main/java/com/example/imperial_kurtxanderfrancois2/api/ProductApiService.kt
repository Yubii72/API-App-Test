package com.example.imperial_kurtxanderfrancois2.api

import com.example.imperial_kurtxanderfrancois2.data.ProductEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RemoteProduct(
    val id: String,
    val name: String,
    val price: Double,
    val lastModified: Long,
    val isSynced: Boolean?
)

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): List<RemoteProduct>

    @POST("products/sync")
    suspend fun syncProducts(@Body products: List<ProductEntity>): Response<Unit>
}
