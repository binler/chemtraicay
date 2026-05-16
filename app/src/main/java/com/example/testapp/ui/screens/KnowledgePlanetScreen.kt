package com.example.testapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.game.GameViewModel
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.ui.components.KidButton
import com.example.testapp.ui.components.KidCard
import com.example.testapp.ui.theme.*

@Composable
fun KnowledgePlanetScreen(viewModel: GameViewModel, onGameSelect: (GameLogic, Int, Int) -> Unit) {
    var currentPlanet by remember { mutableStateOf(CategoryType.LEARNING) }
    var screenSize by remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (currentPlanet == CategoryType.SCIENCE) Color(0xFF000005) else CreamWhite)
    ) {
        // Track screen size
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            screenSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // NAVIGATION HUB (Hành tinh Tri thức)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                KidButton(
                    text = "TRÒ CHƠI 🎮",
                    modifier = Modifier.weight(1f),
                    containerColor = if (currentPlanet == CategoryType.EXERCISE) SoftPink else Color.White,
                    shadowColor = if (currentPlanet == CategoryType.EXERCISE) Color(0xFFD32F2F) else Color(0xFFE0E0E0),
                    onClick = { currentPlanet = CategoryType.EXERCISE }
                )
                KidButton(
                    text = "LỚP HỌC 📚",
                    modifier = Modifier.weight(1f),
                    containerColor = if (currentPlanet == CategoryType.LEARNING) SkyBlue else Color.White,
                    shadowColor = if (currentPlanet == CategoryType.LEARNING) Color(0xFF1976D2) else Color(0xFFE0E0E0),
                    onClick = { currentPlanet = CategoryType.LEARNING }
                )
                KidButton(
                    text = "VŨ TRỤ 🚀",
                    modifier = Modifier.weight(1f),
                    containerColor = if (currentPlanet == CategoryType.SCIENCE) Lavender else Color.White,
                    shadowColor = if (currentPlanet == CategoryType.SCIENCE) Color(0xFF7B1FA2) else Color(0xFFE0E0E0),
                    onClick = { currentPlanet = CategoryType.SCIENCE }
                )
            }

            // CONTENT AREA
            AnimatedContent(
                targetState = currentPlanet,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 0.9f) togetherWith fadeOut() + scaleOut(targetScale = 1.1f)
                },
                label = "PlanetChange"
            ) { targetPlanet ->
                val filteredGames = viewModel.availableGames.filter { 
                    it.category == targetPlanet && it.targetAge == viewModel.selectedAge 
                }
                
                when (targetPlanet) {
                    CategoryType.EXERCISE -> GameZone(filteredGames, screenSize, onGameSelect)
                    CategoryType.LEARNING -> StudyZone(filteredGames, viewModel, screenSize, onGameSelect)
                    CategoryType.SCIENCE -> ScienceZone(filteredGames, screenSize, onGameSelect)
                }
            }
        }
    }
}

@Composable
fun GameZone(games: List<GameLogic>, screenSize: IntSize, onGameSelect: (GameLogic, Int, Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 32.dp)) {
        Text("CÙNG CHƠI THÔI CON! 🍎", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = SoftPink)
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(games) { game ->
                KidCard(
                    modifier = Modifier
                        .height(240.dp)
                        .clickable { onGameSelect(game, screenSize.width, screenSize.height) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(game.icon, fontSize = 80.sp)
                        Text(game.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text(if (game.isAR) "Vận động cùng AI" else "Chơi cùng bé", color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun StudyZone(games: List<GameLogic>, viewModel: GameViewModel, screenSize: IntSize, onGameSelect: (GameLogic, Int, Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("LỚP HỌC VUI NHỘN 🎒", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = SkyBlue)
            Spacer(modifier = Modifier.weight(1f))
            KidButton(
                text = "BÉ 2 TUỔI",
                modifier = Modifier.width(180.dp),
                containerColor = if (viewModel.selectedAge == AgeGroup.TODDLER) SunnyYellow else Color.White,
                onClick = { viewModel.selectedAge = AgeGroup.TODDLER }
            )
            Spacer(modifier = Modifier.width(16.dp))
            KidButton(
                text = "BÉ 5 TUỔI",
                modifier = Modifier.width(180.dp),
                containerColor = if (viewModel.selectedAge == AgeGroup.PRESCHOOL) SunnyYellow else Color.White,
                onClick = { viewModel.selectedAge = AgeGroup.PRESCHOOL }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        games.forEach { game ->
            KidCard(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { onGameSelect(game, screenSize.width, screenSize.height) }
            ) {
                Row(modifier = Modifier.padding(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(game.icon, fontSize = 60.sp)
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(game.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("HỌC THÔI ▶️", color = SkyBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScienceZone(games: List<GameLogic>, screenSize: IntSize, onGameSelect: (GameLogic, Int, Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 32.dp)) {
        Text("KHÁM PHÁ VŨ TRỤ 🌌", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(games) { game ->
                KidCard(
                    modifier = Modifier
                        .height(280.dp)
                        .clickable { onGameSelect(game, screenSize.width, screenSize.height) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(game.icon, fontSize = 90.sp)
                        Text(game.name, color = TextDark, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}
