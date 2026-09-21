/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.UserEntity
import com.example.data.repository.DayChartPoint
import com.example.ui.navigation.Screen
import java.text.NumberFormat
import java.util.Locale

/**
 * En-tête supérieur de l'application avec logo entreprise, titre ETS FORDANY et mention by kgc technologies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FordanyTopBar(
    companyName: String,
    currentUser: UserEntity?,
    onOpenDrawer: () -> Unit,
    onLockSession: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_fordany_logo),
                    contentDescription = "Logo ETS FORDANY",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ETS FORDANY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by kgc technologies",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF90E0EF),
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("drawer_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Ouvrir le menu latéral",
                    tint = Color.White
                )
            }
        },
        actions = {
            if (currentUser != null) {
                // Badge rôle
                Box(
                    modifier = Modifier
                        .background(
                            when (currentUser.role) {
                                "Admin" -> Color(0xFFEF476F)
                                "Opérateur" -> Color(0xFF06D6A0)
                                else -> Color(0xFF118AB2)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentUser.role,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                IconButton(
                    onClick = onLockSession,
                    modifier = Modifier.testTag("lock_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouiller la session",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0A192F)
        )
    )
}

/**
 * Pied de page discret KGC Technologies obligatoire
 */
@Composable
fun KgcFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ETS FORDANY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = "by kgc technologies",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * Tiroir latéral (Drawer) pour les modules complémentaires
 */
@Composable
fun FordanyDrawerContent(
    currentScreen: Screen,
    currentUser: UserEntity?,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Color(0xFF0A192F)
    ) {
        // En-tête branding avec logo officiel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F264A))
                .padding(20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_fordany_logo),
                contentDescription = "Logo ETS FORDANY",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ETS FORDANY",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "by kgc technologies",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF00B4D8),
                    fontWeight = FontWeight.Medium
                )
            )
            if (currentUser != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connecté : ${currentUser.nom} (${currentUser.role})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Écrans du drawer
        NavigationDrawerItem(
            label = { Text("Gestion des Comptes", color = Color.White) },
            icon = { Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = Color(0xFF00B4D8)) },
            selected = currentScreen == Screen.Accounts,
            onClick = {
                onNavigate(Screen.Accounts)
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                selectedContainerColor = Color(0xFF1B3A6B)
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("Gestion des Produits", color = Color.White) },
            icon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF06D6A0)) },
            selected = currentScreen == Screen.Products,
            onClick = {
                onNavigate(Screen.Products)
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                selectedContainerColor = Color(0xFF1B3A6B)
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("Analyses & Statistiques", color = Color.White) },
            icon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFFFFB703)) },
            selected = currentScreen == Screen.Analytics,
            onClick = {
                onNavigate(Screen.Analytics)
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                selectedContainerColor = Color(0xFF1B3A6B)
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
        Divider(color = Color(0xFF1E3A5F), modifier = Modifier.padding(horizontal = 16.dp))

        NavigationDrawerItem(
            label = { Text("Déconnexion", color = Color(0xFFEF476F)) },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFEF476F)) },
            selected = false,
            onClick = {
                onCloseDrawer()
                onLogout()
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        KgcFooter(modifier = Modifier.padding(bottom = 12.dp))
    }
}

/**
 * Carte d'alerte Caisse Basse
 */
@Composable
fun LowCashAlertBanner(
    soldeActuel: Double,
    seuil: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.FRENCH)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFE11D48),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Alerte : Caisse Basse !",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9F1239)
                    )
                )
                Text(
                    text = "Le solde total (${numberFormat.format(soldeActuel)} $currency) est inférieur au seuil fixé (${numberFormat.format(seuil)} $currency).",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBE123C))
                )
            }
        }
    }
}

/**
 * Carte d'alerte Stock Épuisé
 */
@Composable
fun StockAlertBanner(
    comptesEpuises: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Alerte : Stock Unités Épuisé !",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                )
                Text(
                    text = "Solde nul ou négatif sur : ${comptesEpuises.joinToString(", ")}.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB45309))
                )
            }
        }
    }
}

/**
 * Graphique 7 jours dessiné avec Compose Canvas pur, fluide et élégant
 */
@Composable
fun SevenDayProfitChart(
    points: List<DayChartPoint>,
    currency: String,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val numberFormat = NumberFormat.getNumberInstance(Locale.FRENCH).apply {
        maximumFractionDigits = 0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tendance des Bénéfices (7 derniers jours)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )
                Text(
                    text = currency,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxBenefice = points.maxOfOrNull { it.benefice }?.coerceAtLeast(1.0) ?: 1.0
            val minBenefice = points.minOfOrNull { it.benefice }?.coerceAtMost(0.0) ?: 0.0
            val range = (maxBenefice - minBenefice).coerceAtLeast(1.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 36f
                    val topPadding = 20f
                    val chartHeight = h - bottomPadding - topPadding

                    // Ligne du zéro
                    val zeroY = topPadding + chartHeight * (1f - ((0.0 - minBenefice) / range).toFloat()).coerceIn(0f, 1f)
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, zeroY),
                        end = Offset(w, zeroY),
                        strokeWidth = 2f
                    )

                    val stepX = w / (points.size - 1).coerceAtLeast(1)
                    val offsets = mutableListOf<Offset>()

                    for ((index, pt) in points.withIndex()) {
                        val x = index * stepX
                        val normalized = ((pt.benefice - minBenefice) / range).toFloat().coerceIn(0f, 1f)
                        val y = topPadding + chartHeight * (1f - normalized)
                        offsets.add(Offset(x, y))
                    }

                    // Remplissage dégradé sous la courbe
                    if (offsets.size >= 2) {
                        val fillPath = Path().apply {
                            moveTo(offsets.first().x, zeroY)
                            for (pt in offsets) {
                                lineTo(pt.x, pt.y)
                            }
                            lineTo(offsets.last().x, zeroY)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x5500B4D8), Color(0x0500B4D8)),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Tracé de la ligne principale
                        val linePath = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            for (i in 1 until offsets.size) {
                                lineTo(offsets[i].x, offsets[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = Color(0xFF00B4D8),
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )

                        // Cercles des points
                        for (pt in offsets) {
                            drawCircle(
                                color = Color(0xFF0A192F),
                                radius = 6f,
                                center = pt
                            )
                            drawCircle(
                                color = Color(0xFF06D6A0),
                                radius = 4f,
                                center = pt
                            )
                        }
                    }

                    // Labels des jours en bas
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 24f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    for ((index, pt) in points.withIndex()) {
                        val x = index * stepX
                        drawContext.canvas.nativeCanvas.drawText(
                            pt.dayLabel,
                            x,
                            h - 6f,
                            paint
                        )
                    }
                }
            }
        }
    }
}
