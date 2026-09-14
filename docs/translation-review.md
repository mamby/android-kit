# Shared vocabulary translation review

Reviewed on 2026-09-14. Scope: all 46 resource strings in each of the 18
supported language sets (828 entries, including the English reference).
This covers the published Compose vocabulary, including the separate support
prompt files. Demo content and the legacy English catalog are outside this scope.

This is an AI-assisted linguistic and contextual review, not independent
native-speaker certification. The checks below establish resource integrity;
they cannot establish that every phrase is idiomatic in every region. Device
layout, TalkBack pronunciation, and native-speaker acceptance remain unverified.

## Review method

Each entry was compared with English for meaning, grammar, software terminology,
button versus heading usage, consistency, punctuation, and donation optionality.
The resource resolver and component call sites were inspected for ambiguous terms.
Existing acceptable wording was retained rather than rewritten for variety.

All keys were reviewed in these groups:

- Actions: back, add, close, more, retry, cancel, confirm, save.
- Appearance: hide_title_bar, show_title_bar, general, language, theme,
  transparency, min, max, search_languages, no_matching_languages, system.
- Security: security, app_lock, lock_after_leaving_app, immediately,
  after_one_minute, after_five_minutes, after_fifteen_minutes, lock_now.
- App information: support_title, support_description, about, version,
  open_source, contact, app_info, app, privacy_policy, terms_of_use,
  third_party_licenses, source_code, license, contributors.
- Donation prompt: support_prompt_title, support_prompt_description,
  support_prompt_learn_more, support_prompt_not_now, support_prompt_donate.

Resource names above omit the common `androidkit_compose_` prefix.

## Terminology and behavior

- Open source is a software term, distinct from source code. French uses
  **Open source**, replacing **Code ouvert**. This agrees with
  [Mozilla's French usage](https://www.mozilla.org/fr/firefox/linux/) and
  [AOSP's French terminology](https://source.android.com/docs/setup/about?hl=fr).
  Established local terms remain appropriate in other languages; the English
  phrase is not imposed on every locale. Examples retained include Hindi
  **ओपन सोर्स**, also used by
  [Google's Open Source Programs Office](https://developers.google.com/open-source/ghop/faqs?hl=hi),
  and Thai **โอเพนซอร์ส**, used in
  [Google's product documentation](https://support.google.com/product-documentation?hl=th).
- Not now calls `onDismiss`. It neither schedules a reminder nor promises a
  future donation. Indonesian, Japanese, Korean and Vietnamese wording now
  expresses present refusal rather than later action. Thai wording is shortened
  to remove an unnecessary temporal qualifier. These are contextual editorial
  decisions, not claims that the former words are invalid in every UI.
- Lock after leaving the app labels a delay picker, not a command to terminate
  the app or necessarily lock immediately. Spanish now explicitly says after.
- Third-party licenses includes individual authors as well as companies.
  Polish no longer restricts the label to other companies. French uses the more
  explicit Licences de tiers.
- Contributors includes translation, design and other contributions. Thai no
  longer limits the label to co-developers.
- Support copy must remain voluntary, with no urgency or implied loss of access.
  Redundant help-support constructions were simplified. Chinese, Russian and
  Thai donation descriptions state optionality directly and limit that statement
  to donations, rather than implying the app has no requirements of any kind.
- Portuguese retains its existing European Portuguese vocabulary (Guardar,
  aplicação, Contacto, donativo). A base-language resource resolving for pt-BR
  is fallback coverage, not a separate Brazilian Portuguese editorial review.
- Arabic retains Modern Standard Arabic. Simplified Chinese remains zh-Hans;
  this review does not add Traditional Chinese or new regional translations.

The sources above corroborate selected terms only. They do not certify the
remaining translations. Translator context comments follow
[Android's localization guidance](https://developer.android.com/guide/topics/resources/localization#context).

## Per-language results

All 46 entries were reviewed for each row. Counts refer to changed string values,
excluding English context comments.

| Language | Changed | Decisions |
| --- | ---: | --- |
| English | 0 | Reference meaning retained; translator context added. |
| Arabic | 0 | Existing terminology, timings and voluntary donation meaning retained. |
| German | 1 | Remove redundant support phrasing. |
| Spanish | 1 | Make the after-leaving timing explicit. |
| French | 2 | Open source and Licences de tiers. |
| Hindi | 0 | Existing UI terms, minute forms and donation meaning retained. |
| Indonesian | 2 | Natural continued-development copy; present dismissal. |
| Italian | 2 | Natural app-exit phrasing; remove redundant support phrasing. |
| Japanese | 1 | Present dismissal without a later-action promise. |
| Korean | 1 | Present dismissal without a later-action promise. |
| Dutch | 2 | Restore article in app-exit phrase; simplify support copy. |
| Polish | 2 | Simplify support copy; include non-company license owners. |
| Portuguese | 1 | Simplify support copy, preserving existing regional vocabulary. |
| Russian | 2 | Simplify support copy; idiomatic voluntary-donation statement. |
| Thai | 3 | Broader contributors term; precise donation optionality; concise dismissal. |
| Turkish | 0 | Existing software terms, timings and donation meaning retained. |
| Vietnamese | 2 | Simplify continued-development copy; present dismissal. |
| Simplified Chinese | 2 | Simplify support copy; express voluntary donation positively. |
| Total | 24 | 14 translated resource sets changed. |

## Validation boundary

Resource inspection checks XML parsing, identical key sets across locales,
duplicate and empty entries, and unexpected replacement or directional control
characters. All sets have 46 entries. The current vocabulary has no format
placeholders or plurals; the fixed delay strings retain 1, 5 and 15 minutes,
including the spelled-out Arabic singular.

The initial `:compose:lintDebug` and `:demo:assembleDebug` run succeeded (lint:
0 errors, 10 warnings concerning the existing Indonesian qualifier and Kotlin
style). After the final Italian copy refinement and English context comments,
the follow-up reached `:demo:assembleDebug`; its lint rerun was interrupted at
the user's request to avoid unnecessary testing. The contract regression suite
was not reached. Resource-key and fixed-duration checks passed.

The existing `test/gradle/verify-localization.ps1` suite tests the packaging
contract, not translation quality, and is not necessary for routine wording
changes. Build checks do not replace linguistic or device review.

Future wording changes should repeat contextual review for every affected locale
and retain the distinction between editorial review and native-speaker approval.
