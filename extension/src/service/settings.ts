const SETTINGS_KEY = 'linkedinOptimizer.settings';

type Settings = {
  backendUrl: string;
  apiKey: string;
  language: 'fr' | 'en';
};

const defaultSettings: Settings = {
  backendUrl: 'http://localhost:8080',
  apiKey: 'changeme',
  language: 'fr'
};

export async function loadSettings(): Promise<Settings> {
  const result = await chrome.storage.sync.get(SETTINGS_KEY);
  return { ...defaultSettings, ...(result[SETTINGS_KEY] as Settings | undefined) };
}

export async function saveSettings(settings: Settings): Promise<void> {
  await chrome.storage.sync.set({ [SETTINGS_KEY]: settings });
}
