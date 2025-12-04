package com.android.sample.ui.invitation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.android.sample.R
import com.android.sample.model.organization.data.getMockOrganizations
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.model.organization.invitation.displayColor
import com.android.sample.model.organization.invitation.getStringId
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.invitation.DEFAULT_SWIPE_OFFSET
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.ElevationLow
import com.android.sample.ui.theme.FontSizeExtraHuge
import com.android.sample.ui.theme.LetterSpacingLarge
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SizeLarge
import com.android.sample.ui.theme.SpacingExtraSmall
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.WeightExtraHeavy
import java.time.Instant
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Assisted by AI

/**
 * Contains test tag constants for UI testing of [InvitationCard].
 *
 * Each tag corresponds to a UI element that may need to be asserted or interacted with in automated
 * Compose tests.
 */
object InvitationCardTestTags {
  const val CODE_FIELD = "codeField"
  const val ACCEPTED_AT_FIELD = "acceptedAtField"
  const val ACCEPTED_ON_FIELD = "acceptedOnField"
  const val COPY_TO_CLIPBOARD_BUTTON = "copyToClipBoardButton"
  const val INVITATION_STATUS_FIELD = "invitationStatusField"
  const val SWIPE_CARD_BUTTON = "swipeCardButton"
  const val DELETE_INVITATION_BUTTON = "deleteInvitationButton"
}

/**
 * A composable card representing a single invitation inside the invitation overview list.
 *
 * This component displays:
 * - The invitation code (always visible)
 * - A button to copy the invitation code to the clipboard
 * - "Accepted by" (visible only when the invitation has been accepted)
 * - "Accepted on" (visible only when the invitation has been accepted)
 * - The invitation status
 * - A swipe-to-reveal delete action behind the card
 *
 * ## Swipe behavior
 *
 * The card can be dragged horizontally to the left:
 * - If the drag does **not** exceed half the card's height, it snaps back to its initial position.
 * - If the drag exceeds the threshold, it animates fully to the left, revealing a delete button.
 * - The delete button remains available until swiped back.
 *
 * ## Accessibility & Testing
 *
 * All critical elements receive semantic test tags via [InvitationCardTestTags].
 *
 * ## clipboard
 *
 * The invitation code can be copied using the system clipboard via [LocalClipboardManager].
 *
 * ## Parameters
 *
 * @param invitation The invitation data used to populate UI fields.
 * @param modifier Modifier applied to the root composable.
 * @param onClickDelete Callback invoked when the delete button is pressed.
 */
@Composable
fun InvitationCard(
    invitation: Invitation,
    modifier: Modifier = Modifier,
    onClickDelete: () -> Unit = {},
) {
  val cardShape = RoundedCornerShape(CornerRadiusLarge)
  val clipboard = LocalClipboardManager.current
  val scope = rememberCoroutineScope()

  val swipeOffset = remember { Animatable(DEFAULT_SWIPE_OFFSET) }
  var cardHeight by remember { mutableFloatStateOf(0f) }
  val swipeThreshold = cardHeight / 2

  SwipeableCardContainer(
      modifier = modifier,
      swipeOffset = swipeOffset,
      cardHeight = cardHeight,
      onHeightMeasured = { cardHeight = it },
      swipeThreshold = swipeThreshold,
      scope = scope) {
        DeleteBackground(
            modifier =
                Modifier.matchParentSize()
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.error),
            onClickDelete = onClickDelete,
        )

        CardForeground(
            invitation = invitation,
            swipeOffset = swipeOffset,
            swipeThreshold = swipeThreshold,
            clipboard = clipboard,
            cardHeight = cardHeight,
            scope = scope,
            cardShape = cardShape)
      }
}

/**
 * A container that enables horizontal swipe gestures for its content.
 *
 * This Box:
 * - Measures its own height to compute swipe thresholds.
 * - Listens to drag gestures using [detectSwipeGesture].
 * - Hosts both the delete background and the foreground card.
 *
 * @param modifier Modifier for the container.
 * @param swipeOffset The current horizontal offset of the card, updated via gestures.
 * @param cardHeight The measured height of the card, used to determine swipe limits.
 * @param onHeightMeasured Callback invoked once the card height is known.
 * @param swipeThreshold The minimum drag distance needed to trigger the full open animation.
 * @param scope A coroutine scope used for gesture-driven animations.
 * @param content The composable content placed inside this swipeable container.
 */
