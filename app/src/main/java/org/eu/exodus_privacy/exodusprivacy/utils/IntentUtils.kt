package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import java.util.Collections

fun startIntent(context: Context, type: String, value: String, app: String?): Boolean {
    var intent = Intent()
    when (type) {
        "web" -> intent = Intent(Intent.ACTION_VIEW, Uri.parse(value))
        "system" -> intent = Intent(value).apply {
            data = Uri.parse("package:$app")
        }
        "mail" -> intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$value"))
    }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

fun openURL(customTabsIntent: CustomTabsIntent, context: Context, url: String) {
    val packageName = CustomTabsClient.getPackageName(
        context,
        Collections.emptyList(),
    )
    if (packageName != null) {
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } else {
        startIntent(context, "web", url, null)
    }
}
