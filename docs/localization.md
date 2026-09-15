# Kit-owned translations

AndroidKit owns and translates its shared component vocabulary. Components resolve
private Android resources internally from the current application configuration.
Hosts cannot supply a string set or override predefined labels through Kit APIs.
Application content, custom actions, option values, and domain messages remain
host-owned. The App info title and built-in More/Close controls are Kit-owned.

This is a breaking API change: remove `AndroidKitTheme(strings = ...)`,
`AndroidKitStrings`, `AndroidKitThemeTokens.strings`, and toolbar flyout
`contentDescription` overrides. Use host resources for host content.
Do not pass a custom title to the predefined AppInfo settings configuration.

## Required build enforcement

Every consuming application must apply the supplied Gradle script after its
Android application plugin (AGP 8.4 or newer):

```kotlin
apply(from = rootProject.file("gradle/validate-androidkit-resources.gradle"))
```

Declare every application-supported language in the host's Gradle properties:

```properties
androidKitSupportedLocales=en,fr,ar
```

Keep this declaration aligned with the app's language picker and Android locale
configuration. The script validates each variant's static and generated resource
directories and its resolved dependency AARs. The contract is read from the actual
Compose AAR, so a separate copied catalog cannot drift from the consumed version.

For local Android project dependencies, expose their complete AAR through a
consumable variant with usage `androidkit-localization`. The four Kit library
build files show the public `SingleArtifact.AAR` wiring. Every local project in
the application dependency graph must expose this verification variant; missing
variants fail resolution. Published Maven AAR dependencies need no producer setup.

Validation fails for:

- any host or other dependency resource definition using the reserved `androidkit_`
  prefix, including value aliases and configuration-specific overrides;
- a declared host language, host resource locale, or locale-config entry unsupported
  by Kit;
- a missing or incompatible Kit contract, or incomplete bundled translations.

The validation task is a required generated-assets dependency of APK/AAB packaging
and is also attached to `check` and variant lint tasks. It generates an empty
assets directory; it adds no runtime assets to the application.
A standalone Kotlin compile is not a full packaging/contract check.

Android itself still merges app and library resources with app precedence.
Private resources and this build gate enforce the project contract, not protection
against an app author deliberately removing validation or modifying the library.
Applying the script is mandatory for supported integration; merely adding an AAR
cannot install build logic into the consuming project.

## Translation quality workflow

Apply this workflow to Kit and demo resources. English in each module's `values`
directory is the semantic reference, including its translator comments; another
translation or a legacy catalog must not become the source language.

1. Read the English text alongside the call site: heading, action, accessibility
   label, subtitle, destination, or state. Establish what happens when activated.
   Correct an evident source error before translating when its intended meaning
   is established; record the reason and review every affected locale.
2. Consult [the glossary](translation-glossary.md). For ambiguous vocabulary, use
   official platform terminology first, then compare primary documentation or
   localized UI resources from established apps for an equivalent UX context.
   Seek corroboration across products; do not copy an entire sentence merely
   because one popular app uses it. Record exact links and distinguish sourced
   terms from editorial choices. A source can itself contain translation errors.
3. Preserve meaning, register and regional conventions, not English word order.
   Keep nouns for headings and appropriate verbs for actions. Preserve numeric
   values, placeholders, optionality and the absence of promises such as reminders.
   Keep accepted wording unless there is a concrete semantic or UX problem.
4. Perform a separate review pass against English and the call site: look for
   omissions, added promises, false friends, unnatural wording and inconsistent
   terms. Check duplicates in the demo. Record locale/key coverage, corrections,
   references and uncertainties in [the review](translation-review.md). A second
   AI pass is not independent human or native-speaker approval; obtain competent
   human review when claiming that level of assurance.
5. Check XML, duplicate/missing keys and format arguments, then run focused Android
   resource/build checks. Separately inspect the interface for truncation, RTL,
   larger text and accessibility pronunciation when device review is available.
   Report linguistic, build and device evidence separately. A passing build never
   establishes that a translation is idiomatic.

The glossary records project terminology decisions. Updating it and these
instructions guides future edits; it is not an automated semantic build gate.

## Language coverage and maintenance

Kit ships English, French, Arabic, German, Spanish, Hindi, Indonesian, Italian,
Japanese, Korean, Dutch, Polish, Portuguese, Russian, Thai, Turkish, Vietnamese,
and Simplified Chinese resources. New translations are machine-assisted and have
not received native-speaker review. A neutral
translation such as French also covers French regional locales. To support a new
language, add complete Kit translations, update
`compose/src/main/assets/androidkit-localization-contract.json`, and release Kit
before enabling that language in a host. Unsupported declared languages fail the
build instead of silently accepting English fallback.

Android's English fallback remains available for unexpected device configurations.
Hosts still choose and persist their application language with official locale
APIs. Host language selection must not advertise languages absent from its declared
supported set.

Canonical text lives in `compose/src/main/res/values`, with matching resources in
each supported locale directory. Every internal `AndroidKitStrings` field is required
and resolved in `AndroidKitLocalizedStrings.kt`; there are no Kotlin English
defaults. Keep all translations generic and free of demo branding. Kit resources
are private through `values/public.xml`.

See [the translation review](translation-review.md) for terminology decisions,
per-language review coverage, and the distinction between resource validation and
native-speaker approval. Preserve the translator context comments in the English
resources when updating translations.

The previous English catalog and host snapshot validator are legacy artifacts.
They do not satisfy the new contract; replace their application with the new
validator. Existing files are retained for older consumers.

Run `./test/gradle/verify-localization.ps1` for the packaging-gate regression suite.
Fixtures stay under `test`; generated fixture outputs stay under `demo/build`.

See [Android library resource precedence](https://developer.android.com/studio/projects/android-library)
and [Compose resources](https://developer.android.com/develop/ui/compose/resources).
