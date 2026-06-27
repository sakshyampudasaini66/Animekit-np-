package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Product::class,
        Category::class,
        CartItem::class,
        WishlistItem::class,
        Order::class,
        ChatMessage::class,
        UserStats::class,
        Achievement::class,
        Review::class,
        Coupon::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeKitDao(): AnimeKitDao
}
