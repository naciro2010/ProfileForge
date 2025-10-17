import { Fragment } from 'react';
import { ExperienceInput, ProfileSnapshot, ScoreResponse } from '../../types';
import { sanitizeSkills } from '../../lib/validators';

interface ScoreSectionProps {
  profile: ProfileSnapshot;
  score?: ScoreResponse;
  issues: string[];
  loading: boolean;
  onProfileChange: (profile: ProfileSnapshot) => void;
  onAnalyse: () => void;
}

export function ScoreSection({ profile, score, issues, loading, onProfileChange, onAnalyse }: ScoreSectionProps): JSX.Element {
  const updateField = <Key extends keyof ProfileSnapshot>(key: Key, value: ProfileSnapshot[Key]) => {
    onProfileChange({ ...profile, [key]: value });
  };

  const updateExperience = (experience: ExperienceInput) => {
    const experiences = profile.experiences.map((item) => (item.id === experience.id ? experience : item));
    updateField('experiences', experiences);
  };

  const removeExperience = (experienceId: string) => {
    const experiences = profile.experiences.filter((item) => item.id !== experienceId);
    updateField('experiences', experiences);
  };

  const addExperience = () => {
    const newExperience: ExperienceInput = {
      id: crypto.randomUUID(),
      role: '',
      company: '',
      timeframe: '',
      achievements: ''
    };
    updateField('experiences', [...profile.experiences, newExperience]);
  };

  const handleSkillsChange = (value: string) => {
    updateField('skills', sanitizeSkills(value));
  };

  return (
    <section className="space-y-4" aria-labelledby="score-title">
      <header className="space-y-1">
        <h2 id="score-title" className="text-lg font-semibold">
          Score de visibilité
        </h2>
        <p className="text-sm text-slate-400">
          Collez vos sections et analysez votre profil sans modifier LinkedIn. Nous ne stockons rien côté serveur.
        </p>
      </header>

      <div className="space-y-4 rounded-2xl border border-slate-800 bg-slate-900/60 p-4">
        <fieldset className="grid gap-4" aria-describedby="profile-headline-description">
          <legend className="text-sm font-semibold text-slate-200">Informations clés</legend>
          <label className="space-y-2">
            <span className="text-xs uppercase tracking-wide text-slate-400">Headline (≤ 220 caractères)</span>
            <input
              type="text"
              value={profile.headline}
              onChange={(event) => updateField('headline', event.target.value)}
              maxLength={260}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </label>
          <label className="space-y-2">
            <span className="text-xs uppercase tracking-wide text-slate-400">À propos</span>
            <textarea
              value={profile.about}
              onChange={(event) => updateField('about', event.target.value)}
              rows={5}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </label>
          <div className="grid gap-2">
            <label className="space-y-2">
              <span className="text-xs uppercase tracking-wide text-slate-400">Compétences (séparées par virgules ou retours à la ligne)</span>
              <textarea
                value={profile.skills.join('\n')}
                onChange={(event) => handleSkillsChange(event.target.value)}
                rows={3}
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </label>
            <span className="text-xs text-slate-500">Nous conservons les 20 premières compétences pour la mise en cache locale.</span>
          </div>
          <div className="grid gap-2 md:grid-cols-2">
            <label className="space-y-2">
              <span className="text-xs uppercase tracking-wide text-slate-400">Localisation</span>
              <input
                type="text"
                value={profile.location ?? ''}
                onChange={(event) => updateField('location', event.target.value)}
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </label>
            <label className="space-y-2">
              <span className="text-xs uppercase tracking-wide text-slate-400">Rôle ciblé</span>
              <input
                type="text"
                value={profile.targetRole ?? ''}
                onChange={(event) => updateField('targetRole', event.target.value)}
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </label>
          </div>
        </fieldset>

        <fieldset className="space-y-4">
          <legend className="text-sm font-semibold text-slate-200">Expériences</legend>
          {profile.experiences.map((experience, index) => (
            <article key={experience.id} className="space-y-3 rounded-xl border border-slate-800 bg-slate-950/60 p-3">
              <header className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-slate-200">Expérience {index + 1}</h3>
                <button
                  type="button"
                  className="text-xs text-red-300 hover:text-red-200"
                  onClick={() => removeExperience(experience.id)}
                >
                  Supprimer
                </button>
              </header>
              <div className="grid gap-2 md:grid-cols-2">
                <label className="space-y-1 text-xs text-slate-400">
                  Titre
                  <input
                    type="text"
                    value={experience.role}
                    onChange={(event) => updateExperience({ ...experience, role: event.target.value })}
                    className="w-full rounded-lg border border-slate-800 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  />
                </label>
                <label className="space-y-1 text-xs text-slate-400">
                  Entreprise
                  <input
                    type="text"
                    value={experience.company}
                    onChange={(event) => updateExperience({ ...experience, company: event.target.value })}
                    className="w-full rounded-lg border border-slate-800 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  />
                </label>
              </div>
              <label className="space-y-1 text-xs text-slate-400">
                Période
                <input
                  type="text"
                  value={experience.timeframe ?? ''}
                  onChange={(event) => updateExperience({ ...experience, timeframe: event.target.value })}
                  className="w-full rounded-lg border border-slate-800 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                />
              </label>
              <label className="space-y-1 text-xs text-slate-400">
                Réalisations (coller vos bullets — chiffres encouragés)
                <textarea
                  value={experience.achievements}
                  onChange={(event) => updateExperience({ ...experience, achievements: event.target.value })}
                  rows={3}
                  className="w-full rounded-lg border border-slate-800 bg-slate-950 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                />
              </label>
            </article>
          ))}
          <button
            type="button"
            onClick={addExperience}
            className="inline-flex items-center justify-center rounded-full border border-slate-700 px-3 py-1 text-xs font-semibold text-slate-200 transition hover:border-blue-400 hover:text-blue-300"
          >
            + Ajouter une expérience
          </button>
        </fieldset>
      </div>

      <footer className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={onAnalyse}
          className="inline-flex items-center justify-center rounded-full bg-blue-500 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-blue-500/30 transition hover:bg-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:ring-offset-2 focus:ring-offset-slate-950"
          disabled={loading}
        >
          {loading ? 'Analyse en cours…' : 'Analyser ce profil'}
        </button>
        {issues.length > 0 && (
          <ul className="text-xs text-amber-300" aria-live="polite">
            {issues.map((issue) => (
              <li key={issue}>⚠️ {issue}</li>
            ))}
          </ul>
        )}
      </footer>

      {score && (
        <section aria-labelledby="score-breakdown" className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4">
          <header className="flex items-center justify-between">
            <div>
              <h3 id="score-breakdown" className="text-sm font-semibold text-slate-200">
                Score global : {score.total}/100
              </h3>
              <p className="text-xs text-slate-400">Basé sur la complétude du profil et la clarté des sections.</p>
            </div>
          </header>
          <dl className="mt-3 grid grid-cols-2 gap-3 text-xs text-slate-300">
            {Object.entries(score.breakdown).map(([key, value]) => (
              <Fragment key={key}>
                <dt className="capitalize text-slate-400">{key}</dt>
                <dd className="text-right font-semibold text-slate-100">{value}</dd>
              </Fragment>
            ))}
          </dl>
          {score.warnings.length > 0 && (
            <ul className="mt-3 space-y-1 text-xs text-amber-300">
              {score.warnings.map((warning) => (
                <li key={warning}>• {warning}</li>
              ))}
            </ul>
          )}
        </section>
      )}
    </section>
  );
}
