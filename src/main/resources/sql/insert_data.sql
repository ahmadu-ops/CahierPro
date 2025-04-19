-- Active: 1744738886314@@127.0.0.1@3306@gestion_cahier_texte
USE gestion_cahier_texte;

-- 1. Utilisateurs (inchangé)
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES
  ('Fall',    'Mamadou', 'mamadou.fall@example.sn',   'pass123', 'chef_departement'),    -- id_utilisateur = 1
  ('Ndoye',   'Aminata', 'aminata.ndoye@example.sn',   'pass123', 'responsable_classe'),  -- id = 2
  ('Diagne',  'Khouma',  'khouma.diagne@example.sn',    'pass123', 'responsable_classe'),  -- id = 3
  ('Sarr',    'Pape',    'pape.sarr@example.sn',        'pass123', 'responsable_classe'),  -- id = 4
  ('Cissé',   'Ousmane', 'ousmane.cisse@example.sn',    'pass123', 'enseignant'),          -- id = 5
  ('Fall',    'Awa',     'awa.fall@example.sn',         'pass123', 'enseignant'),          -- id = 6
  ('NDiaye',  'Fatou',   'fatou.ndiaye@example.sn',     'pass123', 'enseignant');         -- id = 7

-- 2. Classes (inchangé)
INSERT INTO classe (nom_classe, id_responsable) VALUES
  ('LI1', 2),  -- responsable = Aminata Ndoye
  ('LI2', 3),  -- responsable = Khouma Diagne
  ('LI3', 4);  -- responsable = Pape Sarr

-- 3. Cours : 3 cours par enseignant
INSERT INTO cours (nom_cours, volume_horaire, id_enseignant) VALUES
  -- Enseignant id=5 (Ousmane Cissé)
  ('Programmation',             30, 5),  
  ('Structures de données',     25, 5),
  ('Algorithmique',             20, 5),
  -- Enseignant id=6 (Awa Fall)
  ('Réseaux informatiques',     30, 6),
  ('Systèmes d\'exploitation',  25, 6),
  ('Sécurité des systèmes',     20, 6),
  -- Enseignant id=7 (Fatou NDiaye)
  ('Bases de données',          30, 7),
  ('Modélisation UML',          25, 7),
  ('Programmation Web',         20, 7);

-- 4. Liaison classes ↔ cours : 3 cours par classe
INSERT INTO classe_cours (id_classe, id_cours) VALUES
  -- LI1 (id_classe=1) reprend les cours d'Ousmane Cissé (cours id 1,2,3)
  (1, 1),
  (1, 2),
  (1, 3),
  -- LI2 (id_classe=2) reprend les cours d'Awa Fall (cours id 4,5,6)
  (2, 4),
  (2, 5),
  (2, 6),
  -- LI3 (id_classe=3) reprend les cours de Fatou NDiaye (cours id 7,8,9)
  (3, 7),
  (3, 8),
  (3, 9);

-- 5. Séances : une séance par cours
INSERT INTO seance (id_cours, contenu, date_seance, duree, estValidee) VALUES
  -- Pour les cours de LI1
  (1, 'Intro aux variables et types',          '2025-03-10', 2, TRUE),
  (2, 'Listes, piles et files',               '2025-03-12', 2, TRUE),
  (3, 'Boucles et récursivité',               '2025-03-14', 2, FALSE),
  -- Pour les cours de LI2
  (4, 'Modèle OSI et TCP/IP',                  '2025-03-11', 2, TRUE),
  (5, 'Gestion des processus',                '2025-03-13', 2, TRUE),
  (6, 'Pare-feu et chiffrement',              '2025-03-15', 2, FALSE),
  -- Pour les cours de LI3
  (7, 'Modélisation entité-association',      '2025-03-16', 2, TRUE),
  (8, 'Diagrammes de classes UML',            '2025-03-18', 2, TRUE),
  (9, 'HTML5 et CSS3 de base',                '2025-03-20', 2, FALSE);

-- 6. Fiches pédagogiques : une fiche par séance
INSERT INTO fiche_pedagogique (id_cours, date_generation, pourcentage_realisation, commentaire) VALUES
  (1, '2025-03-11', 85.0, 'Très bon démarrage sur les variables'),
  (2, '2025-03-13', 78.5, 'Manipulation des listes bien comprise'),
  (3, '2025-03-15', 60.0, 'Récursivité à revoir'),
  (4, '2025-03-12', 90.0, 'OSI et TCP/IP assimilés'),
  (5, '2025-03-14', 82.0, 'Processus multitâches OK'),
  (6, '2025-03-16', 55.0, 'Chiffrement mal compris'),
  (7, '2025-03-17', 88.0, 'Modèle E-A bien maîtrisé'),
  (8, '2025-03-19', 80.0, 'UML solide, quelques détails à préciser'),
  (9, '2025-03-21', 70.0, 'HTML/CSS OK, JavaScript à prévoir');
