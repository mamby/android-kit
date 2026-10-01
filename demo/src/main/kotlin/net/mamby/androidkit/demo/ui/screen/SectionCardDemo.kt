package net.mamby.androidkit.demo.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCard
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardEntry
import net.mamby.androidkit.demo.R

@Composable
internal fun SectionCardDemo() {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    val email = stringResource(R.string.section_card_sample_email)
    val otherEmail = stringResource(R.string.section_card_sample_other_email)
    val confirm = stringResource(R.string.action_confirm)
    val share = stringResource(R.string.action_share)
    AndroidKitSectionCard(
        title = stringResource(R.string.demo_section_title),
        description = stringResource(R.string.demo_supporting_text),
        entries = listOf(
            AndroidKitSectionCardEntry.Toggle(
                key = "availability",
                label = stringResource(R.string.component_enabled),
                checked = enabled,
                onCheckedChange = { enabled = it },
            ),
            AndroidKitSectionCardEntry.Info(
                key = "name",
                label = stringResource(R.string.section_card_sample_name),
                value = stringResource(R.string.sample_design),
            ),
            AndroidKitSectionCardEntry.Action(
                key = "email", label = email, actionLabel = confirm,
                onClick = { selected = email }, enabled = enabled,
                contextMenu = { item(label = share, onClick = { selected = share }) },
            ),
            AndroidKitSectionCardEntry.Action(
                key = "other-email", label = otherEmail, actionLabel = confirm,
                onClick = { selected = otherEmail }, enabled = enabled,
            ),
            AndroidKitSectionCardEntry.Action(
                key = "unavailable", label = stringResource(R.string.action_share),
                actionLabel = stringResource(R.string.action_share),
                onClick = {}, enabled = false,
            ),
            AndroidKitSectionCardEntry.Multiline(
                key = "address", text = stringResource(R.string.section_card_sample_address),
            ),
            AndroidKitSectionCardEntry.Multiline(
                key = "notes", text = stringResource(R.string.section_card_sample_notes),
                contextMenu = { item(label = share, onClick = { selected = share }) },
            ),
        ),
    )
    Text(stringResource(R.string.demo_selected_action, selected ?: stringResource(R.string.demo_no_action)))
}
