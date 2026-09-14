# Page support prompt

Use the list-content overload of AndroidKitPage for a Kit-rendered support card
that is the first item in the same LazyColumn as app content. The existing
composable-body overload remains available.

```kotlin
AndroidKitPage(
    title = pageTitle,
    listState = listState,
    supportPrompt = if (eligible) AndroidKitSupportPrompt(
        id = presentationId,
        onDonate = onOpenDonationDestination,
        onDismiss = onPromptDismissed,
    ) else null,
    listContent = {
        items(records, key = { it.id }) { record -> RecordCard(record) }
    },
)
```

The page owns list padding and adds its measured chrome/inset clearance to
contentPadding. The viewport stays edge-to-edge. Body items should use stable
keys; reserve the androidkit:page:support: key prefix for Kit.
The prompt scrolls away and never opens a sheet automatically.

Learn more opens the shared sheet. Not now, Close, Back, scrim, and swipe
dismissal end the presentation and invoke onDismiss once. Donate ends the
presentation and invokes onDonate once; it does not indicate successful payment.
Disabled prompts allow dismissal but cannot open the sheet or invoke donation.
Transient state survives configuration changes. Removing the configuration
removes the sheet. Hosts supply a new ID for each new eligible presentation.

Hosts own eligibility, cooldowns, persisted dismissal, confirmed-donor suppression,
and the destination. Only opt appropriate pages in. There is no payment provider,
analytics, automatic scheduling, or persistent policy inside Kit.

Kit owns the wording and translations in its private androidkit_compose_support_prompt_*
resources. Kit supplies the languages listed in [localization](localization.md). Hosts cannot override the
translations; new languages must be added to Kit first. There are no per-prompt
text or rendering slots. supportCardStyle customizes the card; the sheet uses
the existing bottomSheetStyle theme token.

The existing Settings support footer remains an independent persistent entry.
The Page demo has a Support prompt toggle; its donation action shows a preview
message and collects no payment.
