package net.mamby.androidkit.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.R

/** Shared Kit vocabulary resolved using the current Android resource configuration. */
@Composable
@ReadOnlyComposable
internal fun localizedAndroidKitStrings(): AndroidKitStrings = AndroidKitStrings(
    voiceInputError = stringResource(R.string.androidkit_compose_voice_input_error),
    openAppSettings = stringResource(R.string.androidkit_compose_open_app_settings),
    voiceStarting = stringResource(R.string.androidkit_compose_voice_starting),
    voiceListening = stringResource(R.string.androidkit_compose_voice_listening),
    voiceFinishing = stringResource(R.string.androidkit_compose_voice_finishing),
    voiceStop = stringResource(R.string.androidkit_compose_voice_stop),
    voicePermission = stringResource(R.string.androidkit_compose_voice_permission),
    voiceNoSpeech = stringResource(R.string.androidkit_compose_voice_no_speech),
    search = stringResource(R.string.androidkit_compose_search),
    clearSearch = stringResource(R.string.androidkit_compose_clear_search),
    voiceSearch = stringResource(R.string.androidkit_compose_voice_search),
    voiceSearchUnavailable = stringResource(R.string.androidkit_compose_voice_search_unavailable),
    supportPromptTitle = stringResource(R.string.androidkit_compose_support_prompt_title),
    supportPromptDescription = stringResource(R.string.androidkit_compose_support_prompt_description),
    supportPromptLearnMore = stringResource(R.string.androidkit_compose_support_prompt_learn_more),
    supportPromptNotNow = stringResource(R.string.androidkit_compose_support_prompt_not_now),
    supportPromptDonate = stringResource(R.string.androidkit_compose_support_prompt_donate),
    back = stringResource(R.string.androidkit_compose_back),
    close = stringResource(R.string.androidkit_compose_close),
    more = stringResource(R.string.androidkit_compose_more),
    hideTitleBar = stringResource(R.string.androidkit_compose_hide_title_bar),
    showTitleBar = stringResource(R.string.androidkit_compose_show_title_bar),
    language = stringResource(R.string.androidkit_compose_language),
    theme = stringResource(R.string.androidkit_compose_theme),
    transparency = stringResource(R.string.androidkit_compose_transparency),
    min = stringResource(R.string.androidkit_compose_min),
    max = stringResource(R.string.androidkit_compose_max),
    searchLanguages = stringResource(R.string.androidkit_compose_search_languages),
    noMatchingLanguages = stringResource(R.string.androidkit_compose_no_matching_languages),
    system = stringResource(R.string.androidkit_compose_system),
    appLock = stringResource(R.string.androidkit_compose_app_lock),
    lockAfterLeavingApp = stringResource(R.string.androidkit_compose_lock_after_leaving_app),
    lockNow = stringResource(R.string.androidkit_compose_lock_now),
    website = stringResource(R.string.androidkit_compose_website),
    sourceCode = stringResource(R.string.androidkit_compose_source_code),
    about = stringResource(R.string.androidkit_compose_about),
    version = stringResource(R.string.androidkit_compose_version),
    copyVersion = stringResource(R.string.androidkit_compose_copy_version),
    contact = stringResource(R.string.androidkit_compose_contact),
    contactDescription = stringResource(R.string.androidkit_compose_contact_description),
    privacyPolicy = stringResource(R.string.androidkit_compose_privacy_policy),
    termsOfUse = stringResource(R.string.androidkit_compose_terms_of_use),
    thirdPartyLicenses = stringResource(R.string.androidkit_compose_third_party_licenses),
)
