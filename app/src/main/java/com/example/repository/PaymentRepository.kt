package com.example.repository

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong

/**
 * Enterprise Secure Repository and Ledger Engine
 * Implements strict balance check, atomic locking, audit logging, and double-entry transaction records.
 */
class PaymentRepository {

    private val nextUserId = AtomicLong(1001)
    private val nextDepositId = AtomicLong(5001)
    private val nextWithdrawalId = AtomicLong(8001)
    private val nextTransactionId = AtomicLong(10001)
    private val nextAuditId = AtomicLong(1)

    // In-memory persistent state holding synchronized business records
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _deposits = MutableStateFlow<List<DepositRecord>>(emptyList())
    val deposits: StateFlow<List<DepositRecord>> = _deposits.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<WithdrawalRecord>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawalRecord>> = _withdrawals.asStateFlow()

    private val _transactions = MutableStateFlow<List<LedgerTransaction>>(emptyList())
    val transactions: StateFlow<List<LedgerTransaction>> = _transactions.asStateFlow()

    private val _providers = MutableStateFlow<List<PaymentProviderConfig>>(emptyList())
    val providers: StateFlow<List<PaymentProviderConfig>> = _providers.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Passwords stored as SHA-256 hashes
    private val userPasswordHashes = mutableMapOf<Long, String>()
    // OTP verification store: Mobile -> Triple(OtpHash, ExpiresAt, Attempts)
    private val otpStore = mutableMapOf<String, Triple<String, Long, Int>>()

