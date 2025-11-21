package com.android.sample.ui.replacement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.android.sample.R
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightExtraHeavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Member> MemberSelectionCard(
    modifier: Modifier = Modifier,
    members: List<Member>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedMember: Member?,
    onMemberSelected: (Member) -> Unit,
    memberDisplayName: (Member) -> String?,
    memberEmail: (Member) -> String?,
    memberId: (Member) -> String
) {
    val filteredMembers =
        members.filter { member ->
            val query = searchQuery
            (memberDisplayName(member)?.contains(query, ignoreCase = true) == true) ||
                    (memberEmail(member)?.contains(query, ignoreCase = true) == true) ||
                    (memberId(member).contains(query, ignoreCase = true))
        }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = DefaultCardElevation),
        shape = RoundedCornerShape(CornerRadiusLarge)
    ) {
        Column(Modifier.fillMaxSize()) {

            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(text = stringResource(R.string.search_member)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(ReplacementOrganizeTestTags.SEARCH_BAR),
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription =
                            stringResource(R.string.search_icon_content_description)
                    )
                },
                shape = RoundedCornerShape(CornerRadiusLarge),
            )

            LazyColumn(
                modifier =
                    Modifier
                        .weight(WeightExtraHeavy)
                        .fillMaxWidth()
                        .testTag(ReplacementOrganizeTestTags.MEMBER_LIST),
            ) {
                items(filteredMembers) { member ->
                    val isSelected = member == selectedMember

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) GeneralPalette.Secondary else Color.White
                                )
                                .clickable { onMemberSelected(member) }
                                .padding(vertical = PaddingMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = memberEmail(member) ?: memberId(member),
                            textAlign = TextAlign.Center,
                        )
                    }

                    HorizontalDivider(
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                }
            }

            OutlinedTextField(
                value = stringResource(
                    R.string.selected_member,
                    (selectedMember?.let { memberEmail(it) } ?: "")
                ),
                onValueChange = {}, // readOnly
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(ReplacementOrganizeTestTags.SELECTED_MEMBER_INFO),
                singleLine = true,
                shape = RoundedCornerShape(CornerRadiusLarge),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                    ),
                readOnly = true,
            )
        }
    }
}