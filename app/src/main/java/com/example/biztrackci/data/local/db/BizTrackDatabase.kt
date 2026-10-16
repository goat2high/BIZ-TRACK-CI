package com.example.biztrackci.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.biztrackci.data.local.dao.ExpenseDao
import com.example.biztrackci.data.local.dao.ProductDao
import com.example.biztrackci.data.local.dao.SaleDao
import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BizTrackDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "biztrack_db"

        fun getCallback(scope: CoroutineScope, dbProvider: () -> BizTrackDatabase): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch(Dispatchers.IO) {
                        populateSampleData(dbProvider())
                    }
                }
            }
        }

        private suspend fun populateSampleData(database: BizTrackDatabase) {
            val productDao = database.productDao()
            val saleDao = database.saleDao()
            val expenseDao = database.expenseDao()

            val sampleProducts = listOf(
                ProductEntity(
                    id = 1,
                    name = "Riz Uncle Sam 5kg",
                    category = "Alimentation",
                    costPrice = 3500.0,
                    sellingPrice = 4500.0,
                    stockQuantity = 25,
                    minStockThreshold = 5,
                    unit = "Sac"
                ),
                ProductEntity(
                    id = 2,
                    name = "Huile Dinor 1L",
                    category = "Alimentation",
                    costPrice = 1100.0,
                    sellingPrice = 1400.0,
                    stockQuantity = 40,
                    minStockThreshold = 8,
                    unit = "Bouteille"
                ),
                ProductEntity(
                    id = 3,
                    name = "Sucre Blanc SN SOSUCO 1kg",
                    category = "Alimentation",
                    costPrice = 800.0,
                    sellingPrice = 950.0,
                    stockQuantity = 3, // Low stock sample
                    minStockThreshold = 10,
                    unit = "Sachet"
                ),
                ProductEntity(
                    id = 4,
                    name = "Attiéké Frais (Carton)",
                    category = "Produits Frais",
                    costPrice = 5000.0,
                    sellingPrice = 7000.0,
                    stockQuantity = 12,
                    minStockThreshold = 4,
                    unit = "Carton"
                ),
                ProductEntity(
                    id = 5,
                    name = "Café Ivoire 250g",
                    category = "Boissons",
                    costPrice = 1200.0,
                    sellingPrice = 1600.0,
                    stockQuantity = 2, // Low stock sample
                    minStockThreshold = 5,
                    unit = "Boîte"
                ),
                ProductEntity(
                    id = 6,
                    name = "Savon Lave-Tout Fanico",
                    category = "Entretien",
                    costPrice = 300.0,
                    sellingPrice = 500.0,
                    stockQuantity = 50,
                    minStockThreshold = 10,
                    unit = "Morceau"
                )
            )
            productDao.insertProducts(sampleProducts)

            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            // Sample Sales
            val sale1Id = saleDao.insertSale(
                SaleEntity(
                    timestamp = now - (oneDayMs * 2),
                    totalAmount = 5900.0,
                    paymentMethod = "Wave",
                    staffName = "Kouassi"
                )
            )
            saleDao.insertSaleItems(
                listOf(
                    SaleItemEntity(
                        saleId = sale1Id,
                        productId = 1,
                        productName = "Riz Uncle Sam 5kg",
                        quantity = 1,
                        unitPrice = 4500.0,
                        costPrice = 3500.0
                    ),
                    SaleItemEntity(
                        saleId = sale1Id,
                        productId = 2,
                        productName = "Huile Dinor 1L",
                        quantity = 1,
                        unitPrice = 1400.0,
                        costPrice = 1100.0
                    )
                )
            )

            val sale2Id = saleDao.insertSale(
                SaleEntity(
                    timestamp = now - (oneDayMs * 1),
                    totalAmount = 7000.0,
                    paymentMethod = "Orange Money",
                    staffName = "Awa"
                )
            )
            saleDao.insertSaleItems(
                listOf(
                    SaleItemEntity(
                        saleId = sale2Id,
                        productId = 4,
                        productName = "Attiéké Frais (Carton)",
                        quantity = 1,
                        unitPrice = 7000.0,
                        costPrice = 5000.0
                    )
                )
            )

            val sale3Id = saleDao.insertSale(
                SaleEntity(
                    timestamp = now,
                    totalAmount = 2450.0,
                    paymentMethod = "Cash",
                    staffName = "Kouassi"
                )
            )
            saleDao.insertSaleItems(
                listOf(
                    SaleItemEntity(
                        saleId = sale3Id,
                        productId = 3,
                        productName = "Sucre Blanc SN SOSUCO 1kg",
                        quantity = 1,
                        unitPrice = 950.0,
                        costPrice = 800.0
                    ),
                    SaleItemEntity(
                        saleId = sale3Id,
                        productId = 6,
                        productName = "Savon Lave-Tout Fanico",
                        quantity = 3,
                        unitPrice = 500.0,
                        costPrice = 300.0
                    )
                )
            )

            // Sample Expenses
            expenseDao.insertExpenses(
                listOf(
                    ExpenseEntity(
                        title = "Loyer boutique - Mois en cours",
                        amount = 45000.0,
                        category = "Rent",
                        timestamp = now - (oneDayMs * 5),
                        receiptImagePath = null,
                        notes = "Boutique Marcory Zone 4"
                    ),
                    ExpenseEntity(
                        title = "Facture d'électricité CIE",
                        amount = 12500.0,
                        category = "Utilities",
                        timestamp = now - (oneDayMs * 3),
                        receiptImagePath = null,
                        notes = "Compteur prépayé"
                    ),
                    ExpenseEntity(
                        title = "Transport livraison marchandise",
                        amount = 3500.0,
                        category = "Transport",
                        timestamp = now - (oneDayMs * 1),
                        receiptImagePath = null,
                        notes = "Livraison Adjamé - Marcory"
                    )
                )
            )
        }
    }
}
