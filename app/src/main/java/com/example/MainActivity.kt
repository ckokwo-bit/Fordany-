/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.FordanyRepository
import com.example.ui.FordanyViewModel
import com.example.ui.components.FordanyDrawerContent
import com.example.ui.components.FordanyTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NewOperationScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FordanyTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = FordanyRepository(db.appDao())

        setContent {
            FordanyTheme {
                val viewModel: FordanyViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return FordanyViewModel(repository) as T
                        }
                    }
                )

                FordanyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FordanyApp(viewModel: FordanyViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val authState by viewModel.authUiState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val accounts by viewModel.allAccountsWithBalance.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val operations by viewModel.allOperations.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    val companyName by viewModel.companyName.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val seuilCaisse by viewModel.seuilAlerteCaisse.collectAsState()
    val objectifJour by viewModel.objectifJournalier.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Affichage des messages toast / snackbar
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Si non connecté, premier lancement ou session verrouillée
    if (authState.currentUser == null || authState.isFirstLaunch || authState.isLockedByInactivity) {
        AuthScreen(
            authState = authState,
            onLogin = { l, p -> viewModel.login(l, p) },
            onCreateFirstAdmin = { n, l, p -> viewModel.createFirstAdmin(n, l, p) },
            onUnlock = { p -> viewModel.unlockSession(p) },
            onLogout = { viewModel.logout() }
        )
        return
    }

    // Détection d'inactivité sur toucher d'écran
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { viewModel.touchActivity() })
            }
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                FordanyDrawerContent(
                    currentScreen = currentScreen,
                    currentUser = authState.currentUser,
                    onNavigate = { screen ->
                        currentScreen = screen
                        viewModel.touchActivity()
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    FordanyTopBar(
                        companyName = companyName,
                        currentUser = authState.currentUser,
                        onOpenDrawer = {
                            viewModel.touchActivity()
                            scope.launch { drawerState.open() }
                        },
                        onLockSession = {
                            viewModel.touchActivity()
                            viewModel.unlockSession("") // provoquera l'écran de verrouillage
                        }
                    )
                },
                bottomBar = {
                    // Navigation inférieure avec les 4 écrans principaux
                    NavigationBar(
                        containerColor = Color(0xFF0A192F),
                        tonalElevation = 8.dp
                    ) {
                        val navItems = listOf(Screen.Dashboard, Screen.NewOperation, Screen.History, Screen.Settings)
                        navItems.forEach { screen ->
                            val isSelected = currentScreen == screen
                            val route = screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentScreen = screen
                                    viewModel.touchActivity()
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFF00B4D8),
                                    indicatorColor = Color(0xFF00B4D8),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("nav_item_$route")
                            )
                        }
                    }
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        Screen.Dashboard -> {
                            // Calcul des métriques de tableau de bord
                            val productMap = remember(products) { products.associateBy { it.id } }
                            val activeOps = remember(operations) { operations.filter { it.annulee == 0 } }

                            val cal = Calendar.getInstance()
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            val startToday = cal.timeInMillis
                            val benefJour = activeOps.filter { it.date >= startToday }
                                .sumOf { computeOperationProfit(it, productMap) }

                            cal.time = Date()
                            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            val startThisWeek = cal.timeInMillis
                            val benefSemaine = activeOps.filter { it.date >= startThisWeek }
                                .sumOf { computeOperationProfit(it, productMap) }

                            cal.add(Calendar.WEEK_OF_YEAR, -1)
                            val startLastWeek = cal.timeInMillis
                            val benefSemainePrec = activeOps.filter { it.date in startLastWeek until startThisWeek }
                                .sumOf { computeOperationProfit(it, productMap) }

                            cal.time = Date()
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            val startThisMonth = cal.timeInMillis
                            val benefMois = activeOps.filter { it.date >= startThisMonth }
                                .sumOf { computeOperationProfit(it, productMap) }

                            // Graphique 7 jours
                            val chartDays = remember(activeOps) {
                                val points = mutableListOf<com.example.data.repository.DayChartPoint>()
                                val dayFmt = java.text.SimpleDateFormat("EEE", java.util.Locale.FRENCH)
                                for (i in 6 downTo 0) {
                                    val dayCal = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, -i)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    val startDay = dayCal.timeInMillis
                                    val endDay = startDay + 24 * 3600 * 1000L
                                    val dayBenef = activeOps.filter { it.date in startDay until endDay }
                                        .sumOf { computeOperationProfit(it, productMap) }
                                    points.add(
                                        com.example.data.repository.DayChartPoint(
                                            dayLabel = dayFmt.format(Date(startDay)).replaceFirstChar { it.uppercase() },
                                            benefice = dayBenef
                                        )
                                    )
                                }
                                points
                            }

                            DashboardScreen(
                                companyName = companyName,
                                currency = currency,
                                accounts = accounts,
                                products = products,
                                operations = operations,
                                users = users,
                                chartPoints = chartDays,
                                seuilCaisse = seuilCaisse,
                                objectifJournalier = objectifJour,
                                beneficeJour = benefJour,
                                beneficeSemaine = benefSemaine,
                                beneficeMois = benefMois,
                                beneficeSemainePrecedente = benefSemainePrec,
                                onNavigateNewOperation = { currentScreen = Screen.NewOperation }
                            )
                        }

                        Screen.NewOperation -> {
                            NewOperationScreen(
                                currency = currency,
                                accounts = accounts,
                                products = products,
                                currentUser = authState.currentUser,
                                onSaveOperation = { type, prodId, accId, qte, montant, motif, photoPreuve ->
                                    viewModel.addOperation(type, prodId, accId, qte, montant, motif, photoPreuve)
                                }
                            )
                        }

                        Screen.History -> {
                            HistoryScreen(
                                currency = currency,
                                operations = operations,
                                accounts = accounts.map { it.account },
                                products = products,
                                users = users,
                                currentUser = authState.currentUser,
                                onCancelOperation = { opId, raison -> viewModel.cancelOperation(opId, raison) },
                                onExportCsv = { ctx -> viewModel.exportCsv(ctx) }
                            )
                        }

                        Screen.Settings -> {
                            SettingsScreen(
                                currentCompanyName = companyName,
                                currentCurrency = currency,
                                currentSeuilCaisse = seuilCaisse,
                                currentObjectif = objectifJour,
                                currentUser = authState.currentUser,
                                users = users,
                                onSaveSettings = { n, d, s, o -> viewModel.saveSettings(n, d, s, o) },
                                onCreateUser = { n, l, m, r -> viewModel.createUser(n, l, m, r) },
                                onToggleUserStatus = { u -> viewModel.toggleUserStatus(u) },
                                onUpdatePassword = { oldPass, newPass -> viewModel.updatePassword(oldPass, newPass) },
                                onExportEncryptedDb = { ctx -> viewModel.exportEncryptedDatabase(ctx) }
                            )
                        }

                        Screen.Accounts -> {
                            AccountsScreen(
                                currency = currency,
                                accounts = accounts,
                                currentUser = authState.currentUser,
                                onAddAccount = { n, t, s -> viewModel.addAccount(n, t, s) },
                                onUpdateAccount = { acc -> viewModel.updateAccount(acc) }
                            )
                        }

                        Screen.Products -> {
                            ProductsScreen(
                                currency = currency,
                                products = products,
                                accounts = accounts.map { it.account },
                                currentUser = authState.currentUser,
                                onAddProduct = { n, pv, pa, src, dst -> viewModel.addProduct(n, pv, pa, src, dst) },
                                onUpdateProduct = { p -> viewModel.updateProduct(p) }
                            )
                        }

                        Screen.Analytics -> {
                            val totalCaisse = accounts.filter { it.account.type == "ARGENT" }.sumOf { it.currentBalance }
                            AnalyticsScreen(
                                currency = currency,
                                companyName = companyName,
                                totalCaisse = totalCaisse,
                                accounts = accounts,
                                operations = operations,
                                products = products,
                                onExportPdf = { ctx, totCaisse, accList, bJour, bSemaine, bMois, synth, topProds ->
                                    viewModel.exportPdf(ctx, totCaisse, accList, bJour, bSemaine, bMois, synth, topProds)
                                }
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

private fun computeOperationProfit(
    op: com.example.data.local.entity.OperationEntity,
    productMap: Map<Long, com.example.data.local.entity.ProductEntity>
): Double {
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
