package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
    var reminders by rememberSaveable { mutableStateOf(true) }
    val tags = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() },
        ),
    ) { mutableStateListOf<String>() }
    var dialogVisible by rememberSaveable { mutableStateOf(false) }

    PageScaffold(
        title = stringResource(R.string.forms_title),
        subtitle = stringResource(R.string.forms_subtitle),
        floatingActionButton = {
            FloatingActionBar {
                Button(onClick = { dialogVisible = true }) {
                    Text(stringResource(R.string.show_dialog))
                }
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
