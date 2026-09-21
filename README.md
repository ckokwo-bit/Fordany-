# FORDANY MANAGEMENT
### Système Intégré de Gestion Financière & Commerciale
**Client :** Ets FORDANY (République Démocratique du Congo - RDC)  
**Conception & Développement :** **KGC Technologies**  
**Version :** 1.0.0 Pro  
**Format :** Application Android APK native (Kotlin / Jetpack Compose / SQLite Room)

---

## 1. Présentation Générale
**FORDANY MANAGEMENT** est une solution mobile professionnelle conçue sur mesure pour les besoins opérationnels des **Ets FORDANY**, spécialisés dans la vente de forfaits data, les recharges d'unités flash, les cartes SIM et les transferts d'argent mobile (Airtel Money, Orange Money, M-Pesa Vodacom) en RDC.

L'application fonctionne à **100% hors ligne** avec une base de données locale SQLite chiffrée, garantissant une autonomie totale et une disponibilité permanente même en cas de coupure internet.

---

## 2. La Règle d'Or Financière
Conformément aux directives de gestion rigoureuse :
> **Les soldes actuels des comptes ne sont JAMAIS stockés en dur dans la base de données.**  
> Chaque solde est **systématiquement recalculé dynamiquement** en temps réel depuis l'historique non altérable selon la formule immuable :  
> $$\text{Solde Actuel} = \text{Solde Initial} + \sum \text{Entrées} - \sum \text{Sorties}$$

Toute tentative d'annulation d'opération est opérée en **soft delete** (conservation de la trace avec enregistrement obligatoire de la raison d'annulation et journalisation dans les logs d'audit).

---

## 3. Architecture & Sécurité

### 3.1. Normes de Sécurité Appliquées
- **Hachage des mots de passe :** BCrypt avec un facteur de coût de 12 (`BCrypt.hashpw(password, BCrypt.gensalt(12))`).
- **Chiffrement des données sensibles :** AES-256-GCM avec clé dérivée PBKDF2 (256 bits, 65 536 itérations) pour les motifs d'opérations et les sauvegardes exportables.
- **Gestion des sessions :** Jetons JWT HS256 signés valides 8 heures avec gestion de rafraîchissement.
- **Protection Anti-Brute Force :** Rate limiting strict limité à **5 tentatives consécutives** sur 15 minutes avec verrouillage temporaire du compte.
- **Verrouillage par inactivité :** Verrouillage automatique de la session après **5 minutes d'inactivité**, exigeant le mot de passe utilisateur pour reprendre.
- **Contrôle d'accès basé sur les rôles (RBAC) :**
  * `Admin` : Accès complet, gestion des utilisateurs, paramètres, création de comptes et de produits, export de sauvegardes.
  * `Opérateur` : Enregistrement d'opérations, consultation de l'historique et des soldes, annulation avec justification.
  * `Lecteur` : Consultation des tableaux de bord et rapports uniquement (lecture seule).
- **Audit Trails :** Journalisation systématique de chaque action sensible dans la table `audit_logs`.

---

## 4. Structure des Écrans (5 Écrans Principaux + Menu Latéral)

1. **Écran de Connexion / Premier Lancement :**
   - Au premier démarrage : Assistant de configuration du premier Administrateur.
   - Connexion sécurisée avec compteur de tentatives et mention discrète de **KGC Technologies**.
   - Écran de déverrouillage suite à inactivité prolongée.

2. **Tableau de Bord (Dashboard) :**
   - Solde Caisse total en temps réel (somme des comptes d'argent).
   - Soldes recalculés de tous les comptes (Argent et Unités).
   - Indicateurs de bénéfice net : Aujourd'hui, Cette Semaine, Ce Mois.
   - Graphique Canvas haute performance sur 7 jours.
   - Indicateur dynamique de progression : *"Vous progressez !"* ou *"Attention baisse"*.
   - Alertes configurables (Caisse basse et Stock d'unités épuisé).
   - Suivi de l'objectif journalier avec barre de complétion.
   - Raccourci tactile direct vers "Nouvelle opération".

3. **Nouvelle Opération (Formulaire Unique) :**
   - Formulaire tactile optimisé pour les 4 types : **Vente**, **Dépense**, **Don**, **Achat**.
   - Choix du compte et sélection optionnelle du produit catalogue.
   - Recalcul instantané en direct : affiche le nouveau solde estimé du compte et la marge nette prévisionnelle avant validation.
   - Chiffrement transparent du motif en AES-256-GCM.

4. **Historique & Traçabilité :**
   - Liste chronologique des opérations avec filtres par type, compte, statut et recherche textuelle.
   - Pagination fluide (15 opérations par page).
   - Annulation sécurisée avec saisie obligatoire du motif.
   - Export CSV / Excel via l'Android Sharesheet.

5. **Paramètres & Sécurité :**
   - Configuration du nom d'entreprise (défaut : *Ets FORDANY*), de la devise (*FC*), du seuil d'alerte et de l'objectif journalier.
   - Gestion des utilisateurs (Admin only : ajout, activation / désactivation).
   - Export de la base de données chiffrée (.KGC) en AES-256-GCM.
   - Page "À propos" avec attribution officielle à **KGC Technologies**.

6. **Tiroir Latéral (Drawer) :**
   - **Gestion des Comptes** : CRUD des comptes (Nom libre, type ARGENT/UNITE, solde initial).
   - **Gestion des Produits** : Catalogue des forfaits et SIM avec calcul des marges unitaires.
   - **Analyses & Statistiques** : Comparaisons (Jour J vs J-1, Semaine S vs S-1, Mois M vs M-1), phrase de synthèse automatique, top produits les plus rentables, meilleur jour d'activité, et génération de **Rapports PDF** officiels avec en-têtes et filigranes KGC Technologies.
   - **Déconnexion sécurisée**.

---

## 5. Emplacement de l'APK Généré
Le package APK final signé pour distribution est généré à l'emplacement suivant :
```
app/build/outputs/apk/debug/app-debug.apk
```
*Taille : ~23 Mo | Architecture : Universelle Android 8.0+ (API 26 à 34)*

---

## 6. Signature & Mentions
Développé avec excellence par **KGC Technologies** pour **Ets FORDANY**.  
Tous droits de propriété intellectuelle réservés.
