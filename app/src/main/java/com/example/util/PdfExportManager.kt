/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.TopProductStats
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportManager {

    fun generateFinancialReportPdf(
        context: Context,
        companyName: String,
        currency: String,
        totalCaisse: Double,
        accounts: List<AccountWithBalance>,
        beneficeJour: Double,
        beneficeSemaine: Double,
        beneficeMois: Double,
        syntheseTexte: String,
        topProducts: List<TopProductStats>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 format
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val numberFormat = NumberFormat.getNumberInstance(Locale.FRENCH).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }

        // 1. Fond et En-tête supérieur
        paint.color = Color.parseColor("#0A192F") // Bleu marine KGC / Fordany
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Titre
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FORDANY MANAGEMENT", 30f, 42f, paint)

        // Sous-titre entreprise
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.parseColor("#E0E1DD")
        canvas.drawText("$companyName (RDC) • Rapport Financier & Analyses", 30f, 62f, paint)

        // Mention KGC Technologies dans l'en-tête
        paint.textSize = 10f
        paint.color = Color.parseColor("#00B4D8")
        canvas.drawText("Développé par KGC Technologies", 30f, 80f, paint)

        // Date d'export à droite
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date())
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 10f
        canvas.drawText("Édité le : $dateStr", 565f, 50f, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = 130f

        // 2. Synthèse automatique
        paint.color = Color.parseColor("#F0F7F9")
        canvas.drawRoundRect(30f, y, 565f, y + 55f, 8f, 8f, paint)

        paint.color = Color.parseColor("#0077B6")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SYNTHÈSE COMMERCIALE", 45f, y + 22f, paint)

        paint.color = Color.parseColor("#1B263B")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(syntheseTexte, 45f, y + 42f, paint)

        y += 75f

        // 3. Indicateurs de Bénéfices (3 colonnes)
        val colW = (535f - 20f) / 3f

        // Carte Jour
        drawStatBox(canvas, paint, 30f, y, colW, 60f, "Bénéfice Aujourd'hui", "${numberFormat.format(beneficeJour)} $currency", "#06D6A0")
        // Carte Semaine
        drawStatBox(canvas, paint, 30f + colW + 10f, y, colW, 60f, "Bénéfice Cette Semaine", "${numberFormat.format(beneficeSemaine)} $currency", "#00B4D8")
        // Carte Mois
        drawStatBox(canvas, paint, 30f + (colW + 10f) * 2, y, colW, 60f, "Bénéfice Ce Mois", "${numberFormat.format(beneficeMois)} $currency", "#7209B7")

        y += 85f

        // 4. Section Caisse et Soldes des Comptes
        paint.color = Color.parseColor("#0A192F")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ÉTAT DE LA CAISSE ET DES COMPTES", 30f, y, paint)

        paint.color = Color.parseColor("#00B4D8")
        paint.textSize = 11f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total Caisse : ${numberFormat.format(totalCaisse)} $currency", 565f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 15f

        // Tableau des comptes
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)
        paint.color = Color.parseColor("#334155")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("COMPTE", 40f, y + 15f, paint)
        canvas.drawText("TYPE", 220f, y + 15f, paint)
        canvas.drawText("SOLDE ACTUEL", 400f, y + 15f, paint)

        y += 24f
        paint.typeface = Typeface.DEFAULT
        for (acc in accounts.take(6)) {
            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRect(30f, y, 565f, y + 20f, paint)

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 9.5f
            canvas.drawText(acc.account.nom, 40f, y + 14f, paint)

            paint.color = if (acc.account.type == "ARGENT") Color.parseColor("#0077B6") else Color.parseColor("#D97706")
            canvas.drawText(acc.account.type, 220f, y + 14f, paint)

            val soldeUnit = if (acc.account.type == "ARGENT") "$currency" else "Unités"
            paint.color = if (acc.currentBalance >= 0) Color.parseColor("#0F766E") else Color.parseColor("#BE123C")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${numberFormat.format(acc.currentBalance)} $soldeUnit", 400f, y + 14f, paint)
            paint.typeface = Typeface.DEFAULT

            y += 22f
        }

        y += 20f

        // 5. Section Top Produits
        paint.color = Color.parseColor("#0A192F")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOP PRODUITS & PERFORMANCE", 30f, y, paint)

        y += 15f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)
        paint.color = Color.parseColor("#334155")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PRODUIT", 40f, y + 15f, paint)
        canvas.drawText("QTÉ VENDUE", 220f, y + 15f, paint)
        canvas.drawText("TOTAL VENTES", 340f, y + 15f, paint)
        canvas.drawText("BÉNÉFICE NET", 460f, y + 15f, paint)

        y += 24f
        paint.typeface = Typeface.DEFAULT
        if (topProducts.isEmpty()) {
            paint.color = Color.GRAY
            paint.textSize = 9.5f
            canvas.drawText("Aucune vente enregistrée sur la période.", 40f, y + 14f, paint)
            y += 22f
        } else {
            for (prod in topProducts.take(5)) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, y, 565f, y + 20f, paint)

                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 9.5f
                canvas.drawText(prod.productName, 40f, y + 14f, paint)
                canvas.drawText("${numberFormat.format(prod.totalQuantite)}", 220f, y + 14f, paint)
                canvas.drawText("${numberFormat.format(prod.totalMontant)} $currency", 340f, y + 14f, paint)

                paint.color = Color.parseColor("#0F766E")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${numberFormat.format(prod.totalBenefice)} $currency", 460f, y + 14f, paint)
                paint.typeface = Typeface.DEFAULT

                y += 22f
            }
        }

        // 6. Pied de page KGC Technologies
        paint.color = Color.parseColor("#0A192F")
        canvas.drawRect(0f, 800f, 595f, 842f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("FORDANY MANAGEMENT • Système développé par KGC Technologies pour Ets FORDANY", 297f, 820f, paint)
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8f
        canvas.drawText("Règle d'or financière : Soldes recalculés dynamiquement depuis l'historique non altérable", 297f, 832f, paint)

        document.finishPage(page)

        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "Rapport_FORDANY_$timeStamp.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun drawStatBox(
        canvas: android.graphics.Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        accentHex: String
    ) {
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(x, y, x + w, y + h, 8f, 8f, paint)

        // Bordure accent gauche
        paint.color = Color.parseColor(accentHex)
        canvas.drawRoundRect(x, y, x + 5f, y + h, 4f, 4f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(title, x + 12f, y + 20f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 12f, y + 42f, paint)
    }
}