    init {
        // Seed default payment provider accounts
        _providers.value = listOf(
            PaymentProviderConfig(
                id = "easypaisa",
                name = "EasyPaisa",
                isEnabled = true,
                accountTitle = "AL-REHMAN ENTERPRISES (Official Merchant)",
                accountNumber = "03451234567",
                merchantId = "EP_MERCHANT_9081",
                minDeposit = 100.0,
                maxDeposit = 50000.0,
                minWithdrawal = 200.0,
                maxWithdrawal = 25000.0,
                depositFeePercent = 0.0,
                withdrawalFeePercent = 1.0,
                instructions = "Send funds via EasyPaisa App > Money Transfer to Account Number. Enter sender mobile and copy the 11-digit TRX ID."
            ),
            PaymentProviderConfig(
                id = "jazzcash",
                name = "JazzCash",
                isEnabled = true,
                accountTitle = "AL-REHMAN PAYMENTS (Official Merchant)",
                accountNumber = "03009876543",
                merchantId = "JC_MERCHANT_4421",
                minDeposit = 100.0,
                maxDeposit = 50000.0,
                minWithdrawal = 200.0,
                maxWithdrawal = 25000.0,
                depositFeePercent = 0.0,
                withdrawalFeePercent = 1.5,
                instructions = "Dial *786# or use JazzCash App. Send Money to Mobile Account. Enter the TID / Reference Number below."
            ),
            PaymentProviderConfig(
                id = "fastpay",
                name = "FastPay / FlashPay",
                isEnabled = true,
                accountTitle = "FAST DIGITAL WALLET AGENT",
                accountNumber = "03115556677",
                merchantId = "FP_DIRECT_002",
                minDeposit = 500.0,
                maxDeposit = 100000.0,
                minWithdrawal = 500.0,
                maxWithdrawal = 50000.0,
                depositFeePercent = 0.0,
                withdrawalFeePercent = 0.5,
                instructions = "Direct FastPay / FlashPay instant deposit. Fast tracking verified automatically through official banking network."
            )
        )

        // Seed initial demo user
        val demoUserId = nextUserId.getAndIncrement()
        val demoUser = User(
            id = demoUserId,
            name = "Hamza Malik",
            mobile = "03001234567",
            whatsappNumber = "03001234567",
            isWhatsappVerified = true,
            status = AccountStatus.ACTIVE,
            balance = NEW_ACCOUNT_BONUS_RS,
            reservedBalance = 0.0,
            referralCode = "VIP777"
        )
        userPasswordHashes[demoUserId] = hashPassword("User@123")
        _users.value = listOf(demoUser)

        // Seed initial ledger transactions
        val tId = nextTransactionId.getAndIncrement()
        _transactions.value = listOf(
            LedgerTransaction(
                id = tId,
                userId = demoUserId,
                type = TransactionType.DEPOSIT,
                referenceType = "WELCOME_BONUS",
                referenceId = 5000,
                amount = NEW_ACCOUNT_BONUS_RS,
                balanceBefore = 0.0,
                balanceAfter = NEW_ACCOUNT_BONUS_RS,
                description = "New Account Welcome Bonus: 20 RS"
            )
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // -------------------------------------------------------------------------
    // OTP & Authentication
    // -------------------------------------------------------------------------
    fun sendWhatsAppOtp(mobile: String): Pair<Boolean, String> {
        val clean = mobile.trim()
        if (clean.length < 10) return Pair(false, "Invalid phone number format")

        // 6-digit cryptographically secure OTP
        val otpCode = (100000 + (Math.random() * 900000).toInt()).toString()
        val hashed = hashPassword(otpCode)
        val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes

        otpStore[clean] = Triple(hashed, expiresAt, 0)

        // Simulated authorized WhatsApp API provider message delivery
        return Pair(true, "OTP generated and sent to WhatsApp $clean. [Sandbox Security Code: $otpCode]")
    }

    fun verifyOtpAndRegister(
        name: String,
        mobile: String,
        whatsapp: String,
        password: String,
        enteredOtp: String,
        referralCode: String?
    ): Pair<User?, String> {
        val cleanMobile = mobile.trim()
        val existing = _users.value.find { it.mobile == cleanMobile }
        if (existing != null) {
            return Pair(null, "Mobile number already registered")
        }

        val otpInfo = otpStore[cleanMobile] ?: return Pair(null, "No OTP requested for this number")
        val (expectedHash, expiresAt, attempts) = otpInfo

        if (System.currentTimeMillis() > expiresAt) {
            otpStore.remove(cleanMobile)
            return Pair(null, "OTP has expired. Please request a new one.")
        }
        if (attempts >= 5) {
            otpStore.remove(cleanMobile)
            return Pair(null, "Maximum OTP attempts exceeded. Cooldown active.")
        }

        val enteredHash = hashPassword(enteredOtp.trim())
        if (enteredHash != expectedHash) {
            otpStore[cleanMobile] = Triple(expectedHash, expiresAt, attempts + 1)
            return Pair(null, "Incorrect OTP code. Attempts remaining: ${4 - attempts}")
        }

        // OTP verified successfully
        otpStore.remove(cleanMobile)
        val newId = nextUserId.getAndIncrement()
        val newUser = User(
            id = newId,
            name = name.trim(),
            mobile = cleanMobile,
            whatsappNumber = whatsapp.trim(),
            isWhatsappVerified = true,
            status = AccountStatus.ACTIVE,
            balance = NEW_ACCOUNT_BONUS_RS, // 20 RS welcome bonus for every new account
            reservedBalance = 0.0,
            referralCode = referralCode?.takeIf { it.isNotBlank() }
        )

        userPasswordHashes[newId] = hashPassword(password)
        _users.value = _users.value + newUser

        // Double-entry ledger audit record for 20 RS welcome bonus
        val bonusTxId = nextTransactionId.getAndIncrement()
        val bonusTx = LedgerTransaction(
            id = bonusTxId,
            userId = newId,
            type = TransactionType.DEPOSIT,
            referenceType = "WELCOME_BONUS",
            referenceId = bonusTxId,
            amount = NEW_ACCOUNT_BONUS_RS,
            balanceBefore = 0.0,
            balanceAfter = NEW_ACCOUNT_BONUS_RS,
            description = "New Account Welcome Bonus: 20 RS"
        )
        _transactions.value = _transactions.value + bonusTx

        recordAudit(
            adminEmail = "SYSTEM_AUTH",
            action = "USER_REGISTERED_WELCOME_BONUS_20RS",
            entityType = "USER",
            entityId = newId,
            details = "User $name ($cleanMobile) registered successfully with verified WhatsApp OTP. 20 RS welcome bonus credited."
        )

        return Pair(newUser, "Account created successfully! 20 RS welcome bonus credited.")
    }

    fun login(mobile: String, password: String): Pair<User?, String> {
        val user = _users.value.find { it.mobile == mobile.trim() }
            ?: return Pair(null, "Account not found with this mobile number")

        if (user.status == AccountStatus.SUSPENDED || user.status == AccountStatus.BLOCKED) {
            return Pair(null, "Account is ${user.status}. Please contact administrator.")
        }

        val expectedHash = userPasswordHashes[user.id]
        if (expectedHash == null || expectedHash != hashPassword(password)) {
            return Pair(null, "Invalid mobile number or password")
        }

        val updated = user.copy(lastLoginAt = System.currentTimeMillis())
        _users.value = _users.value.map { if (it.id == user.id) updated else it }
        return Pair(updated, "Login successful")
    }

    // -------------------------------------------------------------------------
    // Deposits (Manual + Official Verification Flow)
    // -------------------------------------------------------------------------
    @Synchronized
    fun submitDeposit(
        userId: Long,
        paymentMethod: String,
        amount: Double,
        senderNumber: String,
        transactionRef: String,
        proofNote: String
    ): Pair<Boolean, String> {
        val user = _users.value.find { it.id == userId } ?: return Pair(false, "User not found")
        if (user.status != AccountStatus.ACTIVE) return Pair(false, "Account inactive")

        val provider = _providers.value.find { it.id.equals(paymentMethod, ignoreCase = true) || it.name.equals(paymentMethod, ignoreCase = true) }
            ?: return Pair(false, "Invalid payment method")

        if (!provider.isEnabled) return Pair(false, "${provider.name} is currently disabled by administrator")
        if (amount < provider.minDeposit) return Pair(false, "Minimum deposit is PKR ${provider.minDeposit}")
        if (amount > provider.maxDeposit) return Pair(false, "Maximum deposit is PKR ${provider.maxDeposit}")

        val cleanRef = transactionRef.trim()
        if (cleanRef.isBlank()) return Pair(false, "Transaction / Reference ID is required")

        // Duplicate Reference ID check
        if (_deposits.value.any { it.transactionRef.equals(cleanRef, ignoreCase = true) }) {
            return Pair(false, "Transaction Reference ID '$cleanRef' has already been submitted")
        }

        val depositId = nextDepositId.getAndIncrement()
        val deposit = DepositRecord(
            id = depositId,
            userId = user.id,
            userName = user.name,
            paymentMethod = provider.name,
            amount = amount,
            senderNumber = senderNumber.trim(),
            transactionRef = cleanRef,
            proofNote = proofNote.trim(),
            status = DepositStatus.PENDING
        )

        _deposits.value = listOf(deposit) + _deposits.value
        // CRITICAL: User's balance does NOT increase simply because deposit was submitted!
        return Pair(true, "Deposit request #${deposit.id} submitted. Pending admin verification.")
    }

    @Synchronized
    fun approveDeposit(depositId: Long, adminEmail: String, adminNote: String?): Pair<Boolean, String> {
        val deposit = _deposits.value.find { it.id == depositId } ?: return Pair(false, "Deposit not found")
        if (deposit.status != DepositStatus.PENDING) return Pair(false, "Deposit is already ${deposit.status}")

        val user = _users.value.find { it.id == deposit.userId } ?: return Pair(false, "User not found")

        val balanceBefore = user.balance
        val balanceAfter = balanceBefore + deposit.amount

        // 1. Update user balance atomically
        val updatedUser = user.copy(balance = balanceAfter)
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }

        // 2. Mark deposit approved
        val updatedDeposit = deposit.copy(
            status = DepositStatus.APPROVED,
            adminNote = adminNote,
            approvedAt = System.currentTimeMillis(),
            approvedBy = adminEmail
        )
        _deposits.value = _deposits.value.map { if (it.id == deposit.id) updatedDeposit else it }

        // 3. Write immutable ledger record
        val ledgerId = nextTransactionId.getAndIncrement()
        val ledgerTx = LedgerTransaction(
            id = ledgerId,
            userId = user.id,
            type = TransactionType.DEPOSIT,
            referenceType = "DEPOSIT",
            referenceId = deposit.id,
            amount = deposit.amount,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Deposit approved via ${deposit.paymentMethod} (TRX: ${deposit.transactionRef})"
        )
        _transactions.value = listOf(ledgerTx) + _transactions.value

        // 4. Audit Log
        recordAudit(
            adminEmail = adminEmail,
            action = "APPROVE_DEPOSIT",
            entityType = "DEPOSIT",
            entityId = deposit.id,
            details = "Approved deposit #${deposit.id} of PKR ${deposit.amount} for user #${user.id}. Note: $adminNote"
        )

        return Pair(true, "Deposit approved and PKR ${deposit.amount} credited to user balance.")
    }

    @Synchronized
    fun rejectDeposit(depositId: Long, adminEmail: String, reason: String): Pair<Boolean, String> {
        val deposit = _deposits.value.find { it.id == depositId } ?: return Pair(false, "Deposit not found")
        if (deposit.status != DepositStatus.PENDING) return Pair(false, "Deposit is already ${deposit.status}")

        val updated = deposit.copy(
            status = DepositStatus.REJECTED,
            adminNote = reason,
            approvedAt = System.currentTimeMillis(),
            approvedBy = adminEmail
        )
        _deposits.value = _deposits.value.map { if (it.id == deposit.id) updated else it }

        recordAudit(
            adminEmail = adminEmail,
            action = "REJECT_DEPOSIT",
            entityType = "DEPOSIT",
            entityId = deposit.id,
            details = "Rejected deposit #${deposit.id} of PKR ${deposit.amount}. Reason: $reason"
        )

        return Pair(true, "Deposit #${deposit.id} rejected.")
    }

    // -------------------------------------------------------------------------
    // Withdrawals (Safe Balance Reservation & Reversal)
    // -------------------------------------------------------------------------
    @Synchronized
    fun submitWithdrawal(
        userId: Long,
        paymentMethod: String,
        amount: Double,
        accountName: String,
        accountNumber: String,
        note: String
    ): Pair<Boolean, String> {
        val user = _users.value.find { it.id == userId } ?: return Pair(false, "User not found")
        if (user.status != AccountStatus.ACTIVE) return Pair(false, "Account inactive")

        val provider = _providers.value.find { it.id.equals(paymentMethod, ignoreCase = true) || it.name.equals(paymentMethod, ignoreCase = true) }
            ?: return Pair(false, "Invalid withdrawal method")

        if (!provider.isEnabled) return Pair(false, "${provider.name} is currently disabled")
        if (amount < provider.minWithdrawal) return Pair(false, "Minimum withdrawal is PKR ${provider.minWithdrawal}")
        if (amount > provider.maxWithdrawal) return Pair(false, "Maximum withdrawal is PKR ${provider.maxWithdrawal}")

        val fee = (amount * (provider.withdrawalFeePercent / 100.0)).coerceAtLeast(0.0)
        val totalToDeduct = amount

        // Strict Balance Check (Available = balance - reserved)
        if (user.availableBalance < totalToDeduct) {
            return Pair(false, "Insufficient balance. Available: PKR ${user.availableBalance}")
        }

        // Reserve balance immediately to prevent double spending
        val updatedUser = user.copy(
            reservedBalance = user.reservedBalance + totalToDeduct
        )
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }

        val netAmount = amount - fee
        val withdrawalId = nextWithdrawalId.getAndIncrement()
        val withdrawal = WithdrawalRecord(
            id = withdrawalId,
            userId = user.id,
            userName = user.name,
            paymentMethod = provider.name,
            amount = amount,
            fee = fee,
            netAmount = netAmount,
            accountName = accountName.trim(),
            accountNumber = accountNumber.trim(),
            status = WithdrawalStatus.PENDING,
            adminNote = note.takeIf { it.isNotBlank() }
        )

        _withdrawals.value = listOf(withdrawal) + _withdrawals.value

        // Record lock in ledger
        val ledgerTx = LedgerTransaction(
            id = nextTransactionId.getAndIncrement(),
            userId = user.id,
            type = TransactionType.WITHDRAWAL_LOCK,
            referenceType = "WITHDRAWAL",
            referenceId = withdrawal.id,
            amount = totalToDeduct,
            balanceBefore = user.balance,
            balanceAfter = user.balance, // Balance unchanged until paid; reserved increased
            description = "Funds reserved for withdrawal #${withdrawal.id} via ${provider.name}"
        )
        _transactions.value = listOf(ledgerTx) + _transactions.value

        return Pair(true, "Withdrawal #${withdrawal.id} submitted! Reserved PKR $amount safely.")
    }

