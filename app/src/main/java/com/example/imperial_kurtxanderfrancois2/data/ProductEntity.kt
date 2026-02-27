package com.example.imperial_kurtxanderfrancois2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val lastModified: Long,
    val isSynced: Boolean
)