@Composable
private fun SwipeableCardContainer(
    modifier: Modifier = Modifier,
    swipeOffset: Animatable<Float, *>,
    cardHeight: Float,
    onHeightMeasured: (Float) -> Unit,
    swipeThreshold: Float,
    scope: CoroutineScope,
    content: @Composable BoxScope.() -> Unit
) {
  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .onGloballyPositioned { coordinates ->
                onHeightMeasured(coordinates.size.height.toFloat())
              }
              .pointerInput(Unit) {
                detectSwipeGesture(
                    swipeOffset = swipeOffset,
                    cardHeight = cardHeight,
                    swipeThreshold = swipeThreshold,
                    scope = scope)
              }) {
        content()
      }
}

/**
 * The foreground card UI, which slides horizontally based on swipe gestures.
 *
 * This composable displays:
 * - The main invitation info (code, accepted fields)
 * - The invitation status
 * - A swipe button that toggles between open/closed states
 *
 * The entire card is offset horizontally according to [swipeOffset].
 *
 * @param invitation The invitation data used to populate UI.
 * @param cardShape The shape applied to the card container.
 * @param swipeOffset A shared animatable controlling the card's horizontal offset.
 * @param swipeThreshold The threshold determining open/close state.
 * @param clipboard The system clipboard used to copy the invitation code.
 * @param cardHeight The card height, used to clamp the swipe range.
 * @param scope Coroutine scope used for swipe animations.
 */
@Composable
fun CardForeground(
    invitation: Invitation,
    cardShape: RoundedCornerShape,
    swipeOffset: Animatable<Float, *>,
    swipeThreshold: Float,
    clipboard: ClipboardManager,
    cardHeight: Float,
    scope: CoroutineScope
) {
  Card(
      modifier =
          Modifier.offset {
                IntOffset(x = swipeOffset.value.roundToInt(), y = DEFAULT_SWIPE_OFFSET.toInt())
              }
              .fillMaxWidth(),
      shape = cardShape,
      elevation = CardDefaults.cardElevation(ElevationLow)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              InvitationMainInfo(
                  modifier = Modifier.weight(WeightExtraHeavy),
                  invitation = invitation,
                  clipboard = clipboard)
              InvitationStatusRowAndSwipeButton(
                  invitation = invitation,
                  swipeOffset = swipeOffset,
                  swipeThreshold = swipeThreshold,
                  cardHeight = cardHeight,
                  scope = scope)
            }
      }
}

/**
 * The red background revealed when the foreground card is swiped left.
 *
 * This background contains a delete icon aligned to the right, allowing users to confirm deletion
 * of the invitation.
 *
 * @param modifier Modifier applied to the full-size background container.
 * @param onClickDelete Callback invoked when the delete button is pressed.
 */
@Composable
fun DeleteBackground(modifier: Modifier, onClickDelete: () -> Unit) {
  Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
    IconButton(
        onClick = onClickDelete,
        modifier =
            Modifier.padding(end = PaddingExtraLarge)
                .testTag(InvitationCardTestTags.DELETE_INVITATION_BUTTON)) {
          Icon(
              imageVector = Icons.Outlined.Delete,
              contentDescription = stringResource(R.string.delete),
              tint = MaterialTheme.colorScheme.onError,
              modifier = Modifier.size(SizeLarge))
        }
  }
}

/**
 * Displays the main textual information for the invitation.
 *
 * Includes:
 * - The invitation code with copy-to-clipboard action
 * - "Accepted by" (shown only if the invitation was accepted)
 * - "Accepted on" (also conditional)
 *
 * The copy button uses the system [clipboard] to store the code as an [AnnotatedString].
 *
 * @param modifier Modifier applied to the column container.
 * @param invitation The invitation whose metadata is displayed.
 * @param clipboard The clipboard manager used for copying the code.
 */
@Composable
fun InvitationMainInfo(modifier: Modifier, invitation: Invitation, clipboard: ClipboardManager) {
  Column(verticalArrangement = Arrangement.spacedBy(SpacingExtraSmall), modifier = modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
          text = invitation.code,
          style = MaterialTheme.typography.titleLarge,
          letterSpacing = LetterSpacingLarge,
          fontSize = FontSizeExtraHuge,
          modifier = Modifier.testTag(InvitationCardTestTags.CODE_FIELD))
      IconButton(
          onClick = { clipboard.setText(AnnotatedString(invitation.code)) },
          modifier = Modifier.testTag(InvitationCardTestTags.COPY_TO_CLIPBOARD_BUTTON)) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
          }
    }

    Column {
      Text(
          text =
              invitation.inviteeEmail?.let { stringResource(R.string.accepted_by, it) }.orEmpty(),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontStyle = FontStyle.Italic,
          modifier = Modifier.testTag(InvitationCardTestTags.ACCEPTED_AT_FIELD))

      Spacer(modifier = Modifier.width(SpacingMedium))

      Text(
          text =
              invitation.acceptedAt
                  ?.let {
                    stringResource(
                        R.string.accepted_on, DateTimeUtils.formatInstantToDateAndTime(it))
                  }
                  .orEmpty(),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontStyle = FontStyle.Italic,
          modifier = Modifier.testTag(InvitationCardTestTags.ACCEPTED_ON_FIELD))
    }
  }
}

