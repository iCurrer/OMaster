package com.silas.omaster.data

import com.silas.omaster.R
import com.silas.omaster.model.ColorCard
import com.silas.omaster.model.ColorInfo
import com.silas.omaster.model.ColorRole

object ColorCardLibrary {

    private object C {
        val brickRed       = ColorInfo("D4A574", R.string.color_brick_red,        ColorRole.PRIMARY)
        val darkBrown      = ColorInfo("8B4513", R.string.color_dark_brown,       ColorRole.PRIMARY)
        val bisque         = ColorInfo("FFE4C4", R.string.color_bisque,           ColorRole.ACCENT)
        val darkGray       = ColorInfo("4A4A4A", R.string.color_dark_gray,        ColorRole.ACCENT)
        val skyBlue        = ColorInfo("87CEEB", R.string.color_sky_blue,         ColorRole.PRIMARY)
        val powderBlue     = ColorInfo("B0E0E6", R.string.color_powder_blue,      ColorRole.PRIMARY)
        val whitePrimary   = ColorInfo("FFFFFF", R.string.color_white,            ColorRole.PRIMARY)
        val whiteAccent    = ColorInfo("FFFFFF", R.string.color_white,            ColorRole.ACCENT)
        val wheat          = ColorInfo("F5DEB3", R.string.color_wheat,            ColorRole.ACCENT)
        val forestGreen    = ColorInfo("228B22", R.string.color_forest_green,     ColorRole.PRIMARY)
        val lightGreen     = ColorInfo("90EE90", R.string.color_light_green,      ColorRole.PRIMARY)
        val saddleBrown    = ColorInfo("8B4513", R.string.color_saddle_brown,     ColorRole.ACCENT)
        val beige          = ColorInfo("F5F5DC", R.string.color_beige,            ColorRole.ACCENT)
        val slateGray      = ColorInfo("708090", R.string.color_slate_gray,       ColorRole.PRIMARY)
        val steelBlue      = ColorInfo("4682B4", R.string.color_steel_blue,       ColorRole.PRIMARY)
        val darkSlateGray  = ColorInfo("2F4F4F", R.string.color_dark_slate_gray,  ColorRole.ACCENT)
        val silver         = ColorInfo("C0C0C0", R.string.color_silver,           ColorRole.ACCENT)
        val coral          = ColorInfo("FF7F50", R.string.color_coral,            ColorRole.PRIMARY)
        val lightPink      = ColorInfo("FFB6C1", R.string.color_light_pink,       ColorRole.PRIMARY)
        val mediumPurple   = ColorInfo("9370DB", R.string.color_medium_purple,    ColorRole.ACCENT)
        val midnightBlue   = ColorInfo("191970", R.string.color_midnight_blue,    ColorRole.ACCENT)
        val deepPink       = ColorInfo("FF1493", R.string.color_deep_pink,        ColorRole.PRIMARY)
        val cyan           = ColorInfo("00FFFF", R.string.color_cyan,             ColorRole.PRIMARY)
        val gold           = ColorInfo("FFD700", R.string.color_gold,             ColorRole.ACCENT)
        val navy           = ColorInfo("000080", R.string.color_navy,             ColorRole.ACCENT)
        val tan            = ColorInfo("D2B48C", R.string.color_tan,              ColorRole.PRIMARY)
        val siennaPrimary  = ColorInfo("A0522D", R.string.color_sienna,           ColorRole.PRIMARY)
        val khaki          = ColorInfo("F0E68C", R.string.color_khaki,            ColorRole.ACCENT)
        val darkOliveGreen = ColorInfo("556B2F", R.string.color_dark_olive_green, ColorRole.ACCENT)
        val black          = ColorInfo("000000", R.string.color_black,            ColorRole.PRIMARY)
        val gray           = ColorInfo("808080", R.string.color_gray,             ColorRole.ACCENT)
        val lightGray      = ColorInfo("D3D3D3", R.string.color_light_gray,       ColorRole.ACCENT)
        val cherryBlossom  = ColorInfo("FFB7C5", R.string.color_cherry_blossom,   ColorRole.PRIMARY)
        val hotPink        = ColorInfo("FF69B4", R.string.color_hot_pink,         ColorRole.PRIMARY)
        val paleGreen      = ColorInfo("98FB98", R.string.color_pale_green,       ColorRole.ACCENT)
        val lemonChiffon   = ColorInfo("FFFACD", R.string.color_lemon_chiffon,    ColorRole.ACCENT)
        val coffee         = ColorInfo("6F4E37", R.string.color_coffee,           ColorRole.PRIMARY)
        val chocolate      = ColorInfo("D2691E", R.string.color_chocolate,        ColorRole.PRIMARY)
        val cornsilk       = ColorInfo("FFF8DC", R.string.color_cornsilk,         ColorRole.ACCENT)
        val siennaAccent   = ColorInfo("A0522D", R.string.color_sienna,           ColorRole.ACCENT)
        val oceanBlue      = ColorInfo("006994", R.string.color_ocean_blue,       ColorRole.PRIMARY)
        val darkBlue       = ColorInfo("003366", R.string.color_dark_blue,        ColorRole.PRIMARY)
        val turquoise      = ColorInfo("40E0D0", R.string.color_turquoise,        ColorRole.ACCENT)
        val honeydew       = ColorInfo("F0FFF0", R.string.color_honeydew,         ColorRole.ACCENT)
        val orangeRed      = ColorInfo("FF4500", R.string.color_orange_red,       ColorRole.PRIMARY)
        val goldenrod      = ColorInfo("DAA520", R.string.color_goldenrod,        ColorRole.PRIMARY)
        val darkRed        = ColorInfo("8B0000", R.string.color_dark_red,         ColorRole.ACCENT)
    }

