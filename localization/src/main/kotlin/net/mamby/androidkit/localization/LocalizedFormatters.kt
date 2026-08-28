package net.mamby.androidkit.localization

import android.icu.text.ListFormatter
import android.os.Build
import android.util.LruCache
import androidx.annotation.RequiresApi
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

/**
 * Locale-explicit formatters that never depend on a process-global default locale.
 *
 * Immutable formatters are shared across threads. Mutable number formatters are cached per thread.
 */
public object LocalizedFormatters {
    public fun date(
        value: LocalDate,
        locale: Locale,
        style: FormatStyle = FormatStyle.MEDIUM,
    ): String = dateTimeFormatters.getOrCreate(
        DateTimeFormatterKey.Date(locale, style),
    ).format(value)

    public fun time(
        value: LocalTime,
        locale: Locale,
        style: FormatStyle = FormatStyle.SHORT,
    ): String = dateTimeFormatters.getOrCreate(
        DateTimeFormatterKey.Time(locale, style),
    ).format(value)

    public fun dateTime(
        value: LocalDateTime,
        locale: Locale,
        dateStyle: FormatStyle = FormatStyle.MEDIUM,
        timeStyle: FormatStyle = FormatStyle.SHORT,
    ): String = dateTimeFormatters.getOrCreate(
        DateTimeFormatterKey.DateTime(locale, dateStyle, timeStyle),
    ).format(value)

    public fun number(
        value: Number,
        locale: Locale,
        maximumFractionDigits: Int = 2,
    ): String {
        val key = NumberFormatterKey.Decimal(
            locale = locale,
            maximumFractionDigits = maximumFractionDigits.coerceAtLeast(0),
        )
        return currentNumberFormatters().getOrCreate(key).format(value)
    }

    public fun currency(
        value: Number,
        currencyCode: String,
        locale: Locale,
    ): String = currentNumberFormatters().getOrCreate(
        NumberFormatterKey.Currency(locale, currencyCode),
    ).format(value)

    public fun list(
        values: List<String>,
        locale: Locale,
    ): String = listFormatters.getOrCreate(locale).format(values)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun list(
        values: List<String>,
        locale: Locale,
        type: LocalizedListType,
        width: LocalizedListWidth,
    ): String {
        val key = TypedListFormatterKey(locale, type, width)
        return TypedListFormatters.cache.getOrCreate(key).format(values)
    }

    private val dateTimeFormatters = object : LruCache<DateTimeFormatterKey, DateTimeFormatter>(
        FormatterCacheSize,
    ) {
        override fun create(key: DateTimeFormatterKey): DateTimeFormatter = when (key) {
            is DateTimeFormatterKey.Date -> DateTimeFormatter.ofLocalizedDate(key.style)
                .withLocale(key.locale)

            is DateTimeFormatterKey.Time -> DateTimeFormatter.ofLocalizedTime(key.style)
                .withLocale(key.locale)

            is DateTimeFormatterKey.DateTime -> DateTimeFormatter.ofLocalizedDateTime(
                key.dateStyle,
                key.timeStyle,
            ).withLocale(key.locale)
        }
    }

    private val numberFormatters = ThreadLocal.withInitial {
        object : LruCache<NumberFormatterKey, NumberFormat>(FormatterCacheSize) {
            override fun create(key: NumberFormatterKey): NumberFormat = when (key) {
                is NumberFormatterKey.Decimal -> NumberFormat.getNumberInstance(key.locale).apply {
                    maximumFractionDigits = key.maximumFractionDigits
                }

                is NumberFormatterKey.Currency -> NumberFormat.getCurrencyInstance(key.locale).apply {
                    currency = Currency.getInstance(key.currencyCode)
                }
            }
        }
    }

    private fun currentNumberFormatters(): LruCache<NumberFormatterKey, NumberFormat> =
        checkNotNull(numberFormatters.get())

    private val listFormatters = object : LruCache<Locale, ListFormatter>(
        FormatterCacheSize,
    ) {
        override fun create(key: Locale): ListFormatter = ListFormatter.getInstance(key)
    }
}

private sealed interface DateTimeFormatterKey {
    data class Date(val locale: Locale, val style: FormatStyle) : DateTimeFormatterKey

    data class Time(val locale: Locale, val style: FormatStyle) : DateTimeFormatterKey

    data class DateTime(
        val locale: Locale,
        val dateStyle: FormatStyle,
        val timeStyle: FormatStyle,
    ) : DateTimeFormatterKey
}

private sealed interface NumberFormatterKey {
    data class Decimal(
        val locale: Locale,
        val maximumFractionDigits: Int,
    ) : NumberFormatterKey

    data class Currency(
        val locale: Locale,
        val currencyCode: String,
    ) : NumberFormatterKey
}

private data class TypedListFormatterKey(
    val locale: Locale,
    val type: LocalizedListType,
    val width: LocalizedListWidth,
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private object TypedListFormatters {
    val cache: LruCache<TypedListFormatterKey, ListFormatter> =
        object : LruCache<TypedListFormatterKey, ListFormatter>(FormatterCacheSize) {
            override fun create(key: TypedListFormatterKey): ListFormatter =
                ListFormatter.getInstance(
                    key.locale,
                    key.type.toPlatformType(),
                    key.width.toPlatformWidth(),
                )
        }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun LocalizedListType.toPlatformType(): ListFormatter.Type = when (this) {
    LocalizedListType.And -> ListFormatter.Type.AND
    LocalizedListType.Or -> ListFormatter.Type.OR
    LocalizedListType.Units -> ListFormatter.Type.UNITS
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun LocalizedListWidth.toPlatformWidth(): ListFormatter.Width = when (this) {
    LocalizedListWidth.Wide -> ListFormatter.Width.WIDE
    LocalizedListWidth.Short -> ListFormatter.Width.SHORT
    LocalizedListWidth.Narrow -> ListFormatter.Width.NARROW
}

private const val FormatterCacheSize = 32

private fun <Key : Any, Value : Any> LruCache<Key, Value>.getOrCreate(key: Key): Value =
    checkNotNull(get(key))
