import { defineManifest } from '@crxjs/vite-plugin'

export default defineManifest({
  manifest_version: 3,
  name: 'LinkedIn Optimizer (Side Panel)',
  description:
    'Suggestions contextuelles conformes pour votre profil LinkedIn : side panel, zéro automatisation, citations marché et salaire.',
  version: '2.0.0',
  action: {
    default_title: 'Ouvrir LinkedIn Optimizer'
  },
  permissions: ['sidePanel', 'activeTab', 'storage', 'scripting'],
  host_permissions: [],
  background: {
    service_worker: 'src/background.ts',
    type: 'module'
  },
  side_panel: {
    default_path: 'sidepanel/index.html'
  },
  options_ui: {
    page: 'options.html',
    open_in_tab: true
  }
})
