/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.DayChartPoint
import com.example.security.CryptoManager
import com.example.ui.components.KgcFooter
import com.example.ui.components.LowCashAlertBanner
import com.example.ui.components.SevenDayProfitChart
import com.example.ui.components.StockAlertBanner
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    companyName: String,
    currency: String,
    accounts: List<AccountWithBalance>,
    products: List<ProductEntity>,
    operations: List<OperationEntity>,
    users: List<UserEntity>,
    chartPoints: List<DayChartPoint>,
    seuilCaisse: Double,
    objectifJournalier: Double,
    beneficeJour: Double,
    beneficeSemaine: Double,
    beneficeMois: Double,
    beneficeSemainePrecedente: Double,
    onNavigateNewOperation: () -> Unit
) {
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    // Calculs de synthèse
    val totalCaisse = accounts.filter { it.account.type == "ARGENT" }.sumOf { it.currentBalance }
    val isCaisseBasse = totalCaisse < seuilCaisse
    val stockEpuise = accounts.filter { it.account.type == "UNITE" && it.currentBalance <= 0.0 }.map { it.account.nom }

    val isProgressing = beneficeSemaine >= beneficeSemainePrecedente
    val progressionPercent = if (beneficeSemainePrecedente != 0.0) {
        ((beneficeSemaine - beneficeSemainePrecedente) / kotlin.math.abs(beneficeSemainePrecedente)) * 100.0
    } else 0.0

    val progressionObjectif = (beneficeJour / objectifJournalier.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    val activeOps = operations.filter { it.annulee == 0 }
    val recentOps = activeOps.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. En-tête officiel ETS FORDANY avec logo
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_fordany_logo),
                        contentDescription = "Logo ETS FORDANY",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ETS FORDANY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0A192F),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "by kgc technologies",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF00B4D8),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Système Sécurisé",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        // 1. Alertes configurables
        item {
            AnimatedVisibility(visible = isCaisseBasse) {
                LowCashAlertBanner(
                    soldeActuel = totalCaisse,
                    seuil = seuilCaisse,
                    currency = currency
                )
            }
            AnimatedVisibility(visible = stockEpuise.isNotEmpty()) {
                StockAlertBanner(comptesEpuises = stockEpuise)
            }
        }

        // 2. Carte Principale : Solde Total Caisse
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF0A192F)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SOLDE TOTAL CAISSE (LIQUIDITÉS)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF90E0EF),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Comptes d'argent recalculés en temps réel",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1B3A6B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF06D6A0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "${numberFormat.format(totalCaisse)} $currency",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barre d'objectif journalier
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Objectif du jour : ${numberFormat.format(objectifJournalier)} $currency",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1))
                        )
                        Text(
                            text = "${(progressionObjectif * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (progressionObjectif >= 1f) Color(0xFF06D6A0) else Color(0xFFFFB703),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressionObjectif },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (progressionObjectif >= 1f) Color(0xFF06D6A0) else Color(0xFF00B4D8),
                        trackColor = Color(0xFF1E3A5F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton Action Rapide Nouvelle Opération
                    Button(
                        onClick = onNavigateNewOperation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dashboard_new_op_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EFFECTUER UNE OPÉRATION", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // 3. Indicateur de Progression / Baisse
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isProgressing) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isProgressing) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProgressing) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isProgressing) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isProgressing) "Vous progressez !" else "Attention baisse",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isProgressing) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        )
                        val sign = if (progressionPercent >= 0) "+" else ""
                        Text(
                            text = "Cette semaine : ${numberFormat.format(beneficeSemaine)} $currency ($sign${String.format(Locale.US, "%.1f", progressionPercent)}% vs semaine passée)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isProgressing) Color(0xFF166534) else Color(0xFF991B1B)
                            )
                        )
                    }
                }
            }
        }

        // 4. Cartes des Bénéfices (Jour, Semaine, Mois)
        item {
            Text(
                text = "Bénéfices Nets Recalculés",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = "Aujourd'hui",
                    amount = beneficeJour,
                    currency = currency,
                    numberFormat = numberFormat,
                    accentColor = Color(0xFF06D6A0),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Cette Semaine",
                    amount = beneficeSemaine,
                    currency = currency,
                    numberFormat = numberFormat,
                    accentColor = Color(0xFF00B4D8),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Ce Mois",
                    amount = beneficeMois,
                    currency = currency,
                    numberFormat = numberFormat,
                    accentColor = Color(0xFF7209B7),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Graphique 7 Jours
        item {
            SevenDayProfitChart(points = chartPoints, currency = currency)
        }

        // 6. Soldes de Tous les Comptes (Argent et Unités)
        item {
            Text(
                text = "Soldes de Tous les Comptes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Text(
                text = "Règle d'or : solde_initial + Σentrées − Σsorties (jamais stocké en dur)",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (accounts.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucun compte configuré. Rendez-vous dans le menu 'Comptes' pour en ajouter (ex: Caisse Principale, Airtel Money, Orange Money, Unités).",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { acc ->
                        AccountBalanceRowCard(
                            acc = acc,
                            currency = currency,
                            numberFormat = numberFormat
                        )
                    }
                }
            }
        }

        // 7. Dernières opérations
        item {
            Text(
                text = "Dernières Opérations",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (recentOps.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucune opération enregistrée pour le moment.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                    )
                }
            } else {
                val accountMap = accounts.associateBy { it.account.id }
                val productMap = products.associateBy { it.id }
                val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentOps.forEach { op ->
                        val compteNom = accountMap[op.compte_id]?.account?.nom ?: "Compte #${op.compte_id}"
                        val produitNom = op.produit_id?.let { productMap[it]?.nom }
                        val motifClair = CryptoManager.decrypt(op.motif_chiffre)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                when (op.type) {
                                                    "Vente" -> Color(0xFFDCFCE7)
                                                    "Dépense" -> Color(0xFFFEE2E2)
                                                    "Don" -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFE0F2FE)
                                                },
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = op.type.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = when (op.type) {
                                                "Vente" -> Color(0xFF16A34A)
                                                "Dépense" -> Color(0xFFDC2626)
                                                "Don" -> Color(0xFFD97706)
                                                else -> Color(0xFF0284C7)
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = op.type + if (produitNom != null) " • $produitNom" else "",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$compteNom • ${dateFormat.format(Date(op.date))}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                            )
                                            if (op.photo_preuve != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = "Preuve photo disponible",
                                                    tint = Color(0xFF00B4D8),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        if (motifClair.isNotBlank()) {
                                            Text(
                                                text = motifClair,
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${if (op.type == "Vente") "+" else "-"}${numberFormat.format(op.montant)} $currency",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (op.type == "Vente") Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                )
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
private fun MetricKpiCard(
    title: String,
    amount: Double,
    currency: String,
    numberFormat: NumberFormat,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(4.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${numberFormat.format(amount)} $currency",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccountBalanceRowCard(
    acc: AccountWithBalance,
    currency: String,
    numberFormat: NumberFormat
) {
    val isArgent = acc.account.type == "ARGENT"
    val unitLabel = if (isArgent) currency else "Unités"

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
                        .size(38.dp)
                        .background(
                            if (isArgent) Color(0xFFE0F2FE) else Color(0xFFFEF3C7),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isArgent) Icons.Default.AccountBalance else Icons.Default.SimCard,
                        contentDescription = null,
                        tint = if (isArgent) Color(0xFF0284C7) else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = acc.account.nom,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Type : ${acc.account.type} • Initial : ${numberFormat.format(acc.account.solde_initial)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${numberFormat.format(acc.currentBalance)} $unitLabel",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (acc.currentBalance >= 0) Color(0xFF0F766E) else Color(0xFFBE123C)
                    )
                )
                Text(
                    text = "+${numberFormat.format(acc.totalEntries)} / -${numberFormat.format(acc.totalSorties)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
