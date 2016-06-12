package net.i09158knct.android.homelight

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.lang.reflect.Field

class AppManager(val context: Context) {

    private val USAGE_STATS_SERVICE = "usagestats"
    private lateinit var usm: UsageStatsManager
    private lateinit var field: Field

    init {
        usm = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager;
        field = UsageStats::class.java.getDeclaredField("mLastEvent")
        field.isAccessible = true
    }

    fun loadRecentAppList(): List<App> {
        val time = System.currentTimeMillis()
        // getLaunchIntentForPackageでnullが返るかで判断したほうがいいかも。
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 60 * 1000, time)
                .filter { field.get(it) == 2 }
                .sortedBy { it.lastTimeStamp }
                .map { App(it) }
    }

    fun isUsageStatPermissionGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager;
        val mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(),
                context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}

data class App(val stat: UsageStats) {
}