package com.example.imperial_kurtxanderfrancois2.data

import com.example.imperial_kurtxanderfrancois2.api.ProductApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ProductApiService
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun refreshProducts() {
        withContext(Dispatchers.IO) {
            try {
                val remoteProducts = apiService.getProducts()
                val localProducts = productDao.getAllProducts().first()
                
                val productsToUpdate = mutableListOf<ProductEntity>()
                
                for (remote in remoteProducts) {
                    val local = localProducts.find { it.id == remote.id }
                    if (local == null) {
                        productsToUpdate.add(remote.copy(isSynced = true))
                    } else {
                        // Conflict Resolution Strategy
                        if (remote.lastModified >= local.lastModified) {
                            productsToUpdate.add(remote.copy(isSynced = true))
                        }
                    }
                }
                
                if (productsToUpdate.isNotEmpty()) {
                    productDao.insertProducts(productsToUpdate)
                }

                // Sync local changes to remote
                syncLocalChanges()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncLocalChanges() {
        withContext(Dispatchers.IO) {
            try {
                val unsynced = productDao.getUnsyncedProducts()
                if (unsynced.isNotEmpty()) {
                    val response = apiService.syncProducts(unsynced)
                    if (response.isSuccessful) {
                        val synced = unsynced.map { it.copy(isSynced = true) }
                        productDao.insertProducts(synced)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveProduct(product: ProductEntity) {
        withContext(Dispatchers.IO) {
            productDao.insertProduct(product.copy(isSynced = false, lastModified = System.currentTimeMillis()))
            syncLocalChanges()
        }
    }
}
