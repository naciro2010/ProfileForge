const normalizeText = (value: string | null | undefined): string | undefined => {
  if (!value) return undefined
  return value
    .replace(/\s+/g, ' ')
    .replace(/…\s*see more/gi, '')
    .replace(/voir plus/gi, '')
    .replace(/[\u{1F300}-\u{1FAFF}]/gu, '')
    .trim() || undefined
}

const pickFirst = <T>(
  selectors: string[],
  extractor: (element: Element) => T | undefined
): T | undefined => {
  for (const selector of selectors) {
    const element = document.querySelector(selector)
    if (element) {
      const value = extractor(element)
      if (value !== undefined) {
        return value
      }
    }
  }
  return undefined
}

const textSelectors = (
  selectors: string[],
  fallback?: string
): string | undefined => {
  const value = pickFirst(selectors, (el) => normalizeText(el.textContent))
  return value ?? fallback
}

const listSelectors = (selectors: string[]): string[] => {
  const results = new Set<string>()
  selectors.forEach((selector) => {
    document.querySelectorAll(selector).forEach((el) => {
      const normalized = normalizeText(el.textContent)
      if (normalized) {
        results.add(normalized)
      }
    })
  })
  return Array.from(results)
}

const hasPhoto = (): boolean => {
  const selectors = [
    'div.pv-top-card__non-self-photo img',
    'img.pv-top-card-profile-picture__image',
    'img.presence-entity__image'
  ]
  return selectors.some((selector) => {
    const img = document.querySelector<HTMLImageElement>(selector)
    return !!img && !!img.src && !img.src.includes('ghost-person')
  })
}

export const linkedinSelectors = {
  fullName: [
    'h1.text-heading-xlarge',
    '.pv-text-details__left-panel h1',
    'div.ph5.pb5 h1'
  ],
  headline: [
    'div.text-body-medium.break-words',
    '.pv-text-details__left-panel .text-body-medium',
    'div.ph5.pb5 div.text-body-medium'
  ],
  about: [
    '.pv-about-section div.inline-show-more-text',
    'section[data-section="about"] .pv-shared-text-with-see-more',
    'section[id*=about] div.inline-show-more-text'
  ],
  location: [
    '.pv-text-details__left-panel .text-body-small.inline.t-black--light.break-words',
    'span.text-body-small.inline.t-black--light.break-words',
    'section.pv-top-card .pv-top-card--list-bullet span.inline-flex'
  ],
  experiences: [
    'section[id*=experience-section] li.artdeco-list__item',
    'section[data-section="experience"] li',
    '.pv-profile-section__section-info.pv-profile-section__section-info--has-no-more a'
  ],
  experienceTitle: [
    'span.mr1.t-bold span[aria-hidden="true"]',
    '.display-flex.align-items-center .t-bold span[aria-hidden="true"]',
    'span.t-14.t-normal'
  ],
  experienceCompany: [
    'span.t-14.t-normal span[aria-hidden="true"]',
    '.pv-entity__secondary-title',
    '.t-14.t-black.t-normal span[aria-hidden="true"]'
  ],
  experienceDates: [
    '.t-14.t-normal.t-black--light span[aria-hidden="true"]',
    '.pv-entity__date-range span:nth-child(2)',
    '.pv2 span.t-14.t-normal.t-black--light span[aria-hidden="true"]'
  ],
  experienceDescription: [
    'div.inline-show-more-text span[aria-hidden="true"]',
    '.pv-entity__description.t-14.t-normal.t-black span[aria-hidden="true"]',
    '.pvs-list__outer-container li span[aria-hidden="true"]'
  ],
  skills: [
    'section[id*=skills-section] span.mr1.t-bold span[aria-hidden="true"]',
    '.pv-skill-category-entity__name-text',
    'section[data-section="skills"] span[aria-hidden="true"]'
  ]
}

export const selectorsUtil = {
  normalizeText,
  pickFirst,
  textSelectors,
  listSelectors,
  hasPhoto
}
