package net.mamby.androidkit.demo.ui.screen

import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCard
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardInteraction
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardEntry
import net.mamby.androidkit.demo.R

@Composable
internal fun SectionCardDemo() {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    val email = stringResource(R.string.section_card_sample_email)
    val otherEmail = stringResource(R.string.section_card_sample_other_email)
    val confirm = stringResource(R.string.action_confirm)
    AndroidKitSectionCard(
        title = stringResource(R.string.demo_section_title),
        description = stringResource(R.string.demo_supporting_text),
        entries = listOf(
            AndroidKitSectionCardEntry.Custom(
                key = "availability",
                interaction = AndroidKitSectionCardInteraction.Toggle(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.component_enabled), Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = null)
                }
            },
            AndroidKitSectionCardEntry.Info(
                key = "name",
                label = stringResource(R.string.section_card_sample_name),
                value = stringResource(R.string.sample_design),
            ),
            AndroidKitSectionCardEntry.Action(
                key = "email", label = email, actionLabel = confirm,
                onClick = { selected = email }, enabled = enabled,
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
            ),
        ),
    )
    Text(stringResource(R.string.demo_selected_action, selected ?: stringResource(R.string.demo_no_action)))
}
