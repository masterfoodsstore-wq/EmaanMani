package com.example.model

/**
 * Data models for NexusGGR / FiversCan Casino API Aggregator
 * (https://github.com/casino-api007/FiversCan.git)
 */

data class FiversProvider(
    val code: String,
    val name: String,
    val status: Int = 1 // 1 = open, 0 = maintenance
)

data class FiversGame(
    val gameCode: String,
    val gameName: String,
    val providerCode: String,
    val banner: String = "",
    val status: Int = 1,
    val directLaunchUrl: String = ""
)

data class FiversBalances(
    val agentBalance: Double = 500000.0,
    val userBalance: Double = 0.0
)

data class FiversLaunchResult(
    val launchUrl: String,
    val status: Int = 1,
    val msg: String = "SUCCESS"
)

object FiversCanDefaults {
    val providers = listOf(
        FiversProvider(code = "PRAGMATIC", name = "Pragmatic Play", status = 1),
        FiversProvider(code = "PGSOFT", name = "PG Soft", status = 1),
        FiversProvider(code = "EVOLUTION", name = "Evolution Live", status = 1),
        FiversProvider(code = "SPRIBE", name = "Spribe", status = 1),
        FiversProvider(code = "HABANERO", name = "Habanero", status = 1),
        FiversProvider(code = "NOLIMIT", name = "NoLimit City", status = 1)
    )

    val games = listOf(
        // Pragmatic Play Slots
        FiversGame(
            gameCode = "vs20olympgate",
            gameName = "Gates of Olympus",
            providerCode = "PRAGMATIC",
            banner = "https://demogamesfree.pragmaticplay.net/gs2c/common/images/game_icons/vs20olympgate.png",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20olympgate&lang=en&cur=PKR"
        ),
        FiversGame(
            gameCode = "vs20sweetbonz",
            gameName = "Sweet Bonanza",
            providerCode = "PRAGMATIC",
            banner = "https://demogamesfree.pragmaticplay.net/gs2c/common/images/game_icons/vs20sweetbonz.png",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20sweetbonz&lang=en&cur=PKR"
        ),
        FiversGame(
            gameCode = "vs20doghouse",
            gameName = "The Dog House",
            providerCode = "PRAGMATIC",
            banner = "https://demogamesfree.pragmaticplay.net/gs2c/common/images/game_icons/vs20doghouse.png",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20doghouse&lang=en&cur=PKR"
        ),
        FiversGame(
            gameCode = "vs25wolfgold",
            gameName = "Wolf Gold",
            providerCode = "PRAGMATIC",
            banner = "https://demogamesfree.pragmaticplay.net/gs2c/common/images/game_icons/vs25wolfgold.png",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs25wolfgold&lang=en&cur=PKR"
        ),
        FiversGame(
            gameCode = "vs20starlight",
            gameName = "Starlight Princess",
            providerCode = "PRAGMATIC",
            banner = "https://demogamesfree.pragmaticplay.net/gs2c/common/images/game_icons/vs20starlight.png",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20starlight&lang=en&cur=PKR"
        ),
        FiversGame(
            gameCode = "vs10bbhas",
            gameName = "Big Bass Bonanza",
            providerCode = "PRAGMATIC",
            banner = "https://demogamesfree.pragmaticplay.net/gs2c/common/images/game_icons/vs10bbhas.png",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs10bbhas&lang=en&cur=PKR"
        ),

        // PG Soft Slots
        FiversGame(
            gameCode = "fortune-tiger",
            gameName = "Fortune Tiger",
            providerCode = "PGSOFT",
            banner = "https://m.pgsoft.com/games/fortune-tiger/icon.png",
            status = 1,
            directLaunchUrl = "https://m.pgsoft.com/games/fortune-tiger/index.html?language=en&btt=1"
        ),
        FiversGame(
            gameCode = "fortune-ox",
            gameName = "Fortune Ox",
            providerCode = "PGSOFT",
            banner = "https://m.pgsoft.com/games/fortune-ox/icon.png",
            status = 1,
            directLaunchUrl = "https://m.pgsoft.com/games/fortune-ox/index.html?language=en&btt=1"
        ),
        FiversGame(
            gameCode = "mahjong-ways-2",
            gameName = "Mahjong Ways 2",
            providerCode = "PGSOFT",
            banner = "https://m.pgsoft.com/games/mahjong-ways-2/icon.png",
            status = 1,
            directLaunchUrl = "https://m.pgsoft.com/games/mahjong-ways-2/index.html?language=en&btt=1"
        ),

        // Spribe Crash Games
        FiversGame(
            gameCode = "aviator",
            gameName = "Aviator Crash",
            providerCode = "SPRIBE",
            banner = "https://spribe.co/assets/img/games/aviator/aviator_banner.jpg",
            status = 1,
            directLaunchUrl = "https://spribe.co/games/aviator"
        ),
        FiversGame(
            gameCode = "mines",
            gameName = "Mines Grid",
            providerCode = "SPRIBE",
            banner = "https://spribe.co/assets/img/games/mines/mines_banner.jpg",
            status = 1,
            directLaunchUrl = "https://spribe.co/games/mines"
        ),

        // Evolution Live Tables
        FiversGame(
            gameCode = "lightningroulette",
            gameName = "Lightning Roulette",
            providerCode = "EVOLUTION",
            banner = "https://www.evolution.com/assets/Uploads/Lightning-Roulette-Thumbnail.jpg",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20olympgate&lang=en&cur=PKR"
        ),
        FiversGame(
            gameCode = "crazytime",
            gameName = "Crazy Time Live",
            providerCode = "EVOLUTION",
            banner = "https://www.evolution.com/assets/Uploads/Crazy-Time-Thumbnail.jpg",
            status = 1,
            directLaunchUrl = "https://demogamesfree.pragmaticplay.net/gs2c/openGame.do?gameSymbol=vs20sweetbonz&lang=en&cur=PKR"
        )
    )
}
