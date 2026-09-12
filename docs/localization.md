# Shared localization

AndroidKit owns the canonical English vocabulary for concepts shared by its
components. Host applications own translations for the locales they support.

The published `localization` artifact contains
`androidkit-english-catalog.json` in its assets. The catalog includes every
user-visible English default owned by AndroidKit components, including shared
actions, title-bar actions, settings section/entry labels, picker labels,
slider labels, and app-lock labels. Hosts must import that catalog
when updating AndroidKit and keep a checked-in translation snapshot containing
the catalog revision for every translated key.

The host localization check must fail when:

- a catalog key is missing from the host snapshot;
- the English value or revision changed without a corresponding translation
  update; or
- a supported host locale has no translation, unless that locale explicitly
  opts into English fallback.

Do not rename a shared key to reflect an app's preferred wording. If the
concept is different, use a separate host-owned key. For example,
`androidkit_settings_transparency` is the shared name for the floating-surface
setting, regardless of whether an app's internal implementation uses alpha or
opacity terminology.

The catalog does not include host data passed through component APIs: app names,
domain-specific setting labels, supported language/theme option names, app-lock
error messages, descriptions, URLs, or action destinations. Those remain
localized by the host because their meaning belongs to the application.

Hosts translate the catalog through `AndroidKitStrings` and pass the translated
value set to `AndroidKitTheme`. Shared settings components use those values when
their corresponding label is omitted; an explicit value remains available for
an intentional product-specific override.

## CI enforcement

Apply `gradle/validate-androidkit-localization.gradle.kts` in the host and set
these Gradle properties to checked-in files:

```properties
androidKitLocalizationCatalog=path/to/androidkit-english-catalog.json
androidKitLocalizationSnapshot=path/to/androidkit-translations.json
```

The snapshot records the catalog revision and translations for each supported
locale:

```json
{
  "locales": ["en", "fr", "ar"],
  "strings": {
    "androidkit_settings_transparency": {
      "revision": 1,
      "translations": {
        "fr": "Transparence",
        "ar": "الشفافية"
      }
    }
  }
}
```

The validator is attached to `check` and fails when AndroidKit adds a key,
changes a revision, removes a key, or lacks a translation for a supported host
locale.

The catalog is a localization contract, not a request for AndroidKit to ship
every host language. A changed English value is intentionally a required host
localization review.
