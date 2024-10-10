@file:OptIn(ProtoLayoutExperimental::class) package com.turtlepaw.health.apps.health.tile

import androidx.annotation.OptIn
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders.SpProp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.FontWeightProp
import androidx.wear.protolayout.expression.ProtoLayoutExperimental

enum class FontStyle(private val fontSize: Float, private val color: Int) {
    PrimaryFontSize(33f, TileColors.PrimaryColor),
    PrimaryLightFontSize(33f, TileColors.LightText),
    SecondaryFontSize(24f, TileColors.LightText);

    private fun getFontSize(): Float {
        return fontSize
    }

    fun getBuilder(): LayoutElementBuilders.FontStyle {
        return LayoutElementBuilders.FontStyle.Builder()
            .setWeight(
                FontWeightProp.Builder()
                    .setValue(LayoutElementBuilders.FONT_WEIGHT_MEDIUM)
                    .build()
            )
            .setColor(
                ColorBuilders.argb(color)
            )
            .setSize(
                SpProp.Builder()
                    .setValue(getFontSize())
                    .build()
            )
            .build()
    }
}