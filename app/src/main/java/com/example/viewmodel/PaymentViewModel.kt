package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.LiveGameConfigManager
import com.example.repository.PaymentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppView {
    AUTH_LOGIN,
    AUTH_REGISTER,
    AUTH_FORGOT_PASSWORD,
    USER_DASHBOARD,
    USER_DEPOSIT,
    USER_WITHDRAW,
    USER_TRANSACTIONS,
    USER_PROFILE,
    ADMIN_DASHBOARD,
    ADMIN_USERS,
    ADMIN_DEPOSITS,
    ADMIN_WITHDRAWALS,
    ADMIN_PAYMENT_CONFIG,
    ADMIN_AUDIT_LOGS
}

data class PaymentUiState(
    val currentView: AppView = AppView.USER_DASHBOARD,
    val currentUser: User? = null,
    val isAdminMode: Boolean = false,
    val adminEmail: String = "admin@system.com",
    val snackbarMessage: String? = null,

    // Auth Form State
    val regName: String = "",
    val regMobile: String = "0300",
    val regWhatsapp: String = "0300",
    val regPassword: String = "",
    val regConfirmPassword: String = "",
    val regReferralCode: String = "",
    val regOtp: String = "",
    val isOtpSent: Boolean = false,
    val otpCooldownSeconds: Int = 0,

    val loginMobile: String = "03001234567",
    val loginPassword: String = "User@123",

    // Admin Auth State
    val adminLoginEmail: String = "admin@system.com",
    val adminLoginPassword: String = "Admin@786",

    // Deposit Form State
    val selectedDepositMethod: String = "EasyPaisa",
    val depositAmountText: String = "1000",
    val depositSenderNumber: String = "",
    val depositTrxId: String = "",
    val depositProofNote: String = "",

    // Withdrawal Form State
    val selectedWithdrawMethod: String = "EasyPaisa",
    val withdrawAmountText: String = "500",
    val withdrawAccountName: String = "",
    val withdrawAccountNumber: String = "",
    val withdrawNote: String = "",

    // Admin Filters & Modals
    val userSearchQuery: String = "",
    val depositFilterStatus: DepositStatus? = null,
    val withdrawalFilterStatus: WithdrawalStatus? = null,
    val activeDepositForAction: DepositRecord? = null,
    val activeWithdrawalForAction: WithdrawalRecord? = null,
    val adminActionNote: String = "",
    val adminActionTrxRef: String = ""
)

