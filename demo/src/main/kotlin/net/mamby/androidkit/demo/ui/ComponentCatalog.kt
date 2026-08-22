package net.mamby.androidkit.demo.ui

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import net.mamby.androidkit.demo.R

enum class ComponentCategory(
    @StringRes val labelResource: Int,
) {
    Layout(R.string.component_category_layout),
    Presentation(R.string.component_category_presentation),
    FormsAndSettings(R.string.component_category_forms_settings),
    FloatingActions(R.string.component_category_floating_actions),
    Navigation(R.string.component_category_navigation),
}

@Serializable
enum class ComponentId(
    val apiName: String,
    val category: ComponentCategory,
    @StringRes val descriptionResource: Int,
) {
    PageScaffold(
        apiName = "PageScaffold",
        category = ComponentCategory.Layout,
        descriptionResource = R.string.component_page_scaffold_description,
    ),
    FloatingTitleBar(
        apiName = "FloatingTitleBar",
        category = ComponentCategory.Layout,
        descriptionResource = R.string.component_floating_title_bar_description,
    ),
    AdaptiveGridPage(
        apiName = "AdaptiveGridPage",
        category = ComponentCategory.Layout,
        descriptionResource = R.string.component_adaptive_grid_page_description,
    ),
    DetailPage(
        apiName = "DetailPage",
        category = ComponentCategory.Layout,
        descriptionResource = R.string.component_detail_page_description,
    ),
    PageFloatingAction(
        apiName = "PageFloatingAction",
        category = ComponentCategory.Layout,
        descriptionResource = R.string.component_page_floating_action_description,
    ),
    StatePresentation(
        apiName = "StatePresentation",
        category = ComponentCategory.Presentation,
        descriptionResource = R.string.component_state_presentation_description,
    ),
    SectionCard(
        apiName = "SectionCard",
        category = ComponentCategory.Presentation,
        descriptionResource = R.string.component_section_card_description,
    ),
    LabeledValue(
        apiName = "LabeledValue",
        category = ComponentCategory.Presentation,
        descriptionResource = R.string.component_labeled_value_description,
    ),
    MetricCard(
        apiName = "MetricCard",
        category = ComponentCategory.Presentation,
        descriptionResource = R.string.component_metric_card_description,
    ),
    EditorSection(
        apiName = "EditorSection",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_editor_section_description,
    ),
    EditorFieldPair(
        apiName = "EditorFieldPair",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_editor_field_pair_description,
    ),
    SwitchField(
        apiName = "SwitchField",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_switch_field_description,
    ),
    ReadOnlyPickerField(
        apiName = "ReadOnlyPickerField",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_read_only_picker_field_description,
    ),
    StringListEditor(
        apiName = "StringListEditor",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_string_list_editor_description,
    ),
    FormDialog(
        apiName = "FormDialog",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_form_dialog_description,
    ),
    SettingsItem(
        apiName = "SettingsItem",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_settings_item_description,
    ),
    AndroidKitModalSheet(
        apiName = "AndroidKitModalSheet",
        category = ComponentCategory.FormsAndSettings,
        descriptionResource = R.string.component_modal_sheet_description,
    ),
    FloatingAddButton(
        apiName = "FloatingAddButton",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_add_button_description,
    ),
    FloatingActionBar(
        apiName = "FloatingActionBar",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_action_bar_description,
    ),
    FloatingActionBarIconItem(
        apiName = "FloatingActionBarIconItem",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_action_bar_icon_item_description,
    ),
    FloatingActionBarIconLabelItem(
        apiName = "FloatingActionBarIconLabelItem",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_action_bar_icon_label_item_description,
    ),
    FloatingActionBarTextItem(
        apiName = "FloatingActionBarTextItem",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_action_bar_text_item_description,
    ),
    FloatingActionBarFlyout(
        apiName = "FloatingActionBarFlyout",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_action_bar_flyout_description,
    ),
    FloatingDropdownMenu(
        apiName = "FloatingDropdownMenu",
        category = ComponentCategory.FloatingActions,
        descriptionResource = R.string.component_floating_dropdown_menu_description,
    ),
    AdaptiveNavigationScaffold(
        apiName = "AdaptiveNavigationScaffold",
        category = ComponentCategory.Navigation,
        descriptionResource = R.string.component_adaptive_navigation_scaffold_description,
    ),
}
