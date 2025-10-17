const SELECTORS: Record<string, string[]> = {
  headline: ['div[data-section="top-card"]', 'section.pv-top-card'],
  about: ['section[id*="about"]', 'section[data-test-id="about"]'],
  skills: ['section[id*="skills"]', 'section[data-test-id="skill-details"]'],
  experience: ['section[id*="experience"]', 'section[data-test-id="experience"]']
};

export async function scrollToSection(section: keyof typeof SELECTORS): Promise<void> {
  const [{ result }] = await chrome.scripting.executeScript({
    target: { tabId: await getActiveTabId() },
    func: (selectors: string[]) => {
      for (const selector of selectors) {
        const element = document.querySelector<HTMLElement>(selector);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          return true;
        }
      }
      return false;
    },
    args: [SELECTORS[section]]
  });

  if (!result) {
    throw new Error('Section introuvable sur la page LinkedIn active.');
  }
}

async function getActiveTabId(): Promise<number> {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  if (!tab?.id) {
    throw new Error('Onglet actif introuvable');
  }
  return tab.id;
}
