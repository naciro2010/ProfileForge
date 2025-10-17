import { defineManifest } from '@crxjs/vite-plugin'

export default defineManifest({
  manifest_version: 3,
  name: 'LinkedIn Optimizer',
  description:
    'Analysez les profils LinkedIn visibles et recevez un score de visibilité et des suggestions personnalisées.',
  version: '0.1.0',
  action: {
    default_popup: 'popup.html',
    default_title: 'LinkedIn Optimizer'
  },
  options_ui: {
    page: 'options.html',
    open_in_tab: true
  },
  permissions: ['activeTab', 'storage', 'scripting'],
  host_permissions: ['https://www.linkedin.com/*'],
  background: {
    service_worker: 'src/background.ts',
    type: 'module'
  },
  content_scripts: [
    {
      matches: ['https://www.linkedin.com/*'],
      js: ['src/content/collect.ts'],
      run_at: 'document_idle'
    }
  ]
})
