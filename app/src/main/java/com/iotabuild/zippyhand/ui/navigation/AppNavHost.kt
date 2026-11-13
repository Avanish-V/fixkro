package com.iotabuild.zippyhand.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.iotabuild.zippyhand.feature.payments.presentation.CashFreePayment
import com.iotabuild.zippyhand.feature.profile.presentation.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Route = Route.Home,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        homeDestination(
            onServiceClick = { productId ->
                navController.safeNavigate(Route.ServiceDetail(productId))
            },
            onLocationClick = {
                navController.safeNavigate(Route.LocationGate)
            }
        )

        authGraph(
            onCreateProfileSuccess = {
                navController.safeNavigate(Route.Home) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            },
            onTermsClick = {
                navController.safeNavigate(Route.Terms)
            },
            onPrivacyClick = {
                navController.safeNavigate(Route.Privacy)
            }
        )

        onboardingGraph(
            onLocationGranted = {
                navController.safeNavigate(Route.Home) {
                    popUpTo(Route.LocationGate) { inclusive = true }
                }
            }
        )

        serviceGraph(
            onServiceClick = { productId ->
                navController.safeNavigate(Route.ServiceDetail(productId))
            },
            onBookNowClick = { productId ->
                navController.safeNavigate(Route.BookingSummary(productId))
            },
            onProceedToPayment = { orderId, amount, name, email, phone ->
                val intent = Intent(context, CashFreePayment::class.java).apply {
                    putExtra("ORDER_ID", orderId)
                    putExtra("ORDER_AMOUNT", amount)
                    putExtra("CUSTOMER_NAME", name)
                    putExtra("CUSTOMER_EMAIL", email)
                    putExtra("CUSTOMER_PHONE", phone)
                }
                context.startActivity(intent)
            },
            onBackClick = {
                navController.navigateUp()
            }
        )

        bookingGraph(
            onBookingClick = { orderId ->
                navController.safeNavigate(Route.BookingDetail(orderId))
            },
            onWriteReviewClick = { route ->
                navController.safeNavigate(route)
            },
            onBackClick = {
                navController.navigateUp()
            }
        )

        offersDestination()

        profileGraph(
            onProfileDetailsClick = {
                navController.safeNavigate(Route.ProfileDetails)
            },
            onAddressesClick = {
                navController.safeNavigate(Route.UserAddresses)
            },
            onChangeCityClick = {
                navController.safeNavigate(Route.LocationGate)
            },
            onLogoutClick = {
                authViewModel.logout()
                navController.safeNavigate(Route.Login) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDeleteAccount = {
                authViewModel.logout()
                navController.safeNavigate(Route.Login) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onTermsClick = {
                navController.safeNavigate(Route.Terms)
            },
            onPrivacyClick = {
                navController.safeNavigate(Route.Privacy)
            },
            onAboutUsClick = {
                navController.safeNavigate(Route.AboutUs)
            },
            onBackClick = {
                navController.navigateUp()
            }
        )
    }
}
