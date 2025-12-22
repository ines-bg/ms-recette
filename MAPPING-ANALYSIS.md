# Analyse du Mapping entre ms-recette et ms-persistance

## ✅ État du Mapping

### Champs Compatibles

| Champ | ms-recette | ms-persistance | Status |
|-------|-----------|----------------|--------|
| `id` | Long | Long | ✅ Compatible |
| `titre` | String | String | ✅ Compatible |
| `tempsTotal` | Integer | Integer | ✅ Compatible |
| `kcal` | Integer | Integer | ✅ Compatible |
| `imageUrl` | String | String | ✅ Compatible |
| `difficulte` | String (Enum) | Enum | ✅ Compatible* |
| `dateCreation` | LocalDateTime | LocalDateTime | ✅ Compatible |
| `dateModification` | LocalDateTime | LocalDateTime | ✅ Compatible |
| `ingredients` | List<IngredientResponse> | List<IngredientDTO> | ✅ Compatible |
| `etapes` | List<EtapeResponse> | List<EtapeDTO> | ✅ Compatible |

*La difficulté est compatible car Jackson convertit automatiquement entre String et Enum.

### ⚠️ Champs NON Mappés (Ignorés Gracieusement)

#### Champs envoyés par ms-recette mais NON utilisés par ms-persistance:
- `description` (String) - **Ignoré lors de l'envoi**
- `categorie` (String) - **Ignoré lors de l'envoi**
- `tags` (List<String>) - **Ignoré lors de l'envoi**

#### Champs attendus par ms-recette mais NON retournés par ms-persistance:
- `noteMoyenne` (Double) - **Sera null dans la réponse**
- `nombreFeedbacks` (Integer) - **Sera null dans la réponse**

## 🔧 Solutions Appliquées

### 1. Configuration Jackson Globale
**Fichier:** `JacksonConfig.java`

```java
@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
```

**Effet:**
- Les champs inconnus dans les réponses de ms-persistance sont ignorés
- Les champs null ne sont pas envoyés à ms-persistance

### 2. Annotations sur les Classes Request/Response

**Toutes les classes Request ont:**
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
```

**Toutes les classes Response ont:**
```java
@JsonIgnoreProperties(ignoreUnknown = true)
```

## 📋 Mapping Détaillé par Endpoint

### POST /api/persistance/recettes (Création)

**ms-recette envoie (RecetteCreateRequest):**
```json
{
  "titre": "string",
  "description": "string",          // ⚠️ Ignoré par ms-persistance
  "tempsTotal": 30,
  "kcal": 500,
  "imageUrl": "string",
  "difficulte": "MOYEN",
  "categorie": "string",             // ⚠️ Ignoré par ms-persistance
  "ingredients": [...],
  "etapes": [...],
  "tags": [...]                      // ⚠️ Ignoré par ms-persistance
}
```

**ms-persistance reçoit (RecetteDTO):**
```json
{
  "titre": "string",
  "tempsTotal": 30,
  "kcal": 500,
  "imageUrl": "string",
  "difficulte": "MOYEN",
  "ingredients": [...],
  "etapes": [...]
}
```

**ms-persistance retourne (RecetteDTO):**
```json
{
  "id": 1,
  "titre": "string",
  "tempsTotal": 30,
  "kcal": 500,
  "imageUrl": "string",
  "difficulte": "MOYEN",
  "dateCreation": "2025-12-22T...",
  "dateModification": "2025-12-22T...",
  "ingredients": [...],
  "etapes": [...]
}
```

**ms-recette convertit en (RecetteResponse):**
```json
{
  "id": 1,
  "titre": "string",
  "description": null,               // ⚠️ Non fourni par ms-persistance
  "tempsTotal": 30,
  "kcal": 500,
  "imageUrl": "string",
  "difficulte": "MOYEN",
  "categorie": null,                 // ⚠️ Non fourni par ms-persistance
  "dateCreation": "2025-12-22T...",
  "dateModification": "2025-12-22T...",
  "ingredients": [...],
  "etapes": [...],
  "tags": null,                      // ⚠️ Non fourni par ms-persistance
  "noteMoyenne": null,               // ⚠️ Non fourni par ms-persistance
  "nombreFeedbacks": null            // ⚠️ Non fourni par ms-persistance
}
```

### Mapping des Ingrédients

**ms-recette (IngredientRequest/Response):**
```json
{
  "id": 1,
  "alimentId": 5,
  "alimentNom": "Spaghetti",
  "quantite": 200.0,
  "unite": "GRAMME",
  "principal": true
}
```

**ms-persistance (IngredientDTO):**
```json
{
  "id": 1,
  "alimentId": 5,
  "alimentNom": "Spaghetti",
  "quantite": 200.0,
  "unite": "GRAMME",
  "principal": true
}
```

✅ **Mapping 100% compatible**

### Mapping des Étapes

**ms-recette (EtapeRequest/Response):**
```json
{
  "id": 1,
  "ordre": 1,
  "temps": 10,
  "texte": "Faire cuire les pâtes"
}
```

**ms-persistance (EtapeDTO):**
```json
{
  "id": 1,
  "ordre": 1,
  "temps": 10,
  "texte": "Faire cuire les pâtes"
}
```

✅ **Mapping 100% compatible**

### Mapping des Fichiers

**ms-recette (FichierRecetteResponse):**
```json
{
  "id": 1,
  "recetteId": 1,
  "nomOriginal": "image.jpg",
  "nomStockage": "uuid.jpg",
  "contentType": "image/jpeg",
  "taille": 1024,
  "type": "IMAGE",
  "url": "path/to/file",
  "dateCreation": "2025-12-22T..."
}
```

**ms-persistance (FichierRecetteDTO):**
```json
{
  "id": 1,
  "nomOriginal": "image.jpg",
  "nomStocke": "uuid.jpg",          // ⚠️ Nom différent
  "contentType": "image/jpeg",
  "taille": 1024,
  "type": "IMAGE",
  "cheminMinio": "path/to/file",    // ⚠️ Nom différent
  "recetteId": 1,
  "dateUpload": "2025-12-22T..."    // ⚠️ Nom différent
}
```

⚠️ **Mapping partiellement compatible** - Les champs avec des noms différents seront null

## 🎯 Recommandations

### Pour une compatibilité totale, modifier dans ms-persistance:

1. **Ajouter les champs manquants dans RecetteDTO:**
   - `description` (String)
   - `categorie` (String)
   - `tags` (List<String>)

2. **Renommer dans FichierRecetteDTO:**
   - `nomStocke` → `nomStockage`
   - `cheminMinio` → `url`
   - `dateUpload` → `dateCreation`

### OU ajuster FichierRecetteResponse dans ms-recette:

```java
@JsonProperty("nomStocke")
private String nomStockage;

@JsonProperty("cheminMinio")
private String url;

@JsonProperty("dateUpload")
private LocalDateTime dateCreation;
```

## ✅ Conclusion

Le mapping actuel fonctionne grâce aux annotations Jackson qui:
1. **Ignorent** les champs inconnus lors de la désérialisation
2. **N'envoient pas** les champs null lors de la sérialisation
3. Permettent une **compatibilité partielle** entre les deux microservices

**Les fonctionnalités principales fonctionnent correctement** malgré les champs manquants, car:
- Les champs essentiels (titre, temps, kcal, ingrédients, étapes) sont tous compatibles
- Les champs supplémentaires (description, catégorie, tags) sont optionnels côté ms-recette
- Les champs manquants dans les réponses (noteMoyenne, nombreFeedbacks) sont simplement null

