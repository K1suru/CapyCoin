package com.example.gestiongastos

import androidx.compose.runtime.key
import com.example.gestiongastos.i18n.tr
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.SharedPreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestiongastos.ui.navigation.Screen
import com.example.gestiongastos.ui.navigation.defaultBottomNavScreens
import com.example.gestiongastos.ui.theme.AppColorTheme
import com.example.gestiongastos.ui.theme.AppThemeMode
import com.example.gestiongastos.ui.theme.GestionGastosTheme
import com.example.gestiongastos.ui.viewmodel.FinanceViewModel

class MainActivity : FragmentActivity() {

    // Misma instancia que obtiene GestionGastosApp con viewModel() (ámbito de la actividad)
    private val financeViewModel: FinanceViewModel by viewModels()

    // Momento en que la app pasó a segundo plano, para volver a bloquearla con huella
    private var backgroundedAtMillis: Long? = null

    fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )

        // BIOMETRIC_STRONG | DEVICE_CREDENTIAL no está soportado en API 28-29 (lanza IllegalArgumentException)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(tr("Desbloquear CapyCoin", "Unlock CapyCoin"))
            .setSubtitle(tr("Confirma tu huella digital o clave para continuar", "Confirm your fingerprint or passcode to continue"))
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        private const val REQUEST_CODE_RESTORE_BACKUP = 1001
        private const val REQUEST_CODE_EXPORT_BACKUP = 1002

        // Tiempo en segundo plano tras el cual se vuelve a pedir la huella
        private const val RELOCK_TIMEOUT_MILLIS = 30_000L
    }

    override fun onStart() {
        super.onStart()
        val backgroundedAt = backgroundedAtMillis
        backgroundedAtMillis = null
        if (backgroundedAt != null &&
            financeViewModel.isBiometricEnabled.value &&
            SystemClock.elapsedRealtime() - backgroundedAt >= RELOCK_TIMEOUT_MILLIS
        ) {
            financeViewModel.setAppUnlocked(false)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            backgroundedAtMillis = SystemClock.elapsedRealtime()
        }
    }

    private var onRestoreFilePicked: ((Uri) -> Unit)? = null
    private var onExportFileCreated: ((Uri) -> Unit)? = null

    /** Pide al usuario dónde guardar la copia (Descargas, Drive, etc.) y devuelve el archivo creado. */
    fun launchExportBackupPicker(
        fileName: String,
        onCreated: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onExportFileCreated = onCreated
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_EXPORT_BACKUP)
        } catch (e: Exception) {
            onExportFileCreated = null
            onError(tr("No se pudo abrir el selector de archivos: ${e.localizedMessage ?: e.message}", "Couldn't open the file picker: ${e.localizedMessage ?: e.message}"))
        }
    }

    fun launchRestoreBackupPicker(
        onPicked: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onRestoreFilePicked = onPicked
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/json", "text/plain", "application/octet-stream", "*/*")
            )
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_RESTORE_BACKUP)
        } catch (e1: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(
                    Intent.createChooser(fallbackIntent, tr("Seleccionar copia de seguridad", "Select backup file")),
                    REQUEST_CODE_RESTORE_BACKUP
                )
            } catch (e2: Exception) {
                onError(tr("No se pudo abrir el selector de archivos: ${e2.localizedMessage ?: e2.message}", "Couldn't open the file picker: ${e2.localizedMessage ?: e2.message}"))
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EXPORT_BACKUP) {
            val callback = onExportFileCreated
            onExportFileCreated = null
            if (resultCode == RESULT_OK) data?.data?.let { uri -> callback?.invoke(uri) }
            return
        }
        if (requestCode == REQUEST_CODE_RESTORE_BACKUP && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Safe to ignore if content provider doesn't support persistable permissions
                }
                val callback = onRestoreFilePicked
                onRestoreFilePicked = null
                callback?.invoke(uri)
            }
        }
    }

    var quickExpenseRequested by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getStringExtra("extra_mode") == "quick_expense") {
            quickExpenseRequested = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent?.getStringExtra("extra_mode") == "quick_expense") {
            quickExpenseRequested = true
        }

        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val initialModeName = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val initialColorName = prefs.getString("color_theme", AppColorTheme.YELLOW.name) ?: AppColorTheme.YELLOW.name
        val initialMode = try { AppThemeMode.valueOf(initialModeName) } catch (e: IllegalArgumentException) { AppThemeMode.SYSTEM }
        val initialColor = try { AppColorTheme.valueOf(initialColorName) } catch (e: IllegalArgumentException) { AppColorTheme.YELLOW }

        setContent {
            var themeMode by remember { mutableStateOf(initialMode) }
            var colorTheme by remember { mutableStateOf(initialColor) }

            // Al cambiar el idioma se vuelve a crear toda la interfaz con los textos nuevos
            val language by financeViewModel.language.collectAsState()
            key(language) {
            GestionGastosTheme(themeMode = themeMode, colorTheme = colorTheme) {
                GestionGastosApp(
                    viewModel = financeViewModel,
                    quickExpenseRequested = quickExpenseRequested,
                    onQuickExpenseHandled = { quickExpenseRequested = false },
                    currentThemeMode = themeMode,
                    onThemeModeSelected = { mode ->
                        themeMode = mode
                        prefs.edit().putString("theme_mode", mode.name).apply()
                    },
                    currentColorTheme = colorTheme,
                    onColorThemeSelected = { color ->
                        colorTheme = color
                        prefs.edit().putString("color_theme", color.name).apply()
                    }
                )
            }
            }
        }
    }
}

internal fun loadBottomNavOrder(prefs: SharedPreferences): List<Screen> {
    val raw = prefs.getString("bottom_nav_order", null) ?: return defaultBottomNavScreens
    val routes = raw.split(",").map { it.trim() }
    val ordered = routes.mapNotNull { route -> defaultBottomNavScreens.find { it.route == route } }
    val remaining = defaultBottomNavScreens.filter { it !in ordered }
    return if (ordered.isNotEmpty()) ordered + remaining else defaultBottomNavScreens
}
