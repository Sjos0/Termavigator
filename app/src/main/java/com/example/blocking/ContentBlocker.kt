package com.example.blocking

import java.util.Locale

class ContentBlocker(private var enabled: Boolean = true) {

    private val blockedDomains = hashSetOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "adnxs.com",
        "adsrvr.org",
        "rubiconproject.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "scorecardresearch.com",
        "quantserve.com",
        "amazon-adsystem.com",
        "casalemedia.com",
        "openx.net",
        "pubmatic.com",
        "advertising.com",
        "adroll.com",
        "adcolony.com",
        "unityads.unity3d.com",
        "applovin.com",
        "vungle.com",
        "chartboost.com",
        "inmobi.com",
        "flurry.com",
        "hotjar.com",
        "smartadserver.com",
        "moatads.com"
    )

    fun setEnabled(isEnabled: Boolean) {
        this.enabled = isEnabled
    }

    fun isEnabled(): Boolean = enabled

    fun shouldBlock(url: String): Boolean {
        if (!enabled) return false
        val host = extractHost(url) ?: return false

        for (domain in blockedDomains) {
            if (host == domain || host.endsWith(".$domain")) {
                return true
            }
        }
        return false
    }

    private fun extractHost(url: String): String? {
        val withoutProtocol = if (url.contains("://")) {
            url.substringAfter("://")
        } else {
            url
        }
        val hostPart = withoutProtocol.substringBefore("/").substringBefore("?").substringBefore("#").substringBefore(":")
        return if (hostPart.isNotBlank()) hostPart.lowercase(Locale.ROOT) else null
    }
}
