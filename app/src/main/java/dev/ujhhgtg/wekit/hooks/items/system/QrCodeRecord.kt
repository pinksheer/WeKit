package dev.ujhhgtg.wekit.hooks.items.system

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.robv.android.xposed.XC_MethodHook
import dev.ujhhgtg.wekit.hooks.api.ui.WeStartActivityApi
import dev.ujhhgtg.wekit.hooks.core.ClickableHookItem
import dev.ujhhgtg.wekit.hooks.core.HookItem
import dev.ujhhgtg.wekit.preferences.WePrefs
import dev.ujhhgtg.wekit.ui.content.AlertDialogContent
import dev.ujhhgtg.wekit.ui.content.TextButton
import dev.ujhhgtg.wekit.ui.utils.showComposeDialog
import dev.ujhhgtg.wekit.utils.formatEpoch
import dev.ujhhgtg.wekit.utils.serialization.DefaultJson
import kotlinx.serialization.Serializable

@HookItem(path = "系统与隐私/二维码扫描记录", description = "记录扫描的二维码 URL")
object QrCodeRecord : ClickableHookItem(), WeStartActivityApi.IStartActivityListener {

    @Serializable
    data class QrRecord(val url: String, val time: Long = System.currentTimeMillis())

    private val records = mutableListOf<QrRecord>()

    private const val KEY_RECORDS = "qr_code_records"

    private var loaded = false

    override fun onEnable() {
        WeStartActivityApi.addListener(this)
    }

    override fun onDisable() {
        WeStartActivityApi.removeListener(this)
    }

    override fun onStartActivity(param: XC_MethodHook.MethodHookParam, intent: Intent) {
        if (intent.extras?.run {
                containsKey("key_scan_qr_code_get_a8key_resp") ||
                        containsKey("key_scan_qr_code_get_a8key_req")
            } != true) return

        val component = intent.component ?: return
        val shortClassName = component.shortClassName ?: return
        if (!shortClassName.contains("MMWebViewUI")) return

        val rawUrl = intent.getStringExtra("rawUrl") ?: return

        if (!loaded) {
            loadRecords()
            loaded = true
        }

        records.add(0, QrRecord(rawUrl))
        saveRecords()
    }

    override fun onClick(context: Context) {
        if (!loaded) {
            loadRecords()
            loaded = true
        }

        showComposeDialog(context) {
            var list by remember { mutableStateOf(records.toList()) }

            AlertDialogContent(
                title = { Text("二维码扫描记录") },
                text = {
                    if (list.isEmpty()) {
                        Text(
                            text = "暂无记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(list, key = { it.time }) { record ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = formatEpoch(record.time, true),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    SelectionContainer {
                                        Text(
                                            text = record.url,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton({
                        records.clear()
                        list = emptyList()
                        clearRecords()
                    }) { Text("清空") }
                },
                confirmButton = { TextButton(onDismiss) { Text("关闭") } }
            )
        }
    }

    private fun saveRecords() {
        WePrefs.putString(KEY_RECORDS, DefaultJson.encodeToString(records.toList()))
    }

    private fun loadRecords() {
        records.clear()
        WePrefs.getString(KEY_RECORDS)
            ?.let { runCatching { DefaultJson.decodeFromString<List<QrRecord>>(it) }.getOrNull() }
            ?.let { records.addAll(it) }
    }

    private fun clearRecords() {
        WePrefs.remove(KEY_RECORDS)
    }
}
