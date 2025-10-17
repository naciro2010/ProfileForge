import { useEffect, useMemo, useState } from 'react';
import { ScoreSection } from './sections/Score';
import { SuggestSection } from './sections/Suggest';
import { MarketSection } from './sections/Market';
import { CompensationSection } from './sections/Compensation';
import {
  CompensationRequest,
  MarketIntelRequest,
  PanelState,
  ProfileSnapshot,
  SuggestionResponse
} from '../types';
import { fetchCompensation, fetchMarketIntel, fetchScore, fetchSuggestions } from '../service/api';
import { loadPanelState, persistPanelState } from '../service/session';
import { loadSettings } from '../service/settings';
import { validateProfile } from '../lib/validators';

const TABS = [
  { id: 'score', label: 'Score' },
  { id: 'suggestions', label: 'Suggestions' },
  { id: 'market', label: 'Marché & Salaire' }
] as const;

type TabId = (typeof TABS)[number]['id'];

export function SidePanelApp(): JSX.Element {
  const [activeTab, setActiveTab] = useState<TabId>('score');
  const [state, setState] = useState<PanelState | null>(null);
  const [settingsLanguage, setSettingsLanguage] = useState<'fr' | 'en'>('fr');

  const [scoreLoading, setScoreLoading] = useState(false);
  const [suggestLoading, setSuggestLoading] = useState(false);
  const [marketLoading, setMarketLoading] = useState(false);
  const [compLoading, setCompLoading] = useState(false);
  const [marketRequest, setMarketRequest] = useState<MarketIntelRequest>({ role: '', country: 'FR', seniority: 'mid' });
  const [compRequest, setCompRequest] = useState<CompensationRequest>({ role: '', country: 'FR', contractType: 'permanent' });
  const [error, setError] = useState<string | undefined>();

  useEffect(() => {
    loadSettings().then((settings) => setSettingsLanguage(settings.language));
    loadPanelState().then((stored) => {
      setState(stored);
      setMarketRequest((prev) => ({
        ...prev,
        role: stored.profile.targetRole || stored.profile.headline,
        seniority: stored.profile.seniority ?? 'mid'
      }));
      setCompRequest((prev) => ({
        ...prev,
        role: stored.profile.targetRole || stored.profile.headline,
        country: stored.profile.location?.split(',').pop()?.trim()?.toUpperCase?.() || prev.country,
        seniority: stored.profile.seniority ?? 'mid'
      }));
    });
  }, []);

  useEffect(() => {
    if (state) {
      void persistPanelState(state);
    }
  }, [state]);

  const profileIssues = useMemo(() => (state ? validateProfile(state.profile) : []), [state]);

  const handleProfileChange = (profile: ProfileSnapshot) => {
    setState((previous) => {
      const fallback: PanelState = {
        profile,
        loading: false
      } as PanelState;
      if (!previous) {
        return { ...fallback, profile };
      }
      return {
        ...previous,
        profile
      };
    });
    setMarketRequest((prev) => ({ ...prev, role: profile.targetRole || profile.headline }));
    setCompRequest((prev) => ({ ...prev, role: profile.targetRole || profile.headline }));
  };

  const handleScore = async () => {
    if (!state) {
      return;
    }
    setScoreLoading(true);
    setError(undefined);
    try {
      const keywords = state.profile.targetRole ? state.profile.targetRole.split(/[,\s]/).filter(Boolean) : undefined;
      const score = await fetchScore(state.profile, keywords);
      setState((previous) => (previous ? { ...previous, score } : previous));
    } catch (err) {
      console.error(err);
      setError((err as Error).message);
    } finally {
      setScoreLoading(false);
    }
  };

  const mergeSuggestions = (incoming: SuggestionResponse): SuggestionResponse => {
    if (!state?.suggestions) {
      return incoming;
    }
    return {
      ...incoming,
      skills: {
        core: deduplicate([...state.suggestions.skills.core, ...incoming.skills.core]),
        trending: deduplicate([...state.suggestions.skills.trending, ...incoming.skills.trending]),
        niceToHave: deduplicate([...state.suggestions.skills.niceToHave, ...incoming.skills.niceToHave])
      }
    };
  };

  const handleSuggest = async () => {
    if (!state) return;
    setSuggestLoading(true);
    setError(undefined);
    try {
      const suggestions = await fetchSuggestions(state.profile, state.profile.targetRole, settingsLanguage);
      setState((previous) => (previous ? { ...previous, suggestions: mergeSuggestions(suggestions) } : previous));
    } catch (err) {
      console.error(err);
      setError((err as Error).message);
    } finally {
      setSuggestLoading(false);
    }
  };

  const handleMarket = async () => {
    setMarketLoading(true);
    setError(undefined);
    try {
      const intel = await fetchMarketIntel({
        ...marketRequest,
        role: marketRequest.role || state?.profile.targetRole || state?.profile.headline || ''
      });
      setState((previous) => (previous ? { ...previous, marketIntel: intel } : previous));
    } catch (err) {
      console.error(err);
      setError((err as Error).message);
    } finally {
      setMarketLoading(false);
    }
  };

  const handleCompensation = async () => {
    setCompLoading(true);
    setError(undefined);
    try {
      const compensation = await fetchCompensation({
        ...compRequest,
        role: compRequest.role || state?.profile.targetRole || state?.profile.headline || ''
      });
      setState((previous) => (previous ? { ...previous, compensation } : previous));
    } catch (err) {
      console.error(err);
      setError((err as Error).message);
    } finally {
      setCompLoading(false);
    }
  };

  if (!state) {
    return (
      <main className="h-full min-h-screen bg-slate-950 p-4 text-slate-200">
        <p>Chargement…</p>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-950 p-4 text-slate-200">
      <header className="mb-4 space-y-2">
        <h1 className="text-xl font-semibold">LinkedIn Optimizer — Panel</h1>
        <p className="text-sm text-slate-400">
          UI side panel conforme : aucune modification du DOM LinkedIn, aucune automatisation. Vos données restent dans Chrome.
        </p>
        {error && <p className="text-xs text-red-400" role="alert">Erreur : {error}</p>}
      </header>

      <nav className="mb-4 flex gap-2" aria-label="Onglets">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            className={`rounded-full px-4 py-2 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-blue-300 focus:ring-offset-2 focus:ring-offset-slate-950 ${
              activeTab === tab.id
                ? 'bg-blue-500 text-white shadow-lg shadow-blue-500/30'
                : 'border border-slate-700 text-slate-300 hover:border-blue-400 hover:text-blue-200'
            }`}
            aria-current={activeTab === tab.id ? 'page' : undefined}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {activeTab === 'score' && (
        <ScoreSection
          profile={state.profile}
          score={state.score}
          issues={profileIssues}
          loading={scoreLoading}
          onProfileChange={handleProfileChange}
          onAnalyse={handleScore}
        />
      )}

      {activeTab === 'suggestions' && (
        <SuggestSection suggestions={state.suggestions} loading={suggestLoading} onGenerate={handleSuggest} />
      )}

      {activeTab === 'market' && (
        <div className="space-y-6">
          <MarketSection
            value={marketRequest}
            intel={state.marketIntel}
            loading={marketLoading}
            onChange={setMarketRequest}
            onRefresh={handleMarket}
          />
          <CompensationSection
            value={compRequest}
            data={state.compensation}
            loading={compLoading}
            onChange={setCompRequest}
            onEstimate={handleCompensation}
          />
        </div>
      )}
    </main>
  );
}

function deduplicate(items: string[]): string[] {
  return Array.from(new Set(items.map((item) => item.trim()))).filter(Boolean);
}
