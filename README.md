# OctoNet

OctoNet est un projet de réseau client-serveur conçu pour gérer la communication entre plusieurs unités (clients) et un serveur central, avec un rôle d’administrateur pour la supervision. L’architecture repose sur des échanges de données structurés et suit des normes définies pour assurer une communication fiable et efficace.

## Structure du projet

- `common/` : Classes communes pour la gestion des trames (`Trame.java`, `Trame_message.java`, `Trame_routage.java`).
- `fr/octonet/` : Composants principaux de l'application :
  - `Main.java` : Point d'entrée du programme.
  - `Admin.java` / `AdminUI.java` : Interface et logique d'administration du réseau.
  - `Client.java` / `ClientWindow.java` : Client réseau et interface utilisateur.
  - `Serveur.java` : Serveur central du réseau.

## Compilation et exécution

1. Compiler le projet :
   ```sh
   javac -d bin common/*.java fr/octonet/*.java
   ```
2. Exécuter le programme principal :
   ```sh
   java -cp bin fr.octonet.Main
   ```

## Fonctionnalités principales
- Simulation de clients et serveurs sur un réseau virtuel.
- Gestion des trames de message et de routage.
- Interface graphique pour l'administration et les clients.

## Auteurs
- Projet réalisé par les étudiants de l'ESISAR, module IN363.

## Licence
Ce projet est à usage pédagogique.
