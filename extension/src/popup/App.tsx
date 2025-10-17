import { Fragment, useEffect, useState } from 'react'
import { Tab } from '@headlessui/react'
import { ClipboardIcon, ArrowPathIcon } from '@heroicons/react/24/outline'
import type { AnalyzerResult, ProfilePayload } from '../types'

const classNames = (...classes: string[]): string => classes.filter(Boolean).join(' ')

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
  } catch (error) {
    console.error('linkedin-optimizer: copy failed', error)
  }
}

type ViewState = 'idle' | 'loading' | 'loaded' | 'error'

const App = () => {
  const [state, setState] = useState<ViewState>('idle')
  const [result, setResult] = useState<AnalyzerResult>({})
  const [profile, setProfile] = useState<ProfilePayload | null>(null)

  useEffect(() => {
    const listener = (message: { type: string; payload: ProfilePayload }) => {
      if (message.type === 'PROFILE_UPDATED') {
        setProfile(message.payload)
      }
    }

    chrome.runtime.onMessage.addListener(listener)
    chrome.runtime.sendMessage({ type: 'PROFILE_STATUS_REQUEST' }, (response) => {
      if (chrome.runtime.lastError) {
        return
      }
      if (response?.profile) {
        setProfile(response.profile)
      }
    })

    return () => {
      chrome.runtime.onMessage.removeListener(listener)
    }
  }, [])

  const triggerAnalysis = () => {
    setState('loading')
    chrome.runtime.sendMessage({ type: 'PROFILE_COLLECTED_REQUEST' }, (response: AnalyzerResult) => {
      if (chrome.runtime.lastError) {
        setState('error')
        setResult({ error: chrome.runtime.lastError.message })
        return
      }
      if (!response) {
        setState('error')
        setResult({ error: 'Aucune réponse reçue.' })
        return
      }
      setResult(response)
      setState(response?.error ? 'error' : 'loaded')
    })
  }

  const renderScore = () => {
    if (state === 'loading') {
      return <p className="animate-pulse text-slate-300">Calcul du score…</p>
    }

    if (!result.score) {
      return <p className="text-slate-300">Cliquez sur « Analyser ce profil » pour obtenir un score.</p>
    }

    return (
      <div className="space-y-4">
        <div>
          <p className="text-4xl font-bold text-emerald-400">{result.score.total}</p>
          <p className="text-sm text-slate-300">Score de visibilité (0-100)</p>
        </div>
        <div>
          <h3 className="text-sm font-semibold text-slate-200">Détails</h3>
          <ul className="mt-2 space-y-1 text-sm text-slate-300">
            {Object.entries(result.score.breakdown).map(([key, value]) => (
              <li key={key} className="flex justify-between">
                <span className="capitalize">{key}</span>
                <span>{value}</span>
              </li>
            ))}
          </ul>
        </div>
        {result.score.warnings.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-amber-400">Avertissements</h3>
            <ul className="mt-1 list-disc space-y-1 pl-4 text-sm text-amber-200">
              {result.score.warnings.map((warning) => (
                <li key={warning}>{warning}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    )
  }

  const renderSuggestions = () => {
    if (state === 'loading') {
      return <p className="animate-pulse text-slate-300">Génération des suggestions…</p>
    }

    if (!result.suggestions) {
      return <p className="text-slate-300">Analysez un profil pour recevoir des suggestions personnalisées.</p>
    }

    return (
      <div className="space-y-4">
        <section>
          <header className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200">Headline</h3>
            <button
              className="inline-flex items-center gap-1 rounded bg-slate-700 px-2 py-1 text-xs text-slate-200 hover:bg-slate-600"
              onClick={() => copyToClipboard(result.suggestions?.headline ?? '')}
            >
              <ClipboardIcon className="h-4 w-4" /> Copier
            </button>
          </header>
          <p className="mt-2 rounded bg-slate-800 p-3 text-sm text-slate-100">
            {result.suggestions.headline}
          </p>
        </section>
        <section>
          <header className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200">À propos</h3>
            <button
              className="inline-flex items-center gap-1 rounded bg-slate-700 px-2 py-1 text-xs text-slate-200 hover:bg-slate-600"
              onClick={() => copyToClipboard(result.suggestions?.about ?? '')}
            >
              <ClipboardIcon className="h-4 w-4" /> Copier
            </button>
          </header>
          <p className="mt-2 whitespace-pre-line rounded bg-slate-800 p-3 text-sm text-slate-100">
            {result.suggestions.about}
          </p>
        </section>
        <section>
          <header className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200">Skills</h3>
            <button
              className="inline-flex items-center gap-1 rounded bg-slate-700 px-2 py-1 text-xs text-slate-200 hover:bg-slate-600"
              onClick={() => copyToClipboard(result.suggestions?.skills.join(', ') ?? '')}
            >
              <ClipboardIcon className="h-4 w-4" /> Copier
            </button>
          </header>
          <ul className="mt-2 flex flex-wrap gap-2">
            {result.suggestions.skills.map((skill) => (
              <li key={skill} className="rounded bg-slate-800 px-2 py-1 text-xs text-slate-100">
                {skill}
              </li>
            ))}
          </ul>
        </section>
        <section>
          <h3 className="text-sm font-semibold text-slate-200">Expériences</h3>
          <div className="mt-2 space-y-3">
            {result.suggestions.experienceBullets.map((exp) => (
              <article key={`${exp.company}-${exp.title}`} className="rounded bg-slate-800 p-3">
                <header className="mb-2">
                  <p className="text-sm font-semibold text-slate-100">{exp.title}</p>
                  <p className="text-xs text-slate-400">{exp.company}</p>
                </header>
                <ul className="list-disc space-y-1 pl-4 text-xs text-slate-200">
                  {exp.bullets.map((bullet) => (
                    <li key={bullet}>{bullet}</li>
                  ))}
                </ul>
              </article>
            ))}
          </div>
        </section>
      </div>
    )
  }

  return (
    <div className="min-h-full bg-slate-900 p-4 text-slate-100">
      <header className="mb-4">
        <h1 className="text-lg font-semibold">LinkedIn Optimizer</h1>
        <p className="text-xs text-slate-300">
          Analyse uniquement les données visibles et respecte les CGU LinkedIn.
        </p>
      </header>
      <div className="mb-4 space-y-2 rounded border border-slate-700 p-3">
        <p className="text-xs text-slate-300">
          {profile?.fullName ? `Profil détecté : ${profile.fullName}` : 'Aucun profil détecté pour le moment.'}
        </p>
        <button
          onClick={triggerAnalysis}
          className="flex w-full items-center justify-center gap-2 rounded bg-emerald-500 px-3 py-2 text-sm font-semibold text-slate-900 hover:bg-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-300"
        >
          <ArrowPathIcon className="h-4 w-4" /> Analyser ce profil
        </button>
        {state === 'error' && result.error && <p className="text-xs text-red-400">{result.error}</p>}
      </div>
      <Tab.Group as={Fragment}>
        <Tab.List className="mb-4 flex space-x-2">
          {['Score', 'Suggestions'].map((tab) => (
            <Tab
              key={tab}
              className={({ selected }) =>
                classNames(
                  'flex-1 rounded px-3 py-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-slate-100',
                  selected ? 'bg-slate-100 text-slate-900' : 'bg-slate-800 text-slate-200'
                )
              }
            >
              {tab}
            </Tab>
          ))}
        </Tab.List>
        <Tab.Panels>
          <Tab.Panel className="rounded border border-slate-700 p-3 text-sm text-slate-100">
            {renderScore()}
          </Tab.Panel>
          <Tab.Panel className="rounded border border-slate-700 p-3 text-sm text-slate-100">
            {renderSuggestions()}
          </Tab.Panel>
        </Tab.Panels>
      </Tab.Group>
    </div>
  )
}

export default App
