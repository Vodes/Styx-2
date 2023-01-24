package moe.styx

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent

fun styxDarkColors() = darkColors(
    primary = Color(0, 180, 85),
    primaryVariant = Color(0, 160, 58),
    secondary = Color(168, 85, 247),
    secondaryVariant = Color(162, 28, 175),
    background = Color(40, 40, 40), surface = Color(40, 40, 40),
    onPrimary = Color(245, 245, 245),
    onSecondary = Color(245, 245, 245), onSurface = Color(224, 224, 224)
)

fun styxLightColors() = lightColors(
    primary = Color(0, 180, 85),
    primaryVariant = Color(0, 160, 58),
    secondary = Color(168, 85, 247),
    secondaryVariant = Color(162, 28, 175),
    background = Color(255, 255, 255), surface = Color(240, 240, 240),
    onPrimary = Color(245, 245, 245),
    onSecondary = Color(245, 245, 245), onSurface = Color(120, 120, 120)
)

val styxTypography = Typography(
    defaultFontFamily = FontFamily(
        Font("fonts/OpenSans-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("fonts/OpenSans-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("fonts/OpenSans-BoldItalic.ttf", FontWeight.Normal, FontStyle.Italic),
        Font("fonts/OpenSans-Italic.ttf", FontWeight.Normal, FontStyle.Italic)
    )
);

@Composable
private fun animateColor(targetValue: Color) =
    animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 1000)
    ).value

@Composable
fun Colors.switch() = copy(
    primary = animateColor(primary),
    primaryVariant = animateColor(primaryVariant),
    secondary = animateColor(secondary),
    secondaryVariant = animateColor(secondaryVariant),
    background = animateColor(background),
    surface = animateColor(surface),
    error = animateColor(error),
    onPrimary = animateColor(onPrimary),
    onSecondary = animateColor(onSecondary),
    onBackground = animateColor(onBackground),
    onSurface = animateColor(onSurface),
    onError = animateColor(onError)
)

private val EnterScales = 1.1f to 0.95f

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    content: ScreenTransitionContent = { it.Content() }
) {
    ScreenTransition(
        navigator = navigator,
        modifier = modifier,
        content = content,
        transition = { fadeIn(spring(stiffness = Spring.StiffnessMedium)) with fadeOut(spring(stiffness = Spring.StiffnessMedium)) }
    )
}