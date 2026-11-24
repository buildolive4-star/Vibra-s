package com.example.data.mock

import com.example.data.model.BadgeInfo
import com.example.data.model.CommentaryItem
import com.example.data.model.HeadToHeadItem
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LineupPlayer
import com.example.data.model.MarketOption
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatRow
import com.example.data.model.MatchStatus
import com.example.data.model.PredictionMarket
import com.example.data.model.QuizQuestion
import com.example.data.model.SportCategory
import com.example.data.model.TeamSummary
import com.example.data.model.TournamentInfo
import com.example.data.model.WeeklyChallenge

object MockSportsData {

    val popularTournaments: List<TournamentInfo> = listOf(
        TournamentInfo("ipl", "IPL", SportCategory.CRICKET, "IPL", 4, 0xFF00A3FF),
        TournamentInfo("icc", "ICC Cricket", SportCategory.CRICKET, "ICC", 3, 0xFF00E676),
        TournamentInfo("fifa", "FIFA", SportCategory.FOOTBALL, "FIFA", 5, 0xFFFFD600),
        TournamentInfo("ucl", "UEFA Champions League", SportCategory.FOOTBALL, "UCL", 4, 0xFF2962FF),
        TournamentInfo("nba", "NBA", SportCategory.BASKETBALL, "NBA", 3, 0xFFFF6D00),
        TournamentInfo("wim", "Wimbledon", SportCategory.TENNIS, "WIM", 2, 0xFF00E676)
    )

    val allKnownTeams: List<String> = listOf(
        "Mumbai Titans", "Chennai Kings", "India Blues", "Australia Gold",
        "Real Madrid", "Man City", "Brazil Stars", "Argentina Albiceleste",
        "LA Lakers", "Boston Celtics", "Carlos Alcaraz", "Jannik Sinner",
        "Team Falcons", "Natus Vincere"
    )

    val badgesCatalog: List<BadgeInfo> = listOf(
        BadgeInfo("ROOKIE_PREDICTOR", "Arena Rookie", "Join 777Bet and enter the prediction arena.", 0),
        BadgeInfo("FIRST_WIN", "Sharp Eye", "Lock in 1st accurate virtual-points call.", 1),
        BadgeInfo("HAT_TRICK", "Streak Strategist", "Submit 3 predictions across live sports.", 3),
        BadgeInfo("CENTURION", "Grand Analyst", "Reach 10 predictions with high consistency.", 10),
        BadgeInfo("QUIZ_MASTER", "Sports Scholar", "Complete a 777Bet trivia challenge.", 0)
    )

    val weeklyChallenges: List<WeeklyChallenge> = listOf(
        WeeklyChallenge("wc_live_3", "Live Pulse Trio", "Submit 3 virtual predictions on any live or upcoming fixture.", 3, 450),
        WeeklyChallenge("wc_multi_5", "All-Rounder Analyst", "Complete 5 predictions across Football, Cricket, or NBA.", 5, 800),
        WeeklyChallenge("wc_win_2", "Double Precision", "Score 2 accurate predictions this week.", 2, 600)
    )

