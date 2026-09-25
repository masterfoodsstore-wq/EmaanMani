package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVATAR_OPTIONS

private val GoldLight = Color(0xFFFFE082)
private val GoldCore = Color(0xFFFFD54F)
private val GoldDark = Color(0xFFC49000)
private val EmeraldDark = Color(0xFF04140C)
private val EmeraldCard = Color(0xEE092015)

@Composable
fun UserAuthScreen(
    onLogin: (String, String, Boolean) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onForgotPassword: (String, String) -> Unit,
    onQuickDemoLogin: ((Boolean) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var isLoginTab by remember { mutableStateOf(false) }
    var showForgotPasswordModal by remember { mutableStateOf(false) }

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register Form State
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(AVATAR_OPTIONS.first()) }
    var showRegPassword by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF0D3B25),
                        Color(0xFF061A10),
                        EmeraldDark
                    ),
                    radius = 1800f
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onNavigateBack != null) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
                    .border(1.dp, GoldCore.copy(alpha = 0.5f), CircleShape)
                    .testTag("auth_back_to_game_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Game",
                    tint = GoldCore
                )
            }
        }

        // Landscape Card Container
        Row(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(EmeraldCard)
                .border(2.dp, GoldCore, RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Panel: Game Branding & Quick Login
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🦁 🦚 🦈",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROYALX PKR",
                        color = GoldCore,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "VIP ARCADE & ENTERTAINMENT",
                        color = Color(0xFFA5D6A7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Play Dragon Tiger, 3D Zoo Roulette & Cyber Slots with free bonus entertainment points.",
                        color = Color(0xFFC8E6C9),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // VIP Platform Highlights
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF092817))
                        .border(1.dp, GoldCore.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MEMBER BENEFITS",
                        color = GoldLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "🎁 Instant 20 RS Welcome Bonus",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "⚡ Real-Time Cloud Sync & Wallet",
                        color = Color(0xFFA5D6A7),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "🛡️ Secure & Verified Gaming",
                        color = Color(0xFFA5D6A7),
                        fontSize = 10.sp
                    )
                }
            }

            // Vertical Gold Divider
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, GoldCore, Color.Transparent)
                        )
                    )
            )

            // Right Panel: Auth Form (Login / Register Tabs)
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF06170F))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (isLoginTab) Modifier.background(Brush.horizontalGradient(listOf(GoldCore, GoldDark)))
                                else Modifier.background(Color.Transparent)
                            )
                            .clickable { isLoginTab = true; validationError = null }
                            .padding(vertical = 6.dp)
                            .testTag("tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOGIN",
                            color = if (isLoginTab) Color(0xFF1E1000) else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (!isLoginTab) Modifier.background(Brush.horizontalGradient(listOf(GoldCore, GoldDark)))
                                else Modifier.background(Color.Transparent)
                            )
                            .clickable { isLoginTab = false; validationError = null }
                            .padding(vertical = 6.dp)
                            .testTag("tab_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CREATE ACCOUNT",
                            color = if (!isLoginTab) Color(0xFF1E1000) else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Error message display
                val activeErr = validationError ?: errorMessage
                if (!activeErr.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x44D32F2F))
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = activeErr,
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isLoginTab) {
                    // LOGIN FORM
                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = { loginIdentifier = it },
                        label = { Text("Username or Email", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldCore, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldCore,
                            unfocusedBorderColor = Color(0xFF2E7D32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = GoldCore,
                            unfocusedLabelColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_login_username")
                    )

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldCore, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                Icon(
                                    imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldCore,
                            unfocusedBorderColor = Color(0xFF2E7D32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = GoldCore,
                            unfocusedLabelColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_login_password")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = GoldCore, checkmarkColor = Color.Black)
                            )
                            Text("Keep me logged in", color = Color.White, fontSize = 10.sp)
                        }
                        Text(
                            text = "Forgot password?",
                            color = GoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showForgotPasswordModal = true }
                                .padding(4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(listOf(GoldCore, GoldDark))
                            )
                            .clickable {
                                if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                                    validationError = "Please enter both username and password"
                                } else {
                                    validationError = null
                                    onLogin(loginIdentifier, loginPassword, rememberMe)
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("btn_submit_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOGIN TO GAME",
                            color = Color(0xFF241400),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // REGISTER FORM
                    // Welcome Bonus Promotion Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0F3822), Color(0xFF062314))))
                            .border(1.dp, GoldCore.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎁", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "NEW ACCOUNT BONUS: 20 RS",
                                    color = GoldCore,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Every new account receives 20 RS bonus instantly!",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        label = { Text("Username (min 3 chars)", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldCore, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldCore,
                            unfocusedBorderColor = Color(0xFF2E7D32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_username")
                    )

                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Email Address", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldCore, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldCore,
                            unfocusedBorderColor = Color(0xFF2E7D32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_email")
                    )

                    // Avatar Picker
                    Text(
                        text = "Choose Your Avatar:",
                        color = GoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AVATAR_OPTIONS.take(6).forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatar == av) GoldCore else Color(0xFF0F321E))
                                    .border(
                                        1.dp,
                                        if (selectedAvatar == av) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedAvatar = av },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = av, fontSize = 16.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Password (min 6 chars)", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldCore, modifier = Modifier.size(16.dp)) },
                        visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldCore,
                            unfocusedBorderColor = Color(0xFF2E7D32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_password")
                    )

                    OutlinedTextField(
                        value = regConfirmPassword,
                        onValueChange = { regConfirmPassword = it },
                        label = { Text("Confirm Password", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldCore, modifier = Modifier.size(16.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldCore,
                            unfocusedBorderColor = Color(0xFF2E7D32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_confirm_password")
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(listOf(GoldCore, GoldDark))
                            )
                            .clickable {
                                if (regUsername.trim().length < 3) {
                                    validationError = "Username must be at least 3 characters"
                                } else if (!regEmail.contains("@")) {
                                    validationError = "Please enter a valid email"
                                } else if (regPassword.length < 6) {
                                    validationError = "Password must be at least 6 characters"
                                } else if (regPassword != regConfirmPassword) {
                                    validationError = "Passwords do not match"
                                } else {
                                    validationError = null
                                    onRegister(regUsername.trim(), regEmail.trim(), regPassword, selectedAvatar)
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("btn_submit_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLAIM 20 RS & CREATE ACCOUNT",
                            color = Color(0xFF241400),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Forgot Password Dialog
        if (showForgotPasswordModal) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordModal = false },
                onReset = { email, newPw ->
                    onForgotPassword(email, newPw)
                    showForgotPasswordModal = false
                }
            )
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onReset: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF061A10))
                .border(2.dp, GoldCore, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PASSWORD RESET",
                color = GoldCore,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Enter your registered email and choose a new secure password.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (err != null) {
                Text(text = err!!, color = Color(0xFFFF8A80), fontSize = 10.sp)
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password (min 6 chars)", fontSize = 10.sp) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF263238))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = Color.White, fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldCore)
                        .clickable {
                            if (email.isBlank() || newPassword.length < 6) {
                                err = "Please enter valid email and 6+ char password"
                            } else {
                                onReset(email, newPassword)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Reset Password", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
