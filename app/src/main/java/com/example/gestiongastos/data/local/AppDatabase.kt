package com.example.gestiongastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN currency TEXT NOT NULL DEFAULT 'COP'")
        db.execSQL("ALTER TABLE card_purchases ADD COLUMN currency TEXT NOT NULL DEFAULT 'COP'")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credit_cards ADD COLUMN isFirstInstallmentInterestFree INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ignored_expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                itemKey TEXT NOT NULL,
                yearMonthText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS paid_credit_cards (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                cardId INTEGER NOT NULL,
                yearMonthText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credit_cards ADD COLUMN paymentDay INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS paid_expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                itemKey TEXT NOT NULL,
                yearMonthText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS settled_purchases (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                purchaseId INTEGER NOT NULL,
                yearMonthText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE incomes ADD COLUMN extraAmount REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE incomes ADD COLUMN extraConcept TEXT DEFAULT NULL")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS received_incomes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                incomeId INTEGER NOT NULL,
                yearMonthText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE incomes ADD COLUMN totalInstallments INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE incomes ADD COLUMN totalAmount REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE incomes ADD COLUMN endDateMillis INTEGER DEFAULT NULL")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN endDateMillis INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE savings ADD COLUMN endDateMillis INTEGER DEFAULT NULL")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credit_cards ADD COLUMN creditLimit REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE credit_cards ADD COLUMN colorIndex INTEGER NOT NULL DEFAULT -1")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN installments INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                expenseId INTEGER NOT NULL,
                amount REAL NOT NULL,
                date INTEGER NOT NULL,
                yearMonthText TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

/**
 * Sentencias de la migración 23 → 24: los montos pasan de REAL (pesos o dólares con decimales) a INTEGER
 * en centavos, con ROUND(x * 100). SQLite no puede cambiar el tipo de una columna, así que cada tabla se
 * crea de nuevo, se copian los datos, se borra la vieja y se renombra. Se conserva el contador de ids
 * (sqlite_sequence) para que nunca se reutilice el id de un registro borrado. Las tasas siguen en REAL.
 * Generado a partir de app/schemas (23.json y 24.json).
 */
internal val MIGRATION_23_24_SQL: List<String> = listOf(
    // incomes
    "CREATE TABLE `incomes_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `concept` TEXT NOT NULL, `amountCents` INTEGER NOT NULL, `date` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `linkedExpenseIdsText` TEXT, `linkedCardPurchaseIdsText` TEXT, `extraAmountCents` INTEGER NOT NULL, `extraConcept` TEXT, `totalInstallments` INTEGER NOT NULL, `totalAmountCents` INTEGER NOT NULL, `endDateMillis` INTEGER)",
    "INSERT INTO `incomes_new` (`id`, `concept`, `amountCents`, `date`, `isRecurring`, `linkedExpenseIdsText`, `linkedCardPurchaseIdsText`, `extraAmountCents`, `extraConcept`, `totalInstallments`, `totalAmountCents`, `endDateMillis`) SELECT `id`, `concept`, CAST(ROUND(`amount` * 100) AS INTEGER), `date`, `isRecurring`, `linkedExpenseIdsText`, `linkedCardPurchaseIdsText`, CAST(ROUND(`extraAmount` * 100) AS INTEGER), `extraConcept`, `totalInstallments`, CAST(ROUND(`totalAmount` * 100) AS INTEGER), `endDateMillis` FROM `incomes`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'incomes_new', seq FROM sqlite_sequence WHERE name = 'incomes' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'incomes_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'incomes') WHERE name = 'incomes_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'incomes')",
    "DROP TABLE `incomes`",
    "ALTER TABLE `incomes_new` RENAME TO `incomes`",
    // expenses
    "CREATE TABLE `expenses_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `concept` TEXT NOT NULL, `amountCents` INTEGER NOT NULL, `date` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `currency` TEXT NOT NULL, `endDateMillis` INTEGER, `installments` INTEGER NOT NULL)",
    "INSERT INTO `expenses_new` (`id`, `concept`, `amountCents`, `date`, `isRecurring`, `currency`, `endDateMillis`, `installments`) SELECT `id`, `concept`, CAST(ROUND(`amount` * 100) AS INTEGER), `date`, `isRecurring`, `currency`, `endDateMillis`, `installments` FROM `expenses`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'expenses_new', seq FROM sqlite_sequence WHERE name = 'expenses' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'expenses_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'expenses') WHERE name = 'expenses_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'expenses')",
    "DROP TABLE `expenses`",
    "ALTER TABLE `expenses_new` RENAME TO `expenses`",
    // credit_cards
    "CREATE TABLE `credit_cards_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `cutoffDay` INTEGER NOT NULL, `paymentDay` INTEGER NOT NULL, `interestRate` REAL NOT NULL, `maintenanceFeeCents` INTEGER NOT NULL, `currency` TEXT NOT NULL, `isFeeWaiverEnabled` INTEGER NOT NULL, `minSpendForFeeWaiverCents` INTEGER NOT NULL, `isFirstInstallmentInterestFree` INTEGER NOT NULL, `creditLimitCents` INTEGER NOT NULL, `colorIndex` INTEGER NOT NULL)",
    "INSERT INTO `credit_cards_new` (`id`, `name`, `cutoffDay`, `paymentDay`, `interestRate`, `maintenanceFeeCents`, `currency`, `isFeeWaiverEnabled`, `minSpendForFeeWaiverCents`, `isFirstInstallmentInterestFree`, `creditLimitCents`, `colorIndex`) SELECT `id`, `name`, `cutoffDay`, `paymentDay`, `interestRate`, CAST(ROUND(`maintenanceFee` * 100) AS INTEGER), `currency`, `isFeeWaiverEnabled`, CAST(ROUND(`minSpendForFeeWaiver` * 100) AS INTEGER), `isFirstInstallmentInterestFree`, CAST(ROUND(`creditLimit` * 100) AS INTEGER), `colorIndex` FROM `credit_cards`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'credit_cards_new', seq FROM sqlite_sequence WHERE name = 'credit_cards' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'credit_cards_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'credit_cards') WHERE name = 'credit_cards_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'credit_cards')",
    "DROP TABLE `credit_cards`",
    "ALTER TABLE `credit_cards_new` RENAME TO `credit_cards`",
    // card_purchases
    "CREATE TABLE `card_purchases_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cardId` INTEGER NOT NULL, `concept` TEXT NOT NULL, `totalAmountCents` INTEGER NOT NULL, `installments` INTEGER NOT NULL, `purchaseDate` INTEGER NOT NULL, `isInterestFree` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `currency` TEXT NOT NULL)",
    "INSERT INTO `card_purchases_new` (`id`, `cardId`, `concept`, `totalAmountCents`, `installments`, `purchaseDate`, `isInterestFree`, `isRecurring`, `currency`) SELECT `id`, `cardId`, `concept`, CAST(ROUND(`totalAmount` * 100) AS INTEGER), `installments`, `purchaseDate`, `isInterestFree`, `isRecurring`, `currency` FROM `card_purchases`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'card_purchases_new', seq FROM sqlite_sequence WHERE name = 'card_purchases' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'card_purchases_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'card_purchases') WHERE name = 'card_purchases_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'card_purchases')",
    "DROP TABLE `card_purchases`",
    "ALTER TABLE `card_purchases_new` RENAME TO `card_purchases`",
    // savings
    "CREATE TABLE `savings_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `concept` TEXT NOT NULL, `amountCents` INTEGER NOT NULL, `date` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `endDateMillis` INTEGER)",
    "INSERT INTO `savings_new` (`id`, `concept`, `amountCents`, `date`, `isRecurring`, `endDateMillis`) SELECT `id`, `concept`, CAST(ROUND(`amount` * 100) AS INTEGER), `date`, `isRecurring`, `endDateMillis` FROM `savings`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'savings_new', seq FROM sqlite_sequence WHERE name = 'savings' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'savings_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'savings') WHERE name = 'savings_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'savings')",
    "DROP TABLE `savings`",
    "ALTER TABLE `savings_new` RENAME TO `savings`",
    // card_purchase_payments
    "CREATE TABLE `card_purchase_payments_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `purchaseId` INTEGER NOT NULL, `amountCents` INTEGER NOT NULL, `date` INTEGER NOT NULL, `yearMonthText` TEXT NOT NULL)",
    "INSERT INTO `card_purchase_payments_new` (`id`, `purchaseId`, `amountCents`, `date`, `yearMonthText`) SELECT `id`, `purchaseId`, CAST(ROUND(`amount` * 100) AS INTEGER), `date`, `yearMonthText` FROM `card_purchase_payments`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'card_purchase_payments_new', seq FROM sqlite_sequence WHERE name = 'card_purchase_payments' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'card_purchase_payments_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'card_purchase_payments') WHERE name = 'card_purchase_payments_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'card_purchase_payments')",
    "DROP TABLE `card_purchase_payments`",
    "ALTER TABLE `card_purchase_payments_new` RENAME TO `card_purchase_payments`",
    // expense_payments
    "CREATE TABLE `expense_payments_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `expenseId` INTEGER NOT NULL, `amountCents` INTEGER NOT NULL, `date` INTEGER NOT NULL, `yearMonthText` TEXT NOT NULL)",
    "INSERT INTO `expense_payments_new` (`id`, `expenseId`, `amountCents`, `date`, `yearMonthText`) SELECT `id`, `expenseId`, CAST(ROUND(`amount` * 100) AS INTEGER), `date`, `yearMonthText` FROM `expense_payments`",
    "INSERT INTO sqlite_sequence (name, seq) SELECT 'expense_payments_new', seq FROM sqlite_sequence WHERE name = 'expense_payments' AND NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'expense_payments_new')",
    "UPDATE sqlite_sequence SET seq = (SELECT seq FROM sqlite_sequence WHERE name = 'expense_payments') WHERE name = 'expense_payments_new' AND EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'expense_payments')",
    "DROP TABLE `expense_payments`",
    "ALTER TABLE `expense_payments_new` RENAME TO `expense_payments`"
)

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_23_24_SQL.forEach { db.execSQL(it) }
    }
}

