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

## Diagramme de classes

```plantuml
@startuml OctoNet

' Style du diagramme
skinparam classAttributeIconSize 0
skinparam class {
    BackgroundColor White
    ArrowColor Black
    BorderColor Black
}

' Classes principales
class Main {
    + {static} main(String[] args)
}

class Admin {
    - Map<String, Client> clients
    - List<String> remoteServers
    - Map<String, String> routingTable
    - Serveur serveur
    - AdminUI adminUI
    - String localIP
    + Admin()
    + addClient(String clientName)
    + addRemoteClient(String clientName, String serverAddress)
    + addRemoteServer(String host)
    + updateRoutingTable(String serializedTable)
    + sendMessage(String from, String to, String message)
    + sendMessageFromClientToClient(String from, String to, String message)
    + removeClient(String clientName)
    + getLocalIP()
    + getPortSrv()
    + getRoutingTable()
    + getRemoteServers()
    + getServeur()
}

class AdminUI {
    - DefaultListModel<String> clientListModel
    - JList<String> clientJList
    - JTextField serverAddressField
    - JTextField clientSrcField
    - JTextField clientDestField
    - JTextField messageField
    - Admin admin
    + {static} Map<String, ClientWindow> clientWindows
    - JLabel serverInfoLabel
    - JTextArea logArea
    - JTextArea routingTableArea
    + AdminUI(Admin admin)
    + addClientToList(String clientName)
    + updateRoutingTable()
    + addMessageToClientWindow(String from, String clientName, String message)
    + addLog(String log)
    + removeClientFromList(String clientName)
    - showClientWindow(String clientName)
    - generateRandomClientId()
}

class Client {
    - String name
    + Client(String name)
    + getName()
    + receiveMessage(String from, String message)
    + sendMessage(String destClient, String message)
    + sendTrame(Trame trame)
    - compressLZ78(String input)
    - decompressLZ78(String compressed)
    - calculateParity(String data)
    - checkParity(String data)
}

class ClientWindow {
    - String clientName
    - JTextArea chatArea
    - JTextField messageField
    - JTextField destField
    - Admin admin
    + ClientWindow(String clientName, Admin admin)
    - sendMessage()
    + receiveMessage(String from, String to, String message)
    + getClientName()
}

class Serveur {
    - Admin admin
    - AdminUI adminUI
    - int port
    + Serveur(Admin admin)
    + start()
    + setAdminUI(AdminUI adminUI)
    + getPort()
    + sendTrameToServer(Trame trame, String serverAddress)
}

' Classes de trames
class Trame {
    + {abstract} int type_message
    + {abstract} String serveur_cible
    + {abstract} String serveur_source
}

class Trame_message {
    + String client_cible
    + String client_source
    + String du
    + Trame_message(int type_message, String serveur_cible, String serveur_source, String client_cible, String client_source, String du)
    + getClient_cible()
    + getClient_source()
    + getDu()
}

class Trame_routage {
    + List<String> serveurs
    + List<List<String>> clients
    + List<Integer> distances
    + Trame_routage(int type_message, String serveur_cible, String serveur_source, List<String> serveurs, List<List<String>> clients, List<Integer> distances)
}

' Relations
Main --> Admin : creates
Admin --> AdminUI : creates
Admin --> Serveur : creates
Admin --> Client : creates
AdminUI --> ClientWindow : creates
Client --> Trame : uses
Client --> Trame_message : uses
Serveur --> Trame : uses
Serveur --> Trame_routage : uses

Trame <|-- Trame_message
Trame <|-- Trame_routage

@enduml
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