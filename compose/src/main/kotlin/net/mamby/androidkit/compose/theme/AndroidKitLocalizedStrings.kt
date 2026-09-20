package net.mamby.androidkit.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.R

/** Shared Kit vocabulary resolved using the current Android resource configuration. */
@Composable
@ReadOnlyComposable
internal fun localizedAndroidKitStrings(): AndroidKitStrings = AndroidKitStrings(
    supportPromptTitle = stringResource(R.string.androidkit_compose_support_prompt_title),
    supportPromptDescription = stringResource(R.string.androidkit_compose_support_prompt_description),
    supportPromptLearnMore = stringResource(R.string.androidkit_compose_support_prompt_learn_more),
    supportPromptNotNow = stringResource(R.string.androidkit_compose_support_prompt_not_now),
    supportPromptDonate = stringResource(R.string.androidkit_compose_support_prompt_donate),
    back = stringResource(R.string.androidkit_compose_back),
    add = stringResource(R.string.androidkit_compose_add),
    close = stringResource(R.string.androidkit_compose_close),
    more = stringResource(R.string.androidkit_compose_more),
    retry = stringResource(R.string.androidkit_compose_retry),
    cancel = stringResource(R.string.androidkit_compose_cancel),
    confirm = stringResource(R.string.androidkit_compose_confirm),
    save = stringResource(R.string.androidkit_compose_save),
    hideTitleBar = stringResource(R.string.androidkit_compose_hide_title_bar),
    showTitleBar = stringResource(R.string.androidkit_compose_show_title_bar),
    general = stringResource(R.string.androidkit_compose_general),
    language = stringResource(R.string.androidkit_compose_language),
    theme = stringResource(R.string.androidkit_compose_theme),
    transparency = stringResource(R.string.androidkit_compose_transparency),
    min = stringResource(R.string.androidkit_compose_min),
    max = stringResource(R.string.androidkit_compose_max),
    searchLanguages = stringResource(R.string.androidkit_compose_search_languages),
    noMatchingLanguages = stringResource(R.string.androidkit_compose_no_matching_languages),
    system = stringResource(R.string.androidkit_compose_system),
    security = stringResource(R.string.androidkit_compose_security),
    appLock = stringResource(R.string.androidkit_compose_app_lock),
    lockAfterLeavingApp = stringResource(R.string.androidkit_compose_lock_after_leaving_app),
    immediately = stringResource(R.string.androidkit_compose_immediately),
    afterOneMinute = stringResource(R.string.androidkit_compose_after_one_minute),
    afterFiveMinutes = stringResource(R.string.androidkit_compose_after_five_minutes),
    afterFifteenMinutes = stringResource(R.string.androidkit_compose_after_fifteen_minutes),
    lockNow = stringResource(R.string.androidkit_compose_lock_now),
    appInformation = stringResource(R.string.androidkit_compose_app_information),
    project = stringResource(R.string.androidkit_compose_project),
    website = stringResource(R.string.androidkit_compose_website),
    sourceCode = stringResource(R.string.androidkit_compose_source_code),
    openSource = stringResource(R.string.androidkit_compose_open_source),
    about = stringResource(R.string.androidkit_compose_about),
    aboutDescription = stringResource(R.string.androidkit_compose_about_description),
    version = stringResource(R.string.androidkit_compose_version),
    copyVersion = stringResource(R.string.androidkit_compose_copy_version),
    contact = stringResource(R.string.androidkit_compose_contact),
    contactDescription = stringResource(R.string.androidkit_compose_contact_description),
    legal = stringResource(R.string.androidkit_compose_legal),
    privacyPolicy = stringResource(R.string.androidkit_compose_privacy_policy),
    termsOfUse = stringResource(R.string.androidkit_compose_terms_of_use),
    thirdPartyLicenses = stringResource(R.string.androidkit_compose_third_party_licenses),
    thirdPartyLicensesDescription = stringResource(R.string.androidkit_compose_third_party_licenses_description),
)
