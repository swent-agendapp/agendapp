package com.android.sample.ui.invitation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.android.sample.R
import com.android.sample.model.organization.data.getMockOrganizations
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.model.organization.invitation.displayColor
import com.android.sample.model.organization.invitation.displayString
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.invitation.DEFAULT_SWIPE_OFFSET
import com.android.sample.ui.invitation.InvitationOverviewScreenTestTags
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
import kotlinx.coroutines.launch

@Composable
fun InvitationCard(
    invitation: Invitation,
    modifier: Modifier = Modifier,
    onClickDelete: () -> Unit = {},
    onCopy: (String) -> Unit = {}
) {
  val cardShape = RoundedCornerShape(CornerRadiusLarge)
  val scope = rememberCoroutineScope()

  val swipeOffset = remember { Animatable(DEFAULT_SWIPE_OFFSET) }
  var cardHeight by remember { mutableFloatStateOf(0f) }
  val swipeThresholds = cardHeight / 2

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .onGloballyPositioned { coordinates ->
                cardHeight = coordinates.size.height.toFloat()
              }
              .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                      scope.launch {
                        if (swipeOffset.value > -swipeThresholds) {
                          swipeOffset.animateTo(DEFAULT_SWIPE_OFFSET)
                        } else {
                          swipeOffset.animateTo(-cardHeight)
                        }
                      }
                    },
                    onDragCancel = { scope.launch { swipeOffset.animateTo(DEFAULT_SWIPE_OFFSET) } },
                    onDrag = { change, dragAmount ->
                      scope.launch {
                        val newValue =
                            (swipeOffset.value + dragAmount.x).coerceIn(
                                -cardHeight, DEFAULT_SWIPE_OFFSET)
                        swipeOffset.snapTo(newValue)
                      }
                    })
              }) {
        // --- RED BACKGROUND ---
        Box(
            modifier =
                Modifier.matchParentSize()
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd) {
              IconButton(
                  onClick = onClickDelete,
                  modifier =
                      Modifier.padding(end = PaddingExtraLarge)
                          .testTag(InvitationOverviewScreenTestTags.DELETE_INVITATION_BUTTON)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(SizeLarge))
                  }
            }

        // --- CARD FOREGROUND (slides with swipeOffset) ---
        Card(
            modifier =
                Modifier.offset {
                      IntOffset(
                          x = swipeOffset.value.roundToInt(), y = DEFAULT_SWIPE_OFFSET.toInt())
                    }
                    .fillMaxWidth(),
            shape = cardShape,
            elevation = CardDefaults.cardElevation(ElevationLow)) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SpacingExtraSmall),
                        modifier = Modifier.weight(WeightExtraHeavy)) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = invitation.code,
                                style = MaterialTheme.typography.titleLarge,
                                letterSpacing = LetterSpacingLarge,
                                fontSize = FontSizeExtraHuge)
                            IconButton(onClick = { onCopy(invitation.code) }) {
                              Icon(
                                  imageVector = Icons.Default.ContentCopy,
                                  contentDescription = null)
                            }
                          }

                          Column {
                            Text(
                                text =
                                    invitation.inviteeEmail
                                        ?.let { stringResource(R.string.accepted_by, it) }
                                        .orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic)

                            Spacer(modifier = Modifier.width(SpacingMedium))

                            Text(
                                text =
                                    invitation.acceptedAt
                                        ?.let {
                                          stringResource(
                                              R.string.accepted_on,
                                              DateTimeUtils.formatInstantToDateAndTime(it))
                                        }
                                        .orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic)
                          }
                        }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall)) {
                          Text(
                              text = invitation.status.displayString(),
                              style = MaterialTheme.typography.titleMedium,
                              color = invitation.status.displayColor(),
                              fontWeight = FontWeight.Bold)
                          IconButton(
                              onClick = {
                                scope.launch {
                                  if (swipeOffset.value < -swipeThresholds) {
                                    swipeOffset.animateTo(DEFAULT_SWIPE_OFFSET)
                                  } else {
                                    swipeOffset.animateTo(-cardHeight)
                                  }
                                }
                              },
                              modifier =
                                  Modifier.testTag(
                                      InvitationOverviewScreenTestTags
                                          .SWIPE_INVITATION_CARD_BUTTON)) {
                                Icon(
                                    imageVector =
                                        if (swipeOffset.value > -swipeThresholds) {
                                          Icons.AutoMirrored.Filled.KeyboardArrowLeft
                                        } else {
                                          Icons.AutoMirrored.Filled.KeyboardArrowRight
                                        },
                                    contentDescription = null)
                              }
                        }
                  }
            }
      }
}

@Preview(showBackground = true)
@Composable
fun InvitationCardPreview() {
  val org = getMockOrganizations().first()
  val invitation =
      Invitation(
          id = "id",
          code = "123456",
          organization = org,
          inviteeEmail = "alice@example.com",
          acceptedAt = Instant.now(),
          status = InvitationStatus.Used)
  InvitationCard(invitation)
}
