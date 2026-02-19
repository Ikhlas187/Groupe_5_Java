-- phpMyAdmin SQL Dump
-- version 5.2.1deb3
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost:3306
-- Généré le : sam. 14 fév. 2026 à 18:58
-- Version du serveur : 8.0.45-0ubuntu0.24.04.1
-- Version de PHP : 8.3.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `gestion_finance`
--

-- --------------------------------------------------------

--
-- Structure de la table `budget`
--

CREATE TABLE `budget` (
  `id_budget` int NOT NULL,
  `id_utilisateur` int NOT NULL,
  `id_categorie` int NOT NULL,
  `montant` decimal(15,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `categorie`
--

CREATE TABLE `categorie` (
  `id_categorie` int NOT NULL,
  `nomCategorie` varchar(100) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categorie`
--

INSERT INTO `categorie` (`id_categorie`, `nomCategorie`) VALUES
(1, 'Alimentation'),
(2, 'Logement'),
(3, 'Santé'),
(4, 'Transport');

-- --------------------------------------------------------

--
-- Structure de la table `depense`
--

CREATE TABLE `depense` (
  `id_depense` int NOT NULL,
  `id_utilisateur` int NOT NULL,
  `id_categorie` int NOT NULL,
  `id_sous_categorie` int DEFAULT NULL,
  `id_budget` int DEFAULT NULL,
  `montant` decimal(15,2) NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `depense`
--

INSERT INTO `depense` (`id_depense`, `id_utilisateur`, `id_categorie`, `id_sous_categorie`, `id_budget`, `montant`, `description`, `date`) VALUES
(1, 1, 1, 1, NULL, 5000.00, 'Restaurant Chez Marie', '2026-02-10'),
(2, 1, 1, 2, NULL, 15000.00, 'Courses au marché', '2026-02-09'),
(3, 1, 2, 4, NULL, 2000.00, 'Taxi', '2026-02-11'),
(4, 1, 3, 7, NULL, 25000.00, 'Loyer mensuel', '2026-02-01'),
(5, 1, 4, 12, NULL, 3000.00, 'Médicaments', '2026-02-08'),
(6, 1, 1, 1, NULL, 8000.00, 'Déjeuner restaurant', '2026-02-12'),
(7, 1, 2, 5, NULL, 1500.00, 'Zem', '2026-02-13');

-- --------------------------------------------------------

--
-- Structure de la table `revenu`
--

CREATE TABLE `revenu` (
  `id_revenu` int NOT NULL,
  `id_utilisateur` int NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `montant` decimal(15,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `sous_categorie`
--

CREATE TABLE `sous_categorie` (
  `id_sous_categorie` int NOT NULL,
  `id_categorie` int NOT NULL,
  `nomSousCategorie` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `sous_categorie`
--

INSERT INTO `sous_categorie` (`id_sous_categorie`, `id_categorie`, `nomSousCategorie`, `description`) VALUES
(1, 1, 'Restaurant', 'Repas au restaurant'),
(2, 1, 'Courses', 'Achats au marché/supermarché'),
(3, 2, 'Carburant', 'Essence/diesel'),
(4, 2, 'Taxi', 'Taxi'),
(5, 2, 'Zem', 'Moto-taxi'),
(6, 3, 'Loyer', 'Paiement mensuel du loyer'),
(7, 4, 'Médicaments', 'Pharmacie');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `id_utilisateur` int NOT NULL,
  `nom` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `prenom` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateur`
--

INSERT INTO `utilisateur` (`id_utilisateur`, `nom`, `prenom`, `email`, `password`, `telephone`, `date_creation`) VALUES
(1, 'AGBETI', 'Ikhlas', 'olaagb40@gmail.com', 'ikhlas2007', '2290162201054', '2026-02-10 12:58:41');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `budget`
--
ALTER TABLE `budget`
  ADD PRIMARY KEY (`id_budget`),
  ADD KEY `id_utilisateur` (`id_utilisateur`),
  ADD KEY `id_categorie` (`id_categorie`);

--
-- Index pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id_categorie`),
  ADD UNIQUE KEY `unique_nomCategorie` (`nomCategorie`);

--
-- Index pour la table `depense`
--
ALTER TABLE `depense`
  ADD PRIMARY KEY (`id_depense`),
  ADD KEY `id_utilisateur` (`id_utilisateur`),
  ADD KEY `id_categorie` (`id_categorie`),
  ADD KEY `id_sous_categorie` (`id_sous_categorie`),
  ADD KEY `id_budget` (`id_budget`);

--
-- Index pour la table `revenu`
--
ALTER TABLE `revenu`
  ADD PRIMARY KEY (`id_revenu`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `sous_categorie`
--
ALTER TABLE `sous_categorie`
  ADD PRIMARY KEY (`id_sous_categorie`),
  ADD KEY `id_categorie` (`id_categorie`);

--
-- Index pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`id_utilisateur`),
  ADD UNIQUE KEY `unique_email` (`email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `budget`
--
ALTER TABLE `budget`
  MODIFY `id_budget` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id_categorie` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `depense`
--
ALTER TABLE `depense`
  MODIFY `id_depense` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `revenu`
--
ALTER TABLE `revenu`
  MODIFY `id_revenu` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `sous_categorie`
--
ALTER TABLE `sous_categorie`
  MODIFY `id_sous_categorie` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  MODIFY `id_utilisateur` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
