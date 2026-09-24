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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentProviderConfig
import com.example.model.User
import com.example.viewmodel.PaymentUiState
import com.example.viewmodel.PaymentViewModel

@Composable
fun WithdrawalScreen(
    uiState: PaymentUiState,
    viewModel: PaymentViewModel,
    user: User,
    providers: List<PaymentProviderConfig>,
    onBack: () -> Unit,
    onBackToLobby: (() -> Unit)? = null
) {
    val selectedProvider = providers.find { it.name.equals(uiState.selectedWithdrawMethod, ignoreCase = true) }
        ?: providers.firstOrNull()

    val requestedAmount = uiState.withdrawAmountText.toDoubleOrNull() ?: 0.0
    val feePercent = selectedProvider?.withdrawalFeePercent ?: 1.0
    val calculatedFee = (requestedAmount * (feePercent / 100.0)).coerceAtLeast(0.0)
    val netAmountToReceive = (requestedAmount - calculatedFee).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back & Title
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
                        .testTag("withdraw_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Request Withdrawal",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fast Mobile Cashout & Banking Payout",
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
                    modifier = Modifier.testTag("withdraw_back_to_lobby_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back to Games", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Available Balance Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "AVAILABLE FOR CASHOUT", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "PKR ${String.format("%,.2f", user.availableBalance)}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(24.dp))
                }
            }
        }

        // Payout Method Selector
        Text(
            text = "RECEIVING PAYMENT METHOD",
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
                val isSelected = uiState.selectedWithdrawMethod.equals(method, ignoreCase = true) ||
                        (method.startsWith("FastPay") && uiState.selectedWithdrawMethod.contains("FastPay", ignoreCase = true))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF818CF8) else Color(0xFF334155),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectWithdrawMethod(method) }
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

        // Beneficiary Details Form
        Text(
            text = "BENEFICIARY ACCOUNT DETAILS",
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
            val quickAmounts = listOf("500", "1000", "2500", "5000", "10000")
            quickAmounts.forEach { amt ->
                val isSelected = uiState.withdrawAmountText == amt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF818CF8) else Color(0xFF334155),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateWithdrawAmount(amt) }
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

            // Max Available Amount chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF312E81))
                    .border(1.dp, Color(0xFF818CF8), RoundedCornerShape(8.dp))
                    .clickable { viewModel.updateWithdrawAmount(user.availableBalance.toInt().coerceAtLeast(0).toString()) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MAX",
                    color = Color(0xFFFDE047),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Amount Field
        OutlinedTextField(
            value = uiState.withdrawAmountText,
            onValueChange = { viewModel.updateWithdrawAmount(it) },
            label = { Text("Withdrawal Amount (PKR)") },
            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF6366F1)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF6366F1),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("withdraw_amount_input")
        )

        // Account Holder Name
        OutlinedTextField(
            value = uiState.withdrawAccountName,
            onValueChange = { viewModel.updateWithdrawAccountName(it) },
            label = { Text("Account Holder Title / Name") },
            placeholder = { Text("Exact name on EasyPaisa / JazzCash") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF6366F1),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("withdraw_acc_name_input")
        )

        // Account Number
        OutlinedTextField(
            value = uiState.withdrawAccountNumber,
            onValueChange = { viewModel.updateWithdrawAccountNumber(it) },
            label = { Text("Receiving Account Number") },
            placeholder = { Text("e.g. 03001234567") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF6366F1),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("withdraw_acc_num_input")
        )

        // Payout Calculation Summary Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Requested Cashout:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(text = "PKR ${String.format("%,.2f", requestedAmount)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Provider Fee ($feePercent%):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(text = "- PKR ${String.format("%,.2f", calculatedFee)}", color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Net Amount You Receive:", color = Color(0xFFC7D2FE), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "PKR ${String.format("%,.2f", netAmountToReceive)}",
                        color = Color(0xFF10B981),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Balance Reservation Policy Notice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF6366F1).copy(alpha = 0.1f))
                .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PKR ${String.format("%,.2f", requestedAmount)} will be reserved immediately upon submission to guarantee double-spend protection. If cancelled, funds return directly to your available balance.",
                    color = Color(0xFFC7D2FE),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Submit Button
        Button(
            onClick = { viewModel.submitWithdrawalRequest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_withdraw_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirm & Request Payout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
