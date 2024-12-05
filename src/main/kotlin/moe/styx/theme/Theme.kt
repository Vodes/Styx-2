package moe.styx.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val defaultTypo = Typography()

object AppFont {
    val OpenSans = FontFamily(
        Font("fonts/OpenSans-Light.ttf", FontWeight.Light, FontStyle.Normal),
        Font("fonts/OpenSans-LightItalic.ttf", FontWeight.Light, FontStyle.Italic),
        Font("fonts/OpenSans-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("fonts/OpenSans-Italic.ttf", FontWeight.Normal, FontStyle.Italic),
        Font("fonts/OpenSans-Medium.ttf", FontWeight.Medium, FontStyle.Normal),
        Font("fonts/OpenSans-MediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
        Font("fonts/OpenSans-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("fonts/OpenSans-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
    )
    val JetbrainsMono = FontFamily(
        Font("fonts/JetBrainsMono-Light.ttf", FontWeight.Light, FontStyle.Normal),
        Font("fonts/JetBrainsMono-LightItalic.ttf", FontWeight.Light, FontStyle.Italic),
        Font("fonts/JetBrainsMono-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("fonts/JetBrainsMono-Italic.ttf", FontWeight.Normal, FontStyle.Italic),
        Font("fonts/JetBrainsMono-Medium.ttf", FontWeight.Medium, FontStyle.Normal),
        Font("fonts/JetBrainsMono-MediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
        Font("fonts/JetBrainsMono-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("fonts/JetBrainsMono-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
    )
}

val FontFamily.Typography: Typography
    get() = Typography(
        displayLarge = defaultTypo.displayLarge.copy(fontFamily = this),
        displayMedium = defaultTypo.displayMedium.copy(fontFamily = this),
        displaySmall = defaultTypo.displaySmall.copy(fontFamily = this),

        headlineLarge = defaultTypo.headlineLarge.copy(fontFamily = this),
        headlineMedium = defaultTypo.headlineMedium.copy(fontFamily = this),
        headlineSmall = defaultTypo.headlineSmall.copy(fontFamily = this),

        titleLarge = defaultTypo.titleLarge.copy(fontFamily = this),
        titleMedium = defaultTypo.titleMedium.copy(fontFamily = this),
        titleSmall = defaultTypo.titleSmall.copy(fontFamily = this),

        bodyLarge = defaultTypo.bodyLarge.copy(fontFamily = this),
        bodyMedium = defaultTypo.bodyMedium.copy(fontFamily = this),
        bodySmall = defaultTypo.bodySmall.copy(fontFamily = this),

        labelLarge = defaultTypo.labelLarge.copy(fontFamily = this),
        labelMedium = defaultTypo.labelMedium.copy(fontFamily = this),
        labelSmall = defaultTypo.labelSmall.copy(fontFamily = this)
    )

private val animationSpec: TweenSpec<Color> = tween(durationMillis = 650)

@Composable
private fun animateColor(targetValue: Color) = animateColorAsState(targetValue = targetValue, animationSpec = animationSpec).value

@Composable
fun ColorScheme.transition() = copy(
    primary = animateColor(primary),
    onPrimary = animateColor(onPrimary),
    primaryContainer = animateColor(primaryContainer),
    onPrimaryContainer = animateColor(onPrimaryContainer),
    secondary = animateColor(secondary),
    onSecondary = animateColor(onSecondary),
    secondaryContainer = animateColor(secondaryContainer),
    onSecondaryContainer = animateColor(onSecondaryContainer),
    tertiary = animateColor(tertiary),
    onTertiary = animateColor(onTertiary),
    tertiaryContainer = animateColor(tertiaryContainer),
    onTertiaryContainer = animateColor(onTertiaryContainer),
    error = animateColor(error),
    errorContainer = animateColor(errorContainer),
    onError = animateColor(onError),
    onErrorContainer = animateColor(onErrorContainer),
    background = animateColor(background),
    onBackground = animateColor(onBackground),
    surface = animateColor(surface),
    onSurface = animateColor(onSurface),
    surfaceVariant = animateColor(surfaceVariant),
    onSurfaceVariant = animateColor(onSurfaceVariant),
    outline = animateColor(outline),
    inverseOnSurface = animateColor(inverseOnSurface),
    inverseSurface = animateColor(inverseSurface),
    inversePrimary = animateColor(inversePrimary),
    surfaceTint = animateColor(surfaceTint),
    outlineVariant = animateColor(outlineVariant),
    scrim = animateColor(scrim)
)
