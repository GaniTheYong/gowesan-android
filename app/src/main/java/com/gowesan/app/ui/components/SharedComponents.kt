package com.gowesan.app.ui.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import com.gowesan.app.ui.theme.*

val gowesanFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TokopediaGreen,
        cursorColor = TokopediaGreen,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = TokopediaGreen,
        unfocusedLabelColor = TextSecondary
    )
