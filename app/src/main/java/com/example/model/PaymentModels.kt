package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AccountStatus {
    ACTIVE, SUSPENDED, BLOCKED
}

enum class DepositStatus {
    PENDING, APPROVED, REJECTED
}

enum class WithdrawalStatus {
    PENDING, PROCESSING, PAID, REJECTED, CANCELLED
}

enum class TransactionType {
    DEPOSIT, WITHDRAWAL_LOCK, WITHDRAWAL_PAID, WITHDRAWAL_REFUND, ADJUSTMENT, GAME_BET, GAME_WIN
}

data class User(
    val id: Long,
    val name: String,
    val mobile: String,
    val whatsappNumber: String,
    val isWhatsappVerified: Boolean,
    val status: AccountStatus,
    val balance: Double,
    val reservedBalance: Double = 0.0,
    val referralCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
) {
    val availableBalance: Double
        get() = (balance - reservedBalance).coerceAtLeast(0.0)
}

data class DepositRecord(
    val id: Long,
    val userId: Long,
    val userName: String,
    val paymentMethod: String, // "EasyPaisa", "JazzCash", "FlashPay"
    val amount: Double,
    val senderNumber: String,
    val transactionRef: String,
    val proofNote: String = "",
    val status: DepositStatus,
    val adminNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val approvedBy: String? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(createdAt))
}

data class WithdrawalRecord(
    val id: Long,
    val userId: Long,
    val userName: String,
    val paymentMethod: String,
    val amount: Double,
    val fee: Double,
    val netAmount: Double,
    val accountName: String,
    val accountNumber: String,
    val status: WithdrawalStatus,
    val transactionRef: String? = null,
    val adminNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processedBy: String? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(createdAt))
}

data class LedgerTransaction(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val referenceType: String,
    val referenceId: Long,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

data class PaymentProviderConfig(
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val accountTitle: String,
    val accountNumber: String,
    val merchantId: String = "",
    val minDeposit: Double = 100.0,
    val maxDeposit: Double = 50000.0,
    val minWithdrawal: Double = 200.0,
    val maxWithdrawal: Double = 25000.0,
    val depositFeePercent: Double = 0.0,
    val withdrawalFeePercent: Double = 1.0,
    val instructions: String = ""
)

data class AuditLog(
    val id: Long,
    val adminEmail: String,
    val action: String,
    val entityType: String,
    val entityId: Long,
    val details: String,
    val ipAddress: String = "127.0.0.1",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
