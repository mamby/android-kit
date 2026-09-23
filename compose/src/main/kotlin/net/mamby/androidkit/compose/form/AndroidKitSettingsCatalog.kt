package net.mamby.androidkit.compose.form

import java.util.IllformedLocaleException
import java.util.Locale
import net.mamby.androidkit.compose.action.AndroidKitActionItem

/** Additional localized terms used only for Settings search. */
public data class AndroidKitSettingsSearchTerms(
    public val byLanguageTag: Map<String, List<String>>,
) {
    init {
        require(byLanguageTag.keys.all(::isSettingsSearchLanguageTag)) {
            "Search language tags must be well-formed BCP-47 tags."
        }
        require(byLanguageTag.values.flatten().all(String::isNotBlank)) { "Search terms must not be blank." }
    }
}

private fun isSettingsSearchLanguageTag(tag: String): Boolean {
    if (tag.isBlank()) return false
    return try {
        Locale.Builder().setLanguageTag(tag).build()
        true
    } catch (_: IllformedLocaleException) {
        false
    }
}

/** Controlled navigation and persistent recent-query state for Settings search. */
public data class AndroidKitSettingsSearchConfiguration(
    public val onOpenSearch: () -> Unit,
    public val recentQueries: List<String>,
    public val onRecentQueriesChange: (List<String>) -> Unit,
)

/** A complete Settings hierarchy used by regular pages and global search. */
public class AndroidKitSettingsCatalog internal constructor(
    internal val search: AndroidKitSettingsSearchConfiguration,
    internal val pages: List<AndroidKitSettingsCatalogPage>,
) {
    internal val pagesByKey: Map<String, AndroidKitSettingsCatalogPage> = pages.associateBy { it.key }
    internal val main: AndroidKitSettingsCatalogPage.Main = pages.filterIsInstance<AndroidKitSettingsCatalogPage.Main>().single()
    internal val about: AndroidKitSettingsCatalogPage.About? = pages.filterIsInstance<AndroidKitSettingsCatalogPage.About>().singleOrNull()

    public val mainPageKey: String get() = main.key
    public val aboutPageKey: String? get() = about?.key
}

/** Builds one validated Settings hierarchy. */
public fun androidKitSettingsCatalog(
    search: AndroidKitSettingsSearchConfiguration,
    content: AndroidKitSettingsCatalogScope.() -> Unit,
): AndroidKitSettingsCatalog {
    val pages = AndroidKitSettingsCatalogScope().apply(content).pages
    require(pages.count { it is AndroidKitSettingsCatalogPage.Main } == 1) {
        "A Settings catalog requires exactly one main page."
    }
    require(pages.count { it is AndroidKitSettingsCatalogPage.About } <= 1) {
        "A Settings catalog supports at most one About page."
    }
    val keys = pages.map { it.key }
    require(keys.size == keys.distinct().size) { "Settings catalog page keys must be unique." }
    return AndroidKitSettingsCatalog(search, pages)
}

@AndroidKitSettingsPageDsl
public class AndroidKitSettingsCatalogScope internal constructor() {
    internal val pages: MutableList<AndroidKitSettingsCatalogPage> = mutableListOf()

    public fun main(
        key: String,
        title: String,
        actions: List<AndroidKitActionItem> = emptyList(),
        searchTerms: AndroidKitSettingsSearchTerms? = null,
        content: AndroidKitSettingsPageScope.() -> Unit = {},
    ) {
        requirePage(key, title)
        pages += AndroidKitSettingsCatalogPage.Main(key, title, actions, searchTerms, content)
    }

    public fun subpage(
        key: String,
        title: String,
        actions: List<AndroidKitActionItem> = emptyList(),
        searchTerms: AndroidKitSettingsSearchTerms? = null,
        content: AndroidKitSettingsPageScope.() -> Unit = {},
    ) {
        requirePage(key, title)
        pages += AndroidKitSettingsCatalogPage.Subpage(key, title, actions, searchTerms, content)
    }

    /** Adds the fixed Kit-owned About page and its Main-page destination. */
    public fun about(
        key: String,
        content: AndroidKitSettingsAbout,
        onOpen: () -> Unit,
    ) {
        require(key.isNotBlank()) { "Settings page keys must not be blank." }
        pages += AndroidKitSettingsCatalogPage.About(key, content, onOpen)
    }

    private fun requirePage(key: String, title: String) {
        require(key.isNotBlank()) { "Settings page keys must not be blank." }
        require(title.isNotBlank()) { "Settings page titles must not be blank." }
    }
}

internal sealed interface AndroidKitSettingsCatalogPage {
    val key: String
    val title: String?
    val actions: List<AndroidKitActionItem>
    val searchTerms: AndroidKitSettingsSearchTerms?

    data class Main(
        override val key: String,
        override val title: String,
        override val actions: List<AndroidKitActionItem>,
        override val searchTerms: AndroidKitSettingsSearchTerms?,
        val content: AndroidKitSettingsPageScope.() -> Unit,
    ) : AndroidKitSettingsCatalogPage

    data class Subpage(
        override val key: String,
        override val title: String,
        override val actions: List<AndroidKitActionItem>,
        override val searchTerms: AndroidKitSettingsSearchTerms?,
        val content: AndroidKitSettingsPageScope.() -> Unit,
    ) : AndroidKitSettingsCatalogPage

    data class About(
        override val key: String,
        val content: AndroidKitSettingsAbout,
        val onOpen: () -> Unit,
    ) : AndroidKitSettingsCatalogPage {
        override val title: String? = null
        override val actions: List<AndroidKitActionItem> = emptyList()
        override val searchTerms: AndroidKitSettingsSearchTerms? = null
    }
}