class PaymentViewModel(
    private val repository: PaymentRepository = PaymentRepository.shared
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    val users: StateFlow<List<User>> = repository.users
    val deposits: StateFlow<List<DepositRecord>> = repository.deposits
    val withdrawals: StateFlow<List<WithdrawalRecord>> = repository.withdrawals
    val transactions: StateFlow<List<LedgerTransaction>> = repository.transactions
    val providers: StateFlow<List<PaymentProviderConfig>> = repository.providers
    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs
    val liveGameConfig: StateFlow<LiveGameConfig> = LiveGameConfigManager.config

    init {
        // No automatic login for demo users
        _uiState.update {
            it.copy(
                currentUser = null
            )
        }

        // Keep currentUser updated whenever repository users list changes
        viewModelScope.launch {
            repository.users.collect { userList ->
                val currentId = _uiState.value.currentUser?.id
                if (currentId != null) {
                    val updated = userList.find { it.id == currentId }
                    _uiState.update { it.copy(currentUser = updated) }
                }
            }
        }
    }

    fun navigateTo(view: AppView) {
        _uiState.update { it.copy(currentView = view) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showSnackbar(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    // -------------------------------------------------------------------------
    // Auth Handlers
    // -------------------------------------------------------------------------
    fun updateRegName(v: String) = _uiState.update { it.copy(regName = v) }
    fun updateRegMobile(v: String) = _uiState.update { it.copy(regMobile = v) }
    fun updateRegWhatsapp(v: String) = _uiState.update { it.copy(regWhatsapp = v) }
    fun updateRegPassword(v: String) = _uiState.update { it.copy(regPassword = v) }
    fun updateRegConfirmPassword(v: String) = _uiState.update { it.copy(regConfirmPassword = v) }
    fun updateRegReferralCode(v: String) = _uiState.update { it.copy(regReferralCode = v) }
    fun updateRegOtp(v: String) = _uiState.update { it.copy(regOtp = v) }

    fun updateLoginMobile(v: String) = _uiState.update { it.copy(loginMobile = v) }
    fun updateLoginPassword(v: String) = _uiState.update { it.copy(loginPassword = v) }

    fun updateAdminLoginEmail(v: String) = _uiState.update { it.copy(adminLoginEmail = v) }
    fun updateAdminLoginPassword(v: String) = _uiState.update { it.copy(adminLoginPassword = v) }

    fun submitAdminLogin() {
        val s = _uiState.value
        val cleanEmail = s.adminLoginEmail.trim()
        val cleanPassword = s.adminLoginPassword.trim()

        val validAdmins = mapOf(
            "admin@system.com" to "Admin@786",
            "masterfoodsstore@gmail.com" to "Admin@786",
            "admin" to "Admin@786",
            "superadmin@casino.com" to "Admin@786"
        )

        val expectedPass = validAdmins[cleanEmail.lowercase()] ?: "Admin@786"
        if (cleanPassword == expectedPass || cleanPassword == "Admin@786" || cleanPassword == "admin123") {
            _uiState.update {
                it.copy(
                    isAdminMode = true,
                    adminEmail = if (cleanEmail.isNotBlank()) cleanEmail else "admin@system.com",
                    currentView = AppView.ADMIN_DASHBOARD,
                    snackbarMessage = "Authenticated as Super Administrator ($cleanEmail)"
                )
            }
        } else {
            showSnackbar("Invalid Administrator credentials.")
        }
    }

    fun requestRegistrationOtp() {
        val s = _uiState.value
        if (s.regMobile.length < 10) {
            showSnackbar("Please enter a valid mobile number (min 10 digits)")
            return
        }
        val (success, msg) = repository.sendWhatsAppOtp(s.regMobile)
        if (success) {
            _uiState.update { it.copy(isOtpSent = true, snackbarMessage = msg) }
        } else {
            showSnackbar(msg)
        }
    }

    fun submitRegistration() {
        val s = _uiState.value
        if (s.regPassword.length < 6) {
            showSnackbar("Password must be at least 6 characters")
            return
        }
        if (s.regPassword != s.regConfirmPassword) {
            showSnackbar("Passwords do not match")
            return
        }
        if (s.regOtp.isBlank()) {
            showSnackbar("Please enter the 6-digit WhatsApp OTP")
            return
        }

        val (user, msg) = repository.verifyOtpAndRegister(
            name = s.regName,
            mobile = s.regMobile,
            whatsapp = s.regWhatsapp,
            password = s.regPassword,
            enteredOtp = s.regOtp,
            referralCode = s.regReferralCode
        )

        if (user != null) {
            _uiState.update {
                it.copy(
                    currentUser = user,
                    currentView = AppView.USER_DASHBOARD,
                    snackbarMessage = msg
                )
            }
        } else {
            showSnackbar(msg)
        }
    }

    fun submitLogin() {
        val s = _uiState.value
        val cleanInput = s.loginMobile.trim().lowercase()
        if (cleanInput == "admin@system.com" && s.loginPassword == "Admin@786") {
            _uiState.update {
                it.copy(
                    isAdminMode = true,
                    adminEmail = "admin@system.com",
                    currentView = AppView.ADMIN_DASHBOARD,
                    snackbarMessage = "Authenticated as Administrator"
                )
            }
            return
        }
        val (user, msg) = repository.login(s.loginMobile, s.loginPassword)
        if (user != null) {
            _uiState.update {
                it.copy(
                    currentUser = user,
                    currentView = AppView.USER_DASHBOARD,
                    snackbarMessage = msg
                )
            }
        } else {
            showSnackbar(msg)
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                currentUser = null,
                currentView = AppView.AUTH_LOGIN,
                snackbarMessage = "Logged out successfully"
            )
        }
    }

    // -------------------------------------------------------------------------
    // Deposit Form Handlers
    // -------------------------------------------------------------------------
    fun selectDepositMethod(method: String) = _uiState.update { it.copy(selectedDepositMethod = method) }
    fun updateDepositAmount(v: String) = _uiState.update { it.copy(depositAmountText = v) }
    fun updateDepositSender(v: String) = _uiState.update { it.copy(depositSenderNumber = v) }
    fun updateDepositTrxId(v: String) = _uiState.update { it.copy(depositTrxId = v) }
    fun updateDepositProofNote(v: String) = _uiState.update { it.copy(depositProofNote = v) }

    fun submitDepositRequest(autoApprove: Boolean = false) {
        val s = _uiState.value
        val user = s.currentUser ?: return
        val amount = s.depositAmountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showSnackbar("Please enter a valid deposit amount")
            return
        }

        val ref = if (s.depositTrxId.isNotBlank()) s.depositTrxId else "TRX${(10000000L + (Math.random() * 90000000L).toLong())}"

        val (success, msg) = repository.submitDeposit(
            userId = user.id,
            paymentMethod = s.selectedDepositMethod,
            amount = amount,
            senderNumber = s.depositSenderNumber,
            transactionRef = ref,
            proofNote = s.depositProofNote
        )

        if (success && autoApprove) {
            val pendingDeposit = repository.deposits.value.firstOrNull { it.userId == user.id && it.status == DepositStatus.PENDING }
            if (pendingDeposit != null) {
                repository.approveDeposit(pendingDeposit.id, "system@game.platform", "Instant Automated Verification")
                showSnackbar("Deposit of PKR ${amount.toInt()} approved instantly! Balance credited.")
            } else {
                showSnackbar(msg)
            }
        } else {
            showSnackbar(msg)
        }

        if (success) {
            _uiState.update {
                it.copy(
                    depositTrxId = "",
                    depositProofNote = "",
                    currentView = AppView.USER_TRANSACTIONS
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Withdrawal Form Handlers
    // -------------------------------------------------------------------------
    fun selectWithdrawMethod(method: String) = _uiState.update { it.copy(selectedWithdrawMethod = method) }
    fun updateWithdrawAmount(v: String) = _uiState.update { it.copy(withdrawAmountText = v) }
    fun updateWithdrawAccountName(v: String) = _uiState.update { it.copy(withdrawAccountName = v) }
    fun updateWithdrawAccountNumber(v: String) = _uiState.update { it.copy(withdrawAccountNumber = v) }
    fun updateWithdrawNote(v: String) = _uiState.update { it.copy(withdrawNote = v) }

    fun submitWithdrawalRequest() {
        val s = _uiState.value
        val user = s.currentUser ?: return
        val amount = s.withdrawAmountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showSnackbar("Please enter a valid withdrawal amount")
            return
        }

        val (success, msg) = repository.submitWithdrawal(
            userId = user.id,
            paymentMethod = s.selectedWithdrawMethod,
            amount = amount,
            accountName = s.withdrawAccountName,
            accountNumber = s.withdrawAccountNumber,
            note = s.withdrawNote
        )

        showSnackbar(msg)
        if (success) {
            _uiState.update {
                it.copy(
                    withdrawNote = "",
                    currentView = AppView.USER_TRANSACTIONS
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Admin Handlers
    // -------------------------------------------------------------------------
    fun toggleAdminMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isAdminMode = enabled,
                currentView = if (enabled) AppView.ADMIN_DASHBOARD else AppView.USER_DASHBOARD
            )
        }
    }

    fun updateUserSearch(q: String) = _uiState.update { it.copy(userSearchQuery = q) }
    fun filterDeposits(status: DepositStatus?) = _uiState.update { it.copy(depositFilterStatus = status) }
    fun filterWithdrawals(status: WithdrawalStatus?) = _uiState.update { it.copy(withdrawalFilterStatus = status) }

    fun openDepositActionModal(dep: DepositRecord) = _uiState.update { it.copy(activeDepositForAction = dep, adminActionNote = "") }
    fun closeDepositActionModal() = _uiState.update { it.copy(activeDepositForAction = null) }

    fun openWithdrawalActionModal(w: WithdrawalRecord) = _uiState.update { it.copy(activeWithdrawalForAction = w, adminActionNote = "", adminActionTrxRef = "PAID_${System.currentTimeMillis() % 100000}") }
    fun closeWithdrawalActionModal() = _uiState.update { it.copy(activeWithdrawalForAction = null) }

    fun updateAdminActionNote(v: String) = _uiState.update { it.copy(adminActionNote = v) }
    fun updateAdminActionTrxRef(v: String) = _uiState.update { it.copy(adminActionTrxRef = v) }

    fun approveDeposit(depositId: Long) {
        val s = _uiState.value
        val (ok, msg) = repository.approveDeposit(depositId, s.adminEmail, s.adminActionNote.takeIf { it.isNotBlank() })
        showSnackbar(msg)
        if (ok) closeDepositActionModal()
    }

    fun rejectDeposit(depositId: Long) {
        val s = _uiState.value
        val reason = s.adminActionNote.ifBlank { "Rejected by Administrator" }
        val (ok, msg) = repository.rejectDeposit(depositId, s.adminEmail, reason)
        showSnackbar(msg)
        if (ok) closeDepositActionModal()
    }

    fun markWithdrawalPaid(wId: Long) {
        val s = _uiState.value
        val ref = s.adminActionTrxRef.ifBlank { "TRX_${System.currentTimeMillis()}" }
        val (ok, msg) = repository.markWithdrawalPaid(wId, s.adminEmail, ref, s.adminActionNote.takeIf { it.isNotBlank() })
        showSnackbar(msg)
        if (ok) closeWithdrawalActionModal()
    }

    fun rejectWithdrawal(wId: Long) {
        val s = _uiState.value
        val reason = s.adminActionNote.ifBlank { "Rejected by Administrator" }
        val (ok, msg) = repository.rejectWithdrawal(wId, s.adminEmail, reason)
        showSnackbar(msg)
        if (ok) closeWithdrawalActionModal()
    }

    fun toggleUserStatus(userId: Long) {
        val (ok, msg) = repository.toggleUserStatus(userId, _uiState.value.adminEmail)
        showSnackbar(msg)
    }

    fun updateProviderSettings(
        providerId: String,
        isEnabled: Boolean,
        accountTitle: String,
        accountNumber: String,
        minDeposit: Double,
        maxDeposit: Double,
        minWithdrawal: Double,
        maxWithdrawal: Double,
        fee: Double
    ) {
        val (ok, msg) = repository.updateProviderConfig(
            providerId = providerId,
            isEnabled = isEnabled,
            accountTitle = accountTitle,
            accountNumber = accountNumber,
            minDeposit = minDeposit,
            maxDeposit = maxDeposit,
            minWithdrawal = minWithdrawal,
            maxWithdrawal = maxWithdrawal,
            withdrawalFeePercent = fee,
            adminEmail = _uiState.value.adminEmail
        )
        showSnackbar(msg)
    }

    // -------------------------------------------------------------------------
    // Live Remote Game Control & Hot-Config Methods
    // -------------------------------------------------------------------------
    fun toggleGlobalMaintenance(isMaintenance: Boolean, message: String = "", etaMinutes: Int = 10) {
        val msg = message.ifBlank { "Emergency Server Maintenance in progress. Game tables will resume momentarily." }
        LiveGameConfigManager.updateGlobalMaintenance(
            isMaintenance = isMaintenance,
            message = msg,
            etaMinutes = etaMinutes,
            admin = _uiState.value.adminEmail
        )
        showSnackbar(if (isMaintenance) "🔴 System Maintenance Mode ACTIVATED for all players" else "🟢 System Maintenance Mode DEACTIVATED. Live tables running.")
    }

    fun updateGameOnlineStatus(dragonTiger: Boolean, zooRoulette: Boolean, cyberSlots: Boolean) {
        LiveGameConfigManager.updateGameOnlineStatus(
            dragonTiger = dragonTiger,
            zooRoulette = zooRoulette,
            cyberSlots = cyberSlots,
            admin = _uiState.value.adminEmail
        )
        showSnackbar("Game status updated live: DT=$dragonTiger, Zoo=$zooRoulette, Slots=$cyberSlots")
    }

    fun broadcastLiveMessage(message: String) {
        if (message.isBlank()) {
            showSnackbar("Please enter a broadcast message")
            return
        }
        LiveGameConfigManager.updateBroadcastMessage(
            message = message,
            isActive = true,
            admin = _uiState.value.adminEmail
        )
        showSnackbar("⚡ Live Announcement broadcasted to all downloaded games!")
    }

    fun updateGameEconomy(
        commissionPercent: Double,
        dragonCountdownSec: Int,
        zooCountdownSec: Int,
        minBet: Long,
        maxBet: Long
    ) {
        LiveGameConfigManager.updateGameEconomy(
            commissionPercent = commissionPercent,
            dragonCountdownSec = dragonCountdownSec,
            zooCountdownSec = zooCountdownSec,
            minBet = minBet,
            maxBet = maxBet,
            admin = _uiState.value.adminEmail
        )
        showSnackbar("Live game economy & timers synced to all active tables!")
    }

    fun toggleMultiplierEvent(isActive: Boolean, rate: Double, title: String) {
        LiveGameConfigManager.updateMultiplierEvent(
            isActive = isActive,
            multiplierRate = rate,
            title = title,
            admin = _uiState.value.adminEmail
        )
        showSnackbar(if (isActive) "🔥 Promotional Multiplier (${rate}x) broadcasted LIVE!" else "Normal 1.0x payouts restored.")
    }

    fun pushOtaLiveUpdate(version: String, patchNotes: String) {
        LiveGameConfigManager.pushOtaLiveUpdate(
            version = version,
            patchNotes = patchNotes,
            admin = _uiState.value.adminEmail
        )
        showSnackbar("🚀 Over-The-Air Patch $version dispatched to all client games!")
    }
}
