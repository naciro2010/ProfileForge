# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2024-05-30
### Added
- Side Panel MV3 extension (React + Tailwind) avec onglets Score, Suggestions, Marché & Salaire.
- Scroll assisté “Cibler” sans injection DOM, stockage session chiffré et options Sync.
- Backend Spring Boot modularisé (score, suggestion, market intel, compensation) avec cache Caffeine & scheduling.
- Estimation salaire/TJM multi-pays (BLS, ONS, APEC, Eurostat) + justifications et sources.
- Agrégation des tendances recruteurs (Indeed Hiring Lab, SHRM, presse) servies via `/api/v1/market-intel`.
- Intégration LLM via interfaces Ollama/OpenRouter + heuristiques fallback.
- Nouveaux tests JUnit/MockMvc pour score, suggestion, compensation et market intel.
- Documentation refondue (README, extension README) + conformité & déploiement low-cost.

## [0.1.0] - 2024-05-27
### Added
- Initial release with Chrome extension scaffold, Spring Boot backend, Docker compose, and CI workflow.
