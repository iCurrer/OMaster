package com.silas.omaster.ui.theme

import androidx.compose.ui.graphics.Color
import com.silas.omaster.R

enum class BrandTheme(
    val id: String,
    val brandNameResId: Int,
    val colorNameResId: Int,
    val primaryColor: Color,
    val hexCode: String
) {
    Hasselblad("hasselblad", R.string.brand_hasselblad, R.string.color_hasselblad_orange, HasselbladOrange, "#FF6600"),
    Zeiss("zeiss", R.string.brand_zeiss, R.string.color_zeiss_blue, ZeissBlue, "#005A9C"),
    Leica("leica", R.string.brand_leica, R.string.color_leica_red, LeicaRed, "#CC0000"),
    Ricoh("ricoh", R.string.brand_ricoh, R.string.color_ricoh_green, RicohGreen, "#00A95C"),
    Fujifilm("fujifilm", R.string.brand_fujifilm, R.string.color_fujifilm_green, FujifilmGreen, "#009B3A"),
    Canon("canon", R.string.brand_canon, R.string.color_canon_red, CanonRed, "#CC0000"),
    Nikon("nikon", R.string.brand_nikon, R.string.color_nikon_yellow, NikonYellow, "#FFC20E"),
    Sony("sony", R.string.brand_sony, R.string.color_sony_orange, SonyOrange, "#F15A24"),
    PhaseOne("phaseone", R.string.brand_phaseone, R.string.color_phaseone_grey, PhaseOneGrey, "#5A5A5A");

    companion object {
        fun fromId(id: String): BrandTheme {
            return entries.find { it.id == id } ?: Hasselblad
        }
    }
}
