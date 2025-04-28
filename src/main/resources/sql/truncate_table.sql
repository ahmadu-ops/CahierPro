-- Active: 1744738886314@@127.0.0.1@3306@gestion_cahier_texte
-- Utiliser la bonne base
USE gestion_cahier_texte;

-- Désactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE fiche_pedagogique;
TRUNCATE TABLE seance;
TRUNCATE TABLE classe_cours;
TRUNCATE TABLE cours;
TRUNCATE TABLE classe;
TRUNCATE TABLE utilisateur;

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;