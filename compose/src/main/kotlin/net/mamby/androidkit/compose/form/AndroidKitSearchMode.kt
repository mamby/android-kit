package net.mamby.androidkit.compose.form

/** When search input requests execution. Input state always updates immediately. */
public enum class AndroidKitSearchMode {
    /** Request search on each edit, including dictation and clearing. */
    Live,
    /** Request search only through the keyboard Search action, for a nonblank query. */
    OnSubmit,
}
