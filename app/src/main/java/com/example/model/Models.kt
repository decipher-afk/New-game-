package com.example.model

enum class MiniGameType(val displayName: String, val description: String) {
    CYBER_PONG("Neon Cyber Pong", "Hyper-speed paddle duel with spin curves and smash power-ups"),
    SNAKE_ARENA("Retro Snake Arena", "Multiplayer slither battle to trap opponents and eat glow orbs"),
    SPACE_BLASTER("Space Blaster Combat", "Top-down dogfight arcade with laser shields & hazard dodging"),
    REFLEX_DUEL("Tap Blitz Duel", "Micro-second reflex speed battle with streak combos")
}

data class UserProfile(
    val id: String = "u_player_1",
    val username: String = "CyberViper",
    val gamerTag: String = "#ARC-7729",
    val avatarEmoji: String = "⚡",
    val level: Int = 12,
    val xp: Int = 1840,
    val maxXp: Int = 2500,
    val coins: Int = 3450,
    val gems: Int = 120,
    val trophies: Int = 1420,
    val rankTier: String = "Platinum II",
    val matchesPlayed: Int = 68,
    val matchesWon: Int = 47,
    val favoriteGame: MiniGameType = MiniGameType.CYBER_PONG,
    val isMuted: Boolean = false,
    val muteRemainingMinutes: Int = 0,
    val isBanned: Boolean = false,
    val equippedPaddleSkin: String = "Neon Cyan Glow",
    val equippedTrail: String = "Plasma Sparkle",
    val equippedTitle: String = "Arena Striker"
) {
    val winRate: Int
        get() = if (matchesPlayed > 0) ((matchesWon.toDouble() / matchesPlayed) * 100).toInt() else 0
}

enum class RoomStatus {
    WAITING,
    STARTING,
    IN_PROGRESS,
    ENDED
}

data class LobbyRoom(
    val id: String,
    val code: String,
    val name: String,
    val gameType: MiniGameType,
    val maxPlayers: Int = 2,
    val currentPlayers: List<PlayerSlot> = emptyList(),
    val status: RoomStatus = RoomStatus.WAITING,
    val isPrivate: Boolean = false,
    val hostId: String,
    val hostName: String,
    val allowedSpectators: Boolean = true,
    val spectatorsCount: Int = 0,
    val pingMs: Int = 24
)

data class PlayerSlot(
    val id: String,
    val name: String,
    val avatar: String,
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    val ping: Int = 28,
    val isSpeaking: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String,
    val timestamp: String,
    val isSpectator: Boolean = false,
    val isSystem: Boolean = false
)

enum class FriendStatus(val label: String) {
    ONLINE("Online"),
    IN_MATCH("In Match"),
    IN_LOBBY("In Lobby"),
    OFFLINE("Offline")
}

data class Friend(
    val id: String,
    val name: String,
    val tag: String,
    val avatar: String,
    val status: FriendStatus,
    val currentActivity: String,
    val activeRoomId: String? = null,
    val trophies: Int = 1200
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardCoins: Int = 200,
    val rewardXp: Int = 150
)

enum class StoreCategory {
    PADDLE_SKIN,
    SNAKE_TRAIL,
    AVATAR,
    TITLE
}

data class StoreItem(
    val id: String,
    val title: String,
    val category: StoreCategory,
    val priceCoins: Int,
    val priceGems: Int,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false,
    val description: String,
    val iconEmoji: String
)

enum class ReportReason(val displayName: String) {
    HARASSMENT("Harassment / Toxic Chat"),
    CHEATING("Cheating / Exploiting"),
    INAPPROPRIATE_NAME("Inappropriate Name or Avatar"),
    GRIEFING_AFK("Griefing / AFK in Match")
}

enum class ReportStatus {
    PENDING,
    RESOLVED_WARNED,
    RESOLVED_MUTED,
    RESOLVED_BANNED,
    DISMISSED
}

data class ModerationReport(
    val id: String,
    val reporterId: String,
    val reporterName: String,
    val targetPlayerName: String,
    val reason: ReportReason,
    val details: String,
    val timestamp: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val matchOrLobby: String = "Lobby #ARC-9"
)

enum class ModerationActionType(val displayName: String) {
    WARNING("Issue Warning"),
    MUTE_1H("Mute (1 Hour)"),
    MUTE_24H("Mute (24 Hours)"),
    BAN("Ban Account"),
    DISMISS("Dismiss Report")
}

data class ModerationActionLog(
    val id: String,
    val targetPlayerName: String,
    val actionType: ModerationActionType,
    val reason: String,
    val moderatorNotes: String,
    val timestamp: String
)

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val actionPrompt: String,
    val isCompleted: Boolean = false
)
