import { MarketIntelRequest, MarketIntelResponse } from '../../types';

interface MarketSectionProps {
  value: MarketIntelRequest;
  intel?: MarketIntelResponse;
  loading: boolean;
  onChange: (value: MarketIntelRequest) => void;
  onRefresh: () => void;
}

export function MarketSection({ value, intel, loading, onChange, onRefresh }: MarketSectionProps): JSX.Element {
  const updateField = <Key extends keyof MarketIntelRequest>(key: Key, fieldValue: MarketIntelRequest[Key]) => {
    onChange({ ...value, [key]: fieldValue });
  };

  return (
    <section className="space-y-4" aria-labelledby="market-title">
      <header className="space-y-1">
        <h2 id="market-title" className="text-lg font-semibold">
          Tendances recruteurs
        </h2>
        <p className="text-sm text-slate-400">
          Sources publiques agrégées (Indeed Hiring Lab, SHRM, presse). Nous citons systématiquement la provenance.
        </p>
      </header>

      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4 space-y-3">
        <div className="grid gap-3 md:grid-cols-2">
          <label className="space-y-1 text-xs text-slate-400">
            Rôle
            <input
              type="text"
              value={value.role}
              onChange={(event) => updateField('role', event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </label>
          <label className="space-y-1 text-xs text-slate-400">
            Pays
            <input
              type="text"
              value={value.country}
              onChange={(event) => updateField('country', event.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              placeholder="FR, US, UK…"
            />
          </label>
        </div>
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
          Secteur (optionnel)
          <input
            type="text"
            value={value.industry ?? ''}
            onChange={(event) => updateField('industry', event.target.value)}
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            placeholder="Data, SaaS, Industrie…"
          />
        </label>
        <button
          type="button"
          onClick={onRefresh}
          className="inline-flex items-center justify-center rounded-full bg-blue-500 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-blue-500/30 transition hover:bg-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:ring-offset-2 focus:ring-offset-slate-950"
          disabled={loading}
        >
          {loading ? 'Rafraîchissement…' : 'Afficher les attentes'}
        </button>
      </div>

      {intel && (
        <div className="space-y-4">
          <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4" aria-label="Compétences clés">
            <header className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-slate-200">Top compétences</h3>
              <span className="text-xs text-slate-400">Actualisé il y a {computeRecencyLabel(intel.refreshedAt)}</span>
            </header>
            <div className="mt-3 grid gap-3 md:grid-cols-3">
              {intel.skills.map((skill) => (
                <article key={`${skill.name}-${skill.category}`} className="rounded-xl border border-slate-800 bg-slate-950/60 p-3">
                  <h4 className="text-sm font-semibold text-slate-200">{skill.name}</h4>
                  <p className="text-xs uppercase tracking-wide text-slate-400">
                    {skill.category === 'core' && 'Indispensable'}
                    {skill.category === 'trending' && 'En hausse'}
                    {skill.category === 'soft' && 'Soft skill'}
                  </p>
                  <p className="mt-2 text-xs text-slate-400">Source : {skill.source}</p>
                </article>
              ))}
            </div>
          </section>

          <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4" aria-label="Signaux recruteurs">
            <h3 className="text-sm font-semibold text-slate-200">Signaux de qualité</h3>
            <ul className="mt-2 space-y-2 text-sm text-slate-200">
              {intel.recruiterSignals.map((signal) => (
                <li key={signal.statement} className="rounded-lg border border-slate-800 bg-slate-950/60 p-3">
                  <p className="font-semibold">{signal.statement}</p>
                  <p className="text-xs text-slate-400">{signal.rationale}</p>
                </li>
              ))}
            </ul>
          </section>

          <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4" aria-label="Sources">
            <h3 className="text-sm font-semibold text-slate-200">Sources</h3>
            <ul className="mt-2 space-y-1 text-xs text-blue-300">
              {intel.sources.map((source) => (
                <li key={source.url}>
                  <a href={source.url} target="_blank" rel="noreferrer" className="hover:underline">
                    {source.name}
                  </a>
                </li>
              ))}
            </ul>
          </section>
        </div>
      )}
    </section>
  );
}

function computeRecencyLabel(isoDate: string): string {
  const refreshed = new Date(isoDate);
  const now = new Date();
  const diff = Math.abs(now.getTime() - refreshed.getTime());
  const days = Math.round(diff / (1000 * 60 * 60 * 24));
  if (days <= 0) return "moins d'un jour";
  if (days === 1) return '1 jour';
  return `${days} jours`;
}
