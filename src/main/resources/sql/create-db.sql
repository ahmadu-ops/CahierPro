-- Création de la base de données (si elle n'existe pas déjà)
CREATE DATABASE IF NOT EXISTS gestion_cahier_texte;
USE gestion_cahier_texte;

-- Table utilisateur : contient tous les utilisateurs (chef de département, responsables de classe, enseignants)
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('chef_departement', 'responsable_classe', 'enseignant') NOT NULL
);

-- Table classe : chaque classe est gérée par un responsable (responsable_classe)
CREATE TABLE IF NOT EXISTS classe (
    id_classe INT AUTO_INCREMENT PRIMARY KEY,
    nom_classe VARCHAR(100) NOT NULL,
    id_responsable INT,
    FOREIGN KEY (id_responsable) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Table cours : contient les informations sur les cours, avec volume horaire et l'enseignant chargé
CREATE TABLE IF NOT EXISTS cours (
    id_cours INT AUTO_INCREMENT PRIMARY KEY,
    nom_cours VARCHAR(100) NOT NULL,
    volume_horaire INT NOT NULL,
    id_enseignant INT,
    FOREIGN KEY (id_enseignant) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Créer la table d'association entre classes et cours
CREATE TABLE IF NOT EXISTS classe_cours (
    id_classe_cours INT AUTO_INCREMENT PRIMARY KEY,
    id_classe INT NOT NULL,
    id_cours INT NOT NULL,
    FOREIGN KEY (id_classe) REFERENCES classe(id_classe) ON DELETE CASCADE,
    FOREIGN KEY (id_cours) REFERENCES cours(id_cours) ON DELETE CASCADE,
    UNIQUE (id_classe, id_cours) -- Éviter les doublons
);

-- Table seance : représente chaque séance d'un cours, avec une date, un contenu, une durée et un statut de validation
CREATE TABLE IF NOT EXISTS seance (
    id_seance INT AUTO_INCREMENT PRIMARY KEY,
    id_cours INT,
    contenu TEXT,
    date_seance DATE,
    duree INT,
    estValidee BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_cours) REFERENCES cours(id_cours) ON DELETE CASCADE
);

-- Table fiche_suivi : contient les fiches de suivi générées pour une séance

-- Supprimer l'ancienne table si elle existe déjà
DROP TABLE IF EXISTS fiche_pedagogique;

-- Créer la nouvelle table avec la structure mise à jour
CREATE TABLE fiche_pedagogique (
    id_fiche INT AUTO_INCREMENT PRIMARY KEY,
    id_cours INT NOT NULL,
    date_generation DATE NOT NULL,
    pourcentage_realisation DOUBLE DEFAULT 0.0,
    commentaire TEXT,
    FOREIGN KEY (id_cours) REFERENCES cours(id_cours) ON DELETE CASCADE
);