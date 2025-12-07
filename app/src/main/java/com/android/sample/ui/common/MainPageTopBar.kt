package com.android.sample.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.TopBarPalette

/**
 * Composable function to create a top bar.
 *
 * @param modifier The modifier to be applied to the top bar.
 * @param title The text to display in the top bar.
 * @param actions Optional composable actions to display in the top bar (e.g., icons, chips).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageTopBar(
    modifier: Modifier = Modifier,
    title: String = "Untitled",
    actions: @Composable () -> Unit = {},
    pastille: @Composable () -> Unit = {},
) {
    Surface(
        color = TopBarPalette.Background,
        shadowElevation = DefaultCardElevation,
    ) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PaddingExtraSmall),
                ) {
                    pastille()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TopBarPalette.Font
                    )
                }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TopBarPalette.Font,
                    navigationIconContentColor = TopBarPalette.Font,
                    actionIconContentColor = TopBarPalette.Font,
                ),
            actions = { actions() })
    }
}
