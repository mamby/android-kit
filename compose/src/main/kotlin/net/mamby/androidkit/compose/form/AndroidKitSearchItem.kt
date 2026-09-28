package net.mamby.androidkit.compose.form

/** Host-owned context for related search items. [key] is independent of the translated [title]. */
public data class AndroidKitSearchGroup(
    public val key: String,
    public val title: String,
) {
    init {
        require(key.isNotBlank()) { "Search group keys must not be blank." }
        require(title.isNotBlank()) { "Search group titles must not be blank." }
    }
}

/**
 * Searchable host content. The host renders the result and calls [onClick] when it is activated.
 * [data] carries the host's domain model without a lookup or an untyped payload.
 * [searchTerms] may contain aliases in any language; they never replace visible text.
 */
public data class AndroidKitSearchItem<T>(
    public val key: String,
    public val title: String,
    public val data: T,
    public val onClick: () -> Unit,
    public val supportingText: String? = null,
    public val group: AndroidKitSearchGroup? = null,
    public val searchTerms: List<String> = emptyList(),
    public val enabled: Boolean = true,
) {
    init {
        require(key.isNotBlank()) { "Search item keys must not be blank." }
        require(title.isNotBlank()) { "Search item titles must not be blank." }
        require(searchTerms.all(String::isNotBlank)) { "Search terms must not be blank." }
    }
}
