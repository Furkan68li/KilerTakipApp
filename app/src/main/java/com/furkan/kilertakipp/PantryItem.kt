package com.furkan.kilertakipp

data class PantryItem(
    val id: String = "",          // Firestore'daki belge ID'si
    val name: String = "",        // Ürün adı
    val expiryDate: String = "",  // Son kullanma tarihi
    val category: String = "",    // Kategori (Süt, Bakliyat vb.)
    val userId: String = ""       // Ürünü ekleyen kullanıcının ID'si
)