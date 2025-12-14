package com.android.sample.ui.category.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.mockData.getMockEventCategory
import com.android.sample.ui.category.EditCategoryScreenTestTags
import com.android.sample.ui.theme.PaddingHuge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.SpacingLarge
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable

@Composable
fun CategoryListSection(
    modifier: Modifier = Modifier,
    categories: List<EventCategory> = getMockEventCategory(),
    reorderState: ReorderableLazyListState,
    onEditCategory: (EventCategory) -> Unit = {},
    onDeleteCategory: (EventCategory) -> Unit = {},
) {
  // Main content list with reordering support
  LazyColumn(
      modifier =
          modifier
              .reorderable(reorderState)
              .detectReorderAfterLongPress(reorderState)
              .testTag(EditCategoryScreenTestTags.CATEGORY_LIST),
      state = reorderState.listState,
      // Space between items
      verticalArrangement = Arrangement.spacedBy(SpacingLarge)) {
        if (categories.isEmpty()) {
          item {
            // Explanation text when no categories have been created
            Column(
                modifier = Modifier.fillParentMaxSize().padding(PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
              Text(
                  text = stringResource(R.string.edit_category_empty_state_title),
                  style = MaterialTheme.typography.titleMedium,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag(EditCategoryScreenTestTags.EMPTY_STATE_TITLE),
              )
              Spacer(modifier = Modifier.height(SpacingLarge))
              Text(
                  text = stringResource(R.string.edit_category_empty_state_message),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag(EditCategoryScreenTestTags.EMPTY_STATE_MESSAGE),
              )
            }
          }
        }
        items(items = categories, key = { it.id }) { category ->
          ReorderableItem(state = reorderState, key = category.id) { isDragging ->
            CategoryCard(
                modifier = Modifier,
                isDragging = isDragging,
                category = category,
                onEditClick = { onEditCategory(category) },
                onDeleteClick = { onDeleteCategory(category) })
          }
        }
        // Additional space to place the last item higher than the "+" floating button
        item { Spacer(modifier = Modifier.height(PaddingHuge)) }
      }
}
