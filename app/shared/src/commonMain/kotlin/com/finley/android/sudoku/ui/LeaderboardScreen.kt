package com.finley.android.sudoku.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.sudoku.model.LeaderboardEntry
import com.finley.android.sudoku.network.NetworkService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var userRank by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val response = NetworkService.getLeaderboard()
        entries = response.entries
        
        // 如果已登录，尝试获取当前用户的排名
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
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Rankings Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Be the first one on the board!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        LeaderboardRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
fun MyRankBar(rank: Int?, username: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (rank != null) "#$rank" else "-",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(56.dp)
            )
            Text(
                text = "$username (You)",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Keep going!",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    val isCurrentUser = entry.username == NetworkService.currentUser?.username
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 4.dp else 0.dp),
        border = if (isCurrentUser) CardDefaults.outlinedCardBorder().copy(brush = SolidColor(MaterialTheme.colorScheme.primary)) else null,
        colors = CardDefaults.cardColors(
            containerColor = when (entry.rank) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.2f) // Gold
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f) // Silver
                3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Bronze
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                when (entry.rank) {
                    1 -> Text("🥇", fontSize = 24.sp)
                    2 -> Text("🥈", fontSize = 24.sp)
                    3 -> Text("🥉", fontSize = 24.sp)
                    else -> Text(
                        text = entry.rank.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username,
                    fontSize = 17.sp,
                    fontWeight = if (isCurrentUser) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Level ${entry.level}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.score.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "points",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