@Database(
    entities = [
        IncomeEntity::class,
        ExpenseEntity::class,
        CreditCardEntity::class,
        CardPurchaseEntity::class,
        SavingsEntity::class,
        OmittedInstallmentEntity::class,
        CardPurchasePaymentEntity::class,
        IgnoredExpenseEntity::class,
        PaidCreditCardEntity::class,
        PaidExpenseEntity::class,
        SettledPurchaseEntity::class,
        ReceivedIncomeEntity::class,
        ExpensePaymentEntity::class
    ],
    version = 24,
    // El esquema se exporta a app/schemas/ (ver room.schemaLocation en build.gradle.kts) para poder
    // escribir tests de migración. No cambia la base de datos: solo deja constancia de cómo es.
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun cardPurchaseDao(): CardPurchaseDao
    abstract fun savingsDao(): SavingsDao
    abstract fun omittedInstallmentDao(): OmittedInstallmentDao
    abstract fun cardPurchasePaymentDao(): CardPurchasePaymentDao
    abstract fun ignoredExpenseDao(): IgnoredExpenseDao
    abstract fun paidCreditCardDao(): PaidCreditCardDao
    abstract fun paidExpenseDao(): PaidExpenseDao
    abstract fun settledPurchaseDao(): SettledPurchaseDao
    abstract fun receivedIncomeDao(): ReceivedIncomeDao
    abstract fun expensePaymentDao(): ExpensePaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                backUpDatabaseBeforeCents(context.applicationContext)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23,
                        MIGRATION_23_24
                    )
                    // Sin fallbackToDestructiveMigrationOnDowngrade: al instalar una versión más vieja
                    // Room falla al abrir la base en vez de borrar todos los datos en silencio.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

