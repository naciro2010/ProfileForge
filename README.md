# LinkedIn Optimizer (Side Panel + Backend Kotlin)

Optimisez un profil LinkedIn en respectant strictement les CGU :

- **Extension Chrome** Manifest V3 basée sur le **Side Panel** (React + Tailwind),
- **Backend Kotlin Spring Boot 3** (API score, suggestions, market-intel, compensation),
- Interfaces **LLM** (Ollama local ou OpenRouter) et agrégation des tendances marché.

> ⚠️ Aucune action automatisée ni modification du DOM LinkedIn. L’utilisateur colle manuellement les textes.

## Sommaire
- [Fonctionnement](#fonctionnement)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation rapide](#installation-rapide)
- [Utilisation](#utilisation)
- [Configuration](#configuration)
- [API](#api)
- [Conformité & limites](#conformité--limites)
- [Déploiement low-cost](#déploiement-low-cost)
- [Données marché & rémunération](#données-marché--rémunération)
- [Tests](#tests)

## Fonctionnement
1. Ouvrez un profil LinkedIn dans Chrome ; le side panel apparaît automatiquement.
2. Collez vos sections (Headline, About, compétences, expériences) dans l’onglet **Score**.
3. Lancez l’analyse → score 0–100, avertissements et diagnostics.
4. Onglet **Suggestions** : variantes prêtes à copier (headline, About, skills, bullets) + boutons **Copier**/**Cibler** (scroll vers la section LinkedIn).
5. Onglet **Marché & Salaire** : tendances recruteurs (Indeed Hiring Lab, SHRM, presse) + estimation salaire/TJM par pays.

## Architecture
```
extension/ (React + Vite + Tailwind)
└─ Side panel MV3 (tabs Score, Suggestions, Marché & Salaire)

backend/ (Spring Boot 3, Kotlin, Java 21)
├─ api/              REST controllers (score, suggest, market-intel, compensation)
├─ suggestion/       Heuristiques + LLM (Ollama/OpenRouter)
├─ marketintel/      Agrégation + cache Caffeine des tendances publiques
├─ compensation/     Estimation salaire/TJM (BLS, ONS, APEC, Eurostat)
├─ score/            Score heuristique déterministe
└─ util/             Text utils, mapping métiers
```

- **Sécurité** : header `X-API-Key`, CORS limité à `chrome-extension://*`, rate limiting Bucket4j.
- **Observabilité** : Actuator (`/actuator/health`), Prometheus.
- **Qualité** : ktlint, detekt, tests JUnit/MockMvc, ESLint/Tailwind côté front.

## Prérequis
- Chrome (Manifest V3).
- Node.js 20+, npm ou pnpm.
- JDK 21.
- Docker (optionnel pour compose + Ollama).
- (Optionnel) Ollama installé (`ollama pull llama3:instruct`).

## Installation rapide
### Backend
```bash
cd backend
gradle wrapper
./gradlew bootRun
# ou via Docker
docker build -t linkedin-optimizer-backend .
docker run -p 8080:8080 -e API_KEY=changeme linkedin-optimizer-backend
```

### Extension
```bash
cd extension
npm install
npm run build
# charger dist/ dans chrome://extensions (mode développeur)
```

### Docker Compose (backend + Ollama)
```bash
docker compose up -d
# backend sur http://localhost:8080, Ollama sur http://localhost:11434
```

## Utilisation
1. Ouvrez `www.linkedin.com` → le side panel se fixe à droite.
2. Renseignez vos sections dans l’onglet **Score** puis cliquez “Analyser ce profil”.
3. Passez sur **Suggestions** pour copier les variantes (headline ≤220, About 3–5 paragraphes, skills ≤12, bullets par expérience).
4. Dans **Marché & Salaire**, renseignez rôle/pays et obtenez les tendances + la fourchette `[low, median, high]` salaire ou TJM.

## Configuration
Options de l’extension :
- URL du backend (`http://localhost:8080` par défaut),
- `X-API-Key`,
- Langue des suggestions (`fr`/`en`).

Variables backend (`src/main/resources/application.yml`):
- `API_KEY` (clé requise),
- `LLM_PROVIDER=ollama|openrouter`, `LLM_MODEL`,
- `OLLAMA_BASE_URL`, `OPENROUTER_API_KEY`,
- `CORS.ALLOWED-ORIGINS` (par défaut `chrome-extension://*`).

## API
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/score` | Score 0–100 + breakdown et warnings (entrée : `ScoreRequest`). |
| POST | `/api/v1/suggest` | Suggestions prêtes à coller (`SuggestionResponse`). |
| POST | `/api/v1/market-intel` | Top skills + signaux recruteurs agrégés (cache 30 j). |
| POST | `/api/v1/compensation` | Fourchette salaire ou TJM `[low, median, high]` + justifications. |
| GET | `/actuator/health` | Healthcheck. |
| GET | `/swagger-ui.html` | Documentation OpenAPI. |

### Exemples JSON
`ScoreRequest`
```json
{
  "profile": {
    "headline": "Senior Data Analyst",
    "about": "J’aide les scale-ups à piloter leur croissance…",
    "skills": ["SQL", "Product Analytics"],
    "experiences": [
      {
        "role": "Lead Data Analyst",
        "company": "Acme",
        "achievements": "Réduction du churn de 15%"
      }
    ],
    "location": "Paris"
  }
}
```

`CompensationRequest`
```json
{
  "role": "Product Manager",
  "country": "FR",
  "seniority": "SENIOR",
  "companyType": "ENTERPRISE",
  "contractType": "PERMANENT"
}
```

## Conformité & limites
- UI side panel : aucune injection DOM, aucune action automatisée.
- Texte fourni par l’utilisateur ou stocké localement (`chrome.storage.session` chiffré par Chrome).
- Sources marché/tendances limitées à des publications publiques whitelistes (Indeed Hiring Lab, SHRM, Forbes/Axios…).
- Pas de scraping massif : seules les données collées par l’utilisateur sont traitées.

## Déploiement low-cost
**Option A – 100 % gratuit**
- Oracle Cloud “Always Free” ARM (jusqu’à 4 OCPU / 24 Go) pour héberger le backend Dockerisé.
- Cloudflare Tunnel pour exposer l’API sans IP publique.
- Caddy ou Traefik pour TLS automatique.

**Option B – VPS low-cost**
- Hetzner Cloud CX11 (≈5 €/mois), Docker Compose `backend + ollama`, Traefik pour HTTPS.

**Option C – PaaS budget**
- Fly.io ou Render Free (pour staging). Vérifier les limites CPU/RAM avant prod.

## Données marché & rémunération
- **USA** : BLS OEWS 2024 (quartiles par SOC + ajustement coût de la vie).
- **UK** : ONS ASHE 2024 (région + SOC).
- **France** : APEC 2024 (cadres) + INSEE (salaires/indépendants).
- **UE** : Eurostat Earnings 2024.
- **TJM France** : baromètres Hays/BDM 2025, Silkhom 2024.
- **Tendances recruteurs** : Indeed Hiring Lab 2025, SHRM 2025, articles presse (Forbes, Axios) relayant LinkedIn.

## Tests
```bash
# backend
./gradlew test
# extension
npm run lint
```
