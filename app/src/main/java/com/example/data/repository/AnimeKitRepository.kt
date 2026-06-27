package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AnimeKitDao
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration

class AnimeKitRepository(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "animekit_database"
        ).fallbackToDestructiveMigration().build()
    }

    val dao: AnimeKitDao by lazy { database.animeKitDao() }

    // --- REPLAY CHANNELS / FLOWS ---
    val products: Flow<List<Product>> = dao.getAllProducts()
    val categories: Flow<List<Category>> = dao.getAllCategories()
    val cartItems: Flow<List<CartItem>> = dao.getCartItems()
    val wishlistItems: Flow<List<WishlistItem>> = dao.getWishlistItems()
    val orders: Flow<List<Order>> = dao.getAllOrders()
    val chatMessages: Flow<List<ChatMessage>> = dao.getAllChatMessages()
    val userStats: Flow<UserStats?> = dao.getUserStats()
    val achievements: Flow<List<Achievement>> = dao.getAllAchievements()
    val coupons: Flow<List<Coupon>> = dao.getAllCoupons()

    var currentUserId: String = ""
    var currentUserEmail: String = ""

    private var userStatsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var wishlistListener: ListenerRegistration? = null
    private var achievementsListener: ListenerRegistration? = null

    init {
        initializeFirebaseSync()
        // Run database initialization in background
        CoroutineScope(Dispatchers.IO).launch {
            checkAndPrepopulate()
        }
    }

    private suspend fun checkAndPrepopulate() {
        val existingProducts = products.first()
        if (existingProducts.isEmpty()) {
            prepopulateAll()
        }
    }

    suspend fun getProductReviews(productId: String): Flow<List<Review>> {
        return dao.getReviewsForProduct(productId)
    }

    suspend fun isProductWishlisted(productId: String): Flow<Boolean> {
        return dao.isWishlisted(productId)
    }

    // --- CART ACTIONS ---
    suspend fun addToCart(productId: String, size: String = "M", color: String = "Default") {
        withContext(Dispatchers.IO) {
            val existing = cartItems.first().find { it.productId == productId && it.selectedSize == size }
            if (existing != null) {
                dao.updateCartQuantity(existing.id, existing.quantity + 1)
            } else {
                dao.insertCartItem(CartItem(productId = productId, quantity = 1, selectedSize = size, selectedColor = color))
            }
        }
    }

    suspend fun updateCartQty(cartId: Int, qty: Int) {
        withContext(Dispatchers.IO) {
            if (qty <= 0) {
                dao.deleteCartItem(cartId)
            } else {
                dao.updateCartQuantity(cartId, qty)
            }
        }
    }

    suspend fun deleteCartItem(cartId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteCartItem(cartId)
        }
    }

    suspend fun clearCart() {
        withContext(Dispatchers.IO) {
            dao.clearCart()
        }
    }

    // --- WISHLIST ACTIONS ---
    suspend fun toggleWishlist(productId: String) {
        withContext(Dispatchers.IO) {
            val isCurrentlyWishlisted = dao.isWishlisted(productId).first()
            if (isCurrentlyWishlisted) {
                dao.removeFromWishlist(productId)
                val db = FirebaseSyncHelper.getFirestore()
                if (db != null && currentUserId.isNotEmpty()) {
                    db.collection("users").document(currentUserId).collection("wishlist").document(productId).delete()
                }
            } else {
                dao.addToWishlist(WishlistItem(productId))
                val db = FirebaseSyncHelper.getFirestore()
                if (db != null && currentUserId.isNotEmpty()) {
                    db.collection("users").document(currentUserId).collection("wishlist").document(productId)
                        .set(mapOf("productId" to productId))
                }
            }
        }
    }

    // --- ORDER ACTIONS ---
    suspend fun placeOrder(
        items: List<CartItem>,
        total: Double,
        paymentMethod: String,
        address: String,
        couponCode: String?,
        discountAmount: Double
    ): Order {
        return withContext(Dispatchers.IO) {
            val orderId = "AK-${(10000..99999).random()}"
            val itemsJson = items.joinToString(separator = ";") { "${it.productId}:${it.quantity}:${it.selectedSize}" }
            
            // Adjust stocks
            items.forEach { cartItem ->
                val prod = products.first().find { it.id == cartItem.productId }
                if (prod != null) {
                    val remainingStock = maxOf(0, prod.stock - cartItem.quantity)
                    dao.updateProductStock(prod.id, remainingStock)
                }
            }

            val order = Order(
                id = orderId,
                status = "Pending",
                totalAmount = total,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "Cash on Delivery") "Unpaid" else "Paid",
                shippingAddress = address,
                itemsJson = itemsJson,
                trackingNumber = "TRACK-${(100000..999999).random()}"
            )
            dao.insertOrder(order)
            dao.clearCart()

            // Reward loyalty points (1 point per 100 NPR spent)
            val currentStats = userStats.first() ?: UserStats()
            val pointsEarned = (total / 100).toInt()
            val xpEarned = pointsEarned * 2
            
            val updatedStats = currentStats.copy(
                loyaltyPoints = currentStats.loyaltyPoints + pointsEarned,
                xp = currentStats.xp + xpEarned,
                level = (currentStats.xp + xpEarned) / 500 + 1
            )
            dao.insertUserStats(updatedStats)

            // Cloud sync order and stats
            val db = FirebaseSyncHelper.getFirestore()
            if (db != null && currentUserId.isNotEmpty()) {
                val orderMap = mapOf(
                    "id" to order.id,
                    "date" to order.date,
                    "status" to order.status,
                    "totalAmount" to order.totalAmount,
                    "paymentMethod" to order.paymentMethod,
                    "paymentStatus" to order.paymentStatus,
                    "shippingAddress" to order.shippingAddress,
                    "itemsJson" to order.itemsJson,
                    "customerName" to updatedStats.fullName,
                    "trackingNumber" to order.trackingNumber
                )
                db.collection("users").document(currentUserId).collection("orders").document(order.id).set(orderMap)
                db.collection("orders").document(order.id).set(orderMap)

                val statsMap = mapOf(
                    "xp" to updatedStats.xp,
                    "loyaltyPoints" to updatedStats.loyaltyPoints,
                    "level" to updatedStats.level,
                    "dailyStreak" to updatedStats.dailyStreak,
                    "lastLoginTime" to updatedStats.lastLoginTime,
                    "profilePhotoUrl" to updatedStats.profilePhotoUrl,
                    "username" to updatedStats.username,
                    "fullName" to updatedStats.fullName,
                    "email" to updatedStats.email,
                    "phoneNumber" to updatedStats.phoneNumber,
                    "deliveryAddress" to updatedStats.deliveryAddress
                )
                db.collection("users").document(currentUserId).collection("stats").document("user_stats").set(statsMap)
            }

            // Trigger order achievement
            val totalOrders = dao.getAllOrders().first().size
            if (totalOrders >= 1) {
                unlockAchievement("first_buy")
            }
            if (totalOrders >= 5) {
                unlockAchievement("anime_collector")
            }

            order
        }
    }

    // --- CHAT ACTIONS ---
    suspend fun sendChatMessage(text: String, isFromAdmin: Boolean = false, imageUrl: String? = null) {
        withContext(Dispatchers.IO) {
            val stats = userStats.first()
            val senderId = if (isFromAdmin) "admin" else (currentUserId.ifEmpty { "customer" })
            val senderName = if (isFromAdmin) "Anime Kit Admin" else (stats?.fullName ?: "Sakshyam Pudasaini")
            val message = ChatMessage(
                id = (100000..999999).random(),
                senderId = senderId,
                senderName = senderName,
                text = text,
                imageUrl = imageUrl,
                isFromAdmin = isFromAdmin,
                timestamp = System.currentTimeMillis()
            )
            dao.insertChatMessage(message)
            
            val db = FirebaseSyncHelper.getFirestore()
            if (db != null) {
                val msgMap = mapOf(
                    "id" to message.id,
                    "senderId" to message.senderId,
                    "senderName" to message.senderName,
                    "text" to message.text,
                    "imageUrl" to message.imageUrl,
                    "timestamp" to message.timestamp,
                    "isFromAdmin" to message.isFromAdmin,
                    "isPinned" to message.isPinned,
                    "isResolved" to message.isResolved,
                    "isRead" to message.isRead
                )
                db.collection("chat_messages").document("msg_${message.timestamp}").set(msgMap)
            }

            // If from customer, trigger automated admin greeting/status sometimes for interactivity
            if (!isFromAdmin) {
                triggerAdminAutoReply(text)
            }
        }
    }

    suspend fun markAllChatMessagesAsRead() {
        withContext(Dispatchers.IO) {
            dao.markAllMessagesAsRead()
            val db = FirebaseSyncHelper.getFirestore()
            if (db != null) {
                db.collection("chat_messages").get().addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { doc ->
                        doc.reference.update("isRead", true)
                    }
                }
            }
        }
    }

    private suspend fun triggerAdminAutoReply(userMessage: String) {
        val response = when {
            userMessage.contains("hello", ignoreCase = true) || userMessage.contains("hi", ignoreCase = true) -> 
                "Namaste! Welcome to Anime Kit Customer Support. How can we help you with your anime merch today? 🌸"
            userMessage.contains("price", ignoreCase = true) || userMessage.contains("cost", ignoreCase = true) ->
                "All our prices are in Nepalese Rupees (NPR) and include VAT. Delivery across Nepal is standard Rs. 150!"
            userMessage.contains("delivery", ignoreCase = true) || userMessage.contains("shipping", ignoreCase = true) ->
                "We deliver Nepal-wide! Inside Kathmandu valley takes 1-2 days. Outside takes 3-4 days via Nepal post or local courier."
            userMessage.contains("esewa", ignoreCase = true) || userMessage.contains("khalti", ignoreCase = true) ->
                "Yes, we accept eSewa, Khalti, and Cash on Delivery! Scan code options will be shown during checkout."
            else -> "Thank you for reaching out! The admin has been notified and will reply in real-time shortly. Feel free to browse our exclusive anime hoodies!"
        }
        
        // Simulating immediate Admin response
        kotlinx.coroutines.delay(1000)
        sendChatMessage(response, isFromAdmin = true)
    }

    suspend fun updateMessagePinned(id: Int, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateMessagePinnedState(id, isPinned)
        }
    }

    suspend fun resolveAllChats() {
        withContext(Dispatchers.IO) {
            dao.markAllChatsResolved(true)
        }
    }

    // --- REVIEWS ---
    suspend fun addReview(productId: String, rating: Int, comment: String, imageUrl: String? = null) {
        withContext(Dispatchers.IO) {
            val stats = userStats.first() ?: UserStats()
            val review = Review(
                id = "REV-${(10000..99999).random()}",
                productId = productId,
                userName = stats.fullName,
                rating = rating,
                comment = comment,
                imageUrl = imageUrl,
                isVerified = true
            )
            dao.insertReview(review)
            
            val db = FirebaseSyncHelper.getFirestore()
            if (db != null) {
                val revMap = mapOf(
                    "id" to review.id,
                    "productId" to review.productId,
                    "userName" to review.userName,
                    "rating" to review.rating,
                    "comment" to review.comment,
                    "imageUrl" to review.imageUrl,
                    "date" to review.date,
                    "isVerified" to review.isVerified
                )
                db.collection("reviews").document(review.id).set(revMap)
            }
            
            // Gain Loyalty Points for review
            val updatedStats = stats.copy(
                loyaltyPoints = stats.loyaltyPoints + 15,
                xp = stats.xp + 30
            )
            saveUserStatsAndProfile(currentUserId, updatedStats)
            unlockAchievement("critic")
        }
    }

    // --- STATS & REWARDS ---
    suspend fun claimDailyReward() {
        withContext(Dispatchers.IO) {
            val stats = userStats.first() ?: UserStats()
            val pointsBonus = 25 * stats.dailyStreak
            val xpBonus = 50 * stats.dailyStreak
            
            val updatedStats = stats.copy(
                loyaltyPoints = stats.loyaltyPoints + pointsBonus,
                xp = stats.xp + xpBonus,
                dailyStreak = stats.dailyStreak + 1,
                lastLoginTime = System.currentTimeMillis()
            )
            saveUserStatsAndProfile(currentUserId, updatedStats)
            unlockAchievement("daily_devoted")
        }
    }

    suspend fun addRewardPoints(points: Int, xp: Int) {
        withContext(Dispatchers.IO) {
            val stats = userStats.first() ?: UserStats()
            val updatedStats = stats.copy(
                loyaltyPoints = stats.loyaltyPoints + points,
                xp = stats.xp + xp,
                level = (stats.xp + xp) / 500 + 1
            )
            saveUserStatsAndProfile(currentUserId, updatedStats)
        }
    }

    suspend fun unlockAchievement(id: String) {
        withContext(Dispatchers.IO) {
            val achievementsList = achievements.first()
            val ach = achievementsList.find { it.id == id }
            if (ach != null && !ach.isUnlocked) {
                dao.unlockAchievement(id)
                
                val db = FirebaseSyncHelper.getFirestore()
                if (db != null && currentUserId.isNotEmpty()) {
                    db.collection("users").document(currentUserId).collection("achievements").document(id)
                        .set(mapOf("id" to id, "isUnlocked" to true))
                }

                // Add points/xp reward
                val stats = userStats.first() ?: UserStats()
                val updatedStats = stats.copy(
                    xp = stats.xp + ach.xpReward,
                    loyaltyPoints = stats.loyaltyPoints + ach.pointsReward
                )
                saveUserStatsAndProfile(currentUserId, updatedStats)
            }
        }
    }

    // --- PREPOPULATE DATA ---
    private suspend fun prepopulateAll() {
        // 1. Categories
        val cats = listOf(
            Category("apparel", "Apparel", "ic_apparel"),
            Category("figures", "Figures", "ic_figures"),
            Category("accessories", "Accessories", "ic_accessories"),
            Category("posters", "Wall Art", "ic_posters")
        )
        dao.insertCategories(cats)

        // 2. Initial User Stats
        dao.insertUserStats(UserStats(loyaltyPoints = 150))

        // 3. Achievements
        val achs = listOf(
            Achievement("daily_devoted", "Otaku Devotion", "Claim your daily login reward inside the Fun Zone", false, 50, 10),
            Achievement("first_buy", "First Merch", "Place your first order in Nepal's premium anime shop", false, 100, 20),
            Achievement("quiz_master", "Hokage Knowledge", "Score a perfect 5/5 in the Anime Quiz", false, 150, 30),
            Achievement("critic", "Merch Critic", "Submit your first photo or text review for a product", false, 60, 15),
            Achievement("anime_collector", "Master Collector", "Place 5 orders to build your anime shrine", false, 300, 100),
            Achievement("memory_god", "Infinite Intellect", "Win the Memory Match game with minimal moves", false, 100, 25)
        )
        dao.insertAchievements(achs)

        // 4. Coupons
        val initialCoupons = listOf(
            Coupon("NEPALOTAKU", 10.0, 500.0, 1000.0, true, "10% off for Nepal Otakus on orders above Rs. 1000"),
            Coupon("CRIMSONRED", 15.0, 1000.0, 2500.0, true, "15% discount for Premium shoppers above Rs. 2500"),
            Coupon("ANIMEKIT", 20.0, 200.0, 500.0, true, "Flat 20% off on your first welcome order!"),
            Coupon("FREEPOST", 100.0, 150.0, 1500.0, true, "Free delivery on anime merch orders above Rs. 1500")
        )
        dao.insertCoupons(initialCoupons)

        // 5. 40 Products (Realistic NPR Prices)
        val prods = mutableListOf<Product>()
        
        val animeList = listOf(
            "Naruto", "One Piece", "Dragon Ball", "Demon Slayer", 
            "Jujutsu Kaisen", "Attack on Titan", "Solo Leveling", 
            "My Hero Academia", "Chainsaw Man", "Blue Lock", 
            "Haikyuu", "Spy x Family"
        )

        // Helper to generate products
        var prodId = 1
        fun addProd(
            name: String,
            desc: String,
            price: Double,
            discountPrice: Double?,
            category: String,
            anime: String,
            stock: Int,
            isPre: Boolean = false,
            isLim: Boolean = false,
            isFlash: Boolean = false,
            rating: Float = 4.8f,
            isBest: Boolean = false,
            isFeat: Boolean = true
        ) {
            prods.add(
                Product(
                    id = "P-${1000 + prodId}",
                    name = name,
                    description = desc,
                    price = price,
                    discountPrice = discountPrice,
                    imageUrl = "https://images.unsplash.com/photo-example", // We use elegant CSS / placeholder rendering fallback in UI
                    category = category,
                    anime = anime,
                    stock = stock,
                    isPreOrder = isPre,
                    isLimitedEdition = isLim,
                    isFlashSale = isFlash,
                    rating = rating,
                    reviewsCount = (5..32).random(),
                    isBestSeller = isBest,
                    isFeatured = isFeat
                )
            )
            prodId++
        }

        // Naruto
        addProd("Akatsuki Cloud Oversized Hoodie", "Premium heavyweight cotton black hoodie featuring the embroidered Akatsuki red cloud from Naruto Shippuden. Perfect for Nepali winters.", 3499.0, 2999.0, "apparel", "Naruto", 15, isBest = true)
        addProd("Kakashi Hatake Nendoroid Action Figure", "Authentic premium scale Kakashi Hatake figure with interchangeable Raikiri (Lightning Blade) and Icha Icha book accessories.", 4999.0, null, "figures", "Naruto", 8, isLim = true)
        addProd("Leaf Village Metal Headband", "Sturdy alloy metal plate headband with Konohagakure emblem embossed. High quality fabric strap for anime cosplay in Nepal.", 450.0, 350.0, "accessories", "Naruto", 50)
        addProd("Itachi Uchiha LED Desk Lamp", "3D optical illusion night light with Itachi Uchiha Mangekyou Sharingan design. Includes 16 dynamic colors and touch remote control.", 1899.0, 1499.0, "posters", "Naruto", 12, isFlash = true)

        // One Piece
        addProd("Monkey D. Luffy Gear 5 Figure", "Extremely detailed action figure of Luffy in his ultimate Gear 5 Sun God Nika form, with custom cloud base and laughing pose.", 6499.0, 5999.0, "figures", "One Piece", 5, isLim = true, isBest = true)
        addProd("Straw Hat Pirates Wanted Poster Set", "Complete premium high-gloss cardstock posters set of all 10 Straw Hat crew members with updated post-Wano bounties.", 1299.0, 999.0, "posters", "One Piece", 30)
        addProd("Roronoa Zoro Three-Sword Keychain", "Alloy keychain replica set featuring Zoro's iconic swords: Wado Ichimonji, Shusui, and Sandai Kitetsu.", 750.0, 590.0, "accessories", "One Piece", 25)
        addProd("Luffy Straw Hat Classic", "Exact replica of Shanks' classic straw hat passed down to Monkey D. Luffy. High-quality straw fiber weaving with red ribbon.", 1200.0, null, "accessories", "One Piece", 15)

        // Dragon Ball
        addProd("Goku Super Saiyan Blue Figure", "Master Stars Piece Super Saiyan God Blue Goku performing the legendary Kamehameha strike. Stands 25cm tall.", 5499.0, null, "figures", "Dragon Ball", 10)
        addProd("Capsule Corp Athletic Joggers", "Super comfy black training joggers styled with the Capsule Corp sci-fi logo print. Unisex fit for active otakus.", 2299.0, 1899.0, "apparel", "Dragon Ball", 20, isFlash = true)
        addProd("Set of 7 Crystal Dragon Balls", "Complete collection of the seven 4.5cm orange transparent acrylic crystal stars. Presented in a luxury golden gift box.", 3999.0, 3299.0, "accessories", "Dragon Ball", 4, isLim = true)

        // Demon Slayer
        addProd("Tanjiro Kamado Checkered Haori", "Lightweight traditional haori coat with Tanjiro's signature black and green checkerboard pattern. Silk polyester blend.", 1999.0, 1699.0, "apparel", "Demon Slayer", 15)
        addProd("Nezuko Box Wooden Replica Back Cabinet", "Handcrafted premium paulownia dark wood replica of Tanjiro's Nezuko carrying cabinet box. Multi-use shelf drawer unit.", 8999.0, null, "figures", "Demon Slayer", 2, isPre = true, isLim = true)
        addProd("Kyojuro Rengoku Flame Katana", "Replica Nichirin steel-colored Katana sword for flame breathing. Non-sharp decorative replica with flame guard hilt.", 4500.0, 3999.0, "accessories", "Demon Slayer", 7, isBest = true)
        addProd("Demon Slayer Slayer Corps Keyring", "Minimalist black metal keyring stamped with the legendary 'Destroy' kanji. Elegant accessory for daily keys.", 399.0, 299.0, "accessories", "Demon Slayer", 100)

        // Jujutsu Kaisen
        addProd("Ryomen Sukuna Cursed Finger Replica", "Realistic 1:1 scale resin replica of Sukuna's decaying cursed finger. Gift box included. Do not consume!", 999.0, 799.0, "accessories", "Jujutsu Kaisen", 40, isFlash = true)
        addProd("Gojo Satoru 'Infinite Void' Hoodie", "Ultra-soft crimson red inner hoodie with Gojo's purple Domain Expansion hand sign minimalist embroidery on chest.", 3499.0, null, "apparel", "Jujutsu Kaisen", 18, isBest = true)
        addProd("Megumi Fushiguro Divine Dog Plush", "Adorable high-quality white shadow divine dog plush toy from Jujutsu Kaisen. Super fluffy stuffing.", 1500.0, 1200.0, "accessories", "Jujutsu Kaisen", 14)
        addProd("Jujutsu High School Metal Enamel Pin", "Replica pin badge of the Tokyo Prefectural Jujutsu High School emblem. Looks classic on backpacks and hoodies.", 350.0, null, "accessories", "Jujutsu Kaisen", 60)

        // Attack on Titan
        addProd("Scouting Legion Green Cloak", "Iconic dark green wool-blend cloak with the fully embroidered 'Wings of Freedom' emblem of the Scouting Regiment.", 2499.0, 2199.0, "apparel", "Attack on Titan", 22, isBest = true)
        addProd("Eren Jaeger Founding Titan Poster", "Giant high-definition retro-styled poster featuring Eren Jaeger's colossal Founding Titan rumbling advance over Marley.", 850.0, 650.0, "posters", "Attack on Titan", 35)
        addProd("Basement Key Pendant Necklace", "Alloy bronze key replica of Grisha Jaeger's basement key that holds the dark secrets of Eldia.", 450.0, 349.0, "accessories", "Attack on Titan", 45)

        // Solo Leveling
        addProd("Sung Jin-Woo 'Shadow Monarch' Tee", "Pure black heavy-weight cotton t-shirt with vivid glowing neon graphic of Sung Jin-Woo summoning Igris and his shadow army.", 1799.0, 1499.0, "apparel", "Solo Leveling", 25, isBest = true)
        addProd("Kasaka's Venom Fang Dagger Resin Replica", "1:1 scale resin display replica of Sung Jinwoo's first powerful boss drop weapon. Styled with glowing purple venom core.", 7499.0, null, "figures", "Solo Leveling", 3, isPre = true, isLim = true)

        // My Hero Academia
        addProd("Deku One For All Glow Glove Set", "Lycra sports-grade fingerless gloves with glow-in-the-dark energy lines representing Deku's Full Cowling power.", 1199.0, 950.0, "accessories", "My Hero Academia", 16)
        addProd("All Might 'Go Beyond Plus Ultra' Figurine", "Supreme action pose All Might sculpture shouting his ultimate united states of smash banner.", 5800.0, 4999.0, "figures", "My Hero Academia", 6)

        // Chainsaw Man
        addProd("Pochita Plush Toy Cushion", "Full size 40cm orange Pochita chainsaw dog plush pillow with plush cord and soft stuffed chainsaw head blade. Super cute!", 2499.0, 1999.0, "accessories", "Chainsaw Man", 12, isBest = true)
        addProd("Chainsaw Man Denji Action Figure", "Highly detailed Denji transformation figure featuring Blood Splatter elements and moving head chainsaw joints.", 6999.0, 6200.0, "figures", "Chainsaw Man", 4, isLim = true)
        addProd("Power Blood Scythe Keychain", "Premium metal keyring designed with Power's crimson red blood scythe weapon and demon horns silhouette.", 499.0, 399.0, "accessories", "Chainsaw Man", 30)

        // Blue Lock
        addProd("Isagi Yoichi Bastard Munchen Jersey", "Breathable polyester soccer jersey with Isagi Yoichi's number 11 and official German league sponsor print style.", 2100.0, 1750.0, "apparel", "Blue Lock", 25, isFlash = true)
        addProd("Blue Lock Metallic Logo Badge", "Polished silver zinc alloy shield pin badge featuring the iconic chains and polygon football Blue Lock project emblem.", 450.0, null, "accessories", "Blue Lock", 50)

        // Haikyuu
        addProd("Karasuno High Volleyball Jersey #10", "Classic black and orange Karasuno High School volleyball team jersey with Shoyo Hinata's number 10 on front and back.", 1850.0, 1599.0, "apparel", "Haikyuu", 20)
        addProd("Nekoma High Red Training Jacket", "Sleek red zip-up athletic training jacket of Nekoma High volleyball club, printed with 'Nekoma' kanji.", 2699.0, null, "apparel", "Haikyuu", 12)
        addProd("Mikasa Volleyball Replica Keychain", "Miniature leather-feel replica volleyball keychain inspired by Haikyuu's match balls. Hand-stitched look.", 399.0, 299.0, "accessories", "Haikyuu", 80)

        // Spy x Family
        addProd("Anya Forger 'Waku Waku' Face Tee", "Soft pastel pink premium cotton t-shirt with Anya Forger's legendary smug smiling face graphic. Extremely cute.", 1699.0, 1399.0, "apparel", "Spy x Family", 30, isBest = true)
        addProd("Loid Forger Spy Twilight Figure", "Sleek scale figure of Loid Forger in his signature green three-piece suit holding a silencer handgun on a stone base.", 5200.0, null, "figures", "Spy x Family", 7)
        addProd("Chimera Lion Plush Doll", "Anya's favourite Chimera plush doll replica with pink lion mane, green wings, and snake tail. Authentic spy companion.", 1899.0, 1550.0, "accessories", "Spy x Family", 15)

        // Added more to reach 40 products as requested!
        addProd("Naruto Sage Mode Canvas Scroll Poster", "Large premium hanging fabric canvas scroll wall art featuring Naruto Uzumaki in Toad Sage Mode with giant scrolls.", 1499.0, 1199.0, "posters", "Naruto", 20)
        addProd("Zoro Wano Kuni Kimono Figurine", "Zoro in his ronin Zorojuro green kimono holding his swords ready to slash. Exceptionally high paint detail.", 5999.0, null, "figures", "One Piece", 6)
        addProd("Demon Slayer Tanjirou Earring Keyrings", "Metal pendant keyring modeled exactly like Tanjiro's Hanafuda earrings. Double-sided graphic.", 350.0, 250.0, "accessories", "Demon Slayer", 120)
        addProd("Jujutsu Kaisen Gojo Eyes Sleep Mask", "Comfortable soft padded eye mask printed with Gojo Satoru's piercing blue eyes. Block out all light in Nepali buses.", 499.0, 399.0, "accessories", "Jujutsu Kaisen", 75, isFlash = true)

        dao.insertProducts(prods)

        // 6. Prepopulate Reviews
        val sampleReviews = listOf(
            Review("R1", "P-1001", "Sakshyam Pudasaini", 5, "Unbelievable quality! The Akatsuki cloud embroidery is thick and robust. Extremely warm and comfortable for Kathmandu winters. Highly recommended!", null, System.currentTimeMillis() - 86400000 * 2, true),
            Review("R2", "P-1001", "Amir Shrestha", 4, "Amazing fit. Oversized but holds its shape. The crimson red color on the inner hood is very premium.", null, System.currentTimeMillis() - 86400000 * 5, true),
            Review("R3", "P-1005", "Rohan Thapa", 5, "Gear 5 Luffy is stunning! The cloud stand is beautiful and looks amazing on my desk. Shipped to Nuwakot in just 3 days!", null, System.currentTimeMillis() - 86400000 * 1, true),
            Review("R4", "P-1014", "Niranjan Giri", 5, "The Rengoku Katana is heavy and looks like actual steel! The flame guard detail is beautiful.", null, System.currentTimeMillis() - 86400000 * 4, true),
            Review("R5", "P-1027", "Prativa Adhikari", 5, "Pochita is so soft! Standard size is perfect to cuddle. Worth every rupee! Waku waku!", null, System.currentTimeMillis() - 86400000 * 3, true)
        )
        dao.insertReviews(sampleReviews)

        // 7. Prepopulate Default Chat Greetings
        val chatGifts = listOf(
            ChatMessage(senderId = "admin", senderName = "Anime Kit Admin", text = "Welcome to Anime Kit! 🇳🇵 Nepal's premium anime merchandise boutique. Chat with us here live for orders, stock inquiries, or suggestions!", timestamp = System.currentTimeMillis() - 1000 * 60 * 10, isFromAdmin = true)
        )
        chatGifts.forEach { dao.insertChatMessage(it) }
    }

    fun initializeFirebaseSync() {
        FirebaseSyncHelper.initialize(context)
        if (!FirebaseSyncHelper.isFirebaseAvailable) {
            Log.w("AnimeKitRepository", "Firebase is not available. Sync disabled.")
            return
        }

        val db = FirebaseSyncHelper.getFirestore() ?: return

        // 1. Products real-time listener
        db.collection("products").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("AnimeKitRepository", "Products sync failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            discountPrice = doc.getDouble("discountPrice"),
                            imageUrl = doc.getString("imageUrl") ?: "",
                            category = doc.getString("category") ?: "",
                            anime = doc.getString("anime") ?: "",
                            stock = doc.getLong("stock")?.toInt() ?: 0,
                            isPreOrder = doc.getBoolean("isPreOrder") ?: false,
                            isLimitedEdition = doc.getBoolean("isLimitedEdition") ?: false,
                            isFlashSale = doc.getBoolean("isFlashSale") ?: false,
                            rating = doc.getDouble("rating")?.toFloat() ?: 4.5f,
                            reviewsCount = doc.getLong("reviewsCount")?.toInt() ?: 0,
                            isBestSeller = doc.getBoolean("isBestSeller") ?: false,
                            isFeatured = doc.getBoolean("isFeatured") ?: true,
                            estimatedDeliveryDays = doc.getLong("estimatedDeliveryDays")?.toInt() ?: 3
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
                if (list.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insertProducts(list)
                    }
                } else {
                    uploadDefaultProductsToFirestore()
                }
            }
        }

        // 2. Coupons real-time listener
        db.collection("coupons").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        Coupon(
                            code = doc.getString("code") ?: doc.id,
                            discountPercentage = doc.getDouble("discountPercentage") ?: 0.0,
                            maxDiscount = doc.getDouble("maxDiscount") ?: 0.0,
                            minSpend = doc.getDouble("minSpend") ?: 0.0,
                            isActive = doc.getBoolean("isActive") ?: true,
                            description = doc.getString("description") ?: ""
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
                if (list.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insertCoupons(list)
                    }
                }
            }
        }

        // 3. Chat Messages real-time listener
        db.collection("chat_messages").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.getLong("id")?.toInt() ?: 0,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            text = doc.getString("text") ?: "",
                            imageUrl = doc.getString("imageUrl"),
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            isFromAdmin = doc.getBoolean("isFromAdmin") ?: false,
                            isPinned = doc.getBoolean("isPinned") ?: false,
                            isResolved = doc.getBoolean("isResolved") ?: false
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
                if (list.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        list.forEach { dao.insertChatMessage(it) }
                    }
                }
            }
        }
    }

    fun startUserSpecificSync(uid: String) {
        currentUserId = uid
        val db = FirebaseSyncHelper.getFirestore() ?: return

        stopUserSpecificSync()

        // Sync UserStats
        userStatsListener = db.collection("users").document(uid).collection("stats").document("user_stats")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val stats = UserStats(
                            id = 1,
                            xp = snapshot.getLong("xp")?.toInt() ?: 0,
                            loyaltyPoints = snapshot.getLong("loyaltyPoints")?.toInt() ?: 100,
                            level = snapshot.getLong("level")?.toInt() ?: 1,
                            dailyStreak = snapshot.getLong("dailyStreak")?.toInt() ?: 1,
                            lastLoginTime = snapshot.getLong("lastLoginTime") ?: System.currentTimeMillis(),
                            profilePhotoUrl = snapshot.getString("profilePhotoUrl") ?: "",
                            username = snapshot.getString("username") ?: "Sakshyam",
                            fullName = snapshot.getString("fullName") ?: "Sakshyam Pudasaini",
                            email = snapshot.getString("email") ?: "sakshyampudasaini66@gmail.com",
                            phoneNumber = snapshot.getString("phoneNumber") ?: "+977-9800000000",
                            deliveryAddress = snapshot.getString("deliveryAddress") ?: "Nuwakot, Nepal"
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            dao.insertUserStats(stats)
                        }
                    } catch (ex: Exception) {
                        Log.e("AnimeKitRepository", "Error parsing stats", ex)
                    }
                }
            }

        // Sync Orders
        ordersListener = db.collection("users").document(uid).collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            Order(
                                id = doc.getString("id") ?: doc.id,
                                date = doc.getLong("date") ?: System.currentTimeMillis(),
                                status = doc.getString("status") ?: "Pending",
                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                paymentMethod = doc.getString("paymentMethod") ?: "Cash on Delivery",
                                paymentStatus = doc.getString("paymentStatus") ?: "Unpaid",
                                shippingAddress = doc.getString("shippingAddress") ?: "",
                                itemsJson = doc.getString("itemsJson") ?: "",
                                customerName = doc.getString("customerName") ?: "Sakshyam Pudasaini",
                                trackingNumber = doc.getString("trackingNumber") ?: ""
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        list.forEach { dao.insertOrder(it) }
                    }
                }
            }

        // Sync Wishlist
        wishlistListener = db.collection("users").document(uid).collection("wishlist")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            WishlistItem(productId = doc.getString("productId") ?: doc.id)
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.clearWishlist()
                        list.forEach { dao.addToWishlist(it) }
                    }
                }
            }

        // Sync Achievements
        achievementsListener = db.collection("users").document(uid).collection("achievements")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            Achievement(
                                id = doc.getString("id") ?: doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                isUnlocked = doc.getBoolean("isUnlocked") ?: false,
                                xpReward = doc.getLong("xpReward")?.toInt() ?: 50,
                                pointsReward = doc.getLong("pointsReward")?.toInt() ?: 10
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insertAchievements(list)
                    }
                }
            }
    }

    fun stopUserSpecificSync() {
        userStatsListener?.remove()
        ordersListener?.remove()
        wishlistListener?.remove()
        achievementsListener?.remove()
        userStatsListener = null
        ordersListener = null
        wishlistListener = null
        achievementsListener = null
    }

    suspend fun clearUserSpecificLocalData() {
        withContext(Dispatchers.IO) {
            dao.clearCart()
            dao.clearWishlist()
            dao.insertUserStats(UserStats(id = 1))
        }
    }

    fun deleteUserCloudData(uid: String) {
        val db = FirebaseSyncHelper.getFirestore() ?: return
        db.collection("users").document(uid).delete()
    }

    private fun uploadDefaultProductsToFirestore() {
        val db = FirebaseSyncHelper.getFirestore() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val list = products.first()
            list.forEach { prod ->
                val map = mapOf(
                    "id" to prod.id,
                    "name" to prod.name,
                    "description" to prod.description,
                    "price" to prod.price,
                    "discountPrice" to prod.discountPrice,
                    "imageUrl" to prod.imageUrl,
                    "category" to prod.category,
                    "anime" to prod.anime,
                    "stock" to prod.stock,
                    "isPreOrder" to prod.isPreOrder,
                    "isLimitedEdition" to prod.isLimitedEdition,
                    "isFlashSale" to prod.isFlashSale,
                    "rating" to prod.rating.toDouble(),
                    "reviewsCount" to prod.reviewsCount,
                    "isBestSeller" to prod.isBestSeller,
                    "isFeatured" to prod.isFeatured,
                    "estimatedDeliveryDays" to prod.estimatedDeliveryDays
                )
                db.collection("products").document(prod.id).set(map)
            }
        }
    }

    fun saveUserStatsAndProfile(uid: String, stats: UserStats) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertUserStats(stats)
            val db = FirebaseSyncHelper.getFirestore() ?: return@launch
            val map = mapOf(
                "xp" to stats.xp,
                "loyaltyPoints" to stats.loyaltyPoints,
                "level" to stats.level,
                "dailyStreak" to stats.dailyStreak,
                "lastLoginTime" to stats.lastLoginTime,
                "profilePhotoUrl" to stats.profilePhotoUrl,
                "username" to stats.username,
                "fullName" to stats.fullName,
                "email" to stats.email,
                "phoneNumber" to stats.phoneNumber,
                "deliveryAddress" to stats.deliveryAddress
            )
            db.collection("users").document(uid).collection("stats").document("user_stats").set(map)
        }
    }
}
