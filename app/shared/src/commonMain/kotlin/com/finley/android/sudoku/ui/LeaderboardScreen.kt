package com.finley.android.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.sudoku.model.LeaderboardEntry
import com.finley.android.sudoku.network.NetworkService
import com.finley.android.sudoku.ui.components.AppScreenBackground
import com.finley.android.sudoku.ui.components.ShimmerBox
import com.finley.android.sudoku.ui.components.pressScale
import com.finley.android.sudoku.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var userRank by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val strings = LocalAppStrings.current

    LaunchedEffect(Unit) {
        val response = NetworkService.getLeaderboard()
        entries = response.entries

        if (NetworkService.currentUser != null) {
            val username = NetworkService.currentUser?.username ?: ""
            userRank = entries.find { it.username == username }?.rank
        }

        isLoading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.leaderboardTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent,
        bottomBar = {
            if (!isLoading && NetworkService.currentUser != null) {
                MyRankBar(userRank, NetworkService.currentUser?.username ?: "")
            }
        }
    ) { padding ->
        AppScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerBox(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        repeat(6) {
                            ShimmerBox(
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                entries.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(88.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = strings.noRankingYet,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.beFirstOnBoard,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            LeaderboardPodium(entries.take(3))
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        items(
                            items = entries.drop(3),
                            key = { it.rank.toString() + it.username }
                        ) { entry ->
                            LeaderboardRow(
                                entry = entry,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPodium(topEntries: List<LeaderboardEntry>) {
    val strings = LocalAppStrings.current
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = strings.topThree,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Layout: [2nd, 1st, 3rd] so 1st is centered and tallest
            val first = topEntries.getOrNull(0)
            val second = topEntries.getOrNull(1)
            val third = topEntries.getOrNull(2)

            PodiumSlot(
                entry = second,
                place = 2,
                height = 96.dp,
                modifier = Modifier.weight(1f)
            )
            PodiumSlot(
                entry = first,
                place = 1,
                height = 116.dp,
                modifier = Modifier.weight(1f)
            )
            PodiumSlot(
                entry = third,
                place = 3,
                height = 88.dp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry?,
    place: Int,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val medal = when (place) {
        1 -> "🥇"
        2 -> "🥈"
        else -> "🥉"
    }
    val bg = when (place) {
        1 -> colorScheme.primaryContainer.copy(alpha = 0.9f)
        2 -> colorScheme.surface.copy(alpha = 0.9f)
        else -> colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .then(Modifier.height(height)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (entry == null) {
            Text(
                text = "—",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        } else {
            Text(medal, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.username,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.score.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (place == 1) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MyRankBar(rank: Int?, username: String) {
    val strings = LocalAppStrings.current
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (rank != null) strings.topGlobal(rank) else strings.notOnBoard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.me(username),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = if (rank != null) strings.topGlobal(rank) else strings.notOnBoard,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = strings.keepGoing,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isCurrentUser = entry.username == NetworkService.currentUser?.username

    val container = when (entry.rank) {
        1 -> colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        2 -> colorScheme.surfaceVariant.copy(alpha = 0.6f)
        3 -> colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> colorScheme.surface.copy(alpha = 0.85f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .then(
                if (isCurrentUser) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .pressScale()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            when (entry.rank) {
                1 -> Text("🥇", fontSize = 22.sp)
                2 -> Text("🥈", fontSize = 22.sp)
                3 -> Text("🥉", fontSize = 22.sp)
                else -> Text(
                    text = entry.rank.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.levelEntry(entry.level),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = "关卡 ${entry.level}",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = entry.score.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.primary
            )
            Text(
                text = "分",
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

