package dev.ujhhgtg.wekit.ui.utils.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

// FIXME: see androidx.compose.ui.platform.AndroidComposeViewAccessibilityDelegateCompat.android.kt line 3484
fun Modifier.fuckYouGoogle(): Modifier = semantics {
    stateDescription = ""
}
