package com.gamenetmehdi.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openExternalUri(
    context: Context,
    uri: String,
    action: String = Intent.ACTION_VIEW,
    showError: Boolean = true,
): Boolean = try {
    context.startActivity(Intent(action, Uri.parse(uri)))
    true
} catch (_: ActivityNotFoundException) {
    if (showError) {
        Toast.makeText(context, "برنامه‌ای برای این کار پیدا نشد", Toast.LENGTH_SHORT).show()
    }
    false
}

fun openVenueMap(context: Context, venue: Venue) {
    val coordinates = "${venue.latitude},${venue.longitude}"
    if (!openExternalUri(context, "geo:$coordinates?q=$coordinates", showError = false)) {
        openExternalUri(context, "https://www.google.com/maps/search/?api=1&query=$coordinates")
    }
}
