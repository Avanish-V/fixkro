package com.iotabuild.zippyhand.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.iotabuild.zippyhand.feature.address.presentation.UserAddressesScreen
import com.iotabuild.zippyhand.feature.booking.presentation.BookingDetailScreen
import com.iotabuild.zippyhand.feature.booking.presentation.BookingSummaryScreen
import com.iotabuild.zippyhand.feature.booking.presentation.BookingsScreen
import com.iotabuild.zippyhand.feature.home.presentation.HomeScreen
import com.iotabuild.zippyhand.feature.offers.presentation.OffersScreen
import com.iotabuild.zippyhand.feature.profile.presentation.CreateProfileScreen
import com.iotabuild.zippyhand.feature.profile.presentation.LoginScreen
import com.iotabuild.zippyhand.feature.profile.presentation.ProfileDetailsScreen
import com.iotabuild.zippyhand.feature.profile.presentation.ProfileScreen
import com.iotabuild.zippyhand.feature.reviews.presentation.WriteReviewScreen
import com.iotabuild.zippyhand.feature.servicecategory.presentation.ServiceListScreen
import com.iotabuild.zippyhand.feature.servicedetails.presentation.ServiceDetailScreen
import com.iotabuild.zippyhand.feature.support.presentation.AboutUsScreen
import com.iotabuild.zippyhand.feature.support.presentation.PrivacyPolicyScreen
import com.iotabuild.zippyhand.feature.support.presentation.TermsScreen
import com.iotabuild.zippyhand.location.presentation.LocationGateScreen

/**
 * Safe navigation extension preventing double taps.
 */
fun NavController.safeNavigate(route: Route, builder: NavOptionsBuilder.() -> Unit = {}) {
    val currentRoute = currentBackStackEntry?.destination
    if (currentRoute?.hasRoute(route::class) == true) return
    navigate(route, builder)
}

/**
 * Bottom tab navigation helper with state save/restore.
 */
fun NavController.navigateToTab(route: Route) {
    safeNavigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Feature: Home Destination
 */
fun NavGraphBuilder.homeDestination(
    onServiceClick: (Int) -> Unit,
    onLocationClick: () -> Unit
) {
    composable<Route.Home>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        HomeScreen(
            onServiceClick = onServiceClick,
            onLocationClick = onLocationClick
        )
    }
}

/**
 * Feature: Auth Graph
 */
fun NavGraphBuilder.authGraph(
    onCreateProfileSuccess: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    composable<Route.Login>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        LoginScreen(
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick
        )
    }
    composable<Route.CreateProfile>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        CreateProfileScreen(
            onProfileCreated = onCreateProfileSuccess
        )
    }
}

/**
 * Feature: Onboarding Graph
 */
fun NavGraphBuilder.onboardingGraph(
    onLocationGranted: () -> Unit
) {
    composable<Route.LocationGate>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        LocationGateScreen(
            onLocationGranted = onLocationGranted,
            onManualSelectCity = {
                onLocationGranted()
            }
        )
    }
}

/**
 * Feature: Services & Details Graph
 */
fun NavGraphBuilder.serviceGraph(
    onServiceClick: (Int) -> Unit,
    onBookNowClick: (Int) -> Unit,
    onProceedToPayment: (String, Int, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    composable<Route.ServiceList>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Route.ServiceList>()
        ServiceListScreen(
            categoryName = route.categoryName,
            onServiceClick = { idString -> onServiceClick(idString.toIntOrNull() ?: 0) },
            onBackClick = onBackClick
        )
    }

    composable<Route.ServiceDetail>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Route.ServiceDetail>()
        ServiceDetailScreen(
            productId = route.productId,
            onBackClick = onBackClick,
            onBookClick = { onBookNowClick(route.productId) }
        )
    }

    composable<Route.BookingSummary>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Route.BookingSummary>()
        BookingSummaryScreen(
            productId = route.productId,
            onBackClick = onBackClick,
            onProceedToPayment = onProceedToPayment
        )
    }
}

/**
 * Feature: Bookings Graph
 */
fun NavGraphBuilder.bookingGraph(
    onBookingClick: (Long) -> Unit,
    onWriteReviewClick: (Route.WriteReview) -> Unit,
    onBackClick: () -> Unit
) {
    composable<Route.Bookings>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        BookingsScreen(
            onBookingClick = onBookingClick
        )
    }

    composable<Route.BookingDetail>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Route.BookingDetail>()
        BookingDetailScreen(
            orderId = route.orderId,
            onBackClick = onBackClick,
            onWriteReviewClick = onWriteReviewClick
        )
    }

    composable<Route.WriteReview>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Route.WriteReview>()
        WriteReviewScreen(
            orderId = route.orderId,
            productId = route.productId,
            customerName = "User",
            onBackClick = onBackClick,
            onSubmitClick = onBackClick
        )
    }
}

/**
 * Feature: Offers Destination
 */
fun NavGraphBuilder.offersDestination() {
    composable<Route.Offers>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        OffersScreen()
    }
}

/**
 * Feature: Profile & Support Graph
 */
fun NavGraphBuilder.profileGraph(
    onProfileDetailsClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onChangeCityClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    composable<Route.Profile>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        ProfileScreen(
            onProfileDetailsClick = onProfileDetailsClick,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
            onAboutUsClick = onAboutUsClick,
            onAddressesClick = onAddressesClick,
            onChangeCityClick = onChangeCityClick,
            onLogoutClick = onLogoutClick,
            onDeleteAccount = onDeleteAccount
        )
    }

    composable<Route.ProfileDetails>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        ProfileDetailsScreen(
            onBackClick = onBackClick
        )
    }

    composable<Route.UserAddresses>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        UserAddressesScreen(
            onBackClick = onBackClick
        )
    }

    composable<Route.Terms>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        TermsScreen(
            onBackClick = onBackClick
        )
    }

    composable<Route.Privacy>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        PrivacyPolicyScreen(
            onBackClick = onBackClick
        )
    }

    composable<Route.AboutUs>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        AboutUsScreen(
            onBackClick = onBackClick
        )
    }
}
