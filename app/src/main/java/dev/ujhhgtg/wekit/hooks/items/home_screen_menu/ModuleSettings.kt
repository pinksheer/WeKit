package dev.ujhhgtg.wekit.hooks.items.home_screen_menu

import com.tencent.mm.ui.LauncherUI
import de.robv.android.xposed.XC_MethodHook
import dev.ujhhgtg.wekit.BuildConfig
import dev.ujhhgtg.wekit.hooks.api.ui.WeHomeScreenPopupMenuApi
import dev.ujhhgtg.wekit.hooks.api.ui.WeSettingsInjector
import dev.ujhhgtg.wekit.hooks.core.HookItem
import dev.ujhhgtg.wekit.hooks.core.SwitchHookItem
import dev.ujhhgtg.wekit.preferences.WePrefs
import dev.ujhhgtg.wekit.ui.utils.ExtensionIcon
import dev.ujhhgtg.wekit.utils.TargetProcesses

@HookItem(name = "模块设置", categories = ["系统与隐私", "首页右上角菜单"], description = "在首页右上角菜单添加「WeKit」选项")
object ModuleSettings : SwitchHookItem(), WeHomeScreenPopupMenuApi.IMenuItemsProvider {

    override fun startup() {
        if (!TargetProcesses.isInMain) return
        _isEnabled = WePrefs.getBoolOrDef(name, true)
        if (_isEnabled) enable()
    }

    override fun onEnable() {
        WeHomeScreenPopupMenuApi.addProvider(this)
    }

    override fun onDisable() {
        WeHomeScreenPopupMenuApi.removeProvider(this)
    }

    override fun getMenuItems(param: XC_MethodHook.MethodHookParam): List<WeHomeScreenPopupMenuApi.MenuItem> =
        listOf(
            WeHomeScreenPopupMenuApi.MenuItem(
                0, BuildConfig.TAG, ExtensionIcon
            ) { WeSettingsInjector.openSettingsDialog(LauncherUI.getInstance()!!) }
        )
}
