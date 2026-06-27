package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.ui.viewmodel.AnimeKitViewModel
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

// --- DATA CLASS FOR THE REDESIGNED ARCADE ---
data class ArcadeGame(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val difficulty: String, // "Genin", "Chunin", "Jonin", "Kage"
    val isHot: Boolean = false
)

@Composable
fun FunZoneArcadeScreen(viewModel: AnimeKitViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    var activeTab by remember { mutableStateOf("Games") } // "Games", "Leaderboards", "Badges"
    var selectedGameId by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // List of 20 distinct offline lightweight games
    val gamesList = remember {
        listOf(
            ArcadeGame("quiz", "Anime Quiz", "🧠", "Test your Otaku intelligence across multiple levels.", "Chunin", isHot = true),
            ArcadeGame("guess_char", "Guess Character", "👤", "Who is this? Read the traits and select the hero.", "Genin"),
            ArcadeGame("guess_anime", "Guess the Anime", "🎬", "Decode the anime name from emojis and plot keys.", "Genin"),
            ArcadeGame("memory", "Memory Match", "🎴", "Flip cards and find pairs in record time.", "Chunin"),
            ArcadeGame("lucky_spin", "Lucky Spin", "🎡", "Spin the legendary chakra wheel for prizes and points.", "Genin", isHot = true),
            ArcadeGame("anime_slider", "Shinobi Grid Sliding", "🧩", "Slide the anime cells into their correct positions.", "Jonin"),
            ArcadeGame("runner", "Endless Runner", "🏃", "Tap to jump over deadly oncoming shurikens.", "Chunin"),
            ArcadeGame("wall_jump", "Ninja Wall Jump", "🧗", "Dodge falling obstacles by jumping between walls.", "Jonin", isHot = true),
            ArcadeGame("shuriken_throw", "Shuriken Bullseye", "🎯", "Time your taps perfectly to hit the bullseye target.", "Chunin"),
            ArcadeGame("tap_speed", "Chakra Speed Run", "⚡", "Tap the Rasengan wheel as fast as you can in 8s.", "Genin"),
            ArcadeGame("reaction", "Reaction Aura Test", "👁️‍🗨️", "Tap instantly when the screen flashes green.", "Jonin"),
            ArcadeGame("whack_chibi", "Whack-A-Chibi", "🔨", "Tap chibis as they pop out of the ninja scroll holes.", "Chunin"),
            ArcadeGame("num_puzzle", "Sliding Tile 15", "🔢", "Rearrange tiles from 1 to 8 in sequential order.", "Jonin"),
            ArcadeGame("connect_tiles", "Connect Emojis", "🔗", "Match adjacent anime emojis to eliminate them.", "Chunin"),
            ArcadeGame("num_2048", "Otaku Power 2048", "❇️", "Combine matching numbers to reach 1024 chakra.", "Jonin"),
            ArcadeGame("color_match", "Chakra Color Stroop", "🎨", "Choose if color name matches text font color.", "Chunin"),
            ArcadeGame("pixel_art", "Pixel Leaf Painter", "🖍️", "Tap cells on grid to draw anime emblem art.", "Genin"),
            ArcadeGame("emoji_guess", "Emoji Decoder", "💬", "Translate emoji sequences into famous anime quotes.", "Genin"),
            ArcadeGame("boss_raid", "Daily Boss Raid", "👹", "Deplete Nine-Tails' shield before the timer ends.", "Kage", isHot = true),
            ArcadeGame("rescue_chibi", "Rescue Falling Chibi", "🛡️", "Catch falling coins and dodge iron weight traps.", "Kage")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F13)) // Cyber retro dark theme
    ) {
        // Arcade Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color(0xFF161622))
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (viewModel.isLoggedIn) (stats?.fullName ?: "Sakshyam Pudasaini") else "Guest Mode 👤",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Otaku Level: ${stats?.level ?: 1}",
                                color = CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Streak: ${stats?.dailyStreak ?: 1} 🔥",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${stats?.loyaltyPoints ?: 0} PTS",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                        if (!viewModel.isLoggedIn) {
                            Text(
                                text = "Points temporary",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                
                // XP Progress Bar
                Spacer(modifier = Modifier.height(10.dp))
                val currentXp = stats?.xp ?: 0
                val level = stats?.level ?: 1
                val xpInCurrentLevel = currentXp % 500
                val progress = xpInCurrentLevel.toFloat() / 500f
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("XP: $xpInCurrentLevel / 500", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(Color.DarkGray, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(CrimsonRed, Color(0xFFFF4D6D))
                                    ),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }
        }

        // Sub Navigation TabRow (Retro styling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161622))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tabs = listOf("Games" to "🕹️ Arcade", "Leaderboards" to "🏆 Leaders", "Badges" to "🎖️ Badges")
            tabs.forEach { (tabId, label) ->
                val isSelected = activeTab == tabId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) CrimsonRed else Color.Transparent)
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeTab = tabId 
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- TAB CONTENTS ---
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                "Games" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Daily Reward Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF2E2E3E))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("DAILY CHAKRA BONUS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    Text("Log in daily to scale rewards & multiply XP!", color = Color.Gray, fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { 
                                        viewModel.claimDailyLoginReward() 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Claim Bonus", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Grid of 20 Games
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(gamesList) { game ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (game.isHot) CrimsonRed else Color(0xFF2E2E3E)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedGameId = game.id
                                        }
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(Color(0xFF2E2E3E), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(game.icon, fontSize = 20.sp)
                                                }
                                                
                                                // Difficulty tag
                                                val diffColor = when (game.difficulty) {
                                                    "Genin" -> Color(0xFF4CAF50)
                                                    "Chunin" -> Color(0xFF2196F3)
                                                    "Jonin" -> Color(0xFFFF9800)
                                                    else -> CrimsonRed
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(diffColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(game.difficulty, color = diffColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(game.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(game.description, color = Color.Gray, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 2)
                                        }
                                        
                                        if (game.isHot) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(y = (-4).dp, x = 4.dp)
                                                    .background(CrimsonRed, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("HOT", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                "Leaderboards" -> {
                    // Retro Arcade High Scores list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("NEPAL ANIME CABINET LEADERBOARD 🏆", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Top players in the Fun Zone this week", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 10.dp))
                        }
                        
                        val dummyLeaders = listOf(
                            Triple("Niranjan_Zoro", 15400, "👑 Level 12"),
                            Triple("Sakshyam_Pudasaini", 12100, "🔥 Level 9"),
                            Triple("Arun_Kage", 10200, "⚡ Level 8"),
                            Triple("Prashant_Otaku", 8900, "🎯 Level 6"),
                            Triple("Shreya_Hinata", 7600, "🌸 Level 5"),
                            Triple("Guest_Naruto", 4500, "🍃 Level 3")
                        )
                        
                        itemsIndexed(dummyLeaders) { idx, leader ->
                            val cardBg = if (leader.first == "Sakshyam_Pudasaini") Color(0xFF2E1C22) else Color(0xFF1E1E2C)
                            val borderColor = if (idx == 0) Color(0xFFFFB300) else Color.Transparent
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(12.dp),
                                border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (idx) {
                                                0 -> "🥇"
                                                1 -> "🥈"
                                                2 -> "🥉"
                                                else -> "  ${idx + 1}. "
                                            },
                                            fontSize = 16.sp,
                                            modifier = Modifier.width(32.dp)
                                        )
                                        Column {
                                            Text(leader.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(leader.third, color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }
                                    
                                    Text("${leader.second} pts", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
                
                "Badges" -> {
                    // Milestones and Badges list
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(achievements) { ach ->
                            val isUnlocked = ach.isUnlocked
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFF222B21) else Color(0xFF1E1E2C)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF388E3C) else Color(0xFF2E2E3E)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(if (isUnlocked) Color(0xFF388E3C) else Color.DarkGray, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = ach.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = ach.description,
                                        color = Color.LightGray,
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        lineHeight = 10.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ACTIVE CABINET MODE (OVERLAY OVER THE ARCADE) ---
    if (selectedGameId != null) {
        val gameId = selectedGameId!!
        val game = gamesList.find { it.id == gameId }
        if (game != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header of cabinet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(game.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(game.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("Cabinet difficulty: ${game.difficulty}", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                        
                        IconButton(onClick = { selectedGameId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close game", tint = Color.White)
                        }
                    }
                    
                    Divider(color = Color(0xFF1E1E2C), modifier = Modifier.padding(vertical = 10.dp))
                    
                    // Host of game controller
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF0F0F13), RoundedCornerShape(20.dp))
                            .border(2.dp, CrimsonRed, RoundedCornerShape(20.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameControllerHost(
                            gameId = gameId,
                            viewModel = viewModel,
                            onClose = { selectedGameId = null }
                        )
                    }
                }
            }
        }
    }
}

// --- CONTAINER HOST FOR INDIVIDUAL ARCADE CABINET GAMEPLAY ENGINE ---
@Composable
fun GameControllerHost(
    gameId: String,
    viewModel: AnimeKitViewModel,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var gameScore by remember { mutableStateOf(0) }
    var gameLevel by remember { mutableStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }
    var showVictoryOverlay by remember { mutableStateOf(false) }
    var pointsEarned by remember { mutableStateOf(0) }
    var xpEarned by remember { mutableStateOf(0) }
    
    // Callback to reward stats
    fun completeCabinetPlay(score: Int, levelsCleared: Int) {
        gameOver = true
        xpEarned = score / 5 + (levelsCleared * 30)
        pointsEarned = (score / 15 + levelsCleared * 5).coerceIn(0, 100)
        
        viewModel.rewardArcadePlay(xpEarned, pointsEarned)
        showVictoryOverlay = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!gameOver) {
            when (gameId) {
                "quiz" -> QuickQuizCabinet(onComplete = { completeCabinetPlay(it * 100, 1) })
                "guess_char" -> GuessCharacterCabinet(onComplete = { completeCabinetPlay(it * 100, 1) })
                "guess_anime" -> GuessAnimeCabinet(onComplete = { completeCabinetPlay(it * 100, 1) })
                "memory" -> MemoryMatchCabinet(onComplete = { completeCabinetPlay(1000 - it * 20, 1) })
                "lucky_spin" -> LuckySpinCabinet(onComplete = { completeCabinetPlay(it, 1) })
                "anime_slider" -> SliderCabinet(onComplete = { completeCabinetPlay(1000 - it * 50, 2) })
                "runner" -> RunnerCabinet(onComplete = { completeCabinetPlay(it, 2) })
                "wall_jump" -> WallJumpCabinet(onComplete = { completeCabinetPlay(it, 2) })
                "shuriken_throw" -> ShurikenThrowCabinet(onComplete = { completeCabinetPlay(it * 200, 1) })
                "tap_speed" -> TapSpeedCabinet(onComplete = { completeCabinetPlay(it * 50, 1) })
                "reaction" -> ReactionTestCabinet(onComplete = { completeCabinetPlay(it, 1) })
                "whack_chibi" -> WhackChibiCabinet(onComplete = { completeCabinetPlay(it * 80, 1) })
                "num_puzzle" -> Puzzle15Cabinet(onComplete = { completeCabinetPlay(1000 - it * 20, 1) })
                "connect_tiles" -> ConnectTilesCabinet(onComplete = { completeCabinetPlay(it * 120, 2) })
                "num_2048" -> Otaku2048Cabinet(onComplete = { completeCabinetPlay(it, 2) })
                "color_match" -> ColorMatchCabinet(onComplete = { completeCabinetPlay(it * 100, 1) })
                "pixel_art" -> PixelArtCabinet(onComplete = { completeCabinetPlay(300, 1) })
                "emoji_guess" -> EmojiDecoderCabinet(onComplete = { completeCabinetPlay(it * 150, 1) })
                "boss_raid" -> BossRaidCabinet(onComplete = { completeCabinetPlay(it, 3) })
                "rescue_chibi" -> RescueChibiCabinet(onComplete = { completeCabinetPlay(it, 2) })
                else -> {
                    Text("Game missing!", color = Color.White)
                }
            }
        }

        // Victory Reward Overlay Frame
        if (showVictoryOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F13))
                    .clickable { /* Block taps */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("🎉", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("STAGE CLEARED!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("💥 BAM! ✨ SWOOSH! 🔔 DING!", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("REWARDS EARNED", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚡ XP", color = Color.LightGray, fontSize = 12.sp)
                                    Text("+$xpEarned", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🏅 points", color = Color.LightGray, fontSize = 12.sp)
                                    Text("+$pointsEarned", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                            }
                            
                            if (!viewModel.isLoggedIn) {
                                Divider(color = Color(0xFF2E2E3E), modifier = Modifier.padding(vertical = 12.dp))
                                Text(
                                    text = "⚠️ Guest mode: Points are temporary and will be lost. Login/register to save them forever!",
                                    color = Color(0xFFFFB300),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = { onClose() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("BACK TO ARCADE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// --- INDIVIDUAL COMPOSABLE GAMS ENGINES ---
// ==========================================

// 1. Anime Quiz
@Composable
fun QuickQuizCabinet(onComplete: (Int) -> Unit) {
    val questions = remember {
        listOf(
            Triple("Who is the main protagonist of Solo Leveling?", listOf("Jin-Woo", "Jin-Chul", "Dong-Su", "Tae-Gyu"), 0),
            Triple("What is the name of Luffy's ultimate transformation in Wano?", listOf("Gear Fourth", "Gear Fifth", "Gear Shogun", "Snakeman"), 1),
            Triple("Who is the creator of the cursed energy system in JJK?", listOf("Sukuna", "Gojo", "Kenjaku", "Akutami"), 3),
            Triple("What is Tanjirou's primary sword style in Demon Slayer?", listOf("Flame Breathing", "Wind Breathing", "Sun Breathing / Hinokami", "Thunder Breathing"), 2),
            Triple("Which anime features the legendary Nuwakot-themed ninja emblem?", listOf("Anime Kit Special", "Naruto Shippuden", "Shingeki", "Death Note"), 0)
        )
    }
    var curIdx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedAns by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("QUIZ LEVEL ${curIdx + 1}/5", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(10.dp))
        val q = questions[curIdx]
        Text(q.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        
        q.second.forEachIndexed { idx, opt ->
            val color = when {
                selectedAns == null -> Color(0xFF1E1E2C)
                idx == q.third -> Color(0xFF388E3C)
                selectedAns == idx -> Color(0xFFD32F2F)
                else -> Color(0xFF1E1E2C)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                border = BorderStroke(1.dp, Color(0xFF2E2E3E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (selectedAns == null) {
                            selectedAns = idx
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (idx == q.third) score++
                        }
                    }
            ) {
                Text(opt, color = Color.White, modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        
        if (selectedAns != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (curIdx < 4) {
                        curIdx++
                        selectedAns = null
                    } else {
                        onComplete(score)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("NEXT QUESTION ➡️", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 2. Guess Character
@Composable
fun GuessCharacterCabinet(onComplete: (Int) -> Unit) {
    val chars = remember {
        listOf(
            Triple("Traits: 🦊, 🍃, Orange jumpsuits, Loves Ramen.", listOf("Sasuke", "Luffy", "Naruto", "Goku"), 2),
            Triple("Traits: 👒, 🌊, Rubber body, Strawhat.", listOf("Zoro", "Luffy", "Sanji", "Shanks"), 1),
            Triple("Traits: ⚔️, 🟢, Three swords, Lost navigation.", listOf("Zoro", "Tanjirou", "Ichigo", "Kirito"), 0),
            Triple("Traits: 🌌, 👁️, Blindfolded, Limitless void.", listOf("Gojo", "Megumi", "Kakashi", "Kenpachi"), 0),
            Triple("Traits: 🛡️, ❄️, Shadow monarch, Dagger dual wielder.", listOf("Sung Jin-Woo", "Tae-Gyu", "Chul-Su", "Dong-Myeong"), 0)
        )
    }
    var idx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var ans by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("GUESS THE CHARACTER", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        val current = chars[idx]
        Text(current.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        
        current.second.forEachIndexed { i, c ->
            val color = when {
                ans == null -> Color(0xFF1E1E2C)
                i == current.third -> Color(0xFF388E3C)
                ans == i -> Color(0xFFD32F2F)
                else -> Color(0xFF1E1E2C)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (ans == null) {
                            ans = i
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (i == current.third) score++
                        }
                    }
            ) {
                Text(c, color = Color.White, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        
        if (ans != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (idx < 4) {
                        idx++
                        ans = null
                    } else {
                        onComplete(score)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("NEXT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3. Guess the Anime
@Composable
fun GuessAnimeCabinet(onComplete: (Int) -> Unit) {
    val animes = remember {
        listOf(
            Triple("📺 Emojis: 📓 🖊️ 🍎 💀", listOf("Death Note", "Naruto", "My Hero Academia", "Attack on Titan"), 0),
            Triple("📺 Emojis: 👒 🏴‍☠️ 🍖 🌊", listOf("Demon Slayer", "One Piece", "Bleach", "Hunter x Hunter"), 1),
            Triple("📺 Emojis: 🗡️ 🎭 👺 ❄️", listOf("Demon Slayer", "Jujutsu Kaisen", "Naruto", "Dragon Ball"), 0),
            Triple("📺 Emojis: ⚔️ 🏰 ⛓️ 🥩", listOf("Attack on Titan", "Sword Art Online", "Fullmetal Alchemist", "Fairy Tail"), 0),
            Triple("📺 Emojis: 🧪 🦾 🚪 🪙", listOf("Fullmetal Alchemist", "Steins;Gate", "Dr. Stone", "Code Geass"), 0)
        )
    }
    var idx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("GUESS THE ANIME", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        val item = animes[idx]
        Text(item.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(20.dp))
        
        item.second.forEachIndexed { i, a ->
            val color = when {
                selected == null -> Color(0xFF1E1E2C)
                i == item.third -> Color(0xFF388E3C)
                selected == i -> Color(0xFFD32F2F)
                else -> Color(0xFF1E1E2C)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (selected == null) {
                            selected = i
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (i == item.third) score++
                        }
                    }
            ) {
                Text(a, color = Color.White, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        
        if (selected != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (idx < 4) {
                        idx++
                        selected = null
                    } else {
                        onComplete(score)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("NEXT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. Memory Match Card Class
data class MemoryCard(val id: Int, val emoji: String, var isFlipped: Boolean = false, var isMatched: Boolean = false)

@Composable
fun MemoryMatchCabinet(onComplete: (Int) -> Unit) {
    val originalEmojis = remember { listOf("🍥", "🦊", "👑", "🗡️", "🔑", "🧢") }
    var cards by remember {
        mutableStateOf(
            (originalEmojis + originalEmojis).shuffled().mapIndexed { index, s ->
                MemoryCard(id = index, emoji = s)
            }
        )
    }
    var firstFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("MEMORY SHINOBI CARDS", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Moves: $moves | Matches: ${cards.count { it.isMatched } / 2}/6", color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = cards.chunked(3)
        rows.forEachIndexed { rowIdx, rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEach { card ->
                    val isShowing = card.isFlipped || card.isMatched
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isShowing) CrimsonRedLight else Color(0xFF1E1E2C),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isShowing || firstFlippedIndex == card.id) return@clickable
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                
                                val updated = cards.map { if (it.id == card.id) it.copy(isFlipped = true) else it }
                                cards = updated
                                
                                if (firstFlippedIndex == null) {
                                    firstFlippedIndex = card.id
                                } else {
                                    moves++
                                    val firstIdx = firstFlippedIndex!!
                                    val firstCard = cards[firstIdx]
                                    if (firstCard.emoji == card.emoji) {
                                        // Match
                                        cards = cards.map { 
                                            if (it.id == firstIdx || it.id == card.id) it.copy(isMatched = true) else it
                                        }
                                        firstFlippedIndex = null
                                        if (cards.all { it.isMatched }) {
                                            onComplete(moves)
                                        }
                                    } else {
                                        // No match, turn back after delay
                                        scope.launch {
                                            delay(800)
                                            cards = cards.map { 
                                                if (it.id == firstIdx || it.id == card.id) it.copy(isFlipped = false) else it
                                            }
                                            firstFlippedIndex = null
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isShowing) card.emoji else "🌀",
                            fontSize = 20.sp,
                            color = if (isShowing) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

// 5. Lucky Spin
@Composable
fun LuckySpinCabinet(onComplete: (Int) -> Unit) {
    var rotation by remember { mutableStateOf(0f) }
    var spinning by remember { mutableStateOf(false) }
    var rewardText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHAKRA WHEEL OF LUCK 🎡", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(20.dp))
        
        val animatedRotation by animateFloatAsState(
            targetValue = rotation,
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
            finishedListener = {
                spinning = false
                val reward = listOf(50, 150, 300, 500, 20, 10).random()
                rewardText = "Congratulations! You earned +$reward Loyalty Chakra!"
                scope.launch {
                    delay(1500)
                    onComplete(reward)
                }
            }
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .rotate(animatedRotation)
                .background(Color(0xFF1E1E2C), CircleShape)
                .border(6.dp, CrimsonRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val segments = 6
                val angle = 360f / segments
                for (i in 0 until segments) {
                    drawArc(
                        color = if (i % 2 == 0) CrimsonRed.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                        startAngle = i * angle,
                        sweepAngle = angle,
                        useCenter = true
                    )
                }
            }
            Text("🌀", fontSize = 48.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (rewardText.isNotEmpty()) {
            Text(rewardText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
        } else {
            Button(
                onClick = {
                    if (!spinning) {
                        spinning = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        rotation += 720f + Random.nextInt(360).toFloat()
                    }
                },
                enabled = !spinning,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text(if (spinning) "SPINNING CHAKRA..." else "LAUNCH WHEEL", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 6. Shinobi Grid Sliding
@Composable
fun SliderCabinet(onComplete: (Int) -> Unit) {
    var cells by remember { mutableStateOf(listOf("🍥", "🦊", "👑", "🗡️", "🔑", "🧢", "🦊", "🔑", "")) }
    var moves by remember { mutableStateOf(0) }
    val targetPattern = remember { listOf("🍥", "🦊", "👑", "🗡️", "🔑", "🧢", "🦊", "🔑", "") }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SHINOBI GRID SLIDER", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Moves: $moves | Aim: Match original ninja patterns!", color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = cells.chunked(3)
        rows.forEachIndexed { rowIdx, rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEachIndexed { colIdx, emoji ->
                    val globalIndex = rowIdx * 3 + colIdx
                    val isEmpty = emoji.isEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isEmpty) Color.Black else Color(0xFF1E1E2C),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isEmpty) Color.Transparent else Color(0xFF2E2E3E), RoundedCornerShape(8.dp))
                            .clickable {
                                if (isEmpty) return@clickable
                                // Check if adjacent to empty cell (index has empty string)
                                val emptyIndex = cells.indexOf("")
                                val isAdjacent = (globalIndex / 3 == emptyIndex / 3 && kotlin.math.abs(globalIndex % 3 - emptyIndex % 3) == 1) ||
                                                (globalIndex % 3 == emptyIndex % 3 && kotlin.math.abs(globalIndex / 3 - emptyIndex / 3) == 1)
                                if (isAdjacent) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val newList = cells.toMutableList()
                                    newList[emptyIndex] = emoji
                                    newList[globalIndex] = ""
                                    cells = newList
                                    moves++
                                    if (cells == targetPattern || moves >= 15) {
                                        onComplete(moves)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 24.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onComplete(moves) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("SUBMIT COMPLETED PATTERN", fontSize = 11.sp)
        }
    }
}

// 7. Endless Runner
@Composable
fun RunnerCabinet(onComplete: (Int) -> Unit) {
    var isJumping by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var obstaclesPassed by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Obstacle animation timeline simulator
    var obstacleOffset by remember { mutableStateOf(1f) } // 1f = far right, 0f = collision/hit
    
    LaunchedEffect(key1 = true) {
        while (obstaclesPassed < 10) {
            delay(16)
            obstacleOffset -= 0.02f
            if (obstacleOffset <= 0f) {
                // Check if colliding (jumping saves user)
                if (!isJumping) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete(score)
                    break
                } else {
                    obstaclesPassed++
                    score += 150
                    obstacleOffset = 1f
                }
            }
        }
        if (obstaclesPassed >= 10) {
            onComplete(score)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    if (!isJumping) {
                        isJumping = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            delay(600) // Jump duration
                            isJumping = false
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SHINOBI ENDLESS RUNNER 🏃", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("TAP SCREEN TO JUMP • Score: $score", color = Color.Gray, fontSize = 11.sp)
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Drawing the runner field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF1E1E2C), RoundedCornerShape(12.dp))
        ) {
            // Runner (represented by emoji)
            val runnerYOffset = if (isJumping) 30.dp else 90.dp
            Text(
                text = "🏃",
                fontSize = 32.sp,
                modifier = Modifier
                    .offset(x = 40.dp, y = runnerYOffset)
            )
            
            // Ground Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White)
                    .align(Alignment.BottomStart)
                    .offset(y = (-10).dp)
            )
            
            // Obstacle (Shuriken) moving left
            val shurikenXOffset = (obstacleOffset * 280).dp
            Text(
                text = "💿",
                fontSize = 24.sp,
                modifier = Modifier
                    .offset(x = shurikenXOffset, y = 94.dp)
            )
        }
    }
}

// 8. Wall Jump Cabinet
@Composable
fun WallJumpCabinet(onComplete: (Int) -> Unit) {
    var onRightWall by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var levelProgress by remember { mutableStateOf(0f) }
    var obstacleIsOnRight by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        while (score < 1000) {
            delay(1000)
            levelProgress += 0.1f
            // Generate obstacle periodically
            obstacleIsOnRight = Random.nextBoolean()
            
            // Check collision
            if (obstacleIsOnRight == onRightWall) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onComplete(score)
                break
            } else {
                score += 100
            }
        }
        if (score >= 1000) {
            onComplete(score)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    onRightWall = !onRightWall
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("NINJA WALL JUMP 🧗", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("TAP TO JUMP WALLS • Score: $score", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFF1E1E2C), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp)
        ) {
            // Left Wall
            Box(modifier = Modifier.width(10.dp).fillMaxHeight().background(Color.White).align(Alignment.CenterStart))
            // Right Wall
            Box(modifier = Modifier.width(10.dp).fillMaxHeight().background(Color.White).align(Alignment.CenterEnd))
            
            // Ninja
            val align = if (onRightWall) Alignment.CenterEnd else Alignment.CenterStart
            val ninjaXOffset = if (onRightWall) (-20).dp else 20.dp
            Text(
                text = "🥷",
                fontSize = 28.sp,
                modifier = Modifier
                    .align(align)
                    .offset(x = ninjaXOffset)
            )
            
            // Obstacle
            val obsAlign = if (obstacleIsOnRight) Alignment.TopEnd else Alignment.TopStart
            val obsXOffset = if (obstacleIsOnRight) (-24).dp else 24.dp
            Text(
                text = "💥",
                fontSize = 24.sp,
                modifier = Modifier
                    .align(obsAlign)
                    .offset(x = obsXOffset, y = 40.dp)
            )
        }
    }
}

// 9. Shuriken Bullseye
@Composable
fun ShurikenThrowCabinet(onComplete: (Int) -> Unit) {
    var indicatorOffset by remember { mutableStateOf(0f) }
    var movingRight by remember { mutableStateOf(true) }
    var score by remember { mutableStateOf(0) }
    var attempts by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        while (attempts < 5) {
            delay(12)
            if (movingRight) {
                indicatorOffset += 0.04f
                if (indicatorOffset >= 1f) {
                    movingRight = false
                }
            } else {
                indicatorOffset -= 0.04f
                if (indicatorOffset <= 0f) {
                    movingRight = true
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SHURIKEN BULLSEYE 🎯", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Hit target in the middle! Attempts: $attempts/5", color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(20.dp))
        
        // Target representation
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.DarkGray, CircleShape)
                .border(8.dp, CrimsonRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(30.dp).background(Color.White, CircleShape))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sliding bar indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Color(0xFF1E1E2C), RoundedCornerShape(10.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
        ) {
            // Target center marker
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(30.dp)
                    .background(Color.Green)
                    .align(Alignment.Center)
            )
            
            // Sliding Pointer
            val alignPos = indicatorOffset * 260
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(Color.White)
                    .offset(x = alignPos.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                attempts++
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Score is higher if close to 0.5f center
                val dev = kotlin.math.abs(indicatorOffset - 0.5f)
                val hitScore = when {
                    dev < 0.1f -> 3
                    dev < 0.25f -> 1
                    else -> 0
                }
                score += hitScore
                if (attempts >= 5) {
                    onComplete(score)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
        ) {
            Text("THROW SHURIKEN!", fontWeight = FontWeight.Bold)
        }
    }
}

// 10. Chakra Speed Run
@Composable
fun TapSpeedCabinet(onComplete: (Int) -> Unit) {
    var taps by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(8) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        onComplete(taps)
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHAKRA RAPID CHARGER ⚡", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Tap rapidly! Time Remaining: ${timeRemaining}s", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF1E1E2C), CircleShape)
                .border(4.dp, CrimsonRed, CircleShape)
                .clickable {
                    taps++
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            contentAlignment = Alignment.Center
        ) {
            Text("⚡", fontSize = 48.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chakra Taps: $taps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// 11. Reaction Aura Test
@Composable
fun ReactionTestCabinet(onComplete: (Int) -> Unit) {
    var state by remember { mutableStateOf("Wait") } // "Wait", "Ready", "Done"
    var startTime by remember { mutableStateOf(0L) }
    var resultTime by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        delay(Random.nextLong(2000, 5000))
        if (state == "Wait") {
            state = "Ready"
            startTime = System.currentTimeMillis()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (state == "Ready") Color(0xFF388E3C) else Color(0xFFD32F2F)
            )
            .clickable {
                if (state == "Wait") {
                    // Tap too early
                    state = "Wait"
                } else if (state == "Ready") {
                    resultTime = System.currentTimeMillis() - startTime
                    state = "Done"
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        delay(1500)
                        onComplete((1000 - resultTime).toInt().coerceIn(10, 1000))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (state) {
                    "Wait" -> "HOLD ENERGY... WAIT FOR GREEN!"
                    "Ready" -> "💥 TAP NOW!"
                    else -> "Reaction: ${resultTime}ms"
                },
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}

// 12. Whack-A-Chibi
@Composable
fun WhackChibiCabinet(onComplete: (Int) -> Unit) {
    var grid by remember { mutableStateOf(MutableList(9) { false }) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(15) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(800)
            val newGrid = MutableList(9) { false }
            newGrid[Random.nextInt(9)] = true
            grid = newGrid
            timeLeft--
        }
        onComplete(score)
    }

    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("WHACK-A-CHIBI 🔨", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Tap active chibis! Time: ${timeLeft}s | Score: $score", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = grid.chunked(3)
        rows.forEachIndexed { rowIdx, rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEachIndexed { colIdx, isActive ->
                    val index = rowIdx * 3 + colIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isActive) Color(0xFFFFB300) else Color(0xFF1E1E2C),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isActive) {
                                    score++
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val nextGrid = grid.toMutableList()
                                    nextGrid[index] = false
                                    grid = nextGrid
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isActive) "🦊" else "🕳️", fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

// 13. Sliding Tile 15 Puzzle (3x3 version for 1-3 mins)
@Composable
fun Puzzle15Cabinet(onComplete: (Int) -> Unit) {
    var board by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 8, 7, 0)) } // almost solved for quick win
    var moves by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SLIDING NUMBERS", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Sort numbers 1 to 8! Moves: $moves", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = board.chunked(3)
        rows.forEachIndexed { rowIdx, rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEachIndexed { colIdx, num ->
                    val globalIndex = rowIdx * 3 + colIdx
                    val isEmpty = num == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isEmpty) Color.Black else Color(0xFF1E1E2C),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isEmpty) Color.Transparent else Color(0xFF2E2E3E), RoundedCornerShape(8.dp))
                            .clickable {
                                if (isEmpty) return@clickable
                                val emptyIdx = board.indexOf(0)
                                val isAdjacent = (globalIndex / 3 == emptyIdx / 3 && kotlin.math.abs(globalIndex % 3 - emptyIdx % 3) == 1) ||
                                                (globalIndex % 3 == emptyIdx % 3 && kotlin.math.abs(globalIndex / 3 - emptyIdx / 3) == 1)
                                if (isAdjacent) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val newBoard = board.toMutableList()
                                    newBoard[emptyIdx] = num
                                    newBoard[globalIndex] = 0
                                    board = newBoard
                                    moves++
                                    if (board == listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)) {
                                        onComplete(moves)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isEmpty) "" else num.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onComplete(moves) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("SUBMIT COMPLETED TILES", fontSize = 11.sp)
        }
    }
}

// 14. Connect Emojis
@Composable
fun ConnectTilesCabinet(onComplete: (Int) -> Unit) {
    val items = remember { listOf("🌸", "🍥", "⚔️", "🦊", "🌸", "🍥", "⚔️", "🦊", "👑", "🛡️", "👑", "🛡️").shuffled() }
    var selectedIndices by remember { mutableStateOf(listOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(setOf<Int>()) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CONNECT ANIME TILES", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Match pairs! Unlocked: ${matchedIndices.size / 2}/6", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = items.chunked(3)
        rows.forEachIndexed { rowIdx, rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEachIndexed { colIdx, emoji ->
                    val index = rowIdx * 3 + colIdx
                    val isMatched = matchedIndices.contains(index)
                    val isSelected = selectedIndices.contains(index)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = when {
                                    isMatched -> Color.Black
                                    isSelected -> CrimsonRed
                                    else -> Color(0xFF1E1E2C)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isMatched || isSelected) return@clickable
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val nextSelected = selectedIndices.toMutableList()
                                nextSelected.add(index)
                                selectedIndices = nextSelected
                                
                                if (selectedIndices.size == 2) {
                                    val first = selectedIndices[0]
                                    val second = selectedIndices[1]
                                    if (items[first] == items[second]) {
                                        val nextMatched = matchedIndices.toMutableSet()
                                        nextMatched.add(first)
                                        nextMatched.add(second)
                                        matchedIndices = nextMatched
                                        if (matchedIndices.size == items.size) {
                                            onComplete(300)
                                        }
                                    }
                                    selectedIndices = listOf()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isMatched) "" else emoji, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

// 15. Otaku Power 2048
@Composable
fun Otaku2048Cabinet(onComplete: (Int) -> Unit) {
    var board by remember { mutableStateOf(MutableList(16) { if (Random.nextInt(10) < 2) 2 else 0 }) }
    var score by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    fun shift() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        // Simulated combined swipe shift for quick win
        val index = board.indexOfFirst { it > 0 }
        if (index != -1) {
            val nextVal = board[index] * 2
            board[index] = 0
            val empty = board.indexOf(0)
            if (empty != -1) {
                board[empty] = nextVal
                score += nextVal
            }
        }
        if (score >= 512) {
            onComplete(score)
        }
    }

    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHAKRA 2048 LITE", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Merge matching power numbers! Score: $score", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = board.chunked(4)
        rows.forEach { rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowList.forEach { valNum ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (valNum == 0) Color(0xFF1E1E2C) else CrimsonRed,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (valNum == 0) "" else valNum.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { shift() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("SWIPE CHAKRA COMBINE", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// 16. Chakra Color Stroop
@Composable
fun ColorMatchCabinet(onComplete: (Int) -> Unit) {
    val wordList = remember { listOf("Crimson", "Emerald", "Gold", "Azure") }
    val colorList = remember { listOf(CrimsonRed, Color(0xFF2E7D32), Color(0xFFFFD600), Color(0xFF29B6F6)) }
    
    var curWordIdx by remember { mutableStateOf(Random.nextInt(4)) }
    var curColorIdx by remember { mutableStateOf(Random.nextInt(4)) }
    var score by remember { mutableStateOf(0) }
    var questionsPlayed by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    fun answer(match: Boolean) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val isMatch = curWordIdx == curColorIdx
        if (isMatch == match) {
            score++
        }
        questionsPlayed++
        if (questionsPlayed >= 5) {
            onComplete(score)
        } else {
            curWordIdx = Random.nextInt(4)
            curColorIdx = Random.nextInt(4)
        }
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHAKRA COLOR STROOP", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Does text meaning match font color? Score: $score/5", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF1E1E2C), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = wordList[curWordIdx],
                color = colorList[curColorIdx],
                fontWeight = FontWeight.Black,
                fontSize = 32.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { answer(false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier.weight(1f)
            ) {
                Text("NO")
            }
            Button(
                onClick = { answer(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                modifier = Modifier.weight(1f)
            ) {
                Text("YES")
            }
        }
    }
}

// 17. Pixel Leaf Painter
@Composable
fun PixelArtCabinet(onComplete: (Int) -> Unit) {
    var pixels by remember { mutableStateOf(MutableList(16) { false }) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("PIXEL LEAF PAINTER", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Color cells to craft sharingan patterns!", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        val rows = pixels.chunked(4)
        rows.forEachIndexed { rIdx, rowList ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEachIndexed { cIdx, isColored ->
                    val index = rIdx * 4 + cIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isColored) CrimsonRed else Color(0xFF1E1E2C),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val nextPixels = pixels.toMutableList()
                                nextPixels[index] = !pixels[index]
                                pixels = nextPixels
                            }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onComplete(200) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("FINISH ARTWORK", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// 18. Emoji Decoder
@Composable
fun EmojiDecoderCabinet(onComplete: (Int) -> Unit) {
    val prompts = remember {
        listOf(
            Triple("📺 Series: 🦊🍃🍥🍜", listOf("Bleach", "Demon Slayer", "Naruto", "One Piece"), 2),
            Triple("📺 Series: 👒🌊🏴‍☠️🗡️", listOf("One Piece", "Naruto", "Dragon Ball", "Death Note"), 0),
            Triple("📺 Series: 🗡️👺👹❄️", listOf("Naruto", "Demon Slayer", "InuYasha", "Gintama"), 1),
            Triple("📺 Series: 🛡️🗡️🐉👾", listOf("Solo Leveling", "Sword Art Online", "Shield Hero", "Goblin Slayer"), 1),
            Triple("📺 Series: 🪐💥🐒🐉", listOf("Dragon Ball", "Sailor Moon", "Naruto", "My Hero Academia"), 0)
        )
    }
    var idx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("EMOJI SHINOBI DECODER", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        val current = prompts[idx]
        Text(current.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        current.second.forEachIndexed { i, opt ->
            val color = when {
                selected == null -> Color(0xFF1E1E2C)
                i == current.third -> Color(0xFF388E3C)
                selected == i -> Color(0xFFD32F2F)
                else -> Color(0xFF1E1E2C)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (selected == null) {
                            selected = i
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (i == current.third) score++
                        }
                    }
            ) {
                Text(opt, color = Color.White, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }
        
        if (selected != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (idx < 4) {
                        idx++
                        selected = null
                    } else {
                        onComplete(score)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("NEXT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 19. Daily Boss Raid
@Composable
fun BossRaidCabinet(onComplete: (Int) -> Unit) {
    var bossHp by remember { mutableStateOf(100) }
    var timer by remember { mutableStateOf(10) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        while (timer > 0 && bossHp > 0) {
            delay(1000)
            timer--
        }
        onComplete(100 - bossHp)
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("DAILY BOSS RAID: DEMON FOX 👹", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Tap boss as fast as possible! Timer: ${timer}s", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // HP Progress Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("HP: $bossHp%", color = Color.White, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(bossHp / 100f)
                        .background(CrimsonRed, RoundedCornerShape(4.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(Color(0xFF1E1E2C), CircleShape)
                .border(4.dp, CrimsonRed, CircleShape)
                .clickable {
                    if (bossHp > 0) {
                        bossHp = (bossHp - 5).coerceAtLeast(0)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (bossHp <= 0) {
                            onComplete(100)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("👹", fontSize = 48.sp)
        }
    }
}

// 20. Rescue Falling Chibi
@Composable
fun RescueChibiCabinet(onComplete: (Int) -> Unit) {
    var chibiPos by remember { mutableStateOf(0.5f) } // 0f left, 1f right
    var score by remember { mutableStateOf(0) }
    var obstaclesCaught by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        while (obstaclesCaught < 6) {
            delay(1500)
            obstaclesCaught++
            // Simulating target position catching
            val obstaclePos = Random.nextFloat()
            if (kotlin.math.abs(chibiPos - obstaclePos) < 0.35f) {
                score += 150
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
        onComplete(score)
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("RESCUE FALLING CHIBI", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Slide bottom basket to catch items! Score: $score", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(0xFF1E1E2C), RoundedCornerShape(12.dp))
        ) {
            // Target catcher at the bottom
            Slider(
                value = chibiPos,
                onValueChange = { chibiPos = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
            )
            
            // Falling Coin representation
            Text(
                text = "🌸",
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 20.dp)
            )
        }
    }
}
