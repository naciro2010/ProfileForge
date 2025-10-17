import { useEffect, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { z } from 'zod'
import type { BackendConfig } from '../types'

const schema = z.object({
  backendUrl: z.string().url({ message: "URL invalide" }),
  apiKey: z.string().min(1, "API Key requise"),
  provider: z.enum(['ollama', 'openrouter']),
  model: z.string().min(1, 'Modèle requis'),
  language: z.enum(['fr', 'en'])
})

const DEFAULTS: BackendConfig = {
  backendUrl: 'http://localhost:8080',
  apiKey: '',
  provider: 'ollama',
  model: 'llama3:instruct',
  language: 'fr'
}

const App = () => {
  const [form, setForm] = useState<BackendConfig>(DEFAULTS)
  const [status, setStatus] = useState<'idle' | 'saved' | 'error'>('idle')
  const [error, setError] = useState<string>('')

  useEffect(() => {
    chrome.storage.sync.get(Object.keys(DEFAULTS), (stored) => {
      setForm({
        backendUrl: stored.backendUrl ?? DEFAULTS.backendUrl,
        apiKey: stored.apiKey ?? DEFAULTS.apiKey,
        provider: stored.provider ?? DEFAULTS.provider,
        model: stored.model ?? DEFAULTS.model,
        language: stored.language ?? DEFAULTS.language
      })
    })
  }, [])

  const handleChange = (key: keyof BackendConfig) => (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const value = event.target.value as BackendConfig[keyof BackendConfig]
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    setStatus('idle')
    setError('')

    const validation = schema.safeParse(form)
    if (!validation.success) {
      setStatus('error')
      setError(validation.error.issues.map((issue) => issue.message).join('\n'))
      return
    }

    chrome.storage.sync.set(validation.data, () => {
      if (chrome.runtime.lastError) {
        setStatus('error')
        setError(chrome.runtime.lastError.message ?? 'Erreur inconnue')
        return
      }
      setStatus('saved')
    })
  }

  return (
    <div className="min-h-screen bg-slate-900 p-6 text-slate-100">
      <header className="mb-6">
        <h1 className="text-2xl font-semibold">LinkedIn Optimizer – Options</h1>
        <p className="text-sm text-slate-300">
          Configurez votre backend et le fournisseur LLM utilisé pour les suggestions.
        </p>
      </header>
      <form
        onSubmit={handleSubmit}
        className="mx-auto max-w-xl space-y-5 rounded border border-slate-700 bg-slate-800 p-6"
      >
        <div>
          <label className="mb-1 block text-sm font-medium" htmlFor="backendUrl">
            Backend URL
          </label>
          <input
            id="backendUrl"
            value={form.backendUrl}
            onChange={handleChange('backendUrl')}
            type="url"
            required
            className="w-full rounded border border-slate-600 bg-slate-900 p-2 text-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium" htmlFor="apiKey">
            API Key
          </label>
          <input
            id="apiKey"
            value={form.apiKey}
            onChange={handleChange('apiKey')}
            type="text"
            required
            className="w-full rounded border border-slate-600 bg-slate-900 p-2 text-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
          />
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <label className="mb-1 block text-sm font-medium" htmlFor="provider">
              Fournisseur LLM
            </label>
            <select
              id="provider"
              value={form.provider}
              onChange={handleChange('provider')}
              className="w-full rounded border border-slate-600 bg-slate-900 p-2 text-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
            >
              <option value="ollama">Ollama (local)</option>
              <option value="openrouter">OpenRouter</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium" htmlFor="model">
              Modèle
            </label>
            <input
              id="model"
              value={form.model}
              onChange={handleChange('model')}
              type="text"
              required
              className="w-full rounded border border-slate-600 bg-slate-900 p-2 text-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
            />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium" htmlFor="language">
            Langue
          </label>
          <select
            id="language"
            value={form.language}
            onChange={handleChange('language')}
            className="w-full rounded border border-slate-600 bg-slate-900 p-2 text-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
          >
            <option value="fr">Français</option>
            <option value="en">Anglais</option>
          </select>
        </div>
        <div className="flex items-center gap-2 text-sm">
          <button
            type="submit"
            className="rounded bg-emerald-500 px-4 py-2 font-semibold text-slate-900 hover:bg-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
          >
            Enregistrer
          </button>
          {status === 'saved' && <span className="text-emerald-400">Configuration sauvegardée.</span>}
          {status === 'error' && <span className="text-red-400">{error}</span>}
        </div>
      </form>
    </div>
  )
}

export default App
