import type { AnalyzerResult, BackendConfig, ProfilePayload } from './types'

const DEFAULT_CONFIG: BackendConfig = {
  backendUrl: 'http://localhost:8080',
  apiKey: '',
  provider: 'ollama',
  model: 'llama3:instruct',
  language: 'fr'
}

let latestProfile: ProfilePayload | null = null

const getConfig = async (): Promise<BackendConfig> => {
  const stored = await chrome.storage.sync.get([
    'backendUrl',
    'apiKey',
    'provider',
    'model',
    'language'
  ])
  return {
    backendUrl: stored.backendUrl ?? DEFAULT_CONFIG.backendUrl,
    apiKey: stored.apiKey ?? DEFAULT_CONFIG.apiKey,
    provider: (stored.provider as BackendConfig['provider']) ?? DEFAULT_CONFIG.provider,
    model: stored.model ?? DEFAULT_CONFIG.model,
    language: (stored.language as BackendConfig['language']) ?? DEFAULT_CONFIG.language
  }
}

const callBackend = async (
  endpoint: string,
  payload: unknown,
  config: BackendConfig
): Promise<Response> => {
  const url = `${config.backendUrl}${endpoint}`
  return fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': config.apiKey
    },
    body: JSON.stringify(payload)
  })
}

const ensureProfile = async (): Promise<ProfilePayload> => {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true })
  if (!tab?.id) {
    throw new Error('Onglet actif introuvable')
  }

  if (latestProfile && latestProfile.url === tab.url) {
    return latestProfile
  }

  try {
    const response = await chrome.tabs.sendMessage(tab.id, { type: 'COLLECT_PROFILE' })
    if (!response?.profile) {
      throw new Error('Profil non disponible')
    }
    latestProfile = response.profile as ProfilePayload
    return latestProfile
  } catch (error) {
    throw new Error('Impossible de lire le profil LinkedIn visible.')
  }
}

const handleProfileAnalysis = async (): Promise<AnalyzerResult> => {
  const config = await getConfig()
  if (!config.backendUrl || !config.apiKey) {
    return { error: 'Configurez l’URL du backend et la clé API dans les options.' }
  }

  try {
    const profile = await ensureProfile()
    const [suggestResponse, scoreResponse] = await Promise.all([
      callBackend(
        '/api/v1/suggest',
        { profile, language: config.language, provider: config.provider, model: config.model },
        config
      ),
      callBackend('/api/v1/score', profile, config)
    ])

    if (!suggestResponse.ok) {
      const error = await suggestResponse.text()
      return { error: `Erreur suggestions: ${error}` }
    }

    if (!scoreResponse.ok) {
      const error = await scoreResponse.text()
      return { error: `Erreur score: ${error}` }
    }

    const suggestions = await suggestResponse.json()
    const score = await scoreResponse.json()
    return { suggestions, score }
  } catch (error) {
    console.error('linkedin-optimizer: backend error', error)
    return { error: error instanceof Error ? error.message : 'Impossible de joindre le backend.' }
  }
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message?.type === 'PROFILE_COLLECTED') {
    latestProfile = message.payload as ProfilePayload
    chrome.runtime.sendMessage({ type: 'PROFILE_UPDATED', payload: latestProfile })
  }

  if (message?.type === 'PROFILE_STATUS_REQUEST') {
    sendResponse({ profile: latestProfile })
    return undefined
  }

  if (message?.type === 'PROFILE_COLLECTED_REQUEST') {
    handleProfileAnalysis()
      .then((result) => sendResponse(result))
      .catch((error) => {
        console.error('linkedin-optimizer: unexpected error', error)
        sendResponse({ error: 'Erreur inattendue.' })
      })
    return true
  }
  return undefined
})
