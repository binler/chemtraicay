package com.example.testapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KhanKidsColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    secondary = MintGreen,
    onSecondary = Color.White,
    tertiary = SoftPink,
    onTertiary = Color.White,
    background = CreamWhite,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    secondaryContainer = SunnyYellow,
    onSecondaryContainer = TextDark
)

val KhanKidsShapes = Shapes(
    small = RoundedCornerShape(32.dp),
    medium = RoundedCornerShape(32.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun RyRoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KhanKidsColorScheme,
        shapes = KhanKidsShapes,
        typography = Typography,
        content = content
    )
}
