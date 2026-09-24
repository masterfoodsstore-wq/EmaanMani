package com.example.model

import androidx.compose.ui.graphics.Color

enum class ZooCategory {
    BEAST,
    BIRD,
    SPECIAL
}

enum class ZooAnimal(
    val id: String,
    val displayName: String,
    val chineseName: String,
    val emoji: String,
    val category: ZooCategory,
    val baseMultiplier: Int,
    val tintColor: Color
) {
    MONKEY("monkey", "Monkey", "猴子", "🐒", ZooCategory.BEAST, 8, Color(0xFFFFA726)),
    RABBIT("rabbit", "Rabbit", "兔子", "🐇", ZooCategory.BEAST, 6, Color(0xFFFFD54F)),
    LION("lion", "Lion", "狮子", "🦁", ZooCategory.BEAST, 12, Color(0xFFFF9800)),
    PANDA("panda", "Panda", "熊猫", "🐼", ZooCategory.BEAST, 8, Color(0xFF81C784)),

    SWALLOW("swallow", "Swallow", "燕子", "🐦", ZooCategory.BIRD, 8, Color(0xFF4FC3F7)),
    PIGEON("pigeon", "Pigeon", "鸽子", "🕊️", ZooCategory.BIRD, 8, Color(0xFFE0E0E0)),
    PEACOCK("peacock", "Peacock", "孔雀", "🦚", ZooCategory.BIRD, 8, Color(0xFF26A69A)),
    EAGLE("eagle", "Eagle", "老鹰", "🦅", ZooCategory.BIRD, 12, Color(0xFFFF7043)),

    SHARK("shark", "Shark", "鲨鱼", "🦈", ZooCategory.SPECIAL, 24, Color(0xFF00E5FF)),
    GOLDEN_TOAD("toad", "Golden Toad", "金蟾", "🐸", ZooCategory.SPECIAL, 100, Color(0xFFFFD700)),
    WILD_CHEST("wild", "Wild Chest", "宝箱", "💎", ZooCategory.SPECIAL, 50, Color(0xFFFFD700))
}

enum class ZooBetTarget(
    val label: String,
    val chineseLabel: String,
    val multiplier: Int,
    val category: ZooCategory
) {
    MONKEY("Monkey", "猴子", 8, ZooCategory.BEAST),
    RABBIT("Rabbit", "兔子", 6, ZooCategory.BEAST),
    LION("Lion", "狮子", 12, ZooCategory.BEAST),
    PANDA("Panda", "熊猫", 8, ZooCategory.BEAST),
    BEAST_GENERAL("Beast", "走兽", 2, ZooCategory.BEAST),

    SWALLOW("Swallow", "燕子", 8, ZooCategory.BIRD),
    PIGEON("Pigeon", "鸽子", 8, ZooCategory.BIRD),
    PEACOCK("Peacock", "孔雀", 8, ZooCategory.BIRD),
    EAGLE("Eagle", "老鹰", 12, ZooCategory.BIRD),
    BIRD_GENERAL("Bird", "飞禽", 2, ZooCategory.BIRD),

    SHARK("Shark", "鲨鱼", 24, ZooCategory.SPECIAL),
    WILD("Wild", "宝箱", 50, ZooCategory.SPECIAL)
}

data class ZooTrackSlot(
    val index: Int,
    val animal: ZooAnimal,
    val specificMultiplier: Int = animal.baseMultiplier,
    val isGoldenBox: Boolean = false
) {
    val label: String
        get() = "${animal.displayName} x$specificMultiplier"
}

