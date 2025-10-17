# LinkedIn Optimizer – Extension Chrome (Side Panel)

Extension Manifest V3 conforme qui ouvre un **Chrome Side Panel** synchronisé avec LinkedIn. Aucune modification du DOM :

- l’utilisateur colle ses sections (headline, À propos, compétences, expériences) directement dans le panel ;
- les suggestions viennent du backend via `X-API-Key` ;
- les boutons **Copier** et **Cibler** déclenchent un scroll doux vers la section LinkedIn correspondante.

## Démarrage

```bash
npm install
npm run dev   # ouvre le side panel en mode Vite + HMR
npm run build # génère dist/ prêt à charger
```

Chargez ensuite `dist/` dans `chrome://extensions` (mode développeur). Pensez à configurer le backend et la clé API dans la page d’options.

## Permissions minimales

- `sidePanel` pour afficher le panneau natif,
- `activeTab` pour cibler l’onglet courant,
- `storage` (Sync + Session) pour mémoriser les préférences,
- `scripting` uniquement pour déclencher un `scrollIntoView` (aucune injection HTML).

## Structure

```
src/
├─ sidepanel/           # App React + Tailwind (tabs Score, Suggestions, Marché & Salaire)
│   ├─ App.tsx
│   ├─ sections/
│   │   ├─ Score.tsx
│   │   ├─ Suggest.tsx
│   │   ├─ Market.tsx
│   │   └─ Compensation.tsx
├─ options/             # Page d’options (backend + API key)
├─ lib/                 # utilitaires (clipboard, scroll, validators)
├─ service/             # accès backend + stockage Chrome
└─ types/               # modèles partagés
```

## Accessibilité & i18n

- navigation 100 % clavier, focus visibles, contrastes AA,
- texte ARIA sur les listes et actions,
- langue FR/EN configurable (propagée au backend).

## Conformité LinkedIn

- Side Panel natif : aucune altération de linkedin.com,
- zéro automatisation (l’utilisateur copie/colle manuellement),
- scroll assisté seulement sur demande (bouton “Cibler”).
