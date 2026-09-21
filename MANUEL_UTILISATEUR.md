# MANUEL D'UTILISATION - FORDANY MANAGEMENT
### Guide Opérationnel pour Ets FORDANY (RDC)
**Développé par KGC Technologies**

---

## 1. Démarrage de l'Application

### 1.1. Premier Lancement
Au tout premier lancement de l'application, l'écran de **Configuration Initiale** apparaît automatiquement :
1. Renseignez le **Nom complet** du gérant ou de l'administrateur principal.
2. Choisissez un **Identifiant (Login)** simple et mémorisable (ex: `admin`).
3. Saisissez un **Mot de passe** sécurisé (au moins 4 caractères).
4. Confirmez le mot de passe et appuyez sur **CRÉER L'ADMINISTRATEUR**.
Le système enregistre l'administrateur avec un chiffrement fort **bcrypt coût 12** et ouvre directement votre espace de travail.

### 1.2. Connexion Quotidienne
- Saisissez votre identifiant et votre mot de passe.
- **Sécurité Anti-Intrusion :** En cas de 5 erreurs consécutives, l'accès est bloqué pendant 15 minutes.
- **Verrouillage d'Inactivité :** Si vous quittez votre téléphone ou ne touchez pas l'écran pendant plus de 5 minutes, l'écran se verrouille automatiquement pour protéger votre caisse. Il vous suffira de retaper votre mot de passe pour reprendre immédiatement votre travail.

---

## 2. Configuration Initiale des Comptes et des Produits

Avant d'enregistrer vos premières opérations, configurez vos comptes depuis le menu latéral (tiroir) :

### 2.1. Créer vos Comptes
Appuyez sur le menu burger en haut à gauche, puis sélectionnez **Gestion des Comptes** :
- **Compte Caisse Principale :** Type `ARGENT`, indiquez vos espèces disponibles.
- **Compte Airtel Money :** Type `ARGENT`, indiquez le solde du compte float Airtel Money.
- **Compte Orange Money :** Type `ARGENT`, indiquez le solde Orange Money.
- **Compte M-Pesa Vodacom :** Type `ARGENT`, indiquez le solde M-Pesa.
- **Compte Stock Unités Airtel :** Type `UNITE`, indiquez le montant d'unités de recharge en stock.
- **Compte Stock Unités Vodacom / Orange :** Type `UNITE`.

### 2.2. Ajouter vos Produits / Forfaits
Dans le menu latéral, sélectionnez **Gestion des Produits** :
- Exemple : *"Forfait Internet 1 Go"* | Prix d'achat : 2 000 FC | Prix de vente : 2 500 FC.
- Exemple : *"Carte SIM Airtel Préactivée"* | Prix d'achat : 500 FC | Prix de vente : 1 000 FC.

---

## 3. Enregistrement des Opérations (Écran Unique)

Rendez-vous sur l'onglet **Nouvelle opération** :
1. **Choisissez le type :**
   - **Vente :** Vente d'un forfait data, de crédit ou d'une SIM. (Augmente le solde du compte d'argent).
   - **Dépense :** Achat de fournitures, loyer, transport, électricité. (Déduit du compte d'argent).
   - **Don :** Gratification ou prélèvement non commercial.
   - **Achat :** Réapprovisionnement de votre stock d'unités ou de cartes SIM.
2. **Sélectionnez le compte** concerné (ex: Caisse Principale ou Airtel Money).
3. **Sélectionnez un produit (optionnel) :** Si vous choisissez un produit, le prix et le montant se calculent automatiquement.
4. **Indiquez le montant et la quantité**.
5. **Saisissez un motif :** Ce texte sera automatiquement chiffré en AES-256-GCM.
6. **Contrôlez le recalcul en temps réel :** Le cadre bleu marine affiche immédiatement votre nouveau solde et votre bénéfice net estimé avant validation.
7. Appuyez sur **ENREGISTRER L'OPÉRATION**.

---

## 4. Consultation de l'Historique et Annulation (Soft Delete)

Sur l'onglet **Historique** :
- Retrouvez toutes les opérations classées de la plus récente à la plus ancienne.
- Utilisez la barre de recherche ou les boutons de filtre (**Vente, Dépense, Don, Achat**) pour trouver une transaction.
- **Pour annuler une erreur de saisie :**
  1. Appuyez sur le bouton **Annuler** en face de l'opération.
  2. Indiquez la raison (ex: *"Erreur montant client"*).
  3. Validez : l'opération passe au statut **ANNULÉE**. Ses montants sont immédiatement retirés du solde et du bénéfice, mais la trace comptable reste archivée pour l'audit.
- **Export CSV :** Appuyez sur le bouton vert **Export CSV** en haut pour partager le relevé complet par WhatsApp, email ou clé USB.

---

## 5. Analyses et Rapport Financier PDF

Dans le menu latéral, appuyez sur **Analyses & Statistiques** :
- Visualisez la phrase synthétique automatique de vos performances de la semaine.
- Comparez vos résultats : *Aujourd'hui vs Hier*, *Cette Semaine vs Semaine Passée*, *Ce Mois vs Mois Passé*.
- Consultez le tableau des **Top Produits** les plus rentables et découvrez votre meilleur jour de vente.
- **Génération du Rapport PDF :** Appuyez sur **Rapport PDF** pour produire un document professionnel format A4 avec en-têtes et filigranes officiels de **KGC Technologies**.

---

## 6. Paramètres et Sauvegarde Chiffrée

Sur l'onglet **Paramètres** :
- Modifiez le nom de votre établissement ou le seuil d'alerte de caisse basse.
- Ajoutez des comptes pour vos employés en leur assignant le rôle **Opérateur** ou **Lecteur**.
- **Sauvegarde de sécurité :** Appuyez sur **EXPORTER LA BASE CHIFFRÉE (.KGC)** pour sauvegarder vos données dans un coffre-fort numérique chiffré en AES-256-GCM.

---
*Solution réalisée par **KGC Technologies** pour **Ets FORDANY**.*
