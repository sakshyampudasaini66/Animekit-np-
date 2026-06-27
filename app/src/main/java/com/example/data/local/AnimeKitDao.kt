package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeKitDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<Product?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateProductStock(id: String, newStock: Int)

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    // --- CART ---
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // --- WISHLIST ---
    @Query("SELECT * FROM wishlist_items")
    fun getWishlistItems(): Flow<List<WishlistItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
    fun isWishlisted(productId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun removeFromWishlist(productId: String)

    @Query("DELETE FROM wishlist_items")
    suspend fun clearWishlist()

    // --- ORDERS ---
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("UPDATE orders SET status = :status, paymentStatus = :paymentStatus WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String, paymentStatus: String)

    // --- CHAT MESSAGES ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET isPinned = :isPinned WHERE id = :id")
    suspend fun updateMessagePinnedState(id: Int, isPinned: Boolean)

    @Query("UPDATE chat_messages SET isResolved = :isResolved")
    suspend fun markAllChatsResolved(isResolved: Boolean)

    @Query("UPDATE chat_messages SET isRead = 1")
    suspend fun markAllMessagesAsRead()

    // --- USER STATS ---
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    // --- ACHIEVEMENTS ---
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("UPDATE achievements SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockAchievement(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    // --- REVIEWS ---
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY date DESC")
    fun getReviewsForProduct(productId: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    // --- COUPONS ---
    @Query("SELECT * FROM coupons")
    fun getAllCoupons(): Flow<List<Coupon>>

    @Query("SELECT * FROM coupons WHERE code = :code LIMIT 1")
    suspend fun getCouponByCode(code: String): Coupon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: Coupon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<Coupon>)
}
