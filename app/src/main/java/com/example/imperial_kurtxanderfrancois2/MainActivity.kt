package com.example.imperial_kurtxanderfrancois2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.imperial_kurtxanderfrancois2.api.ProductApiService
import com.example.imperial_kurtxanderfrancois2.data.ProductDatabase
import com.example.imperial_kurtxanderfrancois2.data.ProductEntity
import com.example.imperial_kurtxanderfrancois2.data.ProductRepository
import com.example.imperial_kurtxanderfrancois2.ui.ProductViewModel
import com.example.imperial_kurtxanderfrancois2.ui.ProductViewModelFactory
import com.example.imperial_kurtxanderfrancois2.ui.theme.Imperial_kurtxanderfrancois2Theme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val database by lazy { ProductDatabase.getDatabase(this) }
    
    private val apiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://d9ea8ebe-0506-4b40-a310-b1ea10d13fd6.mock.pstmn.io/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }

    private val repository by lazy { ProductRepository(database.productDao(), apiService) }
    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Imperial_kurtxanderfrancois2Theme {
                ProductListScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product List") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedProduct = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(products) { product ->
                ProductItem(product = product, onClick = {
                    selectedProduct = product
                    showDialog = true
                })
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            product = selectedProduct,
            onDismiss = { showDialog = false },
            onConfirm = { name, price ->
                val id = selectedProduct?.id ?: UUID.randomUUID().toString()
                viewModel.addOrUpdateProduct(id, name, price)
                showDialog = false
            }
        )
    }
}

@Composable
fun ProductItem(product: ProductEntity, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val lastModifiedText = remember(product.lastModified) {
        dateFormat.format(Date(product.lastModified))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "Price: $${product.price}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Last Modified: $lastModifiedText",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                if (!product.isSynced) {
                    Text(text = "Unsynced", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun ProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                onConfirm(name, p)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
