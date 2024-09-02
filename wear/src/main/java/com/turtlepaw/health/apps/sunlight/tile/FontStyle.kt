@file:OptIn(ProtoLayoutExperimental::class) package com.turtlepaw.health.apps.sunlight.tile

import androidx.annotation.OptIn
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders.SpProp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.FontWeightProp
import androidx.wear.protolayout.expression.ProtoLayoutExperimental
import com.turtlepaw.health.apps.health.tile.TileColors

enum class FontStyle(private val fontSize: Float) {
    PrimaryFontSize(33f),
    SecondaryFontSize(24f);

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
                ColorBuilders.argb(TileColors.PrimaryColor)
            )
            .setSize(
                SpProp.Builder()
                    .setValue(getFontSize())
                    .build()
            )
            .build()
    }
}