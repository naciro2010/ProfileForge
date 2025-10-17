import { FormEvent, useEffect, useState } from 'react';
import '../index.css';
import { loadSettings, saveSettings } from '../service/settings';

type Status = 'idle' | 'saving' | 'saved' | 'error';

export function OptionsApp(): JSX.Element {
  const [backendUrl, setBackendUrl] = useState('');
  const [apiKey, setApiKey] = useState('');
  const [language, setLanguage] = useState<'fr' | 'en'>('fr');
  const [status, setStatus] = useState<Status>('idle');

  useEffect(() => {
    loadSettings().then((settings) => {
      setBackendUrl(settings.backendUrl);
      setApiKey(settings.apiKey);
      setLanguage(settings.language);
    });
  }, []);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setStatus('saving');
    try {
      await saveSettings({ backendUrl, apiKey, language });
      setStatus('saved');
      setTimeout(() => setStatus('idle'), 3000);
    } catch (error) {
      console.error(error);
      setStatus('error');
    }
  };

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100 p-8">
      <div className="mx-auto max-w-xl rounded-2xl bg-slate-900 shadow-lg shadow-slate-900/30 p-8 space-y-6">
        <header className="space-y-2">
          <h1 className="text-2xl font-semibold">LinkedIn Optimizer — Options</h1>
          <p className="text-sm text-slate-300">
            Configurez l&apos;URL du backend, la clé API et la langue par défaut des suggestions.
          </p>
        </header>
        <form className="space-y-6" onSubmit={handleSubmit}>
          <label className="block space-y-2">
            <span className="text-sm font-medium">Backend URL</span>
            <input
              type="url"
              required
              value={backendUrl}
              onChange={(event) => setBackendUrl(event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              placeholder="https://optimizer.example.com"
            />
          </label>
          <label className="block space-y-2">
            <span className="text-sm font-medium">API Key</span>
            <input
              type="text"
              required
              value={apiKey}
              onChange={(event) => setApiKey(event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              placeholder="********"
            />
            <span className="text-xs text-slate-400">Elle doit correspondre à la configuration du backend.</span>
          </label>
          <label className="block space-y-2">
            <span className="text-sm font-medium">Langue des suggestions</span>
            <select
              value={language}
              onChange={(event) => setLanguage(event.target.value as 'fr' | 'en')}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              <option value="fr">Français</option>
              <option value="en">English</option>
            </select>
          </label>
          <button
            type="submit"
            className="inline-flex items-center justify-center rounded-full bg-blue-500 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-blue-500/30 transition hover:bg-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:ring-offset-2 focus:ring-offset-slate-900"
            disabled={status === 'saving'}
          >
            {status === 'saving' ? 'Enregistrement…' : 'Enregistrer'}
          </button>
          <p className="text-xs text-slate-400">
            Les paramètres sont stockés dans Chrome Sync et chiffrés par votre compte Google.
          </p>
        </form>
        {status === 'saved' && <p className="text-sm text-emerald-400">Paramètres mis à jour ✅</p>}
        {status === 'error' && <p className="text-sm text-red-400">Erreur lors de l&apos;enregistrement.</p>}
      </div>
    </main>
  );
}