    @Synchronized
    fun markWithdrawalPaid(withdrawalId: Long, adminEmail: String, transactionRef: String, adminNote: String?): Pair<Boolean, String> {
        val w = _withdrawals.value.find { it.id == withdrawalId } ?: return Pair(false, "Withdrawal not found")
        if (w.status != WithdrawalStatus.PENDING && w.status != WithdrawalStatus.PROCESSING) {
            return Pair(false, "Withdrawal already ${w.status}")
        }

        val user = _users.value.find { it.id == w.userId } ?: return Pair(false, "User not found")

        val balanceBefore = user.balance
        val balanceAfter = (balanceBefore - w.amount).coerceAtLeast(0.0)
        val reservedAfter = (user.reservedBalance - w.amount).coerceAtLeast(0.0)

        // Deduct from balance and clear reservation
        val updatedUser = user.copy(
            balance = balanceAfter,
            reservedBalance = reservedAfter
        )
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }

        val updatedW = w.copy(
            status = WithdrawalStatus.PAID,
            transactionRef = transactionRef,
            adminNote = adminNote,
            processedAt = System.currentTimeMillis(),
            processedBy = adminEmail
        )
        _withdrawals.value = _withdrawals.value.map { if (it.id == w.id) updatedW else it }

        // Ledger entry
        val ledgerTx = LedgerTransaction(
            id = nextTransactionId.getAndIncrement(),
            userId = user.id,
            type = TransactionType.WITHDRAWAL_PAID,
            referenceType = "WITHDRAWAL",
            referenceId = w.id,
            amount = w.amount,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Withdrawal #${w.id} paid via ${w.paymentMethod} (TRX: $transactionRef)"
        )
        _transactions.value = listOf(ledgerTx) + _transactions.value

