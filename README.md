# ms-recette

Microservice de gestion des recettes pour l'application SmartDish.

## Description

Le microservice `ms-recette` stocke un catalogue de recettes et leurs ingrédients associés. Il permet de filtrer et trier les recettes en fonction des ingrédients saisis par l’utilisateur et des notes globales. Ce service communique avec les autres microservices via des API REST au format JSON.

Il fait partie de l'application complète comprenant :
- **SmartDish-front** : application front-end développée en **React**, permettant à l’utilisateur de saisir ses ingrédients, visualiser les recettes proposées et donner son feedback.   
- **ms-utilisateur** : gestion des utilisateurs et de leurs préférences alimentaires  
- **ms-recommendation** : propose une recette optimale selon un agent d’apprentissage par renforcement  
- **ms-feedback** : collecte les évaluations des utilisateurs  
- **API Gateway** : orchestration des appels aux microservices  

---

## Architecture

- **Base de données** : MySQL  
- **Tables principales** :
  - `Recette` : informations sur les recettes  
  - `Aliment` : ingrédients liés aux recettes  
  - `Ingredient` : relation entre recettes et ingrédients  

- **Communication** : REST API + JSON  
- **Déploiement** : docker-compose

---

## FUTUR API Endpoints

### Public

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/recettes?=ingredients...` | Retourne les recettes filtrées par ingrédients |
| GET | `/recette/{id}` | Récupère une recette spécifique par ID |
| GET | `/recettes` | Liste toutes les recettes |
| GET | `/recettes?=motscles` | Filtre les recettes par catégories |

### Privé (interne aux autres services)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/recettes` | Création de recettes pour usage interne |
| POST | `/recette` | Création d’une recette pour usage interne |
| DELETE | `/recette/{id}` | Supprime une recette |
| POST | `/aliments` | Création d'aliments pour usage interne |
| POST | `/aliment` | Création d’un aliment pour usage interne |
| DELETE | `/aliment/{id}` | Supprime un aliment |

---

## Installation

1. Cloner le dépôt :  
```bash
git clone https://github.com/ines-bg/ms-recette.git
cd ms-recette
```

2. Construire l’image Docker :
```bash
docker build -t ms-recette .
```

3. Lancer le service avec docker-compose :
```bash
docker-compose up
```

4. Accéder à l’API via :
```bash
http://localhost:8092.
```
