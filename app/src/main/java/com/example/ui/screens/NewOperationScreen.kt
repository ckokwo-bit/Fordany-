/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.KgcFooter
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOperationScreen(
    currency: String,
    accounts: List<AccountWithBalance>,
    products: List<ProductEntity>,
    currentUser: UserEntity?,
    onSaveOperation: (type: String, produitId: Long?, compteId: Long, quantite: Double, montant: Double, motif: String, photoPreuve: String?) -> Boolean
) {
    val context = LocalContext.current
    val types = listOf("Vente", "Dépense", "Don", "Achat")
    var selectedType by remember { mutableStateOf("Vente") }

    // Compte sélectionné
    var selectedAccount by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.account) }
    var accountExpanded by remember { mutableStateOf(false) }

    // Produit sélectionné (optionnel)
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productExpanded by remember { mutableStateOf(false) }

    var quantiteText by remember { mutableStateOf("1") }
    var montantText by remember { mutableStateOf("") }
    var motifText by remember { mutableStateOf("") }
    var photoPreuvePath by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var operationSavedSuccess by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveProofUriToInternal(context, uri)
            photoPreuvePath = saved
        }
    }

    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    // Recalcul instantané du solde et du bénéfice
    val parsedQuantite = quantiteText.toDoubleOrNull() ?: 1.0
    val parsedMontant = montantText.toDoubleOrNull() ?: 0.0

    val currentAccountWithBal = accounts.find { it.account.id == selectedAccount?.id }
    val soldeActuel = currentAccountWithBal?.currentBalance ?: 0.0

    val nouveauSoldeEstime = when (selectedType) {
        "Vente" -> soldeActuel + parsedMontant
        "Achat", "Dépense", "Don" -> soldeActuel - parsedMontant
        else -> soldeActuel
    }

    val beneficeEstime = when (selectedType) {
        "Vente" -> {
            val coutAchat = (selectedProduct?.prix_achat ?: 0.0) * parsedQuantite
            parsedMontant - coutAchat
        }
        "Dépense", "Don" -> -parsedMontant
        "Achat" -> 0.0 // Immobilisation de stock
        else -> 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titre de l'écran unique
        Column {
            Text(
                text = "Nouvelle Opération",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            )
            Text(
                text = "Formulaire unique avec recalcul instantané des soldes et du bénéfice",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )
        }

        // 1. Sélecteur de type tactile grand format (Vente, Dépense, Don, Achat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { type ->
                val isSelected = selectedType == type
                val color = when (type) {
                    "Vente" -> Color(0xFF06D6A0)
                    "Dépense" -> Color(0xFFEF476F)
                    "Don" -> Color(0xFFFFB703)
                    else -> Color(0xFF00B4D8)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(
                            if (isSelected) color else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedType = type
                            validationError = null
                            operationSavedSuccess = false
                            // Recalculer montant si produit sélectionné
                            if (selectedProduct != null) {
                                if (type == "Vente") {
                                    montantText = ((selectedProduct?.prix_vente ?: 0.0) * parsedQuantite).toString()
                                } else if (type == "Achat") {
                                    montantText = ((selectedProduct?.prix_achat ?: 0.0) * parsedQuantite).toString()
                                }
                            }
                        }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    )
                }
            }
        }

        // Message de succès ou d'erreur
        AnimatedVisibility(visible = operationSavedSuccess) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Opération enregistrée avec succès ! Soldes mis à jour.",
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(visible = validationError != null) {
            validationError?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE11D48))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = it, color = Color(0xFF9F1239), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Formulaire dans une grande carte Material 3
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Choix du Compte (Obligatoire)
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.let { "${it.nom} (${it.type})" } ?: "Sélectionner un compte",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Compte source/destination *") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("op_account_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(acc.account.nom, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Solde : ${numberFormat.format(acc.currentBalance)} ${if (acc.account.type == "ARGENT") currency else "Unités"}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAccount = acc.account
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                // Choix du Produit (Optionnel)
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.nom ?: "Aucun produit sélectionné (opération directe)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Produit associé (Optionnel)") },
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("op_product_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("— Aucun produit (Saisie manuelle) —", color = Color.Gray) },
                            onClick = {
                                selectedProduct = null
                                productExpanded = false
                            }
                        )
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(prod.nom, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Prix vente: ${numberFormat.format(prod.prix_vente)} $currency | Prix achat: ${numberFormat.format(prod.prix_achat)} $currency",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = prod
                                    productExpanded = false
                                    // Calcul automatique du montant selon le type
                                    if (selectedType == "Vente") {
                                        montantText = (prod.prix_vente * parsedQuantite).toString()
                                    } else if (selectedType == "Achat") {
                                        montantText = (prod.prix_achat * parsedQuantite).toString()
                                    }
                                }
                            )
                        }
                    }
                }

                // Quantité et Montant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantiteText,
                        onValueChange = {
                            quantiteText = it
                            val q = it.toDoubleOrNull() ?: 1.0
                            if (selectedProduct != null) {
                                if (selectedType == "Vente") {
                                    montantText = (selectedProduct!!.prix_vente * q).toString()
                                } else if (selectedType == "Achat") {
                                    montantText = (selectedProduct!!.prix_achat * q).toString()
                                }
                            }
                        },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("op_quantite_input")
                    )

                    OutlinedTextField(
                        value = montantText,
                        onValueChange = { montantText = it },
                        label = { Text("Montant ($currency) *") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("op_montant_input")
                    )
                }

                // Motif (texte libre - Chiffrement fort AES-256-GCM)
                OutlinedTextField(
                    value = motifText,
                    onValueChange = { motifText = it },
                    label = { Text("Motif / Note (chiffré AES-256 en base)") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    placeholder = { Text("Ex: Forfait Airtel 2Go client Jean...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("op_motif_input")
                )

                // Preuve photo / reçu (Optionnel)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PREUVE PHOTO / REÇU / BORDEREAU (OPTIONNEL)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (photoPreuvePath == null) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Color(0xFF00B4D8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ajouter une preuve photo / reçu",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    } else {
                        val bitmap = remember(photoPreuvePath) {
                            try {
                                BitmapFactory.decodeFile(photoPreuvePath)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Preuve photo",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF00B4D8),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Preuve photo attachée",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF06D6A0)
                                            )
                                        )
                                        Text(
                                            text = "Enregistrée avec l'opération",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { photoPreuvePath = null }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer la preuve",
                                        tint = Color(0xFFEF476F)
                                    )
                                }
                            }
                        }
                    }
                }

                // Metadonnées automatiques
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Date : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date())}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Par : ${currentUser?.nom ?: "Système"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                        )
                    }
                }
            }
        }

        // 3. Carte de Recalcul Instantané (Prévisualisation temps réel)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A192F)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RECALCUL INSTANTANÉ EN TEMPS RÉEL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Solde actuel du compte :", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "${numberFormat.format(soldeActuel)} $currency",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Nouveau solde après validation :", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "${numberFormat.format(nouveauSoldeEstime)} $currency",
                            color = if (nouveauSoldeEstime >= 0) Color(0xFF06D6A0) else Color(0xFFEF476F),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bénéfice estimé de l'opération :", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${if (beneficeEstime >= 0) "+" else ""}${numberFormat.format(beneficeEstime)} $currency",
                        color = if (beneficeEstime >= 0) Color(0xFF06D6A0) else Color(0xFFEF476F),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 4. Gros Bouton tactile d'enregistrement
        Button(
            onClick = {
                val acc = selectedAccount
                if (acc == null) {
                    validationError = "Veuillez sélectionner un compte."
                    return@Button
                }
                if (parsedMontant <= 0 && selectedType != "Achat") {
                    validationError = "Veuillez saisir un montant supérieur à zéro."
                    return@Button
                }
                val success = onSaveOperation(
                    selectedType,
                    selectedProduct?.id,
                    acc.id,
                    parsedQuantite,
                    parsedMontant,
                    motifText,
                    photoPreuvePath
                )
                if (success) {
                    operationSavedSuccess = true
                    validationError = null
                    montantText = ""
                    motifText = ""
                    photoPreuvePath = null
                    quantiteText = "1"
                    selectedProduct = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_operation_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "ENREGISTRER L'OPÉRATION ($selectedType)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
        }

        KgcFooter()
    }
}

private fun saveProofUriToInternal(context: Context, uri: Uri): String? {
    return try {
        val proofsDir = File(context.filesDir, "proofs")
        if (!proofsDir.exists()) proofsDir.mkdirs()
        val destFile = File(proofsDir, "preuve_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}
