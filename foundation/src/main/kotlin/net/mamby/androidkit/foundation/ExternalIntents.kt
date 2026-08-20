package net.mamby.androidkit.foundation

import android.content.Intent
import android.net.Uri

/** Intent factories that keep launching decisions in the consuming application. */
public object ExternalIntents {
    public fun shareText(title: String, text: String): Intent =
        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TITLE, title)
            .putExtra(Intent.EXTRA_TEXT, text)

    public fun chooser(target: Intent, title: String): Intent =
        Intent.createChooser(target, title)

    public fun view(uri: Uri): Intent = Intent(Intent.ACTION_VIEW, uri)

    public fun dial(phoneNumber: String): Intent =
        Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))

    public fun email(
        address: String,
        subject: String? = null,
        body: String? = null,
    ): Intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        body?.let { putExtra(Intent.EXTRA_TEXT, it) }
    }

    public fun mapSearch(query: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
}
