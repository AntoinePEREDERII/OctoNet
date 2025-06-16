# 🐙 OctoNet

<img src="docs/octonet.png" alt="Logo 
OctoNet" height="150"/>

[![Java](https://img.shields.io/badge/Java-000?style=for-the-badge&logo=openjdk&logoColor=white&color=red)](https://openjdk.org)
[![Réseau](https://img.shields.io/badge/Projet-Network-000?style=for-the-badge&logo=gnu&logoColor=white&color=blue)]()
[![Swing](https://img.shields.io/badge/Interface-Graphique-000?style=for-the-badge&logo=swing&logoColor=white&color=green)]()

## Présentation

**OctoNet** est un projet de réseau pédagogique développé à l'**ESISAR** dans le cadre du module **IN363 — I Make My Own Network (IMMON)** (2024–2025).

Il s'agit d'une **simulation d'échanges de trames réseau** entre plusieurs clients et serveurs, encadrée par un administrateur. Le projet vise à mieux comprendre les problématiques de **transmission**, **routage**, et **gestion d'un réseau distribué**.

## Architecture générale

Le projet repose sur 3 composants principaux :

| Composant  | Description                                                                                                                      |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------- |
| **Client** | Envoie et reçoit des messages via une interface graphique. Communique avec d'autres clients via le serveur.                      |
| **Serveur** | Gère le routage des messages entre les clients et maintient l'état du réseau.                                                    |
| **Admin**  | Interface graphique pour la gestion du réseau (lancement des serveurs et clients, configuration du réseau).                      |

## Fonctionnalités

### ✅ Client
* Interface graphique pour l'envoi et la réception de messages
* Communication avec d'autres clients via le serveur
* Gestion des connexions au serveur

### ✅ Serveur
* Routage des messages entre les clients
* Gestion des connexions clients
* Maintien de l'état du réseau

### ✅ Admin
* Interface graphique pour la gestion du réseau
* Lancement des serveurs et des clients
* Configuration du réseau
## Structure du projet

```
OctoNet/
├── src/
│   ├── common/          → Classes partagées
│   │   ├── Trame.java
│   │   ├── Trame_message.java
│   │   └── Trame_routage.java
│   ├── module-info.java
│   └── fr/
│       └── octonet/
│           ├── Main.java
│           ├── Client.java
│           ├── ClientWindow.java
│           ├── Serveur.java
│           ├── Admin.java
│           └── AdminUI.java
├── bin/
└── README.md
```

## Lancer le projet

### Prérequis
* Java 17+
* IDE compatible Java (recommandé)

### Compilation
```bash
javac src/fr/octonet/*.java
```

### Exécution
```bash
java -cp src fr.octonet.Main
```

## Développement

Projet développé par **Antoine PEREDERII**, **Remy CUVELIER**, **Remi COURTY**, dans le cadre du module **IN363 — Réseaux & Protocoles** à l'**ESISAR (Grenoble INP)**.

Encadrant : **Jean-Baptiste Caignaert**
