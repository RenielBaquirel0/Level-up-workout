package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.QuestsAchievementsScreen
import com.example.ui.screens.WorkoutTrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonAqua
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigatorShell()
            }
        }
    }
}

// Global visual stages
enum class AppScreenState {
    SPLASH,
    AUTH,
    MAIN_WORKSPACE
}

@Composable
fun AppNavigatorShell() {
    val viewModel: WorkoutViewModel = viewModel()
    var appState by remember { mutableStateOf(AppScreenState.SPLASH) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Workouts, 2 = Analytics, 3 = Rewards/Quests, 4 = Profile

    val levelUpEvent by viewModel.levelUpEvent.collectAsState()

    // --- SCREEN ORCHESTRATION ---
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        when (appState) {
            AppScreenState.SPLASH -> {
                SplashScreenLoader(onFinish = {
                    appState = AppScreenState.AUTH
                })
            }
            AppScreenState.AUTH -> {
                AuthenticationGateway(onSuccess = {
                    appState = AppScreenState.MAIN_WORKSPACE
                })
            }
            AppScreenState.MAIN_WORKSPACE -> {
                Scaffold(
                    bottomBar = {
                        WorkspaceBottomNavigationBar(
                            selectedTab = activeTab,
                            onTabSelected = { activeTab = it }
                        )
                    },
                    containerColor = Color(0xFF0D0D0D)
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (activeTab) {
                            0 -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { activeTab = it }
                            )
                            1 -> WorkoutTrackerScreen(
                                viewModel = viewModel
                            )
                            2 -> AnalyticsScreen(
                                viewModel = viewModel
                            )
                            3 -> QuestsAchievementsScreen(
                                viewModel = viewModel
                            )
                            4 -> ProfileSettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // --- GLOBAL OVERLAYS: THE RPG CINEMATIC LEVEL UP CELEBRATION ---
        levelUpEvent?.let { lvlPair ->
            CinematicLevelUpCelebration(
                oldLevel = lvlPair.first,
                newLevel = lvlPair.second,
                onDismiss = { viewModel.closeLevelUpCinematic() }
            )
        }
    }
}

// ------ 1. SPLASH SCREEN COMPOSABLE ------
@Composable
fun SplashScreenLoader(onFinish: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing)
    )
    val opacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200) // 2.2-seconds tactical delay
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .clickable { onFinish() }, // skip trigger
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.rotate(scale * 2f - 2f)
        ) {
            // Stylized holographic rings
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NeonGreen.copy(0.1f), Color.Transparent)
                        )
                    )
                    .border(2.dp, NeonGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LEVELUP WORKOUT",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                modifier = Modifier.shadow(4.dp, shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "RPG STYLE FITNESS PROTOCOL",
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Pulse Loading Bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242428))
            ) {
                var loadingComplete by remember { mutableStateOf(false) }
                val loadingProgress by animateFloatAsState(
                    targetValue = if (loadingComplete) 1f else 0f,
                    animationSpec = tween(1800, easing = FastOutSlowInEasing)
                )
                LaunchedEffect(Unit) {
                    loadingComplete = true
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loadingProgress)
                        .background(NeonAqua)
                )
            }
        }
    }
}

// ------ 2. AUTHENTICATION GATEWAY ------
@Composable
fun AuthenticationGateway(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "IDENTITY GATEWAY",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Establish communication with characters record database",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("SLAYER DISPATCH EMAIL", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color(0xFF2C2C32)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("DECRYPTION ACCESS CODES", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color(0xFF2C2C32)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Standard signup/login bypass simulation
            Button(
                onClick = onSuccess,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_button")
            ) {
                Text(text = "INITIALIZE LOCAL SECURE WORKSPACE", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuccess() }
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔑 ", fontSize = 16.sp)
                Text(text = "LINK OVER GOOGLE SIGN-IN CLOUD", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Enables automatic secure cloud sync with Firestore remote nodes.",
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ------ 3. MAIN WORKSPACE TAB STRIP BAR ------
@Composable
fun WorkspaceBottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF161618),
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val tabIcons = listOf(
            Icons.Default.Home to "Home",
            Icons.Default.SportsGymnastics to "Workouts",
            Icons.Default.ShowChart to "Analytics",
            Icons.Default.LocalFireDepartment to "Rewards",
            Icons.Default.Person to "Profile"
        )

        tabIcons.forEachIndexed { index, pair ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = pair.first,
                        contentDescription = pair.second,
                        tint = if (isSelected) NeonGreen else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = pair.second,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) NeonGreen else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF242428)
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

// ------ 4. THE RPG CINEMATIC LEVEL UP CELEBRATION ------
@Composable
fun CinematicLevelUpCelebration(
    oldLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF20D0D0D)), // Dark background translucent overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Rotating halo stars
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242428))
                    .border(2.dp, NeonGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👑", fontSize = 68.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SLAYER LEVEL UP!",
                color = NeonGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "LEVEL $oldLevel",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "to",
                    tint = NeonGreen
                )
                Text(
                    text = "LEVEL $newLevel",
                    color = NeonAqua,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF161618))
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CHARACTER ATTRIBUTES EXPANSIONS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AttributeLevelUpRow(attribute = "⚙️ PHYSICAL STRENGTH", bonus = "+0.8")
                    AttributeLevelUpRow(attribute = "⚡ CARDIO DURABILITY", bonus = "+0.8")
                    AttributeLevelUpRow(attribute = "🔮 IRON DISCIPLINE", bonus = "+0.4")
                    AttributeLevelUpRow(attribute = "🔥 CONSISTENT WILL", bonus = "+0.4")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_level_up_cinematic")
            ) {
                Text(text = "CONTINUE THE CAMPAIGN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun AttributeLevelUpRow(attribute: String, bonus: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = attribute, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = bonus, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
