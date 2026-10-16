package com.example.learning.model

enum class SsfBadgeTier(
    val tierName: String,
    val colorHex: Long,
    val borderHex: Long
) {
    BRONZE("SSF Level 1 (Scout)", 0xFFCD7F32, 0xFFFFE4B5),
    SILVER("SSF Level 2 (Defender)", 0xFFC0C0C0, 0xFFE0E0E0),
    GOLD("SSF Level 3 (Specialist)", 0xFFFFD700, 0xFFFFF8DC),
    PLATINUM("SSF Level 4 (Master)", 0xFFE5E4E2, 0xFFF0F8FF),
    UCLA_MASTER("UCLA Master of Science Graduate Degree", 0xFF2774AE, 0xFFFFD100)
}

data class Badge(
    val id: String,
    val code: String,
    val name: String,
    val tier: SsfBadgeTier,
    val description: String,
    val criteria: String,
    val iconName: String = "verified",
    val skillsValidated: List<String> = emptyList(),
    val institutionName: String = "Great Learning & SSF"
)

data class UserBadge(
    val badge: Badge,
    val unlocked: Boolean,
    val unlockedDate: String? = null,
    val scorePercentage: Int = 0,
    val credentialId: String? = null
)
