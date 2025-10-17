import {
  CompensationRequest,
  CompensationResponse,
  MarketIntelRequest,
  MarketIntelResponse,
  PanelState,
  ProfileSnapshot,
  ScoreRequest,
  ScoreResponse,
  SuggestRequest,
  SuggestionResponse
} from '../types';
import { loadSettings } from './settings';

async function request<T>(path: string, payload: unknown): Promise<T> {
  const settings = await loadSettings();
  const response = await fetch(new URL(path, settings.backendUrl).toString(), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': settings.apiKey
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Erreur ${response.status}`);
  }

  return (await response.json()) as T;
}

export async function fetchScore(profile: ProfileSnapshot, keywords?: string[]): Promise<ScoreResponse> {
  const payload: ScoreRequest = { profile, keywords };
  return request<ScoreResponse>('/api/v1/score', payload);
}

export async function fetchSuggestions(
  profile: ProfileSnapshot,
  targetRole?: string,
  language?: 'fr' | 'en'
): Promise<SuggestionResponse> {
  const payload: SuggestRequest = { profile, targetRole, language };
  return request<SuggestionResponse>('/api/v1/suggest', payload);
}

export async function fetchMarketIntel(params: MarketIntelRequest): Promise<MarketIntelResponse> {
  const payload = {
    ...params,
    seniority: params.seniority?.toUpperCase()
  };

  return request<MarketIntelResponse>('/api/v1/market-intel', payload);
}

export async function fetchCompensation(params: CompensationRequest): Promise<CompensationResponse> {
  const payload = {
    ...params,
    seniority: params.seniority?.toUpperCase(),
    companyType: params.companyType?.toUpperCase(),
    contractType: params.contractType?.toUpperCase()
  };

  return request<CompensationResponse>('/api/v1/compensation', payload);
}

export function deriveKeywords(profile: ProfileSnapshot): string[] {
  const base = [profile.targetRole, profile.headline]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();
  const fromHeadline = base.split(/[\s,]/).filter((token) => token.length > 3);
  return Array.from(new Set([...fromHeadline, ...profile.skills.map((skill) => skill.toLowerCase())])).slice(0, 12);
}

export function summarisePanelState(state: PanelState): string {
  return [
    `Score: ${state.score?.total ?? 'n/a'}`,
    `Headline: ${state.profile.headline.slice(0, 80)}`,
    `Skills: ${state.profile.skills.slice(0, 5).join(', ')}`
  ].join('\n');
}
