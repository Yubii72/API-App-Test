package com.example.imperial_kurtxanderfrancois2.api

import com.example.imperial_kurtxanderfrancois2.data.ProductEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductEntity>

    @POST("products/sync")
    suspend fun syncProducts(@Body products: List<ProductEntity>): Response<Unit>
}
