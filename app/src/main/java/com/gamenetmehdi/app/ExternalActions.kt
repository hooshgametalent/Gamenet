package com.gamenetmehdi.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.IDN
import java.net.URI

private val allowedOfficialHosts = setOf(
    "ea.com",
    "gran-turismo.com",
    "mortalkombat.com",
    "my.gametalent.ir",
    "playvalorant.com",
    "tekken.com",
)
private val dialablePhone = Regex("^\\+?[0-9]{7,15}$")

class OfficialGameLink internal constructor(
    val uri: String,
    val host: String,
)

object OfficialLinkPolicy {
    fun validate(rawUri: String): OfficialGameLink? {
        val parsed = runCatching { URI(rawUri.trim()) }.getOrNull() ?: return null
        if (!parsed.scheme.equals("https", ignoreCase = true)) return null
        if (parsed.rawUserInfo != null || parsed.rawFragment != null) return null
        if (parsed.port != -1 && parsed.port != 443) return null
        val host = parsed.host?.let { runCatching { IDN.toASCII(it).lowercase() }.getOrNull() } ?: return null
        if (host !in allowedOfficialHosts && allowedOfficialHosts.none { host.endsWith(".$it") }) return null
        return OfficialGameLink(parsed.normalize().toASCIIString(), host)
    }
}

fun openOfficialGameLink(context: Context, link: OfficialGameLink): Boolean =
    launchIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse(link.uri)))

fun openDialer(context: Context, phone: String): Boolean {
    val normalized = phone.filterNot(Char::isWhitespace)
    if (!dialablePhone.matches(normalized)) return false
    return launchIntent(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalized")))
}

fun openVenueMap(context: Context, venue: Venue) {
    val coordinates = "${venue.latitude},${venue.longitude}"
    val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$coordinates?q=$coordinates"))
    if (!launchIntent(context, geoIntent, showError = false)) {
        val fallback = Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .appendPath("maps")
            .appendPath("search")
            .appendPath("")
            .appendQueryParameter("api", "1")
            .appendQueryParameter("query", coordinates)
            .build()
        launchIntent(context, Intent(Intent.ACTION_VIEW, fallback))
    }
}

private fun launchIntent(context: Context, intent: Intent, showError: Boolean = true): Boolean = try {
    context.startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    if (showError) {
        Toast.makeText(context, "برنامه‌ای برای این کار پیدا نشد", Toast.LENGTH_SHORT).show()
    }
    false
}
