package com.android.sample.ui.theme

import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.Palette.Black

/**
 * Centralized palette containing all colors used in the application.
 *
 * Each color is defined directly as a Compose `Color` using an ARGB hexadecimal integer.
 *
 * The color names match the closest corresponding names from the "color-name.com" database, which
 * combines standard CSS/X11 names and descriptive artistic names. Colors without exact matches are
 * assigned the nearest name using RGB/Lab distance. for easier identification and consistency.
 *
 * If a color constant includes a suffix after an underscore (e.g. `_50`), that suffix represents a
 * custom alpha value applied to the same base color. The suffix is always written as a two-digit
 * percentage value corresponding to the ARGB alpha component.
 *
 * Example:
 * - `CadmiumBlue` → 0xFF09099D (opaque)
 * - `CadmiumBlue_50` → 0x8009099D (50% opacity)
 */
object Palette {

  // ----------------------------------------
  // Base / Neutrals
  // ----------------------------------------
  val Platinum = Color(color = 0xFFE6E4E9)
  val Black = Color(color = 0xFF000000)

  // ----------------------------------------
  // Purples
  // ----------------------------------------
  val Ube = Color(color = 0xFF8687BA)
  val RichLilac = Color(color = 0xFFBA68C8)

  // ----------------------------------------
  // Pinks / Reds
  // ----------------------------------------
  val LightCarminePink = Color(color = 0xFFED6B6D)
  val CandyPink = Color(color = 0xFFE57373)

  // ----------------------------------------
  // Greens
  // ----------------------------------------
  val MistyMoss = Color(color = 0xFFB2C778)
  val DarkSeaGreen = Color(color = 0xFF81C784)

  // ----------------------------------------
  // Blues
  // ----------------------------------------
  val CadmiumBlue = Color(color = 0xFF09099D)
  val CadmiumBlue_50 = Color(color = 0x8009099D)
  val BlueJeans = Color(color = 0xFF64B5F6)

  // ----------------------------------------
  // Oranges / Accents
  // ----------------------------------------
  val PastelOrange = Color(color = 0xFFFFB74D)

  // ----------------------------------------
  // Utility function to create a Color from a Long
  // ----------------------------------------
  fun fromLong(argb: Long): Color = Color(argb)
}

// General Color Scheme
object GeneralPalette {
  val Font = Black
  val Primary = CircusPalette.Primary
  val Secondary = CircusPalette.Secondary
  val Tertiary = CircusPalette.Tertiary
}

// General Color Scheme for Dark Mode
object GeneralPaletteDark {
  val Primary = Palette.CadmiumBlue
  val Secondary = Palette.RichLilac
  val Tertiary = Palette.DarkSeaGreen
}

// Circus Color Scheme
object CircusPalette {
  val Primary = Palette.LightCarminePink // Salmon
  val Secondary = Palette.Ube // Violet
  val Tertiary = Palette.MistyMoss // Green
}

// Top Bar Color Scheme
object TopBarPalette {
  val Background = Palette.Platinum
  val Font = GeneralPalette.Font
}

// Map Color Scheme
object MapPalette {
  val Stroke = Palette.CadmiumBlue
  val Fill = Palette.CadmiumBlue_50
}

// Event Color Scheme
object EventPalette {
  val Black = Palette.Black
  val Orange = Palette.PastelOrange
  val Blue = Palette.BlueJeans
  val Green = Palette.DarkSeaGreen
  val Red = Palette.CandyPink
  val Purple = Palette.RichLilac
}
