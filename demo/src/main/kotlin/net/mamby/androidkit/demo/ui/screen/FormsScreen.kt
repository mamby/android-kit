package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.action.FloatingActionBar
import net.mamby.androidkit.compose.action.FloatingActionBarTextItem
import net.mamby.androidkit.compose.form.EditorFieldPair
import net.mamby.androidkit.compose.form.EditorSection
import net.mamby.androidkit.compose.form.FormDialog
import net.mamby.androidkit.compose.form.StringListEditor
import net.mamby.androidkit.compose.form.SwitchField
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.demo.R

@Composable
fun FormsScreen() {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var reminders by rememberSaveable { mutableStateOf(true) }
    var newsletter by rememberSaveable { mutableStateOf(false) }
    var publicProfile by rememberSaveable { mutableStateOf(true) }
    val initialTags = listOf(
        stringResource(R.string.tag_design),
        stringResource(R.string.tag_compose),
        stringResource(R.string.tag_android),
        stringResource(R.string.tag_accessibility),
        stringResource(R.string.tag_adaptive),
    )
    val tags = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() },
        ),
    ) { initialTags.toMutableStateList() }
    var dialogVisible by rememberSaveable { mutableStateOf(false) }

    PageScaffold(
        title = stringResource(R.string.forms_title),
        floatingActionButton = {
            FloatingActionBar {
                FloatingActionBarTextItem(
                    onClick = { dialogVisible = true },
                    label = stringResource(R.string.show_dialog),
                )
            }
        },
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            EditorSection(
                title = stringResource(R.string.profile_section),
                description = stringResource(R.string.profile_section_description),
            ) {
                EditorFieldPair(
                    first = {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.field_name)) },
                            singleLine = true,
                        )
                    },
                    second = {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.field_email)) },
                            singleLine = true,
                        )
                    },
                )
                SwitchField(
                    title = stringResource(R.string.reminders),
                    checked = reminders,
                    onCheckedChange = { reminders = it },
                    supportingText = stringResource(R.string.reminders_description),
                )
            }
            EditorSection(
                title = stringResource(R.string.work_section),
                description = stringResource(R.string.work_section_description),
            ) {
                EditorFieldPair(
                    first = {
                        OutlinedTextField(
                            value = company,
                            onValueChange = { company = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.field_company)) },
                            singleLine = true,
                        )
                    },
                    second = {
                        OutlinedTextField(
                            value = role,
                            onValueChange = { role = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.field_role)) },
                            singleLine = true,
                        )
                    },
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_phone)) },
                    singleLine = true,
                )
            }
            EditorSection(
                title = stringResource(R.string.address_section),
                description = stringResource(R.string.address_section_description),
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_address)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_city)) },
                    singleLine = true,
                )
            }
            EditorSection(
                title = stringResource(R.string.tags_section),
                description = stringResource(R.string.tags_description),
            ) {
                StringListEditor(
                    values = tags,
                    label = stringResource(R.string.tag_hint),
                    onValuesChange = {
                        tags.clear()
                        tags.addAll(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            EditorSection(
                title = stringResource(R.string.preferences_section),
                description = stringResource(R.string.preferences_section_description),
            ) {
                SwitchField(
                    title = stringResource(R.string.newsletter),
                    checked = newsletter,
                    onCheckedChange = { newsletter = it },
                    supportingText = stringResource(R.string.newsletter_description),
                )
                SwitchField(
                    title = stringResource(R.string.public_profile),
                    checked = publicProfile,
                    onCheckedChange = { publicProfile = it },
                    supportingText = stringResource(R.string.public_profile_description),
                )
            }
            EditorSection(
                title = stringResource(R.string.notes_section),
                description = stringResource(R.string.notes_section_description),
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_notes)) },
                    minLines = 4,
                )
            }
            ScrollTestContent()
        }
    }

    if (dialogVisible) {
        FormDialog(
            title = stringResource(R.string.dialog_title),
            onConfirm = { dialogVisible = false },
            onDismiss = { dialogVisible = false },
        ) {
            Text(stringResource(R.string.dialog_body))
        }
    }
}