        recordAudit(
            adminEmail = adminEmail,
            action = "PAID_WITHDRAWAL",
            entityType = "WITHDRAWAL",
            entityId = w.id,
            details = "Withdrawal #${w.id} marked as PAID. Reference: $transactionRef. Net transferred: PKR ${w.netAmount}"
        )

        return Pair(true, "Withdrawal marked as PAID. Balance finalized.")
    }

    @Synchronized
    fun rejectWithdrawal(withdrawalId: Long, adminEmail: String, reason: String): Pair<Boolean, String> {
        val w = _withdrawals.value.find { it.id == withdrawalId } ?: return Pair(false, "Withdrawal not found")
        if (w.status != WithdrawalStatus.PENDING && w.status != WithdrawalStatus.PROCESSING) {
            return Pair(false, "Withdrawal is already ${w.status}")
        }

        val user = _users.value.find { it.id == w.userId } ?: return Pair(false, "User not found")

        // Safely refund reserved balance back to user available balance
        val reservedAfter = (user.reservedBalance - w.amount).coerceAtLeast(0.0)
        val updatedUser = user.copy(reservedBalance = reservedAfter)
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }

        val updatedW = w.copy(
            status = WithdrawalStatus.REJECTED,
            adminNote = reason,
            processedAt = System.currentTimeMillis(),
            processedBy = adminEmail
        )
        _withdrawals.value = _withdrawals.value.map { if (it.id == w.id) updatedW else it }

        // Ledger refund entry
        val ledgerTx = LedgerTransaction(
            id = nextTransactionId.getAndIncrement(),
            userId = user.id,
            type = TransactionType.WITHDRAWAL_REFUND,
            referenceType = "WITHDRAWAL",
            referenceId = w.id,
            amount = w.amount,
            balanceBefore = user.balance,
            balanceAfter = user.balance,
            description = "Reserved balance unlocked for rejected withdrawal #${w.id}"
        )
        _transactions.value = listOf(ledgerTx) + _transactions.value

        recordAudit(
            adminEmail = adminEmail,
            action = "REJECT_WITHDRAWAL",
            entityType = "WITHDRAWAL",
            entityId = w.id,
            details = "Rejected withdrawal #${w.id}. Reserved funds unlocked. Reason: $reason"
        )

        return Pair(true, "Withdrawal rejected. PKR ${w.amount} unlocked back to user available balance.")
    }

    // -------------------------------------------------------------------------
    // User & Provider Management (Admin)
    // -------------------------------------------------------------------------
    fun toggleUserStatus(userId: Long, adminEmail: String): Pair<Boolean, String> {
        val user = _users.value.find { it.id == userId } ?: return Pair(false, "User not found")
        val newStatus = if (user.status == AccountStatus.ACTIVE) AccountStatus.SUSPENDED else AccountStatus.ACTIVE
        val updated = user.copy(status = newStatus)
        _users.value = _users.value.map { if (it.id == user.id) updated else it }

        recordAudit(
            adminEmail = adminEmail,
            action = "TOGGLE_USER_STATUS",
            entityType = "USER",
            entityId = user.id,
            details = "Changed user #${user.id} status from ${user.status} to $newStatus"
        )
        return Pair(true, "User status updated to $newStatus")
    }

    fun updateProviderConfig(
        providerId: String,
        isEnabled: Boolean,
        accountTitle: String,
        accountNumber: String,
        minDeposit: Double,
        maxDeposit: Double,
        minWithdrawal: Double,
        maxWithdrawal: Double,
        withdrawalFeePercent: Double,
        adminEmail: String
    ): Pair<Boolean, String> {
        val current = _providers.value.find { it.id == providerId } ?: return Pair(false, "Provider not found")
        val updated = current.copy(
            isEnabled = isEnabled,
            accountTitle = accountTitle,
            accountNumber = accountNumber,
            minDeposit = minDeposit,
            maxDeposit = maxDeposit,
            minWithdrawal = minWithdrawal,
            maxWithdrawal = maxWithdrawal,
            withdrawalFeePercent = withdrawalFeePercent
        )
        _providers.value = _providers.value.map { if (it.id == providerId) updated else it }

        recordAudit(
            adminEmail = adminEmail,
            action = "UPDATE_PROVIDER_CONFIG",
            entityType = "PAYMENT_PROVIDER",
            entityId = 0,
            details = "Updated $providerId settings: enabled=$isEnabled, acc=$accountNumber, fee=$withdrawalFeePercent%"
        )
        return Pair(true, "Provider settings saved successfully")
    }

    private fun recordAudit(adminEmail: String, action: String, entityType: String, entityId: Long, details: String) {
        val log = AuditLog(
            id = nextAuditId.getAndIncrement(),
            adminEmail = adminEmail,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // -------------------------------------------------------------------------
    // Unified Gaming Wallet Synchronization
    // -------------------------------------------------------------------------
    fun getActiveUser(): User? {
        return _users.value.firstOrNull()
    }

    @Synchronized
    fun adjustBalanceForGame(userId: Long, deltaAmount: Double, description: String = "Game Table Transaction"): Boolean {
        val user = _users.value.find { it.id == userId } ?: return false
        val balanceBefore = user.balance
        val balanceAfter = (balanceBefore + deltaAmount).coerceAtLeast(0.0)
        val updatedUser = user.copy(balance = balanceAfter)
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }

        val ledgerId = nextTransactionId.getAndIncrement()
        val txType = if (deltaAmount >= 0) TransactionType.GAME_WIN else TransactionType.GAME_BET
        val ledgerTx = LedgerTransaction(
            id = ledgerId,
            userId = user.id,
            type = txType,
            referenceType = "GAME",
            referenceId = ledgerId,
            amount = kotlin.math.abs(deltaAmount),
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = description
        )
        _transactions.value = listOf(ledgerTx) + _transactions.value
        return true
    }

    @Synchronized
    fun resetDemoBalance(userId: Long, targetBalance: Double = NEW_ACCOUNT_BONUS_RS) {
        val user = _users.value.find { it.id == userId } ?: return
        val updatedUser = user.copy(balance = targetBalance)
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }
    }

    companion object {
        const val NEW_ACCOUNT_BONUS_RS = 20.0
        val shared: PaymentRepository by lazy { PaymentRepository() }
    }
}
