package com.example.gestiongastos

import com.example.gestiongastos.i18n.tr
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Compartir el APK instalado de la app con otra persona.
fun shareAppApk(context: Context) {
    try {
        val appInfo = context.applicationInfo
        val sourceApk = File(appInfo.sourceDir)
        if (!sourceApk.exists()) {
            Toast.makeText(context, tr("No se encontró el archivo de la aplicación", "Couldn't find the app file"), Toast.LENGTH_SHORT).show()
            return
        }

        val shareDir = File(context.cacheDir, "shared_apk")
        if (!shareDir.exists()) shareDir.mkdirs()
        val targetApk = File(shareDir, "CapyCoin.apk")
        sourceApk.copyTo(targetApk, overwrite = true)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetApk
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, tr("Compartir APK", "Share APK")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, tr("Error al compartir la aplicación: ${e.localizedMessage}", "Couldn't share the app: ${e.localizedMessage}"), Toast.LENGTH_LONG).show()
    }
}
