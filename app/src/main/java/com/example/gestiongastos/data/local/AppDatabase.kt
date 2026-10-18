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
        ReceivedIncomeEntity::class
    ],
    version = 22,
    exportSchema = false
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestion_gastos_db"
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
                        MIGRATION_21_22
                    )
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
