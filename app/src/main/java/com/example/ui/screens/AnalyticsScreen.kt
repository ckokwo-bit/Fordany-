/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.TopProductStats
import com.example.ui.components.KgcFooter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    currency: String,
    companyName: String,
    totalCaisse: Double,
    accounts: List<AccountWithBalance>,
    operations: List<OperationEntity>,
    products: List<ProductEntity>,
    onExportPdf: (
        context: Context,
        totalCaisse: Double,
        accounts: List<AccountWithBalance>,
        beneficeJour: Double,
        beneficeSemaine: Double,
        beneficeMois: Double,
        syntheseTexte: String,
        topProducts: List<TopProductStats>
    ) -> Unit
) {
    val context = LocalContext.current
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    val productMap = remember(products) { products.associateBy { it.id } }
    val activeOps = remember(operations) { operations.filter { it.annulee == 0 } }

    // Calculs de comparaison temporelle
    val cal = Calendar.getInstance()

    // 1. Aujourd'hui vs Hier
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val startToday = cal.timeInMillis
    val opsToday = activeOps.filter { it.date >= startToday }
    val beneficeJour = opsToday.sumOf { computeOpProfit(it, productMap) }

    cal.add(Calendar.DAY_OF_YEAR, -1)
    val startYesterday = cal.timeInMillis
    val opsYesterday = activeOps.filter { it.date in startYesterday until startToday }
    val beneficeHier = opsYesterday.sumOf { computeOpProfit(it, productMap) }

    // 2. Semaine vs Semaine Passée
    cal.time = Date()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val startThisWeek = cal.timeInMillis
    val opsThisWeek = activeOps.filter { it.date >= startThisWeek }
    val beneficeSemaine = opsThisWeek.sumOf { computeOpProfit(it, productMap) }

    cal.add(Calendar.WEEK_OF_YEAR, -1)
    val startLastWeek = cal.timeInMillis
    val opsLastWeek = activeOps.filter { it.date in startLastWeek until startThisWeek }
    val beneficeSemainePassee = opsLastWeek.sumOf { computeOpProfit(it, productMap) }

    // 3. Mois vs Mois Passé
    cal.time = Date()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val startThisMonth = cal.timeInMillis
    val opsThisMonth = activeOps.filter { it.date >= startThisMonth }
    val beneficeMois = opsThisMonth.sumOf { computeOpProfit(it, productMap) }

    cal.add(Calendar.MONTH, -1)
    val startLastMonth = cal.timeInMillis
    val opsLastMonth = activeOps.filter { it.date in startLastMonth until startThisMonth }
    val beneficeMoisPasse = opsLastMonth.sumOf { computeOpProfit(it, productMap) }

    // Phrase synthétique automatique : "Cette semaine vous avez gagné X FC soit +Y% vs semaine passée"
    val diffSemainePct = if (beneficeSemainePassee != 0.0) {
        ((beneficeSemaine - beneficeSemainePassee) / kotlin.math.abs(beneficeSemainePassee)) * 100.0
    } else 0.0
    val sign = if (diffSemainePct >= 0) "+" else ""
    val syntheseTexte = "Cette semaine vous avez réalisé un bénéfice net de ${numberFormat.format(beneficeSemaine)} $currency, soit $sign${String.format(Locale.US, "%.1f", diffSemainePct)}% par rapport à la semaine passée."

    // Calcul des Top Produits
    val topProducts = remember(activeOps, products) {
        val venteOps = activeOps.filter { it.type == "Vente" && it.produit_id != null }
        venteOps.groupBy { it.produit_id!! }.map { (pId, list) ->
            val p = productMap[pId]
            val nom = p?.nom ?: "Produit #$pId"
            val totalQ = list.sumOf { it.quantite }
            val totalM = list.sumOf { it.montant }
            val totalB = list.sumOf { computeOpProfit(it, productMap) }
            TopProductStats(nom, totalQ, totalM, totalB)
        }.sortedByDescending { it.totalBenefice }
    }

    // Meilleur jour de la semaine
    val bestDay = remember(opsThisWeek) {
        val daysFormat = SimpleDateFormat("EEEE", Locale.FRENCH)
        val dayGroups = opsThisWeek.groupBy { daysFormat.format(Date(it.date)).replaceFirstChar { c -> c.uppercase() } }
        val bestEntry = dayGroups.maxByOrNull { entry -> entry.value.sumOf { computeOpProfit(it, productMap) } }
        bestEntry?.key ?: "N/A"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analyses & Statistiques",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Comparaisons périodiques et performances commerciales",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                Button(
                    onClick = {
                        onExportPdf(
                            context,
                            totalCaisse,
                            accounts,
                            beneficeJour,
                            beneficeSemaine,
                            beneficeMois,
                            syntheseTexte,
                            topProducts
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A192F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rapport PDF", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // 1. Phrase Synthétique Automatique dans une grande carte
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A192F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1B3A6B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF00B4D8))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SYNTHÈSE HEBDOMADAIRE AUTOMATIQUE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF00B4D8),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = syntheseTexte,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Meilleur jour d'activité : $bestDay",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF90E0EF), fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 2. Comparaisons des périodes (3 cartes)
        item {
            Text(
                text = "Comparaisons Temporelles",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PeriodCompareCard(
                    title = "Aujourd'hui vs Hier",
                    currentVal = beneficeJour,
                    prevVal = beneficeHier,
                    currency = currency,
                    numberFormat = numberFormat
                )

                PeriodCompareCard(
                    title = "Cette Semaine vs Semaine Passée",
                    currentVal = beneficeSemaine,
                    prevVal = beneficeSemainePassee,
                    currency = currency,
                    numberFormat = numberFormat
                )

                PeriodCompareCard(
                    title = "Ce Mois vs Mois Passé",
                    currentVal = beneficeMois,
                    prevVal = beneficeMoisPasse,
                    currency = currency,
                    numberFormat = numberFormat
                )
            }
        }

        // 3. Top Produits Vendus
        item {
            Text(
                text = "Top Produits les Plus Rentables",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (topProducts.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucune vente de produit enregistrée.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topProducts.take(5).forEachIndexed { index, stat ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                when (index) {
                                                    0 -> Color(0xFFFFD166)
                                                    1 -> Color(0xFFE0E1DD)
                                                    2 -> Color(0xFFF4A261)
                                                    else -> Color(0xFFE2E8F0)
                                                },
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = stat.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Qté vendue : ${numberFormat.format(stat.totalQuantite)} • Chiffre : ${numberFormat.format(stat.totalMontant)} $currency",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+${numberFormat.format(stat.totalBenefice)} $currency",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF16A34A),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Bénéfice net",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF06D6A0))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            KgcFooter()
        }
    }
}

@Composable
private fun PeriodCompareCard(
    title: String,
    currentVal: Double,
    prevVal: Double,
    currency: String,
    numberFormat: NumberFormat
) {
    val diff = currentVal - prevVal
    val isUp = diff >= 0
    val pct = if (prevVal != 0.0) ((diff / kotlin.math.abs(prevVal)) * 100.0) else 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Actuel: ${numberFormat.format(currentVal)} $currency",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "vs Préc: ${numberFormat.format(prevVal)} $currency",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isUp) Color(0xFF16A34A) else Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (isUp) "+" else ""}${String.format(Locale.US, "%.1f", pct)}%",
                    fontWeight = FontWeight.Bold,
                    color = if (isUp) Color(0xFF16A34A) else Color(0xFFDC2626),
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun computeOpProfit(op: OperationEntity, productMap: Map<Long, ProductEntity>): Double {
    return when (op.type) {
        "Vente" -> {
            val cost = (op.produit_id?.let { productMap[it]?.prix_achat } ?: 0.0) * op.quantite
            op.montant - cost
        }
        "Dépense", "Don" -> -op.montant
        "Achat" -> 0.0
        else -> 0.0
    }
}
