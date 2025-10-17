import { PanelState, ProfileSnapshot } from '../types';

const STATE_KEY = 'linkedinOptimizer.panelState';

const defaultProfile: ProfileSnapshot = {
  headline: '',
  about: '',
  skills: [],
  experiences: [],
  location: '',
  seniority: 'mid',
  targetRole: '',
  languages: ['fr']
};

export async function loadPanelState(): Promise<PanelState> {
  const result = await chrome.storage.session.get(STATE_KEY);
  const stored = result[STATE_KEY] as PanelState | undefined;
  return {
    profile: stored?.profile ?? defaultProfile,
    score: stored?.score,
    suggestions: stored?.suggestions,
    marketIntel: stored?.marketIntel,
    compensation: stored?.compensation,
    loading: false,
    error: undefined
  };
}

export async function persistPanelState(state: PanelState): Promise<void> {
  const { loading, error, ...rest } = state;
  await chrome.storage.session.set({ [STATE_KEY]: rest });
}

export function getDefaultProfile(): ProfileSnapshot {
  return structuredClone(defaultProfile);
}
