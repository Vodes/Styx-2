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
        Font("fonts/OpenSans-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal),
        Font("fonts/OpenSans-SemiBoldItalic.ttf", FontWeight.SemiBold, FontStyle.Italic),
        Font("fonts/OpenSans-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("fonts/OpenSans-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
        Font("fonts/OpenSans-ExtraBold.ttf", FontWeight.ExtraBold, FontStyle.Normal),
        Font("fonts/OpenSans-ExtraBoldItalic.ttf", FontWeight.ExtraBold, FontStyle.Italic),
    )
}

val AppTypography = Typography(
    displayLarge = defaultTypo.displayLarge.copy(fontFamily = AppFont.OpenSans),
    displayMedium = defaultTypo.displayMedium.copy(fontFamily = AppFont.OpenSans),
    displaySmall = defaultTypo.displaySmall.copy(fontFamily = AppFont.OpenSans),

    headlineLarge = defaultTypo.headlineLarge.copy(fontFamily = AppFont.OpenSans),
    headlineMedium = defaultTypo.headlineMedium.copy(fontFamily = AppFont.OpenSans),
    headlineSmall = defaultTypo.headlineSmall.copy(fontFamily = AppFont.OpenSans),

    titleLarge = defaultTypo.titleLarge.copy(fontFamily = AppFont.OpenSans),
    titleMedium = defaultTypo.titleMedium.copy(fontFamily = AppFont.OpenSans),
    titleSmall = defaultTypo.titleSmall.copy(fontFamily = AppFont.OpenSans),

    bodyLarge = defaultTypo.bodyLarge.copy(fontFamily = AppFont.OpenSans),
    bodyMedium = defaultTypo.bodyMedium.copy(fontFamily = AppFont.OpenSans),
    bodySmall = defaultTypo.bodySmall.copy(fontFamily = AppFont.OpenSans),

    labelLarge = defaultTypo.labelLarge.copy(fontFamily = AppFont.OpenSans),
    labelMedium = defaultTypo.labelMedium.copy(fontFamily = AppFont.OpenSans),
    labelSmall = defaultTypo.labelSmall.copy(fontFamily = AppFont.OpenSans)
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
