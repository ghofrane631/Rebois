-- Script d'initialisation PostgreSQL + PostGIS
-- À exécuter en tant que superutilisateur (postgres)

-- Créer la base de données foret_db si elle n'existe pas
SELECT 'CREATE DATABASE foret_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'foret_db')\gexec

-- Se connecter à foret_db et activer PostGIS
\c foret_db

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
