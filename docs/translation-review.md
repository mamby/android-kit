# Shared vocabulary translation review

## Floating search (2026-09-21)

Inline dictation replaces the original external-dialog behavior. Added
`voice_starting`, `voice_listening`, `voice_finishing`, `voice_stop`,
`voice_permission`, and `voice_no_speech` (with the Kit prefix) in all 18 locales
listed below. English and translations were reviewed against the starting,
listening, finalizing, stop, permission-denial and no-match call sites. Stop ends
capture while allowing the final result; it does not submit a search. Permission
guidance explicitly refers to app settings, without promising permission or
offline support. These are editorial AI-assisted translations, not native-speaker
review. Existing demo instructions remain applicable and introduce no duplicate
Kit labels. Technical semantics were checked against Android's official
[SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer),
[recognition options](https://developer.android.com/reference/android/speech/RecognizerIntent),
and [permission guidance](https://developer.android.com/training/permissions/requesting).
No external linguistic reference is claimed; idiomatic phrasing and pronunciation
remain unverified by native speakers.

The catalog showcase also adds six demo-owned resources in English, French and
Arabic: page and sheet headings, usage guidance, the sheet-opening action, the
last-submitted query with `%1$s`, and an empty-results message. These were checked
against the two independent live-filtering examples and the keyboard submission
callback. Placeholder parity and action/heading roles are preserved. This is
AI-assisted editorial review, not native-speaker approval.

Added `search`, `clear_search`, `voice_search`, and `voice_search_unavailable`
(all with the `androidkit_compose_` prefix) in all 18 Compose language sets:
English, French, Arabic, German, Spanish, Hindi, Indonesian, Italian, Japanese,
Korean, Dutch, Polish, European Portuguese, Russian, Thai, Turkish, Vietnamese,
and Simplified Chinese. No demo duplicates were introduced.

For the original dialog implementation, English was checked against the component: Search is the input placeholder and
accessible name; Clear search empties the field; Search by voice opens external
speech UI; the error explains a failed speech launch and continued keyboard input.
The error deliberately promises neither offline recognition nor translation.
All translations were re-read against those roles and the English meaning,
including the continued availability of typed input. French uses Rechercher,
Effacer la recherche, and Rechercher à la voix. These new phrases are editorial,
AI-assisted translations; no external linguistic reference or native-speaker
approval is claimed. Idiomatic phrasing and screen-reader pronunciation in each
locale remain candidates for competent human review. XML/build coverage is
separate from language-quality assurance.

The existing localization contract already lists these locales and validates
all AAR string keys dynamically; new vocabulary requires no JSON schema change.

## About navigation summary (2026-09-20)

The new Kit-owned About-row subtitle combines the existing Contact, Version, and
Legal information concepts. English, French, Arabic, German, Spanish, Hindi,
Indonesian, Italian, Japanese, Korean, Dutch, Polish, European Portuguese,
Russian, Thai, Turkish, Vietnamese, and Simplified Chinese were updated. The
demo's optional About description was added in its English, French, and Arabic
language sets. These are editorial translations checked against their About-row
and subpage usage; they have not received native-speaker review.

## Settings reorganization (2026-09-15)

The settings-only sponsor title and description were removed in all 18 Compose
language sets; the separate reusable support-prompt copy remains. App info now
uses translated Legal and Version sections. The Open source entry gained concise
translated supporting text. The former Project repository, License, and
Contribute settings strings were removed because no shared component uses them.
All 18 language sets were reviewed against the English row and section roles.
These translations are AI-assisted and have not received native-speaker review.

The Open source description was subsequently refined to “Explore, use or
contribute” in all 18 language sets. It presents optional ways to engage without
claiming price or coupling the shared component to a repository host.
It is the clickable entry label beneath the Open source section title, rather
than supporting text beneath a duplicate Open source row label.
The temporary App info supporting description was removed from all language sets.
The Third-party licenses row also gained “Licenses for third-party software” as
supporting text in all 18 language sets, clarifying that the destination covers
software license notices rather than presenting a library browser.
The copyable Version entry adds a localized “Copy version” accessibility action
in all 18 language sets. The visible row remains the host-provided version value.

## Previous review: English and UX references (2026-09-14)

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

## 2026-09-20 Settings organization

Added About headings App information and Project, and destination labels Website
and Source code across all 18 supported locales (en, fr, ar, de, es, hi, id, it,
ja, ko, nl, pl, pt, ru, th, tr, vi, zh-Hans). Reused existing Open source strings.
Added demo-owned Appearance section titles in English, French, and Arabic.
Reviewed these as noun labels against their actual About/grouping call sites and
the existing glossary; French Code source follows the accepted glossary term.
No external references or native-speaker approval are claimed for this pass.
No English source correction was required. New wording is machine-assisted;
idiomatic quality, truncation, RTL and pronunciation remain unverified on device.
Resource/compilation validation is separate from linguistic assurance.

### About hint and heading refinement

The main About hint is now “Contact, legal and more”. Updated
androidkit_compose_about_description in all 18 supported locales. Reviewed as a
navigation subtitle listing destinations, not as a contact action or legal advice.
Retained each locale's contact/legal terminology where natural and replaced the
version reference with a general “and more” phrase. No placeholders or timing
claims are present. No demo duplicate of this hint exists; its similarly named
app-description resource is separate content and remains unchanged.
About section headings are hidden; cards, spacing, page title and row labels remain.
XML parsing and duplicate-key checks passed. Wording received an AI review against
English and the actual call site, not native-speaker approval. No new external
terminology references were used. Idiomatic quality and device layout remain
unverified; compilation is not linguistic assurance.

### Final unused-translation audit (2026-09-21)

The About hint was removed again; the earlier hint entry above is historical.
Audited all component modules and the demo for resource references, including
Kotlin/Java references, XML references, plurals and string arrays. Checked internal
AndroidKitStrings consumers rather than counting resolver assignments as UI usage.
Removed 17 unused internal fields and their resources, plus the obsolete Open
source description and App info label. Removed unreferenced demo strings across
English, French and Arabic. Kit removals cover all 18 supported locales.
No active translated wording was changed. XML parsing and source-reference checks
were used; no tests were executed and no linguistic or device review is claimed.

Also removed the unused legacy English JSON catalog and demo JSON translation
snapshot after confirming no build or host configuration consumes them. Earlier
review entries referring to these files are historical. The retained legacy
validator can still accept externally supplied catalogs, but current builds use
the sealed resource contract. Final source audit found no unreferenced translation
resources in any Kit module or demo; locale key sets align (31 Kit, 105 demo
including the nontranslatable app name).

## 2026-09-22 Floating search feedback

Added Voice input error (accessibility label for reopening the error explanation)
and Open app settings (action opening this app's system settings) in all 18 Kit
locales: en, fr, ar, de, es, hi, id, it, ja, ko, nl, pl, pt, ru, th, tr, vi,
zh-Hans. Reviewed the English reference and every translation in the component's
permission-error context. Existing Close and speech-status/error wording is reused;
no demo duplicates or localization-contract schema changes are needed. The AAR
resource gate discovers the two new keys. This was an AI contextual review, with
no external terminology references or native-speaker approval. Idiomatic quality
and all-locale device rendering remain unverified; build checks are separate.
