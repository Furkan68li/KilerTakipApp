package com.furkan.kilertakipp

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    userName: String,
    primaryColor: Color,
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val categories = listOf(
        "Tümü",
        "Sebze",
        "Meyve",
        "Bakliyat",
        "Süt Ürünleri",
        "Et & Protein",
        "İçecek",
        "Atıştırmalık",
        "Diğer"
    )

    var selectedCategory by remember { mutableStateOf("Tümü") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var pantryItems by remember { mutableStateOf(listOf<PantryItem>()) }

    // 🔥 Firestore realtime
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("pantry")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, _ ->
                    pantryItems = snapshot?.documents
                        ?.mapNotNull { it.toObject(PantryItem::class.java)?.copy(id = it.id) }
                        ?: emptyList()
                }
        }
    }

    // 🔍 Arama + kategori + SKT sıralama
    val filteredItems = pantryItems
        .filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        .filter {
            selectedCategory == "Tümü" || it.category == selectedCategory
        }
        .sortedBy {
            daysLeft(it.expiryDate)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kilerim") },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, null, tint = Color.Red)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = primaryColor
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            // 🔍 Arama
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Ürün ara...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // 🏷️ Kategori Filtre
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 📦 Liste
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz ürün yok", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(filteredItems) { item ->
                        PantryCard(
                            item = item,
                            onDelete = {
                                db.collection("pantry")
                                    .document(item.id)
                                    .delete()
                            }
                        )
                    }
                }
            }
        }
    }

    // ➕ Ürün Ekle
    if (showAddDialog) {
        AddProductDialog(
            categoryList = categories.filter { it != "Tümü" },
            onDismiss = { showAddDialog = false },
            onAdd = { name, date, category ->
                val newItem = PantryItem(
                    name = name,
                    expiryDate = date,
                    category = category,
                    userId = auth.currentUser?.uid ?: ""
                )
                db.collection("pantry").add(newItem)
                showAddDialog = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    categoryList: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categoryList.first()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Ürün Ekle") },
        text = {
            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ürün Adı") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categoryList.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    category = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    val c = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> date = "$d/${m + 1}/$y" },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(if (date.isEmpty()) "Son Kullanma Tarihi Seç" else date)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && date.isNotBlank()) {
                    onAdd(name, date, category)
                }
            }) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}


