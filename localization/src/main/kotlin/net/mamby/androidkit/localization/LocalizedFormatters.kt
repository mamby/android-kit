package net.mamby.androidkit.localization

import android.icu.text.ListFormatter
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

public enum class LocalizedListType {
    And,
    Or,
    Units,
}

public enum class LocalizedListWidth {
    Wide,
    Short,
    Narrow,
}

/** Locale-explicit formatters that never depend on a process-global default locale. */
public object LocalizedFormatters {
    public fun date(
        value: LocalDate,
        locale: Locale,
        style: FormatStyle = FormatStyle.MEDIUM,
    ): String = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale).format(value)

    public fun time(
        value: LocalTime,
        locale: Locale,
        style: FormatStyle = FormatStyle.SHORT,
    ): String = DateTimeFormatter.ofLocalizedTime(style).withLocale(locale).format(value)

    public fun dateTime(
        value: LocalDateTime,
        locale: Locale,
        dateStyle: FormatStyle = FormatStyle.MEDIUM,
        timeStyle: FormatStyle = FormatStyle.SHORT,
    ): String = DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)
        .withLocale(locale)
        .format(value)

    public fun number(
        value: Number,
        locale: Locale,
        maximumFractionDigits: Int = 2,
    ): String = NumberFormat.getNumberInstance(locale).apply {
        this.maximumFractionDigits = maximumFractionDigits.coerceAtLeast(0)
    }.format(value)

    public fun currency(
        value: Number,
        currencyCode: String,
        locale: Locale,
    ): String = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode)
    }.format(value)

    public fun list(
        values: List<String>,
        locale: Locale,
        type: LocalizedListType = LocalizedListType.And,
        width: LocalizedListWidth = LocalizedListWidth.Wide,
    ): String {
        val formatterType = when (type) {
            LocalizedListType.And -> ListFormatter.Type.AND
            LocalizedListType.Or -> ListFormatter.Type.OR
            LocalizedListType.Units -> ListFormatter.Type.UNITS
        }
        val formatterWidth = when (width) {
            LocalizedListWidth.Wide -> ListFormatter.Width.WIDE
            LocalizedListWidth.Short -> ListFormatter.Width.SHORT
            LocalizedListWidth.Narrow -> ListFormatter.Width.NARROW
        }
        return ListFormatter.getInstance(locale, formatterType, formatterWidth).format(values)
    }
}
