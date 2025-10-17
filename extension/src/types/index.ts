export type SectionKey = 'headline' | 'about' | 'skills' | 'experience';

export interface ExperienceInput {
  id: string;
  role: string;
  company: string;
  timeframe?: string;
  achievements: string;
}

export interface ProfileSnapshot {
  headline: string;
  about: string;
  skills: string[];
  experiences: ExperienceInput[];
  location?: string;
  seniority?: string;
  targetRole?: string;
  languages?: string[];
}

export interface ScoreRequest {
  profile: ProfileSnapshot;
  keywords?: string[];
}

export interface ScoreResponse {
  total: number;
  breakdown: Record<string, number>;
  warnings: string[];
}

export interface SuggestRequest {
  profile: ProfileSnapshot;
  targetRole?: string;
  language?: 'fr' | 'en';
}

export interface CopyBlock {
  label: string;
  content: string;
}

export interface SkillSuggestions {
  core: string[];
  trending: string[];
  niceToHave: string[];
}

export interface ExperienceSuggestion {
  role: string;
  company: string;
  bullets: string[];
}

export interface SuggestionResponse {
  headline: CopyBlock[];
  about: CopyBlock[];
  skills: SkillSuggestions;
  experiences: ExperienceSuggestion[];
}

export interface MarketIntelRequest {
  role: string;
  country: string;
  seniority?: string;
  industry?: string;
}

export interface SkillInsight {
  name: string;
  category: 'core' | 'trending' | 'soft';
  source: string;
}

export interface RecruiterSignal {
  statement: string;
  rationale: string;
}

export interface MarketIntelResponse {
  refreshedAt: string;
  skills: SkillInsight[];
  recruiterSignals: RecruiterSignal[];
  sources: Array<{ name: string; url: string }>;
}

export interface CompensationRequest {
  role: string;
  country: string;
  region?: string;
  seniority?: string;
  companyType?: string;
  contractType?: 'permanent' | 'freelance';
  industry?: string;
  currency?: string;
}

export interface CompensationResponse {
  currency: string;
  period: 'annual' | 'daily';
  low: number;
  median: number;
  high: number;
  justifications: string[];
  sources: Array<{ name: string; url: string }>;
}

export interface PanelState {
  profile: ProfileSnapshot;
  score?: ScoreResponse;
  suggestions?: SuggestionResponse;
  marketIntel?: MarketIntelResponse;
  compensation?: CompensationResponse;
  loading: boolean;
  error?: string;
}