    private fun card(id: String, theme: Int, desc: Int, tips: Int, challenge: Int,
                     colors: List<ColorInfo>, tags: List<Int>) =
        ColorCard(id, colors, theme, desc, tips, challenge, tags)

    val allCards = with(C) { listOf(
        card("urban_warm", R.string.colorwalk_theme_urban_warm, R.string.colorwalk_desc_urban_warm, R.string.colorwalk_tips_urban_warm, R.string.colorwalk_challenge_urban_warm,
            listOf(brickRed, darkBrown, bisque, darkGray),
            listOf(R.string.colorwalk_scene_architecture, R.string.colorwalk_scene_street, R.string.colorwalk_scene_light)),
        card("seaside_fresh", R.string.colorwalk_theme_seaside_fresh, R.string.colorwalk_desc_seaside_fresh, R.string.colorwalk_tips_seaside_fresh, R.string.colorwalk_challenge_seaside_fresh,
            listOf(skyBlue, powderBlue, whiteAccent, wheat),
            listOf(R.string.colorwalk_scene_seaside, R.string.colorwalk_scene_sky, R.string.colorwalk_scene_minimal)),
        card("forest_green", R.string.colorwalk_theme_forest_green, R.string.colorwalk_desc_forest_green, R.string.colorwalk_tips_forest_green, R.string.colorwalk_challenge_forest_green,
            listOf(forestGreen, lightGreen, darkBrown, beige),
            listOf(R.string.colorwalk_scene_park, R.string.colorwalk_scene_plants, R.string.colorwalk_scene_nature)),
        card("industrial_cool", R.string.colorwalk_theme_industrial_cool, R.string.colorwalk_desc_industrial_cool, R.string.colorwalk_tips_industrial_cool, R.string.colorwalk_challenge_industrial_cool,
            listOf(slateGray, steelBlue, darkSlateGray, silver),
            listOf(R.string.colorwalk_scene_industrial, R.string.colorwalk_scene_geometry, R.string.colorwalk_scene_bridge)),
        card("sunset_glow", R.string.colorwalk_theme_sunset_glow, R.string.colorwalk_desc_sunset_glow, R.string.colorwalk_tips_sunset_glow, R.string.colorwalk_challenge_sunset_glow,
            listOf(coral, lightPink, mediumPurple, midnightBlue),
            listOf(R.string.colorwalk_scene_sunset, R.string.colorwalk_scene_skyline, R.string.colorwalk_scene_reflection)),
        card("neon_night", R.string.colorwalk_theme_neon_night, R.string.colorwalk_desc_neon_night, R.string.colorwalk_tips_neon_night, R.string.colorwalk_challenge_neon_night,
            listOf(deepPink, cyan, gold, navy),
            listOf(R.string.colorwalk_scene_night, R.string.colorwalk_scene_neon, R.string.colorwalk_scene_city)),
        card("vintage_film", R.string.colorwalk_theme_vintage_film, R.string.colorwalk_desc_vintage_film, R.string.colorwalk_tips_vintage_film, R.string.colorwalk_challenge_vintage_film,
            listOf(tan, siennaPrimary, khaki, darkOliveGreen),
            listOf(R.string.colorwalk_scene_old_street, R.string.colorwalk_scene_cafe, R.string.colorwalk_scene_story)),
        card("minimalist_bw", R.string.colorwalk_theme_minimalist_bw, R.string.colorwalk_desc_minimalist_bw, R.string.colorwalk_tips_minimalist_bw, R.string.colorwalk_challenge_minimalist_bw,
            listOf(black, whitePrimary, gray, lightGray),
            listOf(R.string.colorwalk_scene_black_white, R.string.colorwalk_scene_architecture, R.string.colorwalk_scene_shadow)),
        card("spring_blossom", R.string.colorwalk_theme_spring_blossom, R.string.colorwalk_desc_spring_blossom, R.string.colorwalk_tips_spring_blossom, R.string.colorwalk_challenge_spring_blossom,
            listOf(cherryBlossom, hotPink, paleGreen, lemonChiffon),
            listOf(R.string.colorwalk_scene_flower, R.string.colorwalk_scene_park, R.string.colorwalk_scene_soft_light)),
        card("coffee_time", R.string.colorwalk_theme_coffee_time, R.string.colorwalk_desc_coffee_time, R.string.colorwalk_tips_coffee_time, R.string.colorwalk_challenge_coffee_time,
            listOf(coffee, chocolate, cornsilk, siennaAccent),
            listOf(R.string.colorwalk_scene_cafe, R.string.colorwalk_scene_still_life, R.string.colorwalk_scene_indoor)),
        card("ocean_deep", R.string.colorwalk_theme_ocean_deep, R.string.colorwalk_desc_ocean_deep, R.string.colorwalk_tips_ocean_deep, R.string.colorwalk_challenge_ocean_deep,
            listOf(oceanBlue, darkBlue, turquoise, honeydew),
            listOf(R.string.colorwalk_scene_water, R.string.colorwalk_scene_blue_hour, R.string.colorwalk_scene_reflection)),
        card("autumn_leaves", R.string.colorwalk_theme_autumn_leaves, R.string.colorwalk_desc_autumn_leaves, R.string.colorwalk_tips_autumn_leaves, R.string.colorwalk_challenge_autumn_leaves,
            listOf(orangeRed, goldenrod, darkRed, wheat),
            listOf(R.string.colorwalk_scene_autumn, R.string.colorwalk_scene_park, R.string.colorwalk_scene_backlight))
    )}

    fun getRandomCard(): ColorCard = allCards.random()

    fun getCardById(id: String): ColorCard? = allCards.find { it.id == id }
}