data class ZooPlayerBets(
    val monkey: Long = 0L,
    val rabbit: Long = 0L,
    val lion: Long = 0L,
    val panda: Long = 0L,
    val beastGeneral: Long = 0L,

    val swallow: Long = 0L,
    val pigeon: Long = 0L,
    val peacock: Long = 0L,
    val eagle: Long = 0L,
    val birdGeneral: Long = 0L,

    val shark: Long = 0L,
    val wild: Long = 0L
) {
    val total: Long
        get() = monkey + rabbit + lion + panda + beastGeneral +
                swallow + pigeon + peacock + eagle + birdGeneral +
                shark + wild

    fun getAmount(target: ZooBetTarget): Long = when (target) {
        ZooBetTarget.MONKEY -> monkey
        ZooBetTarget.RABBIT -> rabbit
        ZooBetTarget.LION -> lion
        ZooBetTarget.PANDA -> panda
        ZooBetTarget.BEAST_GENERAL -> beastGeneral
        ZooBetTarget.SWALLOW -> swallow
        ZooBetTarget.PIGEON -> pigeon
        ZooBetTarget.PEACOCK -> peacock
        ZooBetTarget.EAGLE -> eagle
        ZooBetTarget.BIRD_GENERAL -> birdGeneral
        ZooBetTarget.SHARK -> shark
        ZooBetTarget.WILD -> wild
    }

    fun hasBets(): Boolean = total > 0L
}

data class ZooHistoryItem(
    val id: Long,
    val winningSlot: Int,
    val winningAnimal: ZooAnimal,
    val multiplier: Int = winningAnimal.baseMultiplier,
    val netWin: Long = 0L,
    val feeDeducted: Long = 0L
)

enum class ZooGamePhase {
    BETTING,
    SPINNING,
    RESULT
}

/**
 * 26-Slot Continuous Perimeter Track (matching reference picture exactly):
 * - Top Row (9 tiles): Monkey, 3 Golden Rabbits, Golden Toad x100, 3 Swallows, Pigeon
 * - Bottom Row (10 tiles): 3 Lions, Shark x24, 3 Eagles, 3 Peacocks
 * - Left Column (7 tiles): 3 Monkeys, Wild Chest, 3 Pandas
 */
val ZOO_TRACK_SLOTS = listOf(
    // TOP ROW (9 tiles: 0..8)
    ZooTrackSlot(0, ZooAnimal.MONKEY, 8),
    ZooTrackSlot(1, ZooAnimal.RABBIT, 6, isGoldenBox = true),
    ZooTrackSlot(2, ZooAnimal.RABBIT, 6, isGoldenBox = true),
    ZooTrackSlot(3, ZooAnimal.RABBIT, 6, isGoldenBox = true),
    ZooTrackSlot(4, ZooAnimal.GOLDEN_TOAD, 100),
    ZooTrackSlot(5, ZooAnimal.SWALLOW, 8),
    ZooTrackSlot(6, ZooAnimal.SWALLOW, 8),
    ZooTrackSlot(7, ZooAnimal.SWALLOW, 8),
    ZooTrackSlot(8, ZooAnimal.PIGEON, 8),

    // BOTTOM ROW (10 tiles: 9..18)
    ZooTrackSlot(9, ZooAnimal.LION, 12),
    ZooTrackSlot(10, ZooAnimal.LION, 12),
    ZooTrackSlot(11, ZooAnimal.LION, 12),
    ZooTrackSlot(12, ZooAnimal.SHARK, 24),
    ZooTrackSlot(13, ZooAnimal.EAGLE, 12),
    ZooTrackSlot(14, ZooAnimal.EAGLE, 12),
    ZooTrackSlot(15, ZooAnimal.EAGLE, 12),
    ZooTrackSlot(16, ZooAnimal.PEACOCK, 8),
    ZooTrackSlot(17, ZooAnimal.PEACOCK, 8),
    ZooTrackSlot(18, ZooAnimal.PEACOCK, 8),

    // LEFT COLUMN (7 tiles: 19..25)
    ZooTrackSlot(19, ZooAnimal.MONKEY, 8),
    ZooTrackSlot(20, ZooAnimal.MONKEY, 8),
    ZooTrackSlot(21, ZooAnimal.MONKEY, 8),
    ZooTrackSlot(22, ZooAnimal.WILD_CHEST, 50),
    ZooTrackSlot(23, ZooAnimal.PANDA, 8),
    ZooTrackSlot(24, ZooAnimal.PANDA, 8),
    ZooTrackSlot(25, ZooAnimal.PANDA, 8)
)
