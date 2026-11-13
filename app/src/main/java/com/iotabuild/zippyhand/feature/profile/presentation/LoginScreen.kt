package com.iotabuild.zippyhand.feature.profile.presentation

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.iotabuild.zippyhand.AppSatartUp.AppStartUpViewModel
import com.iotabuild.zippyhand.R
import com.iotabuild.zippyhand.feature.profile.domain.AuthResult
import com.iotabuild.zippyhand.ui.navigation.Route
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
    startUpViewModel: AppStartUpViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F7FF),
                        Color.White
                    )
                )
            )
    ) {
        DecorativeCircles()

        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(modifier = Modifier.weight(0.6f),visible = visible, enter = fadeIn(animationSpec = tween(1000, 400))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CollageImage(
                            url = R.drawable.banner_1,
                            modifier = Modifier.weight(1f)
                        )
                        CollageImage(
                            url = R.drawable.banner_2,
                            modifier = Modifier.weight(1f),
                            label = "Appliance\nRepair"
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CollageImage(
                            url = R.drawable.banner_3,
                            modifier = Modifier.weight(0.45f)
                        )
                        CollageImage(
                            url = R.drawable.banner_4,
                            modifier = Modifier.weight(0.55f),
                            label = "Expert\nTechnicians"
                        )
                    }
                }
            }

            AnimatedVisibility(modifier = Modifier.weight(0.4f),visible = visible, enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(animationSpec = tween(800, 600))) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.fixkro_logo_2),
                            contentDescription = ""
                        )
                    }
                    
                    Text(
                        text = "Everything Fixed.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Surface(
                        onClick = {
                            viewModel.launchGoogleSignIn(context = context )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when(authState){
                                is AuthResult.IDLE -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_google),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Unspecified
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            "Continue with Google",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3C4043)
                                        )
                                    }
                                }
                                is AuthResult.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
                                    )
                                }
                                is AuthResult.Success -> {
                                    LaunchedEffect(authState) {
                                        startUpViewModel.checkStartUp()
                                    }
                                }
                                is AuthResult.Error -> {

                                }

                                null -> {}
                            }

                        }
                    }

                    if (authState is AuthResult.Error) {
                        Text(
                            text = (authState as AuthResult.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            val footerText = buildAnnotatedString {
                append("By joining, you agree to our ")
                pushStringAnnotation(tag = "TERMS", annotation = "terms")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Terms")
                }
                pop()
                append(" & ")
                pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Privacy Policy")
                }
                pop()
            }

            androidx.compose.foundation.text.ClickableText(
                text = footerText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                ),
                onClick = { offset ->
                    footerText.getStringAnnotations(tag = "TERMS", start = offset, end = offset).firstOrNull()?.let {
                        onTermsClick()
                    }
                    footerText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset).firstOrNull()?.let {
                        onPrivacyClick()
                    }
                },
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )
        }
    }
}

@Composable
fun CollageImage(url: Int, modifier: Modifier = Modifier, label: String? = null) {
    Box(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))) {
        Image(
            painter = painterResource(url),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (label != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(bottom = 12.dp, top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                 Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun DecorativeCircles() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFD4E8FF).copy(alpha = 0.4f),
                radius = 150.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = 0f, y = 100.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFFD4E8).copy(alpha = 0.2f),
                radius = 100.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = screenWidth.toPx(), y = 300.dp.toPx())
            )
        }
    }
}
