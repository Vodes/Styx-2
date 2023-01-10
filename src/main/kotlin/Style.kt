import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

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