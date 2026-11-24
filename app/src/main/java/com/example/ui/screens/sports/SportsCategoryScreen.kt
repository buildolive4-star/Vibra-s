package com.example.ui.screens.sports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatus
import com.example.data.model.SportCategory
import com.example.ui.components.Glass3DCard
import com.example.ui.components.SportPillBadge
import com.example.ui.screens.home.LiveMatchCard
import com.example.ui.screens.home.UpcomingMatchCard
import com.example.ui.theme.LocalBetZoneColors

@Composable
fun SportsCategoryScreen(
    matches: List<MatchItem>,
    selectedSport: SportCategory?,
    selectedStatus: MatchStatus?,
    selectedTournament: String?,
    searchQuery: String,
    favoriteTeams: List<String>,
    onSelectSport: (SportCategory?) -> Unit,
    onSelectStatus: (MatchStatus?) -> Unit,
    onSelectTournament: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleFavoriteTeam: (String) -> Unit,
    onOpenMatchDetails: (String) -> Unit,
    onOpenLiveCentre: (String) -> Unit
) {
    val ext = LocalBetZoneColors.current

    val tournaments = matches
        .filter { selectedSport == null || it.sport == selectedSport }
        .map { it.tournament }
        .distinct()

    val filteredMatches = matches.filter { match ->
        val sportOk = selectedSport == null || match.sport == selectedSport
        val statusOk = selectedStatus == null || match.status == selectedStatus
        val tournamentOk = selectedTournament == null || match.tournament == selectedTournament
        val q = searchQuery.trim().lowercase()
        val searchOk = q.isEmpty() ||
            match.homeTeam.name.lowercase().contains(q) ||
            match.awayTeam.name.lowercase().contains(q) ||
            match.tournament.lowercase().contains(q) ||
            match.sport.label.lowercase().contains(q)
        sportOk && statusOk && tournamentOk && searchOk
    }

    val displayedTeams = filteredMatches
        .flatMap { listOf(it.homeTeam.name, it.awayTeam.name) }
        .distinct()
        .take(8)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sport title & 3D illustration banner
        item {
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.electricBlue
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_betzone_onboard_universe),
                        contentDescription = "Sports Category Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF050814).copy(alpha = 0.88f), Color.Transparent)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = selectedSport?.label ?: "All Sports Arenas",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${filteredMatches.size} Fixtures • Live, Upcoming & Results",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.neonGreen
                        )
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sports_search_input"),
                placeholder = { Text("Search teams, tournaments, or sports...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Sport horizontal pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Surface(
                        modifier = Modifier.testTag("sport_pill_all"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedSport == null) ext.electricBlue else ext.glassSurface,
                        border = BorderStroke(1.dp, if (selectedSport == null) ext.electricBlue else ext.glassBorder),
                        onClick = { onSelectSport(null) }
                    ) {
                        Text(
                            text = "All Sports",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedSport == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(SportCategory.entries) { sport ->
                    SportPillBadge(
                        sport = sport,
                        selected = selectedSport == sport,
                        onClick = { onSelectSport(if (selectedSport == sport) null else sport) }
                    )
                }
            }
        }

        // Live / Upcoming / Completed filter tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf(null to "All", MatchStatus.LIVE to "Live", MatchStatus.UPCOMING to "Upcoming", MatchStatus.COMPLETED to "Completed")
                options.forEach { (status, label) ->
                    val selected = selectedStatus == status
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectStatus(status) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("status_filter_${label.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ext.neonGreen.copy(alpha = 0.22f),
                            selectedLabelColor = ext.neonGreen
                        )
                    )
                }
            }
        }

        // Tournament filter chips
        if (tournaments.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTournament == null,
                            onClick = { onSelectTournament(null) },
                            label = { Text("All Tournaments") }
                        )
                    }
                    items(tournaments) { tourney ->
                        FilterChip(
                            selected = selectedTournament == tourney,
                            onClick = { onSelectTournament(if (selectedTournament == tourney) null else tourney) },
                            label = { Text(tourney) }
                        )
                    }
                }
            }
        }

        // Favourite Teams toggle bar
        if (displayedTeams.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Favourite Teams",
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.subtleText
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayedTeams) { team ->
                            val isFav = favoriteTeams.contains(team)
                            FilterChip(
                                selected = isFav,
                                onClick = { onToggleFavoriteTeam(team) },
                                label = { Text(team) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Favourite $team",
                                        tint = if (isFav) ext.goldenYellow else ext.subtleText
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (filteredMatches.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No fixtures match these filters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Try clearing the search query or switching to All Sports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.subtleText
                    )
                }
            }
        } else {
            items(filteredMatches, key = { it.id }) { match ->
                if (match.status == MatchStatus.LIVE) {
                    LiveMatchCard(
                        match = match,
                        onDetailsClick = { onOpenMatchDetails(match.id) },
                        onLiveCentreClick = { onOpenLiveCentre(match.id) }
                    )
                } else {
                    UpcomingMatchCard(
                        match = match,
                        onClick = { onOpenMatchDetails(match.id) }
                    )
                }
            }
        }
    }
}
