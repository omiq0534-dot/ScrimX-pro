import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

# Update ReferBannerCard
old_refer_banner = """fun ReferBannerCard(referralCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {"""

new_refer_banner = """fun ReferBannerCard(referralCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, AppColors.BorderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground)
    ) {"""
content = content.replace(old_refer_banner, new_refer_banner)

content = content.replace(
    'Text("REFER & EARN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)',
    'Text("REFER & EARN", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)'
)

# StatsCard
old_stats_card = """fun StatsCard(profile: UserProfile?) {
    val totalMatches = profile?.matchesPlayed ?: 0
    val totalWins = profile?.matchesWon ?: 0
    val totalKills = profile?.totalKills ?: 0
    val winRate = if (totalMatches > 0) "${(totalWins * 100) / totalMatches}%" else "0%"
    val kdRatio = if (totalMatches > 0) String.format("%.2f", totalKills.toFloat() / totalMatches) else "0.0"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161A25)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22283A))
    ) {"""

new_stats_card = """fun StatsCard(profile: UserProfile?) {
    val totalMatches = profile?.matchesPlayed ?: 0
    val totalWins = profile?.matchesWon ?: 0
    val totalKills = profile?.totalKills ?: 0
    val winRate = if (totalMatches > 0) "${(totalWins * 100) / totalMatches}%" else "0%"
    val kdRatio = if (totalMatches > 0) String.format("%.2f", totalKills.toFloat() / totalMatches) else "0.0"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderColor)
    ) {"""
content = content.replace(old_stats_card, new_stats_card)

# Divider inside stats card
content = content.replace(
    'HorizontalDivider(color = Color(0xFF1E293B))',
    'HorizontalDivider(color = AppColors.Divider)'
)

# Stat items
old_stat_item = """fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, color = Color(0xFFB0B0B0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}"""
new_stat_item = """fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = AppColors.TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, color = AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}"""
content = content.replace(old_stat_item, new_stat_item)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)

print("ReferBanner and StatsCard patched.")
