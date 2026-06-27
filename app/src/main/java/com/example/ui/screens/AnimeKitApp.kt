package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeKitApp(viewModel: AnimeKitViewModel) {
    val currentScreen = viewModel.currentScreen
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    var showGuestLoginPrompt by remember { mutableStateOf(false) }

    // Clean overlay for custom toast notifications
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (currentScreen != "checkout" && currentScreen != "product_details" && currentScreen != "login" && currentScreen != "signup" && currentScreen != "forgot_password") {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Slate100, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🦊", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "ANIME KIT",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = CrimsonRed
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                        actions = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(36.dp)
                                    .background(Slate50, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { viewModel.currentScreen = "search" }
                                    .testTag("top_bar_search_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(36.dp)
                                    .background(Slate50, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { 
                                        viewModel.currentScreen = "profile"
                                        viewModel.isAdminMode = !viewModel.isAdminMode 
                                        viewModel.triggerToastNotification(
                                            "Role Switched", 
                                            if (viewModel.isAdminMode) "Welcome Owner! Admin Dashboard Unlocked" else "Returned to Nepal Otaku Shopper mode!"
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isAdminMode) Icons.Default.SupervisorAccount else Icons.Default.AdminPanelSettings,
                                    contentDescription = "Toggle Admin Portal",
                                    tint = if (viewModel.isAdminMode) CrimsonRed else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = Slate100,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    )
                }
            },
            bottomBar = {
                if (currentScreen != "checkout" && currentScreen != "product_details" && currentScreen != "login" && currentScreen != "signup" && currentScreen != "forgot_password") {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .drawBehind {
                                drawLine(
                                    color = Slate100,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                    ) {
                        val navItems = listOf(
                            Triple("home", Icons.Default.Home, "Home"),
                            Triple("search", Icons.Default.Search, "Search"),
                            Triple("fun_zone", Icons.Default.Games, "Fun Zone"),
                            Triple("cart", Icons.Default.ShoppingCart, "Cart"),
                            Triple("profile", Icons.Default.Person, "Profile")
                        )
                        navItems.forEach { (route, icon, label) ->
                            val isSelected = currentScreen == route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.currentScreen = route },
                                icon = {
                                    Box {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) CrimsonRed else Color.Gray
                                        )
                                        if (route == "cart" && cartItems.isNotEmpty()) {
                                            Badge(
                                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp),
                                                containerColor = CrimsonRed
                                            ) {
                                                Text(cartItems.sumOf { it.quantity }.toString(), color = Color.White, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                },
                                label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = CrimsonRedLight
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!viewModel.isFirebaseActive && currentScreen != "login" && currentScreen != "signup" && currentScreen != "forgot_password") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF9C4)) // Soft alert yellow
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Running in Room Cache Mode (No cloud sync). To enable cloud real-time syncing, please provide a valid google-services.json.",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Crossfade(targetState = currentScreen, label = "ScreenTransition", modifier = Modifier.weight(1f)) { screen ->
                        when (screen) {
                            "home" -> HomeScreen(viewModel)
                            "search" -> SearchScreen(viewModel)
                            "fun_zone" -> FunZoneArcadeScreen(viewModel)
                            "cart" -> CartScreen(viewModel, onShowGuestPrompt = { showGuestLoginPrompt = true })
                            "profile" -> ProfileScreen(viewModel, onShowGuestPrompt = { showGuestLoginPrompt = true })
                            "product_details" -> ProductDetailsScreen(viewModel, onShowGuestPrompt = { showGuestLoginPrompt = true })
                            "checkout" -> CheckoutScreen(viewModel)
                            "login" -> LoginScreen(viewModel)
                            "signup" -> SignUpScreen(viewModel)
                            "forgot_password" -> ForgotPasswordScreen(viewModel)
                        }
                    }
                }
            }
        }

        // Global Custom Toast Notification Drawer
        AnimatedVisibility(
            visible = viewModel.showToast,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .zIndex(100f)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth().clickable { viewModel.showToast = false }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CrimsonRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(viewModel.toastTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(viewModel.toastDesc, color = Color.LightGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Modern Dialog popup for Guest Mode restrictions
        if (showGuestLoginPrompt) {
            AlertDialog(
                onDismissRequest = { showGuestLoginPrompt = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌸 ", fontSize = 24.sp)
                        Text("Login Required", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                text = {
                    Text(
                        "Login to continue shopping and save your progress.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGuestLoginPrompt = false
                            viewModel.currentScreen = "login"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Text("Login Now", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGuestLoginPrompt = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// --- SCREEN 1: HOMEPAGE ---
@Composable
fun HomeScreen(viewModel: AnimeKitViewModel) {
    val products by viewModel.products.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Promotional Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(160.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(CrimsonRed, Color(0xFFFF4D6D))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    viewModel.searchQuery = ""
                    viewModel.sortByFilter = "Newest"
                    viewModel.currentScreen = "search"
                }
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                Text(
                    text = "EXCLUSIVE DROP",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Solo Leveling\nCollection",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Shop Now",
                        color = CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Decorative background circles with blurred / semi-transparent overlays
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )
            
            // Big transparent star overlay in corner
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = (-10).dp)
            )
        }

        // Shop by Anime (Horizontal List)
        Text(
            "SHOP BY ANIME",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 10.dp)
        )
        val animes = listOf("All", "Naruto", "One Piece", "Demon Slayer", "Jujutsu Kaisen", "Attack on Titan", "Chainsaw Man", "Spy x Family")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animes) { anime ->
                val isSelected = viewModel.searchAnimeFilter == anime
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CrimsonRed else Slate200,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(
                            color = if (isSelected) CrimsonRed else Slate50,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            viewModel.searchAnimeFilter = anime
                            viewModel.currentScreen = "search"
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = anime,
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Flash Sale Section (Crimson Theme)
        val flashProducts = products.filter { it.isFlashSale }
        if (flashProducts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FLASH SALE", fontWeight = FontWeight.Black, fontSize = 16.sp, color = CrimsonRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("02h:41m", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "See All",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        viewModel.searchQuery = ""
                        viewModel.sortByFilter = "Newest"
                        viewModel.currentScreen = "search"
                    }
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(flashProducts) { prod ->
                    ProductCard(prod, onClick = { viewModel.selectProduct(prod.id) })
                }
            }
        }

        // Limited Edition & Pre-Order Block
        val preOrders = products.filter { it.isPreOrder }
        if (preOrders.isNotEmpty()) {
            Text(
                "PRE-ORDER & LIMITED EDITIONS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 10.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(preOrders) { prod ->
                    ProductCard(prod, isPromo = true, onClick = { viewModel.selectProduct(prod.id) })
                }
            }
        }

        // Best Sellers & Featured
        val bestSellers = products.filter { it.isBestSeller }
        Text(
            "BEST SELLERS NEPAL",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 10.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bestSellers) { prod ->
                ProductCard(prod, onClick = { viewModel.selectProduct(prod.id) })
            }
        }

        // New Arrivals Grid
        Text(
            "NEW ARRIVALS",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 10.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val rows = products.take(6).chunked(2)
            rows.forEach { rowProds ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowProds.forEach { prod ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(prod, fillWidth = true, onClick = { viewModel.selectProduct(prod.id) })
                        }
                    }
                    if (rowProds.size < 2) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Customer Reviews Teaser
        Text(
            "WHAT NEPALESE OTAKUS SAY",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 28.dp, bottom = 10.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sakshyam Pudasaini", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(CrimsonRedLight, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("Verified Buyer", color = CrimsonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    repeat(5) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "\"The Akatsuki hoodie is insane! Thick cotton, heavy premium embroidery. Shipped to Nuwakot within 3 days. Recommend to everyone!\"",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

        // Newsletter / Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .background(Color.Black)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("JOIN ANIME KIT CLUB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Get notifications on daily drops and flash sales.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.triggerToastNotification("Subscribed!", "You have joined Nepal's elite Otaku club!") },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Subscribe Now", fontSize = 12.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("© 2026 Anime Kit, Nuwakot, Nepal. All Rights Reserved.", color = Color.DarkGray, fontSize = 9.sp)
            }
        }
    }
}

// --- SHARED COMPONENT: PRODUCT CARD ---
@Composable
fun ProductCard(
    prod: Product,
    isPromo: Boolean = false,
    fillWidth: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(150.dp))
            .clickable { onClick() }
            .border(1.dp, Slate100, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Slate50),
                contentAlignment = Alignment.Center
            ) {
                // Static high-fidelity stylized graphics using CSS gradients / dynamic canvas drawings
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CrimsonRed.copy(alpha = 0.15f), Color.Transparent)
                                ),
                                radius = size.minDimension / 1.5f,
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Stylized text emoji representations representing anime products beautifully
                    val emoji = when (prod.category) {
                        "apparel" -> "🧥"
                        "figures" -> "🎎"
                        "accessories" -> "🔑"
                        else -> "🖼️"
                    }
                    Text(emoji, fontSize = 48.sp)
                }

                // Pre-order or Discount badge overlay
                if (prod.isPreOrder) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color.Black, RoundedCornerShape(bottomEnd = 12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("PRE-ORDER", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (prod.discountPrice != null) {
                    val pct = ((prod.price - prod.discountPrice) / prod.price * 100).toInt()
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(CrimsonRed, RoundedCornerShape(bottomEnd = 12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("-$pct%", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = prod.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                Text(prod.anime, color = CrimsonRed, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        if (prod.discountPrice != null) {
                            Text("Rs. ${prod.discountPrice.toInt()}", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("Rs. ${prod.price.toInt()}", color = Color.Gray, fontSize = 10.sp, style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                        } else {
                            Text("Rs. ${prod.price.toInt()}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                        Text(prod.rating.toString(), fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: SEARCH & FILTER SCREEN ---
@Composable
fun SearchScreen(viewModel: AnimeKitViewModel) {
    val products by viewModel.products.collectAsState()

    var activeQuery by remember { mutableStateOf(viewModel.searchQuery) }

    // Filter Logic
    val filteredProducts = products.filter { prod ->
        val matchesQuery = prod.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                prod.description.contains(viewModel.searchQuery, ignoreCase = true)
        val matchesCategory = viewModel.searchCategoryFilter == "All" || prod.category == viewModel.searchCategoryFilter.lowercase()
        val matchesAnime = viewModel.searchAnimeFilter == "All" || prod.anime == viewModel.searchAnimeFilter
        val matchesPrice = prod.price <= viewModel.maxPriceFilter

        matchesQuery && matchesCategory && matchesAnime && matchesPrice
    }.sortedWith { a, b ->
        val priceA = a.discountPrice ?: a.price
        val priceB = b.discountPrice ?: b.price
        when (viewModel.sortByFilter) {
            "Price Low to High" -> priceA.compareTo(priceB)
            "Price High to Low" -> priceB.compareTo(priceA)
            else -> b.rating.compareTo(a.rating) // Popularity
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text("Search hoodies, keychains, katanas...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate100,
                    unfocusedContainerColor = Slate100,
                    focusedBorderColor = CrimsonRed,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Quick Category Badges
        val categoriesList = listOf("All", "Apparel", "Figures", "Accessories", "Posters")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(categoriesList) { cat ->
                val isSelected = viewModel.searchCategoryFilter == cat
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color.Black else Slate100,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.searchCategoryFilter = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        cat,
                        color = if (isSelected) Color.White else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Expanded Bottom Filters drawer style inline
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sort drop option
                    Text("Sort: ${viewModel.sortByFilter}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonRed, modifier = Modifier.clickable {
                        viewModel.sortByFilter = when(viewModel.sortByFilter) {
                            "Popularity" -> "Price Low to High"
                            "Price Low to High" -> "Price High to Low"
                            else -> "Popularity"
                        }
                    })
                    // Price Threshold Slider
                    Text("Max Price: Rs. ${viewModel.maxPriceFilter.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = viewModel.maxPriceFilter.toFloat(),
                    onValueChange = { viewModel.maxPriceFilter = it.toDouble() },
                    valueRange = 1000f..10000f,
                    colors = SliderDefaults.colors(thumbColor = CrimsonRed, activeTrackColor = CrimsonRed)
                )
            }
        }

        // Grid List of search results
        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔮", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No results found in Nepal's shrine!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Try adjusting your price filter or category.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { prod ->
                    ProductCard(prod, fillWidth = true, onClick = { viewModel.selectProduct(prod.id) })
                }
            }
        }
    }
}

// --- SCREEN 3: PRODUCT DETAILS SCREEN ---
@Composable
fun ProductDetailsScreen(viewModel: AnimeKitViewModel, onShowGuestPrompt: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val prodId = viewModel.selectedProductId
    val prod = products.find { it.id == prodId }

    if (prod == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found!")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Simple Navigation header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = "home" }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { 
                if (!viewModel.isLoggedIn) {
                    onShowGuestPrompt()
                } else {
                    viewModel.toggleWishlist(prod.id) 
                }
            }) {
                val wishlist by viewModel.wishlistItems.collectAsState()
                val isSaved = wishlist.any { it.productId == prod.id }
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) CrimsonRed else Color.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Visual Image Box representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFFFAFAFA)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CrimsonRed.copy(alpha = 0.2f), Color.Transparent)
                                ),
                                radius = size.minDimension / 1.2f,
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = when (prod.category) {
                        "apparel" -> "🧥"
                        "figures" -> "🎎"
                        "accessories" -> "🔑"
                        else -> "🖼️"
                    }
                    Text(emoji, fontSize = 100.sp)
                }

                // Promo Overlays
                if (prod.isLimitedEdition) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("LIMITED EDITION", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(prod.anime.uppercase(), color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(prod.name, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.Black)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rs. ${prod.discountPrice?.toInt() ?: prod.price.toInt()}", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        if (prod.discountPrice != null) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Rs. ${prod.price.toInt()}", color = Color.Gray, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE2F0D9), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(if (prod.stock > 0) "In Stock" else "Pre-Order", color = Color(0xFF385723), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color.LightGray, modifier = Modifier.padding(vertical = 12.dp))

                // Apparel selection
                if (prod.category == "apparel") {
                    Text("SELECT SIZE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("S", "M", "L", "XL").forEach { size ->
                            val isChosen = viewModel.selectedSize == size
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (isChosen) CrimsonRed else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        color = if (isChosen) CrimsonRed else Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectedSize = size }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(size, color = if (isChosen) Color.White else Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("DESCRIPTION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(prod.description, color = Color.DarkGray, fontSize = 13.sp, lineHeight = 18.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Text("SPECIFICATIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Slate100, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("Delivery:", color = Color.Gray, modifier = Modifier.width(100.dp), fontSize = 12.sp)
                            Text("${prod.estimatedDeliveryDays} Days (Nepal Wide)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("Guarantee:", color = Color.Gray, modifier = Modifier.width(100.dp), fontSize = 12.sp)
                            Text("100% Original Anime Merchandise", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }

                // Photo Reviews Section
                Spacer(modifier = Modifier.height(24.dp))
                Text("REVIEWS (${prod.reviewsCount})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                // Form to add review
                var reviewRating by remember { mutableStateOf(5) }
                var reviewComment by remember { mutableStateOf("") }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("WRITE A REVIEW", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CrimsonRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rating: ", fontSize = 12.sp)
                            repeat(5) { starIdx ->
                                Icon(
                                    imageVector = if (starIdx < reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { reviewRating = starIdx + 1 }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            placeholder = { Text("Share your feedback with Nepali otakus...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (reviewComment.isNotEmpty()) {
                                    viewModel.submitProductReview(prod.id, reviewRating, reviewComment)
                                    reviewComment = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Submit Review", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Fixed Purchase buttons at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.addToCart(prod.id) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("add_to_cart_btn"),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.5.dp, Color.Black)
            ) {
                Text("Add to Cart", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (!viewModel.isLoggedIn) {
                        onShowGuestPrompt()
                    } else {
                        viewModel.addToCart(prod.id)
                        viewModel.currentScreen = "cart"
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("buy_now_btn"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("Buy Now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SCREEN 4: SHOPPING CART SCREEN ---
@Composable
fun CartScreen(viewModel: AnimeKitViewModel, onShowGuestPrompt: () -> Unit) {
    val cartItems by viewModel.cartItems.collectAsState()
    val products by viewModel.products.collectAsState()

    if (cartItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛒", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Your Otaku cart is empty!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Go load some premium apparel or keychains.", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.currentScreen = "home" }, colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)) {
                    Text("Shop Merchandise", color = Color.White)
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("MY CART (${cartItems.size})", fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cartItems) { item ->
                val prod = products.find { it.id == item.productId } ?: return@items
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Icon image representer
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val emoji = when (prod.category) {
                                "apparel" -> "🧥"
                                "figures" -> "🎎"
                                "accessories" -> "🔑"
                                else -> "🖼️"
                            }
                            Text(emoji, fontSize = 28.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Text("${prod.anime} | Size: ${item.selectedSize}", color = CrimsonRed, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Rs. ${prod.discountPrice?.toInt() ?: prod.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Qty Adjuster
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.updateCartQuantity(item.id, item.quantity - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Text(item.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = { viewModel.updateCartQuantity(item.id, item.quantity + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Bottom Promos & Totals
        var couponCodeInput by remember { mutableStateOf("") }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Promo coupon area
                if (viewModel.appliedCouponCode.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = couponCodeInput,
                            onValueChange = { couponCodeInput = it },
                            placeholder = { Text("Coupon Code (e.g. ANIMEKIT)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(48.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                        )
                        Button(
                            onClick = { 
                                viewModel.applyCoupon(couponCodeInput)
                                couponCodeInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Apply", fontSize = 12.sp, color = Color.White)
                        }
                    }
                    if (viewModel.couponErrorMessage.isNotEmpty()) {
                        Text(viewModel.couponErrorMessage, color = CrimsonRed, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonRedLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Applied: ${viewModel.appliedCouponCode}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CrimsonRed)
                        }
                        IconButton(onClick = { viewModel.removeAppliedCoupon() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary
                val subtotal = viewModel.calculateCartSubtotal()
                val discount = (subtotal * viewModel.activeCouponDiscountPercentage) / 100.0
                val shipping = if (subtotal >= 1500.0 || viewModel.appliedCouponCode == "FREEPOST") 0.0 else 150.0
                val total = viewModel.calculateCartTotal()

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = Color.Gray, fontSize = 12.sp)
                    Text("Rs. ${subtotal.toInt()}", fontSize = 12.sp)
                }
                if (discount > 0) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Coupon Discount", color = CrimsonRed, fontSize = 12.sp)
                        Text("-Rs. ${discount.toInt()}", color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nepal Delivery Fee", color = Color.Gray, fontSize = 12.sp)
                    Text(if (shipping == 0.0) "FREE" else "Rs. ${shipping.toInt()}", fontSize = 12.sp, color = if (shipping == 0.0) Color(0xFF385723) else Color.Black)
                }
                
                Divider(color = Color.LightGray, modifier = Modifier.padding(vertical = 8.dp))
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total NPR", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("Rs. ${total.toInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = CrimsonRed)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { 
                        if (!viewModel.isLoggedIn) {
                            onShowGuestPrompt()
                        } else {
                            viewModel.currentScreen = "checkout" 
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("checkout_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Proceed to Checkout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SCREEN 5: CHECKOUT / PAYMENT SCREEN ---
@Composable
fun CheckoutScreen(viewModel: AnimeKitViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = "cart" }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Shipping & Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("DELIVERY ADDRESS IN NEPAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.checkoutAddress,
                onValueChange = { viewModel.checkoutAddress = it },
                label = { Text("Exact Address (e.g. Battar, Nuwakot or New Road, Kathmandu)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.checkoutPhone,
                onValueChange = { viewModel.checkoutPhone = it },
                label = { Text("Nepali Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("SELECT PAYMENT GATEWAY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            
            val methods = listOf(
                Pair("Cash on Delivery", "COD (Pay at door across Nepal)"),
                Pair("eSewa", "eSewa Direct Payment"),
                Pair("Khalti", "Khalti Wallet Scan")
            )
            methods.forEach { (code, label) ->
                val isSelected = viewModel.checkoutPaymentMethod == code
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) CrimsonRedLight else Color.White),
                    border = BorderStroke(1.dp, if (isSelected) CrimsonRed else Color.LightGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.checkoutPaymentMethod = code },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.checkoutPaymentMethod = code },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimsonRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            if (viewModel.checkoutPaymentMethod != "Cash on Delivery") {
                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🇮🇹 QR Scan Code Activated", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Simply click 'Place Order' below to simulate immediate direct payment API validation.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.handleCheckout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_order_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Place Order (NPR ${viewModel.calculateCartTotal().toInt()})", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SCREEN 6: FUN ZONE (GAMIFICATION / QUIZ / SPIN) ---
@Composable
fun FunZoneScreen(viewModel: AnimeKitViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    var activeTab by remember { mutableStateOf("Games") } // "Games", "Stats & Loyalty"

    Column(modifier = Modifier.fillMaxSize()) {
        // Loyalty Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sakshyam Pudasaini", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Otaku Level: ${stats?.level ?: 1} | Streak: ${stats?.dailyStreak ?: 1} 🔥", color = CrimsonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Card(colors = CardDefaults.cardColors(containerColor = CrimsonRed)) {
                    Text("${stats?.loyaltyPoints ?: 0} NPR Points", color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Toggle Tabs
        TabRow(selectedTabIndex = if (activeTab == "Games") 0 else 1, contentColor = CrimsonRed, containerColor = Color.White) {
            Tab(selected = activeTab == "Games", onClick = { activeTab = "Games" }) {
                Text("🕹️ Games", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeTab == "Stats & Loyalty", onClick = { activeTab = "Stats & Loyalty" }) {
                Text("🏆 Achievements", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
        }

        if (activeTab == "Games") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Daily Reward Claim Check
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DAILY LOGIN BONUSES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Claim daily to multiply your points!", color = Color.Gray, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.claimDailyLoginReward() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Claim Reward", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                // Game 1: Lucky Spin Wheel
                Text("🎡 DAILY LUCKY SPIN", fontWeight = FontWeight.Black, fontSize = 13.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Graphic spin block
                        val rotation by animateFloatAsState(targetValue = viewModel.spinWheelDegree, animationSpec = tween(durationMillis = 2000))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .rotate(rotation)
                                .background(Color(0xFFFAFAFA), CircleShape)
                                .border(4.dp, CrimsonRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw simplistic pointer divisions
                            Text("🎯", fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.runSpinWheel() },
                            enabled = !viewModel.spinIsRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                        ) {
                            Text(if (viewModel.spinIsRunning) "SPINNING..." else "SPIN FOR MERCH PRIZES", color = Color.White)
                        }
                        if (viewModel.lastSpinReward.isNotEmpty()) {
                            Text("You Won: ${viewModel.lastSpinReward}", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                // Game 2: Anime Quiz
                Spacer(modifier = Modifier.height(16.dp))
                Text("🧠 HOKAGE ANIME QUIZ", fontWeight = FontWeight.Black, fontSize = 13.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (viewModel.quizIsCompleted) {
                            Text("Quiz Completed! Score: ${viewModel.quizScore}/5", fontWeight = FontWeight.Bold, color = CrimsonRed)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.restartQuiz() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                                Text("Play Again", color = Color.White)
                            }
                        } else {
                            val q = viewModel.quizQuestions[viewModel.currentQuizQuestionIndex]
                            Text("Question ${viewModel.currentQuizQuestionIndex + 1} of 5:", color = Color.Gray, fontSize = 11.sp)
                            Text(q.question, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                            
                            q.options.forEachIndexed { idx, option ->
                                val isSelected = viewModel.quizAnswerSelected == idx
                                val isCorrect = idx == q.correctIndex
                                val buttonColor = when {
                                    viewModel.quizAnswerSelected == null -> Color.White
                                    isCorrect -> Color(0xFFE2F0D9)
                                    isSelected -> Color(0xFFFCE4D6)
                                    else -> Color.White
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = buttonColor),
                                    border = BorderStroke(1.dp, Color.LightGray),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.selectQuizAnswer(idx) }
                                ) {
                                    Text(option, modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (viewModel.quizAnswerSelected != null) {
                                Text(viewModel.quizFeedbackMessage, color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                Button(
                                    onClick = { viewModel.nextQuizQuestion() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Next Question", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Game 3: Memory Match Game
                Spacer(modifier = Modifier.height(16.dp))
                Text("🎴 MEMORY MATCH (OTAQUIS)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Moves: ${viewModel.memoryMoves}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Matches: ${viewModel.memoryMatches}/6", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val cards = viewModel.memoryCards
                        val rows = cards.chunked(4)
                        rows.forEachIndexed { rowIdx, rowCards ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowCards.forEach { card ->
                                    val isShowing = card.isFlipped || card.isMatched
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(55.dp)
                                            .padding(vertical = 4.dp)
                                            .background(
                                                color = if (isShowing) CrimsonRedLight else Color.Black,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { viewModel.flipMemoryCard(card.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isShowing) card.content else "キット",
                                            color = if (isShowing) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                        if (viewModel.memoryIsCompleted) {
                            Text("Infinite Intellect achieved! Play again!", color = CrimsonRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        Button(onClick = { viewModel.resetMemoryGame() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                            Text("Reset Board", color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Stats, Achievements & Leaderboard List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("MY OTAKU ACHIEVEMENT MILESTONES", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                items(achievements) { ach ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (ach.isUnlocked) Color(0xFFE2F0D9) else Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (ach.isUnlocked) Color(0xFF385723) else Color.LightGray)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (ach.isUnlocked) Color(0xFF385723) else Color.Gray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(ach.description, color = Color.DarkGray, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("+${ach.pointsReward} NPR", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Unlocked", color = Color(0xFF385723), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(if (ach.isUnlocked) 1f else 0f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: PROFILE, CHAT & ADMIN PORTAL ---
@Composable
fun ProfileScreen(viewModel: AnimeKitViewModel, onShowGuestPrompt: () -> Unit) {
    var showChatDialog by remember { mutableStateOf(false) }
    val orders by viewModel.orders.collectAsState()
    val wishlist by viewModel.wishlistItems.collectAsState()
    val products by viewModel.products.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Mode Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (viewModel.isAdminMode) "ADMIN STORE DASHBOARD" else "MY OTAKU ACCOUNT",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = if (viewModel.isAdminMode) CrimsonRed else Color.Black
            )
            // Quick Chat bubble
            if (!viewModel.isAdminMode) {
                IconButton(onClick = { 
                    if (!viewModel.isLoggedIn) {
                        onShowGuestPrompt()
                    } else {
                        showChatDialog = true 
                    }
                }) {
                    Icon(Icons.Default.Chat, contentDescription = "Live Support", tint = CrimsonRed)
                }
            }
        }

        if (viewModel.isAdminMode) {
            // ADMIN PORTAL VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Financial Analytics Card
                item {
                    val sales = orders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount }
                    Card(colors = CardDefaults.cardColors(containerColor = Color.Black), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TOTAL REVENUE (NEPAL)", color = Color.Gray, fontSize = 11.sp)
                            Text("Rs. ${sales.toInt()}", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Total Orders Received: ${orders.size}", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Coupon Generator
                item {
                    Text("CREATE NEW PROMO COUPON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            var code by remember { mutableStateOf("") }
                            var pct by remember { mutableStateOf("15") }
                            var minSpend by remember { mutableStateOf("1000") }
                            var desc by remember { mutableStateOf("") }

                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                label = { Text("Coupon Code (e.g. SAGAR)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = pct,
                                    onValueChange = { pct = it },
                                    label = { Text("Discount %") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = minSpend,
                                    onValueChange = { minSpend = it },
                                    label = { Text("Min Spend (NPR)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = desc,
                                onValueChange = { desc = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (code.isNotEmpty()) {
                                        viewModel.adminCreateCoupon(
                                            code = code,
                                            pct = pct.toDoubleOrNull() ?: 10.0,
                                            maxDisc = 500.0,
                                            minSpnd = minSpend.toDoubleOrNull() ?: 1000.0,
                                            desc = desc
                                        )
                                        code = ""
                                        desc = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Activate Promo Coupon", color = Color.White)
                            }
                        }
                    }
                }

                // Customer Chat Manager
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LIVE CUSTOMER INQUIRIES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "Mark All Resolved", 
                            color = CrimsonRed, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 11.sp, 
                            modifier = Modifier.clickable { viewModel.adminMarkAllChatsResolved() }
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Open Chat Portal Inline", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(onClick = { showChatDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                                Text("Open Real-time Admin Chat Engine", color = Color.White)
                            }
                        }
                    }
                }

                // Active orders List
                item {
                    Text("MANAGE INCOMING ORDERS", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                }

                if (orders.isEmpty()) {
                    item {
                        Text("No orders placed yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    items(orders) { order ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ID: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(order.status, color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text("Address: ${order.shippingAddress}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("Total: Rs. ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.adminUpdateOrderStatus(order.id, "Shipping") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Ship Out", color = Color.White, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.adminUpdateOrderStatus(order.id, "Delivered") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF385723)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Deliver", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // STANDARD CUSTOMER ACCOUNT VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Details and Edit Form
                item {
                    val stats by viewModel.userStats.collectAsState()
                    var isEditing by remember { mutableStateOf(false) }
                    var isShowingSettings by remember { mutableStateOf(false) }
                    var isShowingDeleteConfirm by remember { mutableStateOf(false) }

                    // Local temp input states
                    var fullNameInput by remember(stats) { mutableStateOf(stats?.fullName ?: "") }
                    var usernameInput by remember(stats) { mutableStateOf(stats?.username ?: "") }
                    var phoneInput by remember(stats) { mutableStateOf(stats?.phoneNumber ?: "") }
                    var addressInput by remember(stats) { mutableStateOf(stats?.deliveryAddress ?: "") }
                    var photoUrlInput by remember(stats) { mutableStateOf(stats?.profilePhotoUrl ?: "") }

                    // Change email / password states
                    var changeEmailInput by remember { mutableStateOf("") }
                    var changePasswordInput by remember { mutableStateOf("") }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(CrimsonRedLight, CircleShape)
                                        .border(2.dp, CrimsonRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photoUrlInput.isNotEmpty()) {
                                        Text(photoUrlInput, fontSize = 28.sp)
                                    } else {
                                        Text("👤", fontSize = 28.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stats?.fullName?.uppercase() ?: "SAKSHYAM PUDASAINI", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
                                    Text("@${stats?.username ?: "sakshyam"}", color = Color.Gray, fontSize = 12.sp)
                                    Text(viewModel.currentUserEmail.ifEmpty { stats?.email ?: "sakshyampudasaini66@gmail.com" }, color = Color.Gray, fontSize = 11.sp)
                                    if (stats?.phoneNumber?.isNotEmpty() == true) {
                                        Text("📱 ${stats?.phoneNumber}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    if (stats?.deliveryAddress?.isNotEmpty() == true) {
                                        Text("📍 ${stats?.deliveryAddress}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { isEditing = !isEditing },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) Color.Black else CrimsonRed),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isEditing) "Cancel Edit" else "Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { isShowingSettings = !isShowingSettings },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Text(if (isShowingSettings) "Hide Security" else "Account Security", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isEditing) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("UPDATE PROFILE DETAILS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CrimsonRed)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = fullNameInput,
                                    onValueChange = { fullNameInput = it },
                                    label = { Text("Full Name", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = usernameInput,
                                    onValueChange = { usernameInput = it },
                                    label = { Text("Username", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    label = { Text("Phone Number", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = addressInput,
                                    onValueChange = { addressInput = it },
                                    label = { Text("Delivery Address", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = photoUrlInput,
                                    onValueChange = { photoUrlInput = it },
                                    label = { Text("Avatar Emoji (e.g. 🦸, 🍥, 🦊, 🌸)", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.updateProfile(fullNameInput, usernameInput, phoneInput, addressInput, photoUrlInput)
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("SAVE PROFILE DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isShowingSettings) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("ACCOUNT SECURITY SETTINGS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CrimsonRed)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = changeEmailInput,
                                    onValueChange = { changeEmailInput = it },
                                    label = { Text("New Email Address", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        if (changeEmailInput.isNotEmpty()) {
                                            viewModel.changeEmail(changeEmailInput) { success ->
                                                if (success) {
                                                    viewModel.triggerToastNotification("Email Changed", "Account email updated successfully!")
                                                    changeEmailInput = ""
                                                } else {
                                                    viewModel.triggerToastNotification("Change Failed", viewModel.authErrorMessage)
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("UPDATE ACCOUNT EMAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = changePasswordInput,
                                    onValueChange = { changePasswordInput = it },
                                    label = { Text("New Secure Password", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        if (changePasswordInput.isNotEmpty()) {
                                            viewModel.changePassword(changePasswordInput) { success ->
                                                if (success) {
                                                    viewModel.triggerToastNotification("Password Changed", "Account password updated successfully!")
                                                    changePasswordInput = ""
                                                } else {
                                                    viewModel.triggerToastNotification("Change Failed", viewModel.authErrorMessage)
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("UPDATE PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.logout() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("LOGOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { isShowingDeleteConfirm = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("DELETE ACCOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (isShowingDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { isShowingDeleteConfirm = false },
                            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                            text = { Text("Are you absolutely sure you want to delete your account permanently? All orders, wishlist, and accumulated loyalty rewards will be lost forever. This action is IRREVERSIBLE.", fontSize = 12.sp) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteAccount { success ->
                                            isShowingDeleteConfirm = false
                                            if (!success) {
                                                viewModel.triggerToastNotification("Deletion Failed", viewModel.authErrorMessage)
                                            }
                                        }
                                    }
                                ) {
                                    Text("DELETE FOREVER", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { isShowingDeleteConfirm = false }) {
                                    Text("CANCEL", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        )
                    }
                }

                // Loyalty Points display
                item {
                    val stats by viewModel.userStats.collectAsState()
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("MY CURRENT LOYALTY SAVINGS", color = Color.Gray, fontSize = 11.sp)
                            Text("${stats?.loyaltyPoints ?: 0} NPR Points", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Equal to Rs. ${stats?.loyaltyPoints ?: 0} discount on checkout!", color = Color.DarkGray, fontSize = 11.sp)
                        }
                    }
                }

                // Wishlist display
                item {
                    Text("MY WISHLIST (${wishlist.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (wishlist.isEmpty()) {
                    item {
                        Text("No items wishlisted. Tap favorite hearts on items!", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(wishlist) { item ->
                                val p = products.find { it.id == item.productId }
                                if (p != null) {
                                    ProductCard(p, onClick = { viewModel.selectProduct(p.id) })
                                }
                            }
                        }
                    }
                }

                // Order history display
                item {
                    Text("MY RECENT ORDERS (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                }

                if (!viewModel.isLoggedIn) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShowGuestPrompt() }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔒 Order History Locked", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrimsonRed)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Login to save your progress, place orders, and track your history.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else if (orders.isEmpty()) {
                    item {
                        Text("No order history yet. Start buying exclusive merch!", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    items(orders) { order ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Order ${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(CrimsonRedLight, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(order.status, color = CrimsonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Total: Rs. ${order.totalAmount.toInt()} via ${order.paymentMethod}", fontSize = 11.sp)
                                Text("Delivery: ${order.shippingAddress}", fontSize = 11.sp, color = Color.Gray)
                                Text("Tracking: ${order.trackingNumber}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // CUSTOMER SUPPORT CHAT OVERLAY ENGINE
        if (showChatDialog) {
            AlertDialog(
                onDismissRequest = { showChatDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewModel.isAdminMode) "Owner Live Support Portal" else "Anime Kit Nepal Live Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { showChatDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    val messages by viewModel.chatMessages.collectAsState()
                    Column(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                        // Chats Scroll Container
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { msg ->
                                val isMe = if (viewModel.isAdminMode) msg.isFromAdmin else !msg.isFromAdmin
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMe) CrimsonRed else Slate100
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(msg.senderName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
                                            Text(msg.text, fontSize = 12.sp, color = if (isMe) Color.White else Color.Black)
                                            if (msg.imageUrl != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Simulated image stamp
                                                Text(msg.imageUrl, fontSize = 18.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sticker simulator selectors
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val stickerList = listOf("🔥", "🌸", "🍥", "👑", "🛡️")
                            stickerList.forEach { sticker ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFF5F5F5), CircleShape)
                                        .clickable { viewModel.chatSelectedStickerUrl = sticker }
                                        .border(
                                            width = 1.dp,
                                            color = if (viewModel.chatSelectedStickerUrl == sticker) CrimsonRed else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(sticker, fontSize = 16.sp)
                                }
                            }
                        }

                        // Input Writer Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = viewModel.chatInputText,
                                onValueChange = { viewModel.chatInputText = it },
                                placeholder = { Text("Write message...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.sendChatMessage() },
                                modifier = Modifier.testTag("send_chat_msg_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = CrimsonRed)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AnimeKitViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepSignedIn by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Anime Logo Illustration
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(CrimsonRedLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🍥", fontSize = 54.sp)
            }

            Text(
                text = "Welcome to Anime Kit",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Premium Anime Merchandise Store • Nuwakot, Nepal",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (validationError.isNotEmpty()) {
                Text(
                    text = validationError,
                    color = CrimsonRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else if (viewModel.authErrorMessage.isNotEmpty()) {
                Text(
                    text = viewModel.authErrorMessage,
                    color = CrimsonRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    validationError = ""
                },
                label = { Text("Email Address") },
                placeholder = { Text("otaku@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonRed,
                    focusedLabelColor = CrimsonRed
                )
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    validationError = ""
                },
                label = { Text("Password") },
                placeholder = { Text("••••••••") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonRed,
                    focusedLabelColor = CrimsonRed
                )
            )

            // Keep me signed in & Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { keepSignedIn = !keepSignedIn }
                ) {
                    Checkbox(
                        checked = keepSignedIn,
                        onCheckedChange = { keepSignedIn = it },
                        colors = CheckboxDefaults.colors(checkedColor = CrimsonRed)
                    )
                    Text("Keep Me Signed In", fontSize = 11.sp, color = Color.DarkGray)
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 11.sp,
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen = "forgot_password" }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (viewModel.isAuthLoading) {
                CircularProgressIndicator(color = CrimsonRed)
            } else {
                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty()) {
                            validationError = "Please enter both email and password."
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            validationError = "Please enter a valid email address."
                        } else {
                            viewModel.login(email.trim(), password.trim()) { success ->
                                if (success) {
                                    viewModel.triggerToastNotification("Welcome Back!", "Logged in successfully as $email")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_btn_submit"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        text = "OR SIGN IN WITH",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                // Google Sign In Simulated Trigger
                OutlinedButton(
                    onClick = {
                        viewModel.loginWithGoogle(
                            idToken = "simulated_google_token_${(100000..999999).random()}",
                            email = "sakshyampudasaini66@gmail.com",
                            name = "Sakshyam Pudasaini"
                        ) { success ->
                            if (success) {
                                viewModel.triggerToastNotification("Welcome, Sakshyam!", "Logged in with Google!")
                            }
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🦅 ", fontSize = 16.sp)
                        Text("Sign In with Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "Sign Up",
                    fontSize = 12.sp,
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen = "signup" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "⚡ Browse as Guest",
                fontSize = 13.sp,
                color = CrimsonRed,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clickable { 
                        viewModel.browseAsGuest()
                    }
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: AnimeKitViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(CrimsonRedLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🦊", fontSize = 44.sp)
            }

            Text(
                text = "Join Anime Kit Nepal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )

            if (validationError.isNotEmpty()) {
                Text(
                    text = validationError,
                    color = CrimsonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else if (viewModel.authErrorMessage.isNotEmpty()) {
                Text(
                    text = viewModel.authErrorMessage,
                    color = CrimsonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Inputs
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; validationError = "" },
                label = { Text("Full Name") },
                placeholder = { Text("Sakshyam Pudasaini") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("signup_fullname_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; validationError = "" },
                label = { Text("Username") },
                placeholder = { Text("sakshyam66") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; validationError = "" },
                label = { Text("Email Address") },
                placeholder = { Text("sakshyam@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; validationError = "" },
                label = { Text("Password (Min 6 chars)") },
                placeholder = { Text("••••••••") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; validationError = "" },
                label = { Text("Phone Number") },
                placeholder = { Text("+977-98XXXXXXXX") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; validationError = "" },
                label = { Text("Delivery Address") },
                placeholder = { Text("Nuwakot, Nepal") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (viewModel.isAuthLoading) {
                CircularProgressIndicator(color = CrimsonRed)
            } else {
                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty() || fullName.trim().isEmpty() || username.trim().isEmpty()) {
                            validationError = "Please fill in all required fields."
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            validationError = "Please enter a valid email address."
                        } else if (password.length < 6) {
                            validationError = "Password must be at least 6 characters long."
                        } else {
                            viewModel.signUp(email.trim(), password.trim(), fullName.trim(), username.trim(), phone.trim(), address.trim()) { success ->
                                if (success) {
                                    viewModel.triggerToastNotification("Welcome to Anime Kit!", "Account successfully created!")
                                    viewModel.sendEmailVerification { verified ->
                                        if (verified) {
                                            viewModel.triggerToastNotification("Verification Sent", "Please verify your email address.")
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("signup_submit_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "Login",
                    fontSize = 12.sp,
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen = "login" }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(viewModel: AnimeKitViewModel) {
    var email by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(CrimsonRedLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🔑", fontSize = 44.sp)
            }

            Text(
                text = "Forgot Password?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )

            Text(
                text = "Enter your registered email address and we will send you a password reset link.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (validationError.isNotEmpty()) {
                Text(
                    text = validationError,
                    color = CrimsonRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (viewModel.authErrorMessage.isNotEmpty()) {
                Text(
                    text = viewModel.authErrorMessage,
                    color = CrimsonRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (resetSent) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Reset link sent! Please check your inbox and follow the link to change your password.",
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; validationError = "" },
                    label = { Text("Email Address") },
                    placeholder = { Text("sakshyam@example.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonRed, focusedLabelColor = CrimsonRed)
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (viewModel.isAuthLoading) {
                    CircularProgressIndicator(color = CrimsonRed)
                } else {
                    Button(
                        onClick = {
                            if (email.trim().isEmpty()) {
                                validationError = "Please enter your email address."
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                validationError = "Please enter a valid email address."
                            } else {
                                viewModel.forgotPassword(email.trim()) { success ->
                                    if (success) {
                                        resetSent = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SEND RESET LINK", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Back to Login",
                fontSize = 12.sp,
                color = CrimsonRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { viewModel.currentScreen = "login" }
            )
        }
    }
}
