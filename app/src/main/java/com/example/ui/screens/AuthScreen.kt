package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppView
import com.example.viewmodel.PaymentUiState
import com.example.viewmodel.PaymentViewModel

@Composable
fun AuthScreen(
    uiState: PaymentUiState,
    viewModel: PaymentViewModel,
    isRegister: Boolean
) {
    // 0 = Member Login, 1 = Member Register, 2 = Admin Portal
    var authMode by remember(isRegister) { mutableStateOf(if (isRegister) 1 else 0) }
    var showAdminPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App / Shield Logo
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    if (authMode == 2) Color(0xFFE11D48) else Color(0xFF6366F1)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (authMode == 2) Icons.Default.AdminPanelSettings else Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = when (authMode) {
                0 -> "Welcome Back"
                1 -> "Create Account"
                else -> "Super Admin Portal"
            },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = when (authMode) {
                0 -> "Sign in to access your casino wallet"
                1 -> "WhatsApp OTP verified instant registration"
                else -> "Merchant settlement, bank reconciliation & ledger control"
            },
            color = Color(0xFF94A3B8),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Three-way Mode Switcher (Member Login | Register | Admin Setup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                Triple(0, "Member Login", Icons.Default.Person),
                Triple(1, "Register", Icons.Default.AppRegistration),
                Triple(2, "Admin Login", Icons.Default.Security)
            ).forEach { (mode, label, icon) ->
                val isSelected = authMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                if (mode == 2) Color(0xFFE11D48) else Color(0xFF6366F1)
                            } else Color.Transparent
                        )
                        .clickable { authMode = mode }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (authMode) {
                    // =========================================================
                    // 0: MEMBER LOGIN
                    // =========================================================
                    0 -> {
                        // Quick credentials helper for test
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("DEFAULT DEMO MEMBER CREDENTIALS:", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Mobile: 03001234567 | Password: User@123", color = Color(0xFF38BDF8), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        OutlinedTextField(
                            value = uiState.loginMobile,
                            onValueChange = { viewModel.updateLoginMobile(it) },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("login_mobile_input")
                        )

                        OutlinedTextField(
                            value = uiState.loginPassword,
                            onValueChange = { viewModel.updateLoginPassword(it) },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        Button(
                            onClick = { viewModel.submitLogin() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_login_btn")
                        ) {
                            Text("Login to Wallet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // =========================================================
                    // 1: REGISTRATION (WHATSAPP OTP)
                    // =========================================================
                    1 -> {
                        // 20 RS Welcome Bonus Promotion Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF064E3B))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🎁", fontSize = 24.sp)
                                Column {
                                    Text(
                                        "NEW ACCOUNT BONUS: 20 RS",
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        "Get instant 20 RS welcome bonus credited upon account creation!",
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.regName,
                            onValueChange = { viewModel.updateRegName(it) },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                        )

                        OutlinedTextField(
                            value = uiState.regMobile,
                            onValueChange = { viewModel.updateRegMobile(it) },
                            label = { Text("Mobile Number (Primary)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input")
                        )

                        OutlinedTextField(
                            value = uiState.regWhatsapp,
                            onValueChange = { viewModel.updateRegWhatsapp(it) },
                            label = { Text("WhatsApp Number (For OTP)") },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("reg_whatsapp_input")
                        )

                        // Request OTP Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.regOtp,
                                onValueChange = { viewModel.updateRegOtp(it) },
                                label = { Text("6-Digit OTP") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.weight(1f).testTag("reg_otp_input")
                            )

                            Button(
                                onClick = { viewModel.requestRegistrationOtp() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(56.dp).testTag("request_otp_btn")
                            ) {
                                Text(if (uiState.isOtpSent) "Resend" else "Get OTP", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = uiState.regPassword,
                            onValueChange = { viewModel.updateRegPassword(it) },
                            label = { Text("Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("reg_pass_input")
                        )

                        OutlinedTextField(
                            value = uiState.regConfirmPassword,
                            onValueChange = { viewModel.updateRegConfirmPassword(it) },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.regReferralCode,
                            onValueChange = { viewModel.updateRegReferralCode(it) },
                            label = { Text("Referral Code (Optional)") },
                            leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { viewModel.submitRegistration() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_reg_btn")
                        ) {
                            Text("Create Account (Get 20 RS Bonus)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // =========================================================
                    // 2: ADMIN SETUP & LOGIN
                    // =========================================================
                    2 -> {
                        // Admin Credentials Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2D1520))
                                .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("OFFICIAL SUPER ADMIN CREDENTIALS", color = Color(0xFFFDA4AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Admin Login / Email:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text("admin@system.com", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Column {
                                        Text("Master Password:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text("Admin@786", color = Color(0xFFF43F5E), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateAdminLoginEmail("admin@system.com")
                                        viewModel.updateAdminLoginPassword("Admin@786")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C0519)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Auto-Fill Admin Credentials", color = Color(0xFFFDA4AF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.adminLoginEmail,
                            onValueChange = { viewModel.updateAdminLoginEmail(it) },
                            label = { Text("Admin Email / ID") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFFF43F5E)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFE11D48),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_email_input")
                        )

                        OutlinedTextField(
                            value = uiState.adminLoginPassword,
                            onValueChange = { viewModel.updateAdminLoginPassword(it) },
                            label = { Text("Admin Master Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF43F5E)) },
                            trailingIcon = {
                                IconButton(onClick = { showAdminPassword = !showAdminPassword }) {
                                    Icon(
                                        imageVector = if (showAdminPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            visualTransformation = if (showAdminPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFE11D48),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_pass_input")
                        )

                        Button(
                            onClick = { viewModel.submitAdminLogin() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_admin_login_btn")
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LOGIN TO ADMIN PANEL", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        // Admin Permissions & Capabilities Guide
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("ADMIN PRIVILEGES & SCOPE:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("• Reconcile pending EasyPaisa, JazzCash, FlashPay deposits", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text("• Authorize member withdrawals & record bank TRX IDs", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text("• Freeze / Unfreeze member accounts & balance liabilities", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text("• Configure payment till numbers, limits, and fee percentages", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text("• Inspect double-entry immutable financial ledger & audit logs", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Footer
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (authMode) {
                0 -> {
                    Text("Need a member account? ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        text = "Register with WhatsApp OTP",
                        color = Color(0xFF818CF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { authMode = 1 }
                    )
                }
                1 -> {
                    Text("Already registered? ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        text = "Member Login",
                        color = Color(0xFF818CF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { authMode = 0 }
                    )
                }
                2 -> {
                    Text("Switch back to ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        text = "Member Area",
                        color = Color(0xFF818CF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { authMode = 0 }
                    )
                }
            }
        }
    }
}
