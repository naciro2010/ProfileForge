import { CompensationRequest, CompensationResponse } from '../../types';

interface CompensationSectionProps {
  value: CompensationRequest;
  data?: CompensationResponse;
  loading: boolean;
  onChange: (value: CompensationRequest) => void;
  onEstimate: () => void;
}

export function CompensationSection({ value, data, loading, onChange, onEstimate }: CompensationSectionProps): JSX.Element {
  const updateField = <Key extends keyof CompensationRequest>(key: Key, fieldValue: CompensationRequest[Key]) => {
    onChange({ ...value, [key]: fieldValue });
  };

  return (
    <section className="space-y-4" aria-labelledby="compensation-title">
      <header className="space-y-1">
        <h2 id="compensation-title" className="text-lg font-semibold">
          Salaire & TJM estimés
        </h2>
        <p className="text-sm text-slate-400">Fourchettes issues de sources publiques (BLS, ONS, APEC, Eurostat, baromètres TJM).</p>
      </header>

      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4 space-y-3">
        <div className="grid gap-3 md:grid-cols-2">
          <label className="space-y-1 text-xs text-slate-400">
            Pays
            <input
              type="text"
              value={value.country}
              onChange={(event) => updateField('country', event.target.value)}
              placeholder="FR, US…"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </label>
          <label className="space-y-1 text-xs text-slate-400">
            Région (optionnel)
            <input
              type="text"
              value={value.region ?? ''}
              onChange={(event) => updateField('region', event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              placeholder="Île-de-France, California…"
            />
          </label>
        </div>
        <label className="space-y-1 text-xs text-slate-400">
          Rôle ou intitulé normalisé
          <input
            type="text"
            value={value.role}
            onChange={(event) => updateField('role', event.target.value)}
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </label>
        <div className="grid gap-3 md:grid-cols-2">
          <label className="space-y-1 text-xs text-slate-400">
            Séniorité
            <select
              value={value.seniority ?? 'mid'}
              onChange={(event) => updateField('seniority', event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              <option value="junior">Junior</option>
              <option value="mid">Intermédiaire</option>
              <option value="senior">Senior</option>
              <option value="lead">Lead / Principal</option>
            </select>
          </label>
          <label className="space-y-1 text-xs text-slate-400">
            Type d&apos;entreprise
            <select
              value={value.companyType ?? 'sme'}
              onChange={(event) => updateField('companyType', event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              <option value="startup">Startup</option>
              <option value="sme">PME</option>
              <option value="scaleup">ETI</option>
              <option value="enterprise">Grand groupe</option>
            </select>
          </label>
        </div>
        <label className="space-y-1 text-xs text-slate-400">
          Type de contrat
          <select
            value={value.contractType ?? 'permanent'}
            onChange={(event) => updateField('contractType', event.target.value as 'permanent' | 'freelance')}
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          >
            <option value="permanent">CDI / Permanent</option>
            <option value="freelance">Freelance / Indépendant</option>
          </select>
        </label>
        <label className="space-y-1 text-xs text-slate-400">
          Devise (optionnel)
          <input
            type="text"
            value={value.currency ?? ''}
            onChange={(event) => updateField('currency', event.target.value)}
            placeholder="EUR, USD…"
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </label>
        <button
          type="button"
          onClick={onEstimate}
          className="inline-flex items-center justify-center rounded-full bg-blue-500 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-blue-500/30 transition hover:bg-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:ring-offset-2 focus:ring-offset-slate-950"
          disabled={loading}
        >
          {loading ? 'Calcul…' : 'Estimer'}
        </button>
      </div>

      {data && (
        <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4" aria-label="Fourchettes">
          <header className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200">Fourchette {data.period === 'daily' ? 'TJM' : 'salaire annuel'}</h3>
            <span className="text-xs text-slate-400">Devise : {data.currency}</span>
          </header>
          <dl className="mt-3 grid grid-cols-3 gap-3 text-center text-sm text-slate-100">
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-400">P25</dt>
              <dd className="text-lg font-semibold">{formatAmount(data.low, data.currency)}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-400">Médian</dt>
              <dd className="text-lg font-semibold">{formatAmount(data.median, data.currency)}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-400">P75</dt>
              <dd className="text-lg font-semibold">{formatAmount(data.high, data.currency)}</dd>
            </div>
          </dl>
          <ul className="mt-3 space-y-1 text-xs text-slate-300">
            {data.justifications.map((reason) => (
              <li key={reason}>• {reason}</li>
            ))}
          </ul>
          <footer className="mt-3">
            <h4 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Sources</h4>
            <ul className="mt-2 space-y-1 text-xs text-blue-300">
              {data.sources.map((source) => (
                <li key={source.url}>
                  <a href={source.url} target="_blank" rel="noreferrer" className="hover:underline">
                    {source.name}
                  </a>
                </li>
              ))}
            </ul>
          </footer>
        </section>
      )}
    </section>
  );
}

function formatAmount(value: number, currency: string): string {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0
  }).format(value);
}
