package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AnimeKitRepository
import com.example.data.repository.FirebaseSyncHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AnimeKitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimeKitRepository(application)

    // --- AUTHENTICATION STATES ---
    var isLoggedIn by mutableStateOf(false)
    var isAuthLoading by mutableStateOf(false)
    var authErrorMessage by mutableStateOf("")
    var currentUserEmail by mutableStateOf("")
    var currentUserId by mutableStateOf("")
    var isEmailVerified by mutableStateOf(false)
    var isFirebaseActive by mutableStateOf(false)

    // --- DB STATES (READ REPLICAS) ---
    val products = repository.products.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val cartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val wishlistItems = repository.wishlistItems.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val orders = repository.orders.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val chatMessages = repository.chatMessages.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val userStats = repository.userStats.stateIn(viewModelScope, SharingStarted.Eagerly, UserStats())
    val achievements = repository.achievements.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val coupons = repository.coupons.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- NAVIGATION / SCREEN STACKS ---
    var currentScreen by mutableStateOf("home") // "home", "search", "fun_zone", "cart", "profile", "product_details", "checkout", "admin"
    var selectedProductId by mutableStateOf<String?>(null)

    // --- PRODUCT DETAILS CHOICES ---
    var selectedSize by mutableStateOf("M")
    var selectedColor by mutableStateOf("Default")

    // --- SEARCH / FILTER STATES ---
    var searchQuery by mutableStateOf("")
    var searchCategoryFilter by mutableStateOf("All")
    var searchAnimeFilter by mutableStateOf("All")
    var maxPriceFilter by mutableStateOf(10000.0)
    var sortByFilter by mutableStateOf("Popularity") // "Popularity", "Price Low to High", "Price High to Low", "Newest"

    // --- CHECKOUT FORM ---
    var checkoutAddress by mutableStateOf("Nuwakot, Nepal")
    var checkoutPhone by mutableStateOf("+977-9800000000")
    var checkoutPaymentMethod by mutableStateOf("Cash on Delivery") // "Cash on Delivery", "eSewa", "Khalti"

    // --- CART COUPON ENGINE ---
    var appliedCouponCode by mutableStateOf("")
    var activeCouponDiscountPercentage by mutableStateOf(0.0)
    var couponErrorMessage by mutableStateOf("")

    // --- CHAT WRITER STATE ---
    var chatInputText by mutableStateOf("")
    var chatSelectedStickerUrl by mutableStateOf<String?>(null) // Simulation of image sending
    var isAdminTyping by mutableStateOf(false)

    // --- SYSTEM MODE (CUSTOMER VS ADMIN TOGGLE) ---
    var isAdminMode by mutableStateOf(false)

    // --- QUIZ GAME STATE ---
    val quizQuestions = listOf(
        QuizQuestion("Who is known as the Ghost of the Uchiha in Naruto?", listOf("Itachi", "Madara", "Sasuke", "Obito"), 1),
        QuizQuestion("What is Luffy's ultimate form called in One Piece?", listOf("Gear 2", "Gear 4", "Gear 5", "Snakeman"), 2),
        QuizQuestion("Which anime features characters fighting with Cursed Energy?", listOf("Demon Slayer", "My Hero Academia", "Jujutsu Kaisen", "Bleach"), 2),
        QuizQuestion("What is the name of Sung Jin-Woo's shadow marshal companion?", listOf("Beru", "Igris", "Tusk", "Iron"), 1),
        QuizQuestion("What is Anya's favorite food in Spy x Family?", listOf("Cake", "Peanuts", "Burgers", "Carrots"), 1)
    )
    var currentQuizQuestionIndex by mutableStateOf(0)
    var quizScore by mutableStateOf(0)
    var quizIsCompleted by mutableStateOf(false)
    var quizFeedbackMessage by mutableStateOf("")
    var quizAnswerSelected by mutableStateOf<Int?>(null)

    // --- MEMORY MATCH STATE ---
    val memoryIcons = listOf("🦊", "👒", "⚡", "🗡️", "👁️", "🥜", "🦊", "👒", "⚡", "🗡️", "👁️", "🥜")
    var memoryCards by mutableStateOf<List<MemoryCard>>(emptyList())
    var firstFlippedIndex by mutableStateOf<Int?>(null)
    var secondFlippedIndex by mutableStateOf<Int?>(null)
    var memoryMoves by mutableStateOf(0)
    var memoryMatches by mutableStateOf(0)
    var memoryIsCompleted by mutableStateOf(false)

    // --- SPIN WHEEL STATE ---
    var spinWheelDegree by mutableStateOf(0f)
    var spinIsRunning by mutableStateOf(false)
    var lastSpinReward by mutableStateOf("")
    var lastSpinTime by mutableStateOf(0L)

    // --- NOTIFICATIONS ---
    var notificationsList by mutableStateOf(listOf(
        NotificationItem("Welcome Offer", "Use coupon ANIMEKIT to get flat 20% off your first order!", "promo"),
        NotificationItem("Free Shipping Alert", "Orders above Rs. 1500 qualify for free Nepal-wide delivery!", "info")
    ))

    init {
        resetMemoryGame()
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        FirebaseSyncHelper.initialize(getApplication())
        isFirebaseActive = FirebaseSyncHelper.isFirebaseAvailable
        val auth = FirebaseSyncHelper.getAuth()
        if (auth != null && auth.currentUser != null) {
            val user = auth.currentUser!!
            isLoggedIn = true
            currentUserEmail = user.email ?: ""
            currentUserId = user.uid
            isEmailVerified = user.isEmailVerified
            
            // Check if admin
            isAdminMode = user.email == "admin@animekit.com"
            
            // Start sync
            repository.startUserSpecificSync(user.uid)
        } else {
            isLoggedIn = false
            isAdminMode = false
            currentScreen = "home" // Start on home screen by default for guest/new users!
        }
    }

    // --- GENERAL ---
    fun selectProduct(id: String) {
        selectedProductId = id
        currentScreen = "product_details"
        // Default options
        selectedSize = "M"
        selectedColor = "Default"
    }

    // --- CART LOGIC ---
    fun addToCart(productId: String) {
        viewModelScope.launch {
            repository.addToCart(productId, selectedSize, selectedColor)
            triggerToastNotification("Added to Cart", "Product successfully added to your shopping cart!")
        }
    }

    fun updateCartQuantity(cartId: Int, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQty(cartId, newQty)
        }
    }

    fun deleteCartItem(cartId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(cartId)
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val formatted = code.trim().uppercase()
            val matches = coupons.value.find { it.code == formatted && it.isActive }
            if (matches != null) {
                // Calculate minimum spend
                val subtotal = calculateCartSubtotal()
                if (subtotal >= matches.minSpend) {
                    appliedCouponCode = matches.code
                    activeCouponDiscountPercentage = matches.discountPercentage
                    couponErrorMessage = ""
                    triggerToastNotification("Coupon Applied!", "${matches.discountPercentage}% discount has been applied!")
                } else {
                    couponErrorMessage = "Min spend for this coupon is Rs. ${matches.minSpend.toInt()}"
                    appliedCouponCode = ""
                    activeCouponDiscountPercentage = 0.0
                }
            } else {
                couponErrorMessage = "Invalid or expired coupon code"
                appliedCouponCode = ""
                activeCouponDiscountPercentage = 0.0
            }
        }
    }

    fun removeAppliedCoupon() {
        appliedCouponCode = ""
        activeCouponDiscountPercentage = 0.0
        couponErrorMessage = ""
    }

    fun calculateCartSubtotal(): Double {
        var subtotal = 0.0
        val prods = products.value
        cartItems.value.forEach { item ->
            val p = prods.find { it.id == item.productId }
            if (p != null) {
                val activePrice = p.discountPrice ?: p.price
                subtotal += activePrice * item.quantity
            }
        }
        return subtotal
    }

    fun calculateCartTotal(): Double {
        val sub = calculateCartSubtotal()
        val discount = (sub * activeCouponDiscountPercentage) / 100.0
        val shipping = if (sub >= 1500.0 || appliedCouponCode == "FREEPOST") 0.0 else 150.0
        return sub - discount + shipping
    }

    fun handleCheckout() {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch
            val sub = calculateCartSubtotal()
            val discount = (sub * activeCouponDiscountPercentage) / 100.0
            val total = calculateCartTotal()
            
            val order = repository.placeOrder(
                items = items,
                total = total,
                paymentMethod = checkoutPaymentMethod,
                address = checkoutAddress,
                couponCode = if (appliedCouponCode.isNotEmpty()) appliedCouponCode else null,
                discountAmount = discount
            )

            // Add notification
            notificationsList = listOf(
                NotificationItem("Order Confirmed", "Your order ${order.id} has been placed successfully!", "order")
            ) + notificationsList

            // Clear coupon state
            removeAppliedCoupon()
            
            // Go to profile to see order history
            currentScreen = "profile"
        }
    }

    // --- WISHLIST ---
    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // --- CHAT LOGIC ---
    fun sendChatMessage() {
        if (chatInputText.trim().isEmpty() && chatSelectedStickerUrl == null) return
        val textToSend = chatInputText.trim()
        val sticker = chatSelectedStickerUrl
        
        chatInputText = ""
        chatSelectedStickerUrl = null

        viewModelScope.launch {
            repository.sendChatMessage(
                text = textToSend.ifEmpty { "Sent a sticker" },
                isFromAdmin = isAdminMode,
                imageUrl = sticker
            )
            if (!isAdminMode) {
                isAdminTyping = true
                kotlinx.coroutines.delay(1200)
                isAdminTyping = false
            }
        }
    }

    fun markAllChatMessagesAsRead() {
        viewModelScope.launch {
            repository.markAllChatMessagesAsRead()
        }
    }

    // --- REVIEWS ---
    fun submitProductReview(productId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.addReview(productId, rating, comment)
            triggerToastNotification("Review Submitted", "Thank you for reviewing! You earned +15 Loyalty Points!")
        }
    }

    // --- FUN ZONE GAMES ---
    fun claimDailyLoginReward() {
        viewModelScope.launch {
            val stats = userStats.value ?: UserStats()
            val diff = System.currentTimeMillis() - stats.lastLoginTime
            // In real app, check for 24 hours. For demo, we allow once every session or every launch if diff > 5000ms
            repository.claimDailyReward()
            triggerToastNotification("Daily Reward!", "Claimed Daily login reward! Streak: ${stats.dailyStreak + 1}")
        }
    }

    fun rewardArcadePlay(xp: Int, points: Int) {
        viewModelScope.launch {
            repository.addRewardPoints(points, xp)
            if (xp >= 200) {
                repository.unlockAchievement("quiz_master")
            }
        }
    }

    fun selectQuizAnswer(index: Int) {
        if (quizAnswerSelected != null || quizIsCompleted) return
        quizAnswerSelected = index
        val currentQ = quizQuestions[currentQuizQuestionIndex]
        if (index == currentQ.correctIndex) {
            quizScore++
            quizFeedbackMessage = "Correct! Well done, Otaku! 🌸"
        } else {
            quizFeedbackMessage = "Wrong! The correct answer was: ${currentQ.options[currentQ.correctIndex]}"
        }
    }

    fun nextQuizQuestion() {
        quizAnswerSelected = null
        quizFeedbackMessage = ""
        if (currentQuizQuestionIndex < quizQuestions.size - 1) {
            currentQuizQuestionIndex++
        } else {
            quizIsCompleted = true
            // Reward user
            viewModelScope.launch {
                val pts = quizScore * 10
                val xp = quizScore * 20
                repository.addRewardPoints(pts, xp)
                triggerToastNotification("Quiz Finished!", "You scored $quizScore/5! Received +$pts Loyalty Points!")
                if (quizScore == 5) {
                    repository.unlockAchievement("quiz_master")
                }
            }
        }
    }

    fun restartQuiz() {
        currentQuizQuestionIndex = 0
        quizScore = 0
        quizIsCompleted = false
        quizFeedbackMessage = ""
        quizAnswerSelected = null
    }

    // --- MEMORY MATCH ---
    fun resetMemoryGame() {
        val shuffled = memoryIcons.shuffled()
        memoryCards = shuffled.mapIndexed { idx, icon ->
            MemoryCard(id = idx, content = icon)
        }
        firstFlippedIndex = null
        secondFlippedIndex = null
        memoryMoves = 0
        memoryMatches = 0
        memoryIsCompleted = false
    }

    fun flipMemoryCard(index: Int) {
        if (memoryCards[index].isMatched || memoryCards[index].isFlipped) return
        if (firstFlippedIndex != null && secondFlippedIndex != null) return // Wait for reset animation

        // Flip card
        memoryCards = memoryCards.mapIndexed { idx, card ->
            if (idx == index) card.copy(isFlipped = true) else card
        }

        if (firstFlippedIndex == null) {
            firstFlippedIndex = index
        } else {
            secondFlippedIndex = index
            memoryMoves++
            checkForMemoryMatch()
        }
    }

    private fun checkForMemoryMatch() {
        val firstIdx = firstFlippedIndex ?: return
        val secondIdx = secondFlippedIndex ?: return
        
        val card1 = memoryCards[firstIdx]
        val card2 = memoryCards[secondIdx]

        if (card1.content == card2.content) {
            // Match found!
            memoryMatches++
            memoryCards = memoryCards.mapIndexed { idx, card ->
                if (idx == firstIdx || idx == secondIdx) card.copy(isMatched = true) else card
            }
            firstFlippedIndex = null
            secondFlippedIndex = null

            if (memoryMatches == 6) {
                memoryIsCompleted = true
                viewModelScope.launch {
                    val rewardPoints = maxOf(10, 50 - memoryMoves)
                    repository.addRewardPoints(rewardPoints, rewardPoints * 2)
                    triggerToastNotification("Memory Game Won!", "Solved in $memoryMoves moves! Earned +$rewardPoints points!")
                    if (memoryMoves <= 10) {
                        repository.unlockAchievement("memory_god")
                    }
                }
            }
        } else {
            // Mismatch: Flip back after short delay in view or handler. We clear indexes
            // For safety we will let the UI animate or we reset after a manual action.
            // Let's do it on a short thread/coroutine delay so the cards stay visible briefly
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                memoryCards = memoryCards.mapIndexed { idx, card ->
                    if (idx == firstIdx || idx == secondIdx) card.copy(isFlipped = false) else card
                }
                firstFlippedIndex = null
                secondFlippedIndex = null
            }
        }
    }

    // --- SPIN WHEEL ---
    fun runSpinWheel() {
        if (spinIsRunning) return
        spinIsRunning = true
        viewModelScope.launch {
            val randomDegrees = (1080..2160).random().toFloat()
            spinWheelDegree += randomDegrees
            kotlinx.coroutines.delay(2000) // Spin delay
            
            // Calculate segment award
            val segment = ((spinWheelDegree % 360) / 60).toInt()
            val reward = when(segment) {
                0 -> "+15 Loyalty Points"
                1 -> "Free Delivery Coupon (NEPALOTAKU)"
                2 -> "+30 XP"
                3 -> "+5 Loyalty Points"
                4 -> "Try Again Tomorrow!"
                else -> "+50 Loyalty Points Super Prize!"
            }
            lastSpinReward = reward
            spinIsRunning = false
            lastSpinTime = System.currentTimeMillis()

            // Apply reward
            when(segment) {
                0 -> repository.addRewardPoints(15, 20)
                1 -> repository.dao.insertCoupon(Coupon("NEPALOTAKU", 10.0, 500.0, 1000.0, true, "10% off for Spin Winners!"))
                2 -> repository.addRewardPoints(0, 30)
                3 -> repository.addRewardPoints(5, 10)
                5 -> repository.addRewardPoints(50, 100)
            }
            triggerToastNotification("Lucky Spin Result!", "You won: $reward")
        }
    }

    // --- ADMIN DASHBOARD OPERATIONS ---
    fun adminUpdateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            val currentOrder = orders.value.find { it.id == orderId } ?: return@launch
            val nextPaymentStatus = if (newStatus == "Delivered") "Paid" else currentOrder.paymentStatus
            repository.dao.updateOrderStatus(orderId, newStatus, nextPaymentStatus)
            
            // Notify customer in notification log
            notificationsList = listOf(
                NotificationItem("Order $newStatus", "Your order $orderId has been updated to $newStatus!", "order")
            ) + notificationsList
        }
    }

    fun adminCreateCoupon(code: String, pct: Double, maxDisc: Double, minSpnd: Double, desc: String) {
        viewModelScope.launch {
            val coupon = Coupon(
                code = code.trim().uppercase(),
                discountPercentage = pct,
                maxDiscount = maxDisc,
                minSpend = minSpnd,
                isActive = true,
                description = desc
            )
            repository.dao.insertCoupon(coupon)
            triggerToastNotification("Coupon Created", "New coupon ${coupon.code} is now live!")
        }
    }

    fun adminTogglePinMessage(messageId: Int, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updateMessagePinned(messageId, isPinned)
        }
    }

    fun adminMarkAllChatsResolved() {
        viewModelScope.launch {
            repository.resolveAllChats()
            triggerToastNotification("Inquiries Resolved", "All customer chat conversations marked as resolved.")
        }
    }

    // --- TOAST NOTIFICATIONS STATE ---
    var toastTitle by mutableStateOf("")
    var toastDesc by mutableStateOf("")
    var showToast by mutableStateOf(false)

    fun triggerToastNotification(title: String, desc: String) {
        toastTitle = title
        toastDesc = desc
        showToast = true
        // Auto dismiss after 4 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (toastTitle == title) {
                showToast = false
            }
        }
    }

    // --- FIREBASE AUTHENTICATION FLOWS ---

    fun signUp(email: String, psswrd: String, fullName: String, username: String, phone: String, address: String, onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        authErrorMessage = ""
        val auth = FirebaseSyncHelper.getAuth()
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, psswrd)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            currentUserId = user.uid
                            currentUserEmail = user.email ?: ""
                            isLoggedIn = true
                            isEmailVerified = user.isEmailVerified
                            isAdminMode = user.email == "admin@animekit.com"
                            
                            val stats = UserStats(
                                id = 1,
                                xp = 0,
                                loyaltyPoints = 150, // Welcome points!
                                level = 1,
                                dailyStreak = 1,
                                lastLoginTime = System.currentTimeMillis(),
                                profilePhotoUrl = "",
                                username = username,
                                fullName = fullName,
                                email = email,
                                phoneNumber = phone,
                                deliveryAddress = address
                            )
                            repository.saveUserStatsAndProfile(user.uid, stats)
                            repository.startUserSpecificSync(user.uid)
                            
                            isAuthLoading = false
                            currentScreen = "home"
                            onComplete(true)
                        } else {
                            isAuthLoading = false
                            authErrorMessage = "Failed to retrieve user details."
                            onComplete(false)
                        }
                    } else {
                        isAuthLoading = false
                        authErrorMessage = task.exception?.localizedMessage ?: "Sign up failed."
                        onComplete(false)
                    }
                }
        } else {
            // Simulated Offline fallback
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isLoggedIn = true
                currentUserId = "local_user_${email.hashCode()}"
                currentUserEmail = email
                isAdminMode = email == "admin@animekit.com"
                
                val stats = UserStats(
                    id = 1,
                    xp = 0,
                    loyaltyPoints = 150,
                    level = 1,
                    dailyStreak = 1,
                    lastLoginTime = System.currentTimeMillis(),
                    profilePhotoUrl = "",
                    username = username,
                    fullName = fullName,
                    email = email,
                    phoneNumber = phone,
                    deliveryAddress = address
                )
                repository.dao.insertUserStats(stats)
                isAuthLoading = false
                currentScreen = "home"
                onComplete(true)
            }
        }
    }

    fun login(email: String, psswrd: String, onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        authErrorMessage = ""
        val auth = FirebaseSyncHelper.getAuth()
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, psswrd)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            currentUserId = user.uid
                            currentUserEmail = user.email ?: ""
                            isLoggedIn = true
                            isEmailVerified = user.isEmailVerified
                            isAdminMode = user.email == "admin@animekit.com"
                            
                            repository.startUserSpecificSync(user.uid)
                            
                            isAuthLoading = false
                            currentScreen = "home"
                            onComplete(true)
                        } else {
                            isAuthLoading = false
                            authErrorMessage = "Failed to retrieve user details."
                            onComplete(false)
                        }
                    } else {
                        isAuthLoading = false
                        authErrorMessage = task.exception?.localizedMessage ?: "Authentication failed."
                        onComplete(false)
                    }
                }
        } else {
            // Simulated Offline login
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isLoggedIn = true
                currentUserId = "local_user_${email.hashCode()}"
                currentUserEmail = email
                isAdminMode = email == "admin@animekit.com"
                
                val existing = repository.dao.getUserStats().first()
                if (existing == null) {
                    val stats = UserStats(
                        id = 1,
                        email = email,
                        fullName = "Sakshyam Pudasaini",
                        username = "Sakshyam"
                    )
                    repository.dao.insertUserStats(stats)
                }
                
                isAuthLoading = false
                currentScreen = "home"
                onComplete(true)
            }
        }
    }

    fun loginWithGoogle(idToken: String, email: String, name: String, onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        authErrorMessage = ""
        val auth = FirebaseSyncHelper.getAuth()
        if (auth != null && idToken.isNotEmpty()) {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            currentUserId = user.uid
                            currentUserEmail = user.email ?: ""
                            isLoggedIn = true
                            isEmailVerified = true
                            isAdminMode = user.email == "admin@animekit.com"
                            
                            repository.startUserSpecificSync(user.uid)
                            
                            isAuthLoading = false
                            currentScreen = "home"
                            onComplete(true)
                        } else {
                            isAuthLoading = false
                            authErrorMessage = "Failed to authenticate with Google."
                            onComplete(false)
                        }
                    } else {
                        isAuthLoading = false
                        authErrorMessage = task.exception?.localizedMessage ?: "Google Sign-In failed."
                        onComplete(false)
                    }
                }
        } else {
            // Simulated Google Sign-In
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isLoggedIn = true
                currentUserId = "google_user_${email.hashCode()}"
                currentUserEmail = email
                isAdminMode = email == "admin@animekit.com"
                
                val stats = UserStats(
                    id = 1,
                    email = email,
                    fullName = name,
                    username = name.split(" ").firstOrNull() ?: "Otaku"
                )
                repository.dao.insertUserStats(stats)
                isAuthLoading = false
                currentScreen = "home"
                onComplete(true)
            }
        }
    }

    fun forgotPassword(email: String, onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        authErrorMessage = ""
        val auth = FirebaseSyncHelper.getAuth()
        if (auth != null) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    isAuthLoading = false
                    if (task.isSuccessful) {
                        onComplete(true)
                    } else {
                        authErrorMessage = task.exception?.localizedMessage ?: "Password reset failed."
                        onComplete(false)
                    }
                }
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isAuthLoading = false
                onComplete(true)
            }
        }
    }

    fun sendEmailVerification(onComplete: (Boolean) -> Unit) {
        val auth = FirebaseSyncHelper.getAuth()
        val user = auth?.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true)
                    } else {
                        authErrorMessage = task.exception?.localizedMessage ?: "Failed to send verification."
                        onComplete(false)
                    }
                }
        } else {
            onComplete(true)
        }
    }

    fun logout() {
        val auth = FirebaseSyncHelper.getAuth()
        auth?.signOut()
        repository.stopUserSpecificSync()
        
        viewModelScope.launch {
            repository.clearUserSpecificLocalData()
        }
        
        isLoggedIn = false
        currentUserEmail = ""
        currentUserId = ""
        isAdminMode = false
        isEmailVerified = false
        currentScreen = "login"
    }

    fun browseAsGuest() {
        val auth = FirebaseSyncHelper.getAuth()
        auth?.signOut()
        repository.stopUserSpecificSync()
        
        viewModelScope.launch {
            repository.clearUserSpecificLocalData()
        }
        
        isLoggedIn = false
        currentUserEmail = ""
        currentUserId = ""
        isAdminMode = false
        isEmailVerified = false
        currentScreen = "home"
    }

    fun updateProfile(fullName: String, username: String, phone: String, address: String, photoUrl: String) {
        val stats = userStats.value ?: UserStats()
        val updated = stats.copy(
            fullName = fullName,
            username = username,
            phoneNumber = phone,
            deliveryAddress = address,
            profilePhotoUrl = photoUrl
        )
        viewModelScope.launch {
            repository.saveUserStatsAndProfile(currentUserId, updated)
            triggerToastNotification("Profile Updated", "Your profile details have been saved!")
        }
    }

    fun changePassword(newPass: String, onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        val auth = FirebaseSyncHelper.getAuth()
        val user = auth?.currentUser
        if (user != null) {
            user.updatePassword(newPass)
                .addOnCompleteListener { task ->
                    isAuthLoading = false
                    if (task.isSuccessful) {
                        onComplete(true)
                    } else {
                        authErrorMessage = task.exception?.localizedMessage ?: "Failed to update password."
                        onComplete(false)
                    }
                }
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isAuthLoading = false
                onComplete(true)
            }
        }
    }

    fun changeEmail(newEmail: String, onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        val auth = FirebaseSyncHelper.getAuth()
        val user = auth?.currentUser
        if (user != null) {
            user.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    isAuthLoading = false
                    if (task.isSuccessful) {
                        currentUserEmail = newEmail
                        val stats = userStats.value ?: UserStats()
                        updateProfile(stats.fullName, stats.username, stats.phoneNumber, stats.deliveryAddress, stats.profilePhotoUrl)
                        onComplete(true)
                    } else {
                        authErrorMessage = task.exception?.localizedMessage ?: "Failed to update email."
                        onComplete(false)
                    }
                }
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                currentUserEmail = newEmail
                isAuthLoading = false
                onComplete(true)
            }
        }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        isAuthLoading = true
        val auth = FirebaseSyncHelper.getAuth()
        val user = auth?.currentUser
        if (user != null) {
            repository.deleteUserCloudData(user.uid)
            user.delete()
                .addOnCompleteListener { task ->
                    isAuthLoading = false
                    if (task.isSuccessful) {
                        logout()
                        onComplete(true)
                    } else {
                        authErrorMessage = task.exception?.localizedMessage ?: "Failed to delete account."
                        onComplete(false)
                    }
                }
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                logout()
                isAuthLoading = false
                onComplete(true)
            }
        }
    }
}

// --- DATA TYPES FOR VIEW STATES ---
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class MemoryCard(
    val id: Int,
    val content: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class NotificationItem(
    val title: String,
    val message: String,
    val type: String // "order", "promo", "info"
)
