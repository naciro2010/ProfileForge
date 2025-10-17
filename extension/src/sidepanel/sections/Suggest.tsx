import { copyToClipboard } from '../../lib/clipboard';
import { scrollToSection } from '../../lib/scroll';
import { CopyBlock, ExperienceSuggestion, SuggestionResponse } from '../../types';

interface SuggestSectionProps {
  suggestions?: SuggestionResponse;
  loading: boolean;
  onGenerate: () => void;
}

export function SuggestSection({ suggestions, loading, onGenerate }: SuggestSectionProps): JSX.Element {
  const handleCopy = async (content: string) => {
    await copyToClipboard(content);
  };

  const renderCopyBlocks = (items: CopyBlock[], section: 'headline' | 'about') => (
    <ul className="space-y-3">
      {items.map((block) => (
        <li key={block.label} className="rounded-xl border border-slate-800 bg-slate-950/60 p-3">
          <header className="flex items-center justify-between gap-2">
            <h3 className="text-sm font-semibold text-slate-200">{block.label}</h3>
            <div className="flex items-center gap-2">
              <button
                type="button"
                className="rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-200 hover:border-blue-400 hover:text-blue-200"
                onClick={() => handleCopy(block.content)}
              >
                Copier
              </button>
              <button
                type="button"
                className="rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-200 hover:border-blue-400 hover:text-blue-200"
                onClick={() => scrollToSection(section)}
              >
                Cibler
              </button>
            </div>
          </header>
          <p className="mt-2 whitespace-pre-line text-sm leading-relaxed text-slate-200">{block.content}</p>
        </li>
      ))}
    </ul>
  );

  const renderExperiences = (items: ExperienceSuggestion[]) => (
    <ul className="space-y-3">
      {items.map((experience) => (
        <li key={`${experience.role}-${experience.company}`} className="rounded-xl border border-slate-800 bg-slate-950/60 p-3">
          <header className="flex items-center justify-between">
            <div>
              <h4 className="text-sm font-semibold text-slate-200">{experience.role}</h4>
              <p className="text-xs text-slate-400">{experience.company}</p>
            </div>
            <button
              type="button"
              className="rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-200 hover:border-blue-400 hover:text-blue-200"
              onClick={() => scrollToSection('experience')}
            >
              Cibler
            </button>
          </header>
          <ul className="mt-2 space-y-1 text-sm text-slate-200">
            {experience.bullets.map((bullet, index) => (
              <li key={index} className="flex gap-2">
                <span aria-hidden>•</span>
                <span>{bullet}</span>
              </li>
            ))}
          </ul>
          <div className="mt-3 flex justify-end">
            <button
              type="button"
              className="rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-200 hover:border-blue-400 hover:text-blue-200"
              onClick={() => handleCopy(experience.bullets.join('\n'))}
            >
              Copier les bullets
            </button>
          </div>
        </li>
      ))}
    </ul>
  );

  return (
    <section className="space-y-4" aria-labelledby="suggestions-title">
      <header className="space-y-1">
        <h2 id="suggestions-title" className="text-lg font-semibold">
          Suggestions prêtes à coller
        </h2>
        <p className="text-sm text-slate-400">
          Les variantes sont générées localement puis enrichies par le backend. Copiez ce qui vous convient et collez dans LinkedIn.
        </p>
      </header>
      <button
        type="button"
        onClick={onGenerate}
        className="inline-flex items-center justify-center rounded-full bg-blue-500 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-blue-500/30 transition hover:bg-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:ring-offset-2 focus:ring-offset-slate-950"
        disabled={loading}
      >
        {loading ? 'Génération en cours…' : 'Générer des suggestions'}
      </button>

      {!suggestions && <p className="text-sm text-slate-400">Aucune suggestion pour le moment. Analysez d&apos;abord votre profil.</p>}

      {suggestions && (
        <div className="space-y-6">
          <section aria-label="Headline suggestions" className="space-y-3">
            <h3 className="text-sm font-semibold text-slate-200">Headline</h3>
            {renderCopyBlocks(suggestions.headline, 'headline')}
          </section>

          <section aria-label="About suggestions" className="space-y-3">
            <h3 className="text-sm font-semibold text-slate-200">À propos</h3>
            {renderCopyBlocks(suggestions.about, 'about')}
          </section>

          <section aria-label="Skills suggestions" className="space-y-3">
            <h3 className="text-sm font-semibold text-slate-200">Compétences recommandées</h3>
            <div className="grid gap-3 md:grid-cols-3">
              {(['core', 'trending', 'niceToHave'] as const).map((category) => (
                <article key={category} className="rounded-xl border border-slate-800 bg-slate-950/60 p-3">
                  <header className="flex items-center justify-between">
                    <h4 className="text-xs font-semibold uppercase tracking-wide text-slate-400">
                      {category === 'core' && 'Indispensables'}
                      {category === 'trending' && 'En hausse'}
                      {category === 'niceToHave' && 'Bonus'}
                    </h4>
                    <button
                      type="button"
                      className="rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-200 hover:border-blue-400 hover:text-blue-200"
                      onClick={() => handleCopy(suggestions.skills[category].join(', '))}
                    >
                      Copier
                    </button>
                  </header>
                  <ul className="mt-2 space-y-1 text-sm text-slate-200">
                    {suggestions.skills[category].map((skill) => (
                      <li key={skill}>{skill}</li>
                    ))}
                  </ul>
                  <div className="mt-3 flex justify-end">
                    <button
                      type="button"
                      className="rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-200 hover:border-blue-400 hover:text-blue-200"
                      onClick={() => scrollToSection('skills')}
                    >
                      Cibler
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section aria-label="Experience suggestions" className="space-y-3">
            <h3 className="text-sm font-semibold text-slate-200">Bullets d&apos;expérience</h3>
            {renderExperiences(suggestions.experiences)}
          </section>
        </div>
      )}
    </section>
  );
}
