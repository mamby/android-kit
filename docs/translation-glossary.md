# Translation glossary

English resource values and their UX context define meaning. These are project
terminology decisions, not a claim that every term has native-speaker approval.
Keep the existing regional conventions: European Portuguese, Modern Standard
Arabic and Simplified Chinese. Do not impose English loanwords on every language.

## Meaning and French terminology

| English reference | UX context | French | Avoid |
| --- | --- | --- | --- |
| Feedback or questions | Subtitle below Contact; opinions, suggestions or questions about the app | Commentaires ou questions | Fil, flux, a store-rating command or a bug-only destination |
| Open source | Software licensing/development category | Open source | Code ouvert, source ouverte; confusing it with Source code |
| Source code | The software's source text | Code source | Using this for every project/repository link |
| Project repository | Project repository link, including more than code | Dépôt du projet | Dropping the repository meaning |
| Contribute | Action opening contribution guidance, including translation/design | Contribuer | Contributeurs (a list of people), developer-only wording |
| Contributors | People who contributed | Contributeurs | Contribuer (an action) |
| Third-party licenses | Licenses covering external software by individuals or organizations | Licences de tiers | Restricting third parties to companies |
| Support this app | Voluntary support | Soutenir cette application | Technical assistance, mandatory funding or urgency |
| Not now | Dismiss this prompt | Pas maintenant | A promise to remind the user later |
| Transparency | Visual surface transparency | Transparence | Reversing the meaning to opacity |
| System | Follow the device setting; a resolved value may be appended | Système | Restricting a generic label to system language |
| More | Overflow menu, also an accessibility label | Plus | Ajouter (a creation action) |
| Formatters | Utilities formatting dates, numbers and lists for a locale | Outils de formatage | Formateurs (people providing training) |
| Search settings | Title/action for global search across the Settings catalog | Rechercher dans les paramètres | A web search or searching only the current page |
| Recent searches | Previously submitted or activated Settings queries | Recherches récentes | Recently changed settings |
| Report a bug | Search alias that resolves to Contact | Signaler un bug | Claiming that Contact itself is a dedicated bug tracker |

The subtitle is a noun phrase, not a command to send a message. If a future
component actually submits feedback, its action needs its own English reference
and contextual translation. Never reuse a subtitle as an unrelated button label.

## Contact subtitle by language

The English reference is **Feedback or questions**. The entire phrases below are
editorial translations for our Contact row. The linked Chrome help pages were
consulted on 2026-09-14 for the feedback terminology, not as a source for the
complete phrase or as proof of the quality of every translation in a locale.

| Locale | Contact subtitle | Terminology reference |
| --- | --- | --- |
| ar | تعليقات أو أسئلة | [Chrome Arabic](https://support.google.com/chrome/answer/95315?hl=ar) |
| de | Feedback oder Fragen | [Chrome German](https://support.google.com/chrome/answer/95315?hl=de) |
| es | Comentarios o preguntas | [Chrome Spanish](https://support.google.com/chrome/answer/95315?co=GENIE.Platform%3DAndroid&hl=es) |
| fr | Commentaires ou questions | [Chrome French](https://support.google.com/chrome/answer/95315?hl=fr) |
| hi | फ़ीडबैक या सवाल | [Chrome Hindi](https://support.google.com/chrome/answer/95315?hl=hi) |
| id | Masukan atau pertanyaan | [Chrome Indonesian](https://support.google.com/chrome/answer/95315?hl=id) |
| it | Feedback o domande | [Chrome Italian](https://support.google.com/chrome/answer/95315?hl=it) |
| ja | フィードバックまたは質問 | [Chrome Japanese](https://support.google.com/chrome/answer/95315?hl=ja) |
| ko | 의견 또는 질문 | [Chrome Korean](https://support.google.com/chrome/answer/95315?hl=ko) |
| nl | Feedback of vragen | [Chrome Dutch](https://support.google.com/chrome/answer/95315?hl=nl) |
| pl | Opinie lub pytania | [Chrome Polish](https://support.google.com/chrome/answer/95315?hl=pl) |
| pt | Comentários ou perguntas | [Chrome European Portuguese](https://support.google.com/chrome/answer/95315?hl=pt) |
| ru | Отзывы или вопросы | [Chrome Russian](https://support.google.com/chrome/answer/95315?co=GENIE.Platform%3DAndroid&hl=ru) |
| th | ความคิดเห็นหรือคำถาม | [Chrome Thai](https://support.google.com/chrome/answer/95315?hl=th) |
| tr | Geri bildirim veya sorular | [Chrome Turkish](https://support.google.com/chrome/answer/95315?hl=tr) |
| vi | Phản hồi hoặc câu hỏi | [Chrome Vietnamese](https://support.google.com/chrome/answer/95315?hl=vi) |
| zh-Hans | 反馈或问题 | [Chrome Simplified Chinese](https://support.google.com/chrome/answer/95315?hl=zh-Hans) |

## Additional evidence and contextual decisions

- French feedback terminology is corroborated by
  [Microsoft Feedback Hub](https://support.microsoft.com/fr-fr/windows/envoyer-des-commentaires-%C3%A0-microsoft-avec-l-application-hub-de-commentaires-f59187f8-8739-22d6-ba93-f66612949332).
  This supports the term Commentaires, without turning Contact into a bug-report
  workflow or promising an immediate reply.
- [Mozilla's French Firefox terms](https://www.mozilla.org/fr/about/legal/terms/firefox/)
  distinguish source code from open-source licenses, supporting the distinction
  between Code source and Open source.
- Thai Contribute uses **มีส่วนร่วม**, a broad participation action. The verb is
  also used by [AOSP contribution documentation](https://source.android.com/docs/setup/contribute?authuser=0&hl=th).
  Omitting a development-only qualifier is our contextual choice to include
  translation and design work.
- Arabic Localization uses **التوطين**, also used in
  [Microsoft's localization discussion](https://learn.microsoft.com/ar-sa/training/support/integrations-learn-platform-api-faq).
  This demo covers multiple languages and regional formatting, not only Arabic
  translation (التعريب) or a language selector (اللغة).
- In the Arabic demo, visibility means whether the sheet is shown, and chrome
  means its surrounding interface elements. Neither means eyesight or only a
  bar. Catalog uses الكتالوج consistently in the settings title and preview.
  These are editorial decisions based on the English text and component usage.

Official help pages can themselves include machine translations. Use them as
evidence of specific terminology, inspect context, and seek other product or
competent human evidence if wording remains doubtful. No single source certifies
an entire language. See [the required workflow](localization.md#translation-quality-workflow).
