package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double, // In NPR
    val discountPrice: Double?, // In NPR (if any)
    val imageUrl: String,
    val category: String, // e.g. "Apparel", "Figures", "Accessories", "Posters"
    val anime: String, // e.g. "Naruto", "One Piece", etc.
    val stock: Int,
    val isPreOrder: Boolean = false,
    val isLimitedEdition: Boolean = false,
    val isFlashSale: Boolean = false,
    val rating: Float = 4.5f,
    val reviewsCount: Int = 0,
    val isBestSeller: Boolean = false,
    val isFeatured: Boolean = true,
    val estimatedDeliveryDays: Int = 3
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String // Icon string name
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val quantity: Int,
    val selectedSize: String = "M",
    val selectedColor: String = "Default"
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val date: Long = System.currentTimeMillis(),
    val status: String, // "Pending", "Processing", "Shipping", "Delivered", "Cancelled"
    val totalAmount: Double,
    val paymentMethod: String, // "Cash on Delivery", "eSewa", "Khalti"
    val paymentStatus: String, // "Paid", "Unpaid"
    val shippingAddress: String,
    val itemsJson: String, // JSON serialization of List<CartItemWithDetails>
    val customerName: String = "Sakshyam Pudasaini",
    val trackingNumber: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromAdmin: Boolean = false,
    val isPinned: Boolean = false,
    val isResolved: Boolean = false,
    val isRead: Boolean = false
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Single-row configuration
    val xp: Int = 0,
    val loyaltyPoints: Int = 100, // Starts with a welcome bonus!
    val level: Int = 1,
    val dailyStreak: Int = 1,
    val lastLoginTime: Long = System.currentTimeMillis(),
    val profilePhotoUrl: String = "",
    val username: String = "Sakshyam",
    val fullName: String = "Sakshyam Pudasaini",
    val email: String = "sakshyampudasaini66@gmail.com",
    val phoneNumber: String = "+977-9800000000",
    val deliveryAddress: String = "Nuwakot, Nepal"
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val xpReward: Int = 50,
    val pointsReward: Int = 10
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val productId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val imageUrl: String? = null,
    val date: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey val code: String,
    val discountPercentage: Double,
    val maxDiscount: Double,
    val minSpend: Double,
    val isActive: Boolean = true,
    val description: String = ""
)
