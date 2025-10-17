export type ExperienceEntry = {
  title: string
  company: string
  dates?: string
  description?: string
}

export type ProfilePayload = {
  url: string
  locale?: string
  fullName?: string
  headline?: string
  about?: string
  location?: string
  experiences: ExperienceEntry[]
  skills: string[]
  hasPhoto?: boolean
}

export type BackendConfig = {
  backendUrl: string
  apiKey: string
  provider: 'ollama' | 'openrouter'
  model: string
  language: 'fr' | 'en'
}

export type ScoreResponse = {
  total: number
  breakdown: Record<string, number>
  warnings: string[]
}

export type SuggestionResponse = {
  headline: string
  about: string
  skills: string[]
  experienceBullets: {
    company: string
    title: string
    bullets: string[]
  }[]
}

export type AnalyzerResult = {
  score?: ScoreResponse
  suggestions?: SuggestionResponse
  error?: string
}
