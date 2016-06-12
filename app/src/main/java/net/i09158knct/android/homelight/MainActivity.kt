package net.i09158knct.android.homelight

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_app.view.*

class MainActivity : Activity(), AppListAdapter.IEventListener {

    private lateinit var am: AppManager
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenPermissionSetting.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        am = AppManager(this)
        adapter = AppListAdapter(this, mutableListOf(), this)
        lstApp.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent.createChooser(
                    Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    getString(R.string.chooseHome));
            startActivity(intent);
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun refreshList() {
        adapter.clear()

        val apps = am.loadRecentAppList()

        btnOpenPermissionSetting.visibility = View.GONE
        if (apps.size == 0 && !am.isUsageStatPermissionGranted()) {
            btnOpenPermissionSetting.visibility = View.VISIBLE
        }

        adapter.addAll(apps)
        adapter.notifyDataSetInvalidated()
    }

    override fun onClick(app: App) {
        val intent = packageManager.getLaunchIntentForPackage(app.stat.packageName)
        if (intent == null) {
            Toast.makeText(this, "Can't Start (getLaunchIntentForPackage() == null)", Toast.LENGTH_SHORT).show()
            return;
        }

        startActivity(intent)
    }

}

class AppListAdapter(
        context: Context,
        apps: MutableList<App>,
        val listener: IEventListener)
: ArrayAdapter<App>(context, 0, apps) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view = convertView ?: View.inflate(context, R.layout.item_app, null)
        val app = getItem(position) ?: return view

        val name = try {
            val info = context.packageManager.getApplicationInfo(app.stat.packageName, 0)
            context.packageManager.getApplicationLabel(info)
        } catch (ex: PackageManager.NameNotFoundException) {
            app.stat.packageName
        }


        view.txtTitle.text = name
        view.setOnClickListener { listener.onClick(app) }
        return view
    }

    interface IEventListener {
        fun onClick(app: App): Unit
    }
}
