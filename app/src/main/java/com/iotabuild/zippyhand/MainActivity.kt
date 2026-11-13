package com.iotabuild.zippyhand

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.iotabuild.zippyhand.AppSatartUp.AppStartUpViewModel
import com.iotabuild.zippyhand.feature.profile.domain.StartUpState
import com.iotabuild.zippyhand.ui.common.FixkroErrorScreen
import com.iotabuild.zippyhand.ui.common.ZippyLoadingScreen
import com.iotabuild.zippyhand.ui.navigation.AppNavHost
import com.iotabuild.zippyhand.ui.navigation.Route
import com.iotabuild.zippyhand.ui.navigation.navigateToTab
import com.iotabuild.zippyhand.ui.theme.BorderColor
import com.iotabuild.zippyhand.ui.theme.LightText
import com.iotabuild.zippyhand.ui.theme.MainBlue
import com.iotabuild.zippyhand.ui.theme.ZippyhandTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val pendingRouteState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        pendingRouteState.value = intent?.getStringExtra("NAVIGATE_TO")
        enableEdgeToEdge()
        setContent {
            ZippyhandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val appStartUpViewModel: AppStartUpViewModel = koinViewModel()
                    val startUpState = appStartUpViewModel.state.collectAsStateWithLifecycle()

                    when (val state = startUpState.value) {
                        is StartUpState.Loading -> {
                            ZippyLoadingScreen()
                        }
                        is StartUpState.LoginRequired -> {
                            ZippyhandMainScreen(
                                navController = navController,
                                startDestination = Route.Login,
                                pendingRoute = pendingRouteState.value,
                                onPendingRouteHandled = { pendingRouteState.value = null }
                            )
                        }
                        is StartUpState.LocationGate -> {
                            ZippyhandMainScreen(
                                navController = navController,
                                startDestination = Route.LocationGate,
                                pendingRoute = pendingRouteState.value,
                                onPendingRouteHandled = { pendingRouteState.value = null }
                            )
                        }
                        is StartUpState.Home -> {
                            ZippyhandMainScreen(
                                navController = navController,
                                startDestination = Route.Home,
                                pendingRoute = pendingRouteState.value,
                                onPendingRouteHandled = { pendingRouteState.value = null }
                            )
                        }
                        is StartUpState.Error -> {
                            FixkroErrorScreen(
                                message = state.message,
                                onRetry = {
                                    appStartUpViewModel.checkStartUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val navigateTo = intent.getStringExtra("NAVIGATE_TO")
        if (navigateTo != null) {
            pendingRouteState.value = navigateTo
        }
    }
}

enum class AppDestinations(
    val label: String,
    val iconNormal: Int,
    val iconBold: Int,
    val route: Route
) {
    HOME("Appliance", R.drawable.house_crack_bold, R.drawable.house_crack, Route.Home),
    BOOKINGS("Bookings", R.drawable.calendar_clock__bold, R.drawable.calendar_clock, Route.Bookings),
    PROFILE("Profile", R.drawable.user, R.drawable.user_bold, Route.Profile),
}

@Composable
fun ZippyhandMainScreen(
    navController: NavHostController,
    startDestination: Route = Route.Home,
    pendingRoute: String? = null,
    onPendingRouteHandled: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(pendingRoute) {
        if (pendingRoute == "BOOKINGS") {
            navController.navigateToTab(Route.Bookings)
            onPendingRouteHandled()
        }
    }

    val showNavigationSuite = AppDestinations.entries.any {
        currentDestination?.hasRoute(it.route::class) == true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showNavigationSuite,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {

            SlickTabBar(
                currentDestination = currentDestination,
                onNavigate = { route ->
                    navController.navigateToTab(route)
                }
            )
        }
    }
}

@Composable
fun SlickTabBar(
    currentDestination: NavDestination?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 0.5.dp, color = BorderColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AppDestinations.entries.forEach { item ->
                val isSelected = currentDestination?.hasRoute(item.route::class) == true

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) MainBlue else LightText,
                    animationSpec = tween(durationMillis = 200),
                    label = "iconColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MainBlue else LightText,
                    animationSpec = tween(durationMillis = 200),
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(if (isSelected) item.iconBold else item.iconNormal),
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