/**
 * Displays the invitation status along with a button that toggles swipe open/close.
 *
 * The button icon changes depending on the swipe state:
 * - A delete icon when the card is closed
 * - A back-arrow when the card is fully open
 *
 * Pressing the button triggers animations that either:
 * - Open the card fully (revealing the delete background), or
 * - Close it back to its default position.
 *
 * @param invitation The invitation whose status text and color are shown.
 * @param swipeOffset The horizontal swipe offset state.
 * @param swipeThreshold The threshold at which the card is considered "open".
 * @param cardHeight The maximum swipe distance (equal to card height).
 * @param scope Coroutine scope used to run swipe animations.
 */
@Composable
fun InvitationStatusRowAndSwipeButton(
    invitation: Invitation,
    swipeOffset: Animatable<Float, *>,
    swipeThreshold: Float,
    cardHeight: Float,
    scope: CoroutineScope
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall)) {
        Text(
            text = stringResource(invitation.status.getStringId()),
            style = MaterialTheme.typography.titleMedium,
            color = invitation.status.displayColor(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(InvitationCardTestTags.INVITATION_STATUS_FIELD))
        IconButton(
            onClick = {
              scope.launch {
                if (swipeOffset.value < -swipeThreshold) {
                  swipeOffset.animateTo(DEFAULT_SWIPE_OFFSET)
                } else {
                  swipeOffset.animateTo(-cardHeight)
                }
              }
            },
            modifier = Modifier.testTag(InvitationCardTestTags.SWIPE_CARD_BUTTON)) {
              Icon(
                  imageVector =
                      if (swipeOffset.value > -swipeThreshold) {
                        Icons.Outlined.Delete
                      } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                      },
                  contentDescription = null)
            }
      }
}

/**
 * Detects horizontal drag gestures and updates the card's swipe offset.
 *
 * Behavior:
 * - Dragging updates the card position via [Animatable.snapTo].
 * - Releasing the drag triggers an animation:
 *     - Snap back if the drag did not exceed [swipeThreshold].
 *     - Fully open (to -cardHeight) if the threshold was exceeded.
 * - Canceling the drag always returns the card to its default position.
 *
 * @param swipeOffset The horizontal offset animatable.
 * @param cardHeight Maximum negative offset allowed for the swipe.
 * @param swipeThreshold The threshold determining open/close behavior.
 * @param scope Coroutine scope used to execute animations.
 */
private suspend fun PointerInputScope.detectSwipeGesture(
    swipeOffset: Animatable<Float, *>,
    cardHeight: Float,
    swipeThreshold: Float,
    scope: CoroutineScope
) {
  detectDragGestures(
      onDragEnd = {
        scope.launch {
          if (swipeOffset.value > -swipeThreshold) {
            swipeOffset.animateTo(DEFAULT_SWIPE_OFFSET)
          } else {
            swipeOffset.animateTo(-cardHeight)
          }
        }
      },
      onDragCancel = { scope.launch { swipeOffset.animateTo(DEFAULT_SWIPE_OFFSET) } },
      onDrag = { _, dragAmount ->
        scope.launch {
          val newValue =
              (swipeOffset.value + dragAmount.x).coerceIn(-cardHeight, DEFAULT_SWIPE_OFFSET)
          swipeOffset.snapTo(newValue)
        }
      })
}

/**
 * A preview showing the `InvitationCard` with mock invitation data.
 *
 * Useful during UI development to validate the layout and swipe interactions.
 */
@Preview(showBackground = true)
@Composable
fun InvitationCardPreview() {
  val org = getMockOrganizations().first()
  val invitation =
      Invitation(
          id = "id",
          code = "123456",
          organizationId = org.id,
          inviteeEmail = "alice@example.com",
          acceptedAt = Instant.now(),
          status = InvitationStatus.Used)
  InvitationCard(invitation)
}
