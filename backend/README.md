# LinkedIn Optimizer – Backend

API Spring Boot (Kotlin) qui calcule un score heuristique et génère des suggestions (via LLM) pour un profil LinkedIn.

## Démarrage rapide

```bash
gradle wrapper  # facultatif : génère ./gradlew ignoré du dépôt
./gradlew bootRun
```

Variables d'environnement clés :

- `API_KEY` : clé API requise pour toutes les requêtes.
- `LLM_PROVIDER` : `ollama` (défaut) ou `openrouter`.
- `LLM_MODEL` : nom du modèle à utiliser (`llama3:instruct`, `mistral`, ...).
- `OLLAMA_BASE_URL` : URL du serveur Ollama (défaut `http://localhost:11434`).
- `OPENROUTER_API_KEY` : clé OpenRouter (si provider `openrouter`).

## Endpoints

- `POST /api/v1/score`
- `POST /api/v1/suggest`
- `POST /api/v1/target`
- `GET /actuator/health`
- `GET /v3/api-docs`

Toutes les requêtes doivent inclure `X-API-Key`.

## Exemples

### Requête `/api/v1/score`

```json
{
  "url": "https://www.linkedin.com/in/john-doe",
  "headline": "Data Analyst | SQL • Python",
  "about": "J'explore vos données pour révéler les insights clés.",
  "skills": ["SQL", "Python", "Tableau"],
  "hasPhoto": true
}
```

Réponse :

```json
{
  "total": 72,
  "breakdown": {
    "photo": 10,
    "headline": 10,
    "about": 6,
    "experiences": 15,
    "achievements": 9,
    "skills": 12,
    "location": 5,
    "keywords": 5
  },
  "warnings": [
    "Ajoutez des verbes d'action et des métriques dans vos expériences."
  ]
}
```

### Requête `/api/v1/suggest`

```json
{
  "profile": {
    "url": "https://www.linkedin.com/in/john-doe",
    "headline": "Data Analyst",
    "about": "J'analyse les données.",
    "skills": ["SQL", "Python", "Tableau"],
    "experiences": [
      { "title": "Data Analyst", "company": "Acme", "description": "Je crée des dashboards" }
    ],
    "hasPhoto": true
  },
  "provider": "ollama",
  "model": "llama3:instruct",
  "language": "fr"
}
```

Réponse :

```json
{
  "headline": "Data Analyst | Transformez vos données en décisions",
  "about": "Paragraphe 1...",
  "skills": ["SQL", "Python", "Tableau", "ETL", "Automation"],
  "experienceBullets": [
    {
      "company": "Acme",
      "title": "Data Analyst",
      "bullets": [
        "Optimisé les reporting hebdomadaires, réduisant le temps d'analyse de 30%."
      ]
    }
  ]
}
```

## Tests & Qualité

```bash
gradle wrapper  # si ce n'est pas déjà fait
./gradlew test
./gradlew detekt
./gradlew ktlintCheck
```

## Historisation (optionnel)

Activez `HISTORY_ENABLED=true` pour stocker les réponses LLM dans le dossier défini par `HISTORY_DIRECTORY`.