    val sportsQuizzes: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "quiz_cricket_1",
            sport = "Cricket",
            question = "How many overs are bowled per side in a standard IPL T20 match?",
            options = listOf("15 overs", "20 overs", "25 overs", "50 overs"),
            correctIndex = 1,
            bonusPoints = 200
        ),
        QuizQuestion(
            id = "quiz_football_1",
            sport = "Football",
            question = "How many minutes of regular time make up a standard FIFA or UEFA Champions League match?",
            options = listOf("80 minutes", "90 minutes", "100 minutes", "120 minutes"),
            correctIndex = 1,
            bonusPoints = 200
        ),
        QuizQuestion(
            id = "quiz_tennis_1",
            sport = "Tennis",
            question = "Which Grand Slam tournament is traditionally played on grass courts?",
            options = listOf("Roland Garros", "US Open", "Wimbledon", "Australian Open"),
            correctIndex = 2,
            bonusPoints = 200
        ),
        QuizQuestion(
            id = "quiz_nba_1",
            sport = "Basketball",
            question = "How long is the standard shot clock per possession in the NBA?",
            options = listOf("18 seconds", "24 seconds", "30 seconds", "35 seconds"),
            correctIndex = 1,
            bonusPoints = 200
        ),
        QuizQuestion(
            id = "quiz_esports_1",
            sport = "Esports",
            question = "How many active players compete on each side in a standard tactical 5v5 Esports match?",
            options = listOf("3", "4", "5", "6"),
            correctIndex = 2,
            bonusPoints = 200
        )
    )

    fun seededRivals(): List<LeaderboardEntry> = listOf(
        LeaderboardEntry(
            userId = "rival_arjun", displayName = "ArjunVortex", avatarKey = "electric_wolf",
            virtualPoints = 9450, totalPredictions = 42, correctPredictions = 34, accuracyPct = 81,
            badges = listOf("ROOKIE_PREDICTOR", "HAT_TRICK", "CENTURION"), periodBucket = "ALL"
        ),
        LeaderboardEntry(
            userId = "rival_elena", displayName = "ElenaStrike", avatarKey = "neon_phoenix",
            virtualPoints = 8120, totalPredictions = 37, correctPredictions = 29, accuracyPct = 78,
            badges = listOf("ROOKIE_PREDICTOR", "HAT_TRICK"), periodBucket = "ALL"
        ),
        LeaderboardEntry(
            userId = "rival_marco", displayName = "MarcoTactix", avatarKey = "gold_crown",
            virtualPoints = 7380, totalPredictions = 31, correctPredictions = 23, accuracyPct = 74,
            badges = listOf("ROOKIE_PREDICTOR", "FIRST_WIN"), periodBucket = "ALL"
        ),
        LeaderboardEntry(
            userId = "rival_kiran", displayName = "KiranSixer", avatarKey = "cobalt_falcon",
            virtualPoints = 6210, totalPredictions = 28, correctPredictions = 20, accuracyPct = 71,
            badges = listOf("ROOKIE_PREDICTOR"), periodBucket = "ALL"
        ),
        LeaderboardEntry(
            userId = "rival_sana", displayName = "SanaAce99", avatarKey = "cyber_lion",
            virtualPoints = 4890, totalPredictions = 19, correctPredictions = 13, accuracyPct = 68,
            badges = listOf("ROOKIE_PREDICTOR"), periodBucket = "ALL"
        )
    )

    val initialMatches: List<MatchItem> = listOf(
        MatchItem(
            id = "ipl_mi_csk_01",
            sport = SportCategory.CRICKET,
            tournament = "IPL",
            status = MatchStatus.LIVE,
            venue = "Wankhede Arena, Mumbai",
            timerLabel = "16.4 OV",
            countdownSeconds = 0L,
            momentumHome = 0.62f,
            summaryNote = "Mumbai Titans need 34 runs off 20 balls",
            homeTeam = TeamSummary("Mumbai Titans", "MI", "168/4", "(16.4 ov)", 0xFF00A3FF, 0xFF0047AB),
            awayTeam = TeamSummary("Chennai Kings", "CSK", "201/5", "(20.0 ov)", 0xFFFFD600, 0xFFB28900),
            stats = listOf(
                MatchStatRow("Current Run Rate", "10.08", "10.05", 0.50f),
                MatchStatRow("Boundaries (4s/6s)", "19", "21", 0.47f),
                MatchStatRow("Dot Balls", "24", "29", 0.45f),
                MatchStatRow("Powerplay Score", "64/1", "58/2", 0.54f)
            ),
            commentary = listOf(
                CommentaryItem("16.4", "SIX! Massive over long-on", "Surya Sharma launches a crisp lofted drive deep into the top tier!", true),
                CommentaryItem("16.3", "1 Run", "Quick single clipped to deep mid-wicket.", false),
                CommentaryItem("16.2", "FOUR! Pierces cover", "Cracking timing off the back foot past point.", true),
                CommentaryItem("16.1", "Dot ball", "Sharp yorker on middle stump, dug out solidly.", false)
            ),
            lineups = listOf(
                LineupPlayer("Surya Sharma", "Batter", "MI", "9.2", "68* (31)"),
                LineupPlayer("Rohit Verma", "Captain", "MI", "8.5", "44 (25)"),
                LineupPlayer("Ruturaj Dev", "Opener", "CSK", "8.9", "79 (46)"),
                LineupPlayer("Matheesha Rao", "Pacer", "CSK", "8.3", "2/29 (3.4)")
            ),
            headToHead = listOf(
                HeadToHeadItem("Apr 2026", "MI 194/4 vs CSK 191/6", "Mumbai Titans"),
                HeadToHeadItem("May 2025", "CSK 182/5 vs MI 176/8", "Chennai Kings"),
                HeadToHeadItem("Mar 2025", "MI 212/3 vs CSK 198/7", "Mumbai Titans")
            ),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "Mumbai Titans", 172), MarketOption("o2", "Chennai Kings", 215))),
                PredictionMarket("m2", "Total Runs (2nd Innings)", listOf(MarketOption("o3", "Over 198.5 Runs", 185), MarketOption("o4", "Under 198.5 Runs", 195))),
                PredictionMarket("m3", "Player Performance", listOf(MarketOption("o5", "Surya Sharma 75+ Runs", 210), MarketOption("o6", "Rao 3+ Wickets", 240)))
            )
        ),
        MatchItem(
            id = "ucl_rm_mci_02",
            sport = SportCategory.FOOTBALL,
            tournament = "UEFA Champions League",
            status = MatchStatus.LIVE,
            venue = "Santiago Bernabeu",
            timerLabel = "72:18",
            countdownSeconds = 0L,
            momentumHome = 0.56f,
            summaryNote = "End-to-end quarterfinal battle under floodlights",
            homeTeam = TeamSummary("Real Madrid", "RMA", "2", "Shots: 14", 0xFF2962FF, 0xFFFFD600),
            awayTeam = TeamSummary("Man City", "MCI", "2", "Shots: 12", 0xFF00A3FF, 0xFF152247),
            stats = listOf(
                MatchStatRow("Possession", "44%", "56%", 0.44f),
                MatchStatRow("Shots on Target", "7", "6", 0.54f),
                MatchStatRow("Expected Goals (xG)", "2.18", "1.94", 0.53f),
                MatchStatRow("Corners", "6", "5", 0.55f)
            ),
            commentary = listOf(
                CommentaryItem("72'", "CLOSE! Hit the woodwork", "Vinicius Jr cuts inside and curls an effort off the far post!", true),
                CommentaryItem("67'", "GOAL! 2-2 Equalizer", "Phil Foden drills a low left-footed rocket from 20 yards.", true),
                CommentaryItem("58'", "Yellow Card", "Rodri cautioned for tactical midfield challenge.", false),
                CommentaryItem("49'", "GOAL! Real Madrid 2-1", "Jude Bellingham finishes a sweeping counterattack.", true)
            ),
            lineups = listOf(
                LineupPlayer("Jude Bellingham", "CAM", "RMA", "8.8", "1G • 91% Pass"),
                LineupPlayer("Vinicius Jr", "LW", "RMA", "8.6", "1A • 6 Dribbles"),
                LineupPlayer("Erling Haaland", "ST", "MCI", "8.4", "1G • 4 Shots"),
                LineupPlayer("Phil Foden", "RW", "MCI", "8.9", "1G • 3 Key Passes")
            ),
            headToHead = listOf(
                HeadToHeadItem("Apr 2025", "Real Madrid 3 - 3 Man City", "Draw"),
                HeadToHeadItem("May 2024", "Man City 1 - 1 Real Madrid", "Real Madrid (Pen)"),
                HeadToHeadItem("May 2023", "Man City 4 - 0 Real Madrid", "Man City")
            ),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "Real Madrid", 230), MarketOption("o2", "Draw", 265), MarketOption("o3", "Man City", 240))),
                PredictionMarket("m2", "Total Goals", listOf(MarketOption("o4", "Over 4.5 Goals", 190), MarketOption("o5", "Under 4.5 Goals", 188))),
                PredictionMarket("m3", "First Team to Score Next", listOf(MarketOption("o6", "Real Madrid", 195), MarketOption("o7", "Man City", 205)))
            )
        ),
        MatchItem(
            id = "nba_lal_bos_03",
            sport = SportCategory.BASKETBALL,
            tournament = "NBA",
            status = MatchStatus.LIVE,
            venue = "Crypto.com Arena",
            timerLabel = "Q3 04:22",
            countdownSeconds = 0L,
            momentumHome = 0.51f,
            summaryNote = "Fast-paced 3rd quarter perimeter shootout",
            homeTeam = TeamSummary("LA Lakers", "LAL", "84", "3PT: 42%", 0xFFFFD600, 0xFF6A1B9A),
            awayTeam = TeamSummary("Boston Celtics", "BOS", "81", "3PT: 39%", 0xFF00E676, 0xFF004D40),
            stats = listOf(
                MatchStatRow("Field Goal %", "49%", "47%", 0.51f),
                MatchStatRow("Rebounds", "34", "31", 0.52f),
                MatchStatRow("Assists", "22", "20", 0.52f),
                MatchStatRow("Fast Break Pts", "18", "14", 0.56f)
            ),
            commentary = listOf(
                CommentaryItem("Q3 04:22", "3-POINTER! Lakers lead by 3", "Austin Reaves drains a deep corner triple off the kickout.", true),
                CommentaryItem("Q3 05:01", "Alley-Oop Dunk", "Anthony Davis slams home the lob in traffic.", true),
                CommentaryItem("Q3 05:44", "Timeout Celtics", "Boston regroups after a 7-0 Lakers burst.", false)
            ),
            lineups = listOf(
                LineupPlayer("LeBron James", "SF", "LAL", "9.1", "24 PTS • 8 AST"),
                LineupPlayer("Anthony Davis", "C", "LAL", "8.9", "21 PTS • 11 REB"),
                LineupPlayer("Jayson Tatum", "PF", "BOS", "9.0", "26 PTS • 7 REB"),
                LineupPlayer("Jaylen Brown", "SG", "BOS", "8.4", "19 PTS • 2 STL")
            ),
            headToHead = listOf(
                HeadToHeadItem("Feb 2026", "Lakers 114 - 108 Celtics", "LA Lakers"),
                HeadToHeadItem("Dec 2025", "Celtics 121 - 115 Lakers", "Boston Celtics")
            ),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "LA Lakers", 178), MarketOption("o2", "Boston Celtics", 202))),
                PredictionMarket("m2", "Total Points", listOf(MarketOption("o3", "Over 224.5", 190), MarketOption("o4", "Under 224.5", 190))),
                PredictionMarket("m3", "Player Performance", listOf(MarketOption("o5", "LeBron 30+ Points", 215), MarketOption("o6", "Tatum 32+ Points", 225)))
            )
        ),
        MatchItem(
            id = "icc_ind_aus_04",
            sport = SportCategory.CRICKET,
            tournament = "ICC Cricket",
            status = MatchStatus.UPCOMING,
            venue = "Melbourne Cricket Ground",
            timerLabel = "Today • 14:00",
            countdownSeconds = 4520L,
            momentumHome = 0.55f,
            summaryNote = "ODI Championship Super Clash",
            homeTeam = TeamSummary("India Blues", "IND", "VS", "Rank #1", 0xFF00A3FF, 0xFF1565C0),
            awayTeam = TeamSummary("Australia Gold", "AUS", "VS", "Rank #2", 0xFFFFD600, 0xFF00E676),
            stats = listOf(
                MatchStatRow("Last 5 Wins", "4", "4", 0.50f),
                MatchStatRow("Avg 1st Inn", "312", "298", 0.51f)
            ),
            commentary = listOf(
                CommentaryItem("PRE", "Pitch Report", "True bounce surface ideal for stroke-makers and high-pace seam.", false)
            ),
            lineups = listOf(
                LineupPlayer("Virat Kapoor", "Batter", "IND", "9.3", "Avg 58.2"),
                LineupPlayer("Jasprit Sen", "Pacer", "IND", "9.4", "Econ 4.6"),
                LineupPlayer("Pat Mitchell", "Captain", "AUS", "8.9", "Avg 23.1"),
                LineupPlayer("Travis Marsh", "Opener", "AUS", "8.8", "SR 118.4")
            ),
            headToHead = listOf(
                HeadToHeadItem("Mar 2026", "IND 289/6 vs AUS 272/9", "India Blues"),
                HeadToHeadItem("Nov 2025", "AUS 305/7 vs IND 299/8", "Australia Gold")
            ),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "India Blues", 180), MarketOption("o2", "Australia Gold", 200))),
                PredictionMarket("m2", "Total Runs (1st Innings)", listOf(MarketOption("o3", "Over 295.5 Runs", 188), MarketOption("o4", "Under 295.5 Runs", 192)))
            )
        ),
        MatchItem(
            id = "fifa_bra_arg_05",
            sport = SportCategory.FOOTBALL,
            tournament = "FIFA",
            status = MatchStatus.UPCOMING,
            venue = "Maracana Stadium",
            timerLabel = "Tonight • 20:30",
            countdownSeconds = 9840L,
            momentumHome = 0.50f,
            summaryNote = "South American Superclasico Qualifier",
            homeTeam = TeamSummary("Brazil Stars", "BRA", "VS", "FIFA #3", 0xFFFFD600, 0xFF00E676),
            awayTeam = TeamSummary("Argentina Albiceleste", "ARG", "VS", "FIFA #1", 0xFF00A3FF, 0xFFFFFFFF),
            stats = listOf(
                MatchStatRow("Recent Form", "W-W-D-W", "W-W-W-D", 0.48f)
            ),
            commentary = listOf(
                CommentaryItem("PRE", "Lineup Watch", "Both nations expected to field full-strength attacking trios.", false)
            ),
            lineups = listOf(
                LineupPlayer("Rodrygo Silva", "FW", "BRA", "8.7", "6G in Qualifiers"),
                LineupPlayer("Lautaro Alvarez", "ST", "ARG", "8.9", "8G in Qualifiers")
            ),
            headToHead = listOf(
                HeadToHeadItem("Nov 2025", "Brazil 0 - 1 Argentina", "Argentina Albiceleste")
            ),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "Brazil Stars", 235), MarketOption("o2", "Draw", 290), MarketOption("o3", "Argentina Albiceleste", 220))),
                PredictionMarket("m2", "First Team to Score", listOf(MarketOption("o4", "Brazil Stars", 195), MarketOption("o5", "Argentina Albiceleste", 185)))
            )
        ),
        MatchItem(
            id = "wim_alc_sin_06",
            sport = SportCategory.TENNIS,
            tournament = "Wimbledon",
            status = MatchStatus.UPCOMING,
            venue = "Centre Court, London",
            timerLabel = "Tomorrow • 16:00",
            countdownSeconds = 18600L,
            momentumHome = 0.52f,
            summaryNote = "Men's Singles Semifinal Showcase",
            homeTeam = TeamSummary("Carlos Alcaraz", "ALC", "VS", "Seed #1", 0xFF00E676, 0xFF00A3FF),
            awayTeam = TeamSummary("Jannik Sinner", "SIN", "VS", "Seed #2", 0xFFFFD600, 0xFF2962FF),
            stats = listOf(
                MatchStatRow("1st Serve Won", "78%", "80%", 0.49f)
            ),
            commentary = listOf(
                CommentaryItem("PRE", "Court Conditions", "Crisp afternoon grass speed favors aggressive returners.", false)
            ),
            lineups = emptyList(),
            headToHead = listOf(
                HeadToHeadItem("Jul 2025", "Alcaraz 3 - 2 Sinner", "Carlos Alcaraz")
            ),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "Carlos Alcaraz", 188), MarketOption("o2", "Jannik Sinner", 192)))
            )
        ),
        MatchItem(
            id = "vol_ita_pol_07",
            sport = SportCategory.VOLLEYBALL,
            tournament = "Nations League",
            status = MatchStatus.LIVE,
            venue = "Arena Lodz",
            timerLabel = "SET 3 • 19-17",
            countdownSeconds = 0L,
            momentumHome = 0.54f,
            summaryNote = "Sets tied 1-1 in high-flying rally battle",
            homeTeam = TeamSummary("Italy Azzurri", "ITA", "1 (19)", "Sets: 1", 0xFF00A3FF, 0xFF00E676),
            awayTeam = TeamSummary("Poland Eagles", "POL", "1 (17)", "Sets: 1", 0xFFFF3D57, 0xFFFFFFFF),
            stats = listOf(
                MatchStatRow("Aces", "6", "5", 0.55f),
                MatchStatRow("Kill Blocks", "9", "8", 0.53f)
            ),
            commentary = listOf(
                CommentaryItem("SET 3", "MONSTER BLOCK!", "Alessandro Michieletto shuts down the pipe spike!", true)
            ),
            lineups = emptyList(),
            headToHead = listOf(HeadToHeadItem("Sep 2025", "Italy 3 - 2 Poland", "Italy Azzurri")),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "Italy Azzurri", 182), MarketOption("o2", "Poland Eagles", 198)))
            )
        ),
        MatchItem(
            id = "hky_ind_ned_08",
            sport = SportCategory.HOCKEY,
            tournament = "FIH Pro League",
            status = MatchStatus.UPCOMING,
            venue = "Kalinga Stadium",
            timerLabel = "19:30",
            countdownSeconds = 7400L,
            momentumHome = 0.50f,
            summaryNote = "Penalty-corner specialists clash",
            homeTeam = TeamSummary("India Hockey", "IND", "VS", "FIH #3", 0xFF00A3FF, 0xFFFFD600),
            awayTeam = TeamSummary("Netherlands Oranje", "NED", "VS", "FIH #1", 0xFFFF6D00, 0xFF2962FF),
            stats = emptyList(),
            commentary = emptyList(),
            lineups = emptyList(),
            headToHead = listOf(HeadToHeadItem("Aug 2025", "India 2 - 2 Netherlands", "Draw")),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "India Hockey", 225), MarketOption("o2", "Netherlands Oranje", 175)))
            )
        ),
        MatchItem(
            id = "esp_flc_navi_09",
            sport = SportCategory.ESPORTS,
            tournament = "Esports Masters",
            status = MatchStatus.COMPLETED,
            venue = "LAN Stage, Berlin",
            timerLabel = "FINAL • BO3",
            countdownSeconds = 0L,
            momentumHome = 0.65f,
            summaryNote = "Team Falcons clinched Map 3 Inferno 13-9",
            homeTeam = TeamSummary("Team Falcons", "FLC", "2", "13-9 Map 3", 0xFF00E676, 0xFF070B19),
            awayTeam = TeamSummary("Natus Vincere", "NAVI", "1", "9-13 Map 3", 0xFFFFD600, 0xFF152247),
            stats = listOf(
                MatchStatRow("Headshot %", "54%", "49%", 0.52f),
                MatchStatRow("First Kills", "38", "31", 0.55f)
            ),
            commentary = listOf(
                CommentaryItem("FINAL", "ACE CLUTCH!", "NiKo closes out Map 3 with a crisp triple-entry onto B site!", true)
            ),
            lineups = emptyList(),
            headToHead = listOf(HeadToHeadItem("Oct 2026", "Falcons 2 - 1 NAVI", "Team Falcons")),
            markets = listOf(
                PredictionMarket("m1", "Match Winner", listOf(MarketOption("o1", "Team Falcons", 175), MarketOption("o2", "Natus Vincere", 210)))
            )
        )
    )

    fun advanceLiveTick(matches: List<MatchItem>, tick: Int): List<MatchItem> =
        matches.map { match ->
            when (match.status) {
                MatchStatus.UPCOMING -> match.copy(
                    countdownSeconds = (match.countdownSeconds - 1L).coerceAtLeast(0L)
                )
                MatchStatus.LIVE -> {
                    val delta = if (tick % 2 == 0) 0.02f else -0.02f
                    val newMomentum = (match.momentumHome + delta).coerceIn(0.32f, 0.72f)
                    val updatedTimer = when (match.id) {
                        "ucl_rm_mci_02" -> {
                            val totalSecs = (72 * 60 + 18 + tick).coerceAtMost(89 * 60 + 55)
                            "%02d:%02d".format(totalSecs / 60, totalSecs % 60)
                        }
                        "ipl_mi_csk_01" -> {
                            val runs = 168 + (tick / 6)
                            return@map match.copy(
                                momentumHome = newMomentum,
                                homeTeam = match.homeTeam.copy(score = "$runs/4")
                            )
                        }
                        else -> match.timerLabel
                    }
                    match.copy(timerLabel = updatedTimer, momentumHome = newMomentum)
                }
                MatchStatus.COMPLETED -> match
            }
        }
}