const val DATABASE_NAME = "gestion_gastos_db"

/** Última versión de la base con montos en decimales (antes de pasar a centavos). */
private const val LAST_DECIMAL_VERSION = 23

/**
 * Antes de la migración a centavos (23 → 24) guarda una copia de los archivos de la base tal como están,
 * en la carpeta propia de la app en el almacenamiento (Android/data/<app>/files/antes_de_centavos), que
 * se puede sacar por USB. Solo se hace una vez. Si la copia falla la migración sigue: es una sola
 * transacción (o se completa o no cambia nada) y lo contrario dejaría la app sin poder abrir.
 */
private fun backUpDatabaseBeforeCents(context: Context) {
    try {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) return
        val version = android.database.sqlite.SQLiteDatabase
            .openDatabase(dbFile.path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY)
            .use { it.version }
        if (version != LAST_DECIMAL_VERSION) return
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        copyDatabaseFiles(dbFile, java.io.File(baseDir, "antes_de_centavos"))
    } catch (e: Exception) {
        android.util.Log.w("AppDatabase", "No se pudo copiar la base antes de migrar", e)
    }
}

/**
 * Copia el archivo de la base y sus acompañantes (-wal, -shm) a [backupDir]. No pisa una copia que ya
 * exista. Devuelve true si la copia quedó (o ya estaba).
 */
internal fun copyDatabaseFiles(dbFile: java.io.File, backupDir: java.io.File): Boolean {
    val target = java.io.File(backupDir, dbFile.name)
    if (target.exists()) return true
    if (!dbFile.exists()) return false
    if (!backupDir.isDirectory && !backupDir.mkdirs()) return false
    // Primero los acompañantes y al final el archivo principal: si algo falla a medias no queda
    // una copia que parezca completa.
    listOf("-wal", "-shm").forEach { suffix ->
        val extra = java.io.File(dbFile.path + suffix)
        if (extra.exists()) extra.copyTo(java.io.File(backupDir, extra.name), overwrite = true)
    }
    dbFile.copyTo(target, overwrite = false)
    return target.length() == dbFile.length()
}
