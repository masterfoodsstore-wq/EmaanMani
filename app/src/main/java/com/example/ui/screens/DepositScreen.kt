package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentProviderConfig
import com.example.viewmodel.PaymentUiState
import com.example.viewmodel.PaymentViewModel

@Composable
fun DepositScreen(
    uiState: PaymentUiState,
    viewModel: PaymentViewModel,
    providers: List<PaymentProviderConfig>,
    onBack: () -> Unit,
    onBackToLobby: (() -> Unit)? = null
) {
    val selectedProvider = providers.find { it.name.equals(uiState.selectedDepositMethod, ignoreCase = true) }
        ?: providers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("deposit_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Deposit Funds",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "EasyPaisa • JazzCash • FastPay • Bank",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            if (onBackToLobby != null) {
                Button(
                    onClick = onBackToLobby,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("deposit_back_to_lobby_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back to Games", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Provider Selector Pills
        Text(
            text = "SELECT PAYMENT METHOD",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("EasyPaisa", "JazzCash", "FastPay / FlashPay").forEach { method ->
                val isSelected = uiState.selectedDepositMethod.equals(method, ignoreCase = true) ||
                        (method.startsWith("FastPay") && uiState.selectedDepositMethod.contains("FastPay", ignoreCase = true))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF34D399) else Color(0xFF334155),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectDepositMethod(method) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (method.contains("/")) "FlashPay" else method,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Official Merchant Instructions & Details Card
        if (selectedProvider != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL MERCHANT ACCOUNT",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                color = Color(0xFF10B981),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(text = "Account Title", color = Color(0xFF64748B), fontSize = 11.sp)
                            Text(text = selectedProvider.accountTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Account / Till Number", color = Color(0xFF64748B), fontSize = 11.sp)
                            Text(text = selectedProvider.accountNumber, color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Text(
                        text = selectedProvider.instructions,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Limits: PKR ${selectedProvider.minDeposit.toInt()} - PKR ${selectedProvider.maxDeposit.toInt()}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Fee: Free (0%)",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Deposit Verification Submission Form
        Text(
            text = "SUBMIT PAYMENT DETAILS",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Quick Amount Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickAmounts = listOf("500", "1000", "2500", "5000", "10000", "25000")
            quickAmounts.forEach { amt ->
                val isSelected = uiState.depositAmountText == amt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF34D399) else Color(0xFF334155),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateDepositAmount(amt) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = amt,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Amount Field
        OutlinedTextField(
            value = uiState.depositAmountText,
            onValueChange = { viewModel.updateDepositAmount(it) },
            label = { Text("Deposit Amount (PKR)") },
            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF10B981)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF10B981),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deposit_amount_input")
        )

        // Sender Mobile Number
        OutlinedTextField(
            value = uiState.depositSenderNumber,
            onValueChange = { viewModel.updateDepositSender(it) },
            label = { Text("Sender Mobile Number") },
            placeholder = { Text("e.g. 03001234567") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF10B981),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deposit_sender_input")
        )

        // Transaction / Reference ID (Mandatory for Reconciliation)
        OutlinedTextField(
            value = uiState.depositTrxId,
            onValueChange = { viewModel.updateDepositTrxId(it) },
            label = { Text("Transaction / Reference ID (TRX ID)") },
            placeholder = { Text("e.g. 18273645091") },
            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = Color(0xFF94A3B8)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF10B981),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deposit_trx_input")
        )

        // Proof Note / Screenshot Attachment simulator
        OutlinedTextField(
            value = uiState.depositProofNote,
            onValueChange = { viewModel.updateDepositProofNote(it) },
            label = { Text("Payment Proof Note (Optional)") },
            placeholder = { Text("e.g. Screenshot taken from EasyPaisa / Bank confirmation message") },
            leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF94A3B8)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF10B981),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Security Anti-Fraud Notice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.1f))
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Balance will update once your transaction ID is verified against the merchant banking system. Fake submissions result in immediate account suspension.",
                    color = Color(0xFFFCD34D),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Submit Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.submitDepositRequest(autoApprove = false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_deposit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit for Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = { viewModel.submitDepositRequest(autoApprove = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("instant_auto_approve_deposit_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCD34D))
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFBBF24))
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚡ Instant Test Credit (Auto-Approve)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}
