# Shared vocabulary translation review

## Current review: English and UX references (2026-09-14)

This review supersedes the scope and counts of the historical review below.
It follows [the localization workflow](localization.md#translation-quality-workflow)
and records durable decisions in [the glossary](translation-glossary.md).

### Coverage

- Published Compose resources: all 47 keys in each of 18 language sets, including
  the English reference and support prompt resources (846 entries).
- Demo resources: all 146 translatable keys in English, French and Arabic (438
  entries), plus the intentionally untranslated Android Kit app name.
- Legacy `demo/androidkit-translations.json`: all 34 entries in French and Arabic
  (68 translations), compared with `localization/src/main/assets/androidkit-english-catalog.json`.
  This snapshot remains a legacy artifact; it is not the runtime source of truth.

Every entry was read against English for meaning, grammar, UX role, register and
consistency. Contact and contribution call sites, the content-managed sheet and
the localization demo were inspected to resolve context. A separate pass checked
the corrected wording against those meanings, glossary entries and references.
This was AI-assisted review, not an independent human or native-speaker review.

The current Kit key inventory is the historical inventory below with `source_code`
replaced by `project_repository`, `contributors` replaced by `contribute`, and
`contact_description` added. The previous report alone therefore did not cover
the current vocabulary.

### Corrections in this pass

| Area | Changed values | Reason |
| --- | ---: | --- |
| Kit English | 1 | Correct Feed or questions to Feedback or questions. |
| Kit Arabic, German, Spanish, French, Hindi, Indonesian, Italian, Japanese, Korean, Dutch, Polish, Portuguese, Russian, Turkish, Vietnamese, Simplified Chinese | 1 each | Restore user feedback, not a news feed, stream or channel. |
| Kit Thai | 2 | Correct feedback and broaden Contribute beyond development. |
| Demo French | 4 | Open source, Licences de tiers, preserve the action meaning in the support preview, and replace the false friend formateurs with outils de formatage. |
| Demo Arabic | 4 | Use localization consistently for the multilingual screen/navigation, clarify sheet visibility and chrome, and keep catalog terminology consistent. |
| Legacy French | 0 | Existing wording retained. |
| Legacy Arabic | 2 | Keep System generic and express open-source software development explicitly. |
| Total | 29 | 28 translated values and one English source correction. |

The user identified Feedback as the intended meaning. The actual source had
Feed or questions, which had propagated into all 17 translations. The Contact
renderer uses this as supporting text on a contact link; it does not display a
news feed or submit an app-store rating. The English reference and translator
comment now make that distinction explicit. Contribution guidance also has an
action-specific comment instead of the former description of a list of people.

Feedback terminology was checked in localized Chrome help for all 17 languages;
French was additionally corroborated with Microsoft Feedback Hub. The glossary
links each consulted page and the Mozilla, AOSP and Microsoft references for
other ambiguous terms. These sources support selected terms, not every sentence
or an assertion that every language is equally idiomatic. The remaining
translations were retained after contextual review; no change was made merely
to vary acceptable wording. Existing voluntary donation meaning, timing values,
regional conventions and formatting arguments were preserved.

### Verification

XML parsing, identical translatable key sets per locale, duplicate/empty values,
replacement characters and positional format arguments passed for Kit and demo.
Legacy JSON parsing and revision alignment with its English reference passed.
After the final resource pass, `:compose:lintDebug :demo:assembleDebug` completed
with `BUILD SUCCESSFUL` (exit 0). The demo packaging localization gate passed;
lint retained the existing locale-folder and Compose style findings. No automated
test source was added and no instrumentation or screenshot suite was run.
No device layout, TalkBack or native-speaker validation was performed, and no
claim of those checks follows from resource integrity or compilation.

## Historical review before the current settings vocabulary

The remaining sections describe the earlier pass and its validation at that time,
not additional changes or fresh validation in the current pass.

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
