package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.model.FiversBalances
import com.example.model.FiversCanDefaults
import com.example.model.FiversGame
import com.example.model.FiversProvider
import com.example.repository.FiversCanClient
import com.example.repository.UserAccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FiversCanUiState(
    val providers: List<FiversProvider> = emptyList(),
    val selectedProvider: FiversProvider? = null,
    val games: List<FiversGame> = emptyList(),
    val userCode: String = "player_1001",
    val agentBalance: Double = 500000.0,
    val userBalance: Double = 500.0,
    val activeGameUrl: String? = null,
    val activeGameTitle: String? = null,
    val isLoading: Boolean = false,
    val isGamePlayerOpen: Boolean = false,
    val feedbackMessage: String? = null,
    val transferAmount: String = "100"
)

class FiversCanViewModel(application: Application) : AndroidViewModel(application) {

    val client = FiversCanClient(
        apiUrl = BuildConfig.RAPID_API_URL,
        agentCode = "royalx_agent",
        agentToken = BuildConfig.RAPID_API_KEY
    )
    private val userAccountRepo = UserAccountRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(FiversCanUiState())
    val uiState: StateFlow<FiversCanUiState> = _uiState.asStateFlow()

    init {
        // Derive player code and initial balance from local session
        val currentUser = userAccountRepo.currentUser.value
        val code = currentUser?.username ?: "player_${System.currentTimeMillis() % 10000}"
        val points = currentUser?.gamePoints ?: 2500L

        _uiState.update {
            it.copy(
                userCode = code,
                userBalance = points.toDouble(),
                agentBalance = 500000.0
            )
        }

        loadProviders()
    }

    fun loadProviders() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val list = mutableListOf<FiversProvider>()
            try {
                val res = client.providerList()
                val arr = res.optJSONArray("providers")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        list.add(
                            FiversProvider(
                                code = p.getString("code"),
                                name = p.optString("name", p.getString("code")),
                                status = p.optInt("status", 1)
                            )
                        )
                    }
                }
            } catch (_: Exception) {}

            val finalProvs = if (list.isNotEmpty()) list else FiversCanDefaults.providers
            val first = finalProvs.firstOrNull { it.status == 1 } ?: finalProvs.firstOrNull()

            _uiState.update {
                it.copy(
                    providers = finalProvs,
                    selectedProvider = first,
                    isLoading = false
                )
            }
            if (first != null) {
                selectProvider(first)
            }
        }
    }

    fun selectProvider(provider: FiversProvider) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(selectedProvider = provider, isLoading = true) }
            val list = mutableListOf<FiversGame>()
            try {
                val res = client.gameList(provider.code)
                val arr = res.optJSONArray("games")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val g = arr.getJSONObject(i)
                        list.add(
                            FiversGame(
                                gameCode = g.getString("game_code"),
                                gameName = g.optString("game_name", g.getString("game_code")),
                                providerCode = provider.code,
                                banner = g.optString("banner", ""),
                                status = g.optInt("status", 1)
                            )
                        )
                    }
                }
            } catch (_: Exception) {}

            val finalGames = if (list.isNotEmpty()) {
                list
            } else {
                FiversCanDefaults.games.filter { it.providerCode.equals(provider.code, ignoreCase = true) }
            }
            _uiState.update { it.copy(games = finalGames, isLoading = false) }
        }
    }

    fun launchGame(game: FiversGame) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, feedbackMessage = "Launching ${game.gameName}...") }
            var launchUrl = game.directLaunchUrl
            try {
                val res = client.gameLaunch(
                    userCode = _uiState.value.userCode,
                    providerCode = game.providerCode,
                    gameCode = game.gameCode
                )
                val serverUrl = res.optString("launch_url", "")
                if (serverUrl.isNotBlank()) {
                    launchUrl = serverUrl
                }
            } catch (_: Exception) {}

            if (launchUrl.isBlank()) {
                launchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20olympgate&lang=en&cur=PKR"
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeGameUrl = launchUrl,
                    activeGameTitle = game.gameName,
                    isGamePlayerOpen = true,
                    feedbackMessage = null
                )
            }
        }
    }

    fun closeGamePlayer() {
        _uiState.update {
            it.copy(
                isGamePlayerOpen = false,
                activeGameUrl = null,
                activeGameTitle = null
            )
        }
        // Balance moved while player was in the game — refresh
        refreshBalance()
    }

    fun depositFunds(amount: Double) {
        if (amount <= 0) return
        val currentPts = userAccountRepo.currentUser.value?.gamePoints ?: 0L
        if (currentPts < amount.toLong()) {
            _uiState.update { it.copy(feedbackMessage = "Insufficient balance in app wallet.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            userAccountRepo.adjustPoints(-amount.toLong())
            try {
                client.userDeposit(_uiState.value.userCode, amount, "dep_" + System.currentTimeMillis())
            } catch (_: Exception) {}
            _uiState.update {
                it.copy(
                    userBalance = it.userBalance + amount,
                    agentBalance = (it.agentBalance - amount).coerceAtLeast(0.0),
                    feedbackMessage = "Deposit successful: +PKR $amount added to game session."
                )
            }
        }
    }

    fun withdrawFunds(amount: Double) {
        if (amount <= 0) return
        if (_uiState.value.userBalance < amount) {
            _uiState.update { it.copy(feedbackMessage = "Cannot withdraw more than current game balance.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            userAccountRepo.adjustPoints(amount.toLong())
            try {
                client.userWithdraw(_uiState.value.userCode, amount, "wd_" + System.currentTimeMillis())
            } catch (_: Exception) {}
            _uiState.update {
                it.copy(
                    userBalance = (it.userBalance - amount).coerceAtLeast(0.0),
                    agentBalance = it.agentBalance + amount,
                    feedbackMessage = "Withdrawal successful: PKR $amount cashed out to app wallet."
                )
            }
        }
    }

    fun refreshBalance() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = client.moneyInfo(_uiState.value.userCode)
                val agentBal = res.optJSONObject("agent")?.optDouble("balance", _uiState.value.agentBalance)
                    ?: _uiState.value.agentBalance
                val userBal = res.optJSONObject("user")?.optDouble("balance", _uiState.value.userBalance)
                    ?: _uiState.value.userBalance
                _uiState.update {
                    it.copy(
                        agentBalance = agentBal,
                        userBalance = if (userBal > 0) userBal else it.userBalance
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun setTransferAmount(str: String) {
        _uiState.update { it.copy(transferAmount = str) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
