import { linkedinSelectors, selectorsUtil } from '../lib/selectors'
import type { ExperienceEntry, ProfilePayload } from '../types'

const waitForPage = (timeoutMs = 4000): Promise<void> => {
  return new Promise((resolve) => {
    let timeoutId: number
    const observer = new MutationObserver(() => {
      if (document.readyState === 'complete') {
        clearTimeout(timeoutId)
        observer.disconnect()
        resolve()
      }
    })

    observer.observe(document, { childList: true, subtree: true })

    timeoutId = window.setTimeout(() => {
      observer.disconnect()
      resolve()
    }, timeoutMs)
  })
}

const collectExperiences = (): ExperienceEntry[] => {
  const nodes = document.querySelectorAll(linkedinSelectors.experiences.join(','))
  const experiences: ExperienceEntry[] = []

  nodes.forEach((node) => {
    const scopedQuery = (selectors: string[]): string | undefined => {
      for (const selector of selectors) {
        const element = node.querySelector(selector)
        if (element) {
          const value = selectorsUtil.normalizeText(element.textContent ?? '')
          if (value) {
            return value
          }
        }
      }
      return undefined
    }

    const title = scopedQuery(linkedinSelectors.experienceTitle)
    const company = scopedQuery(linkedinSelectors.experienceCompany)
    const dates = scopedQuery(linkedinSelectors.experienceDates)
    const description = scopedQuery(linkedinSelectors.experienceDescription)

    if (title || company || description) {
      experiences.push({
        title: title ?? '',
        company: company ?? '',
        dates,
        description
      })
    }
  })

  return experiences
}

const collectProfile = (): ProfilePayload => {
  const url = window.location.href
  const locale = document.documentElement.lang ?? undefined
  const fullName = selectorsUtil.textSelectors(linkedinSelectors.fullName)
  const headline = selectorsUtil.textSelectors(linkedinSelectors.headline)
  const about = selectorsUtil.textSelectors(linkedinSelectors.about)
  const location = selectorsUtil.textSelectors(linkedinSelectors.location)
  const experiences = collectExperiences()
  const skills = selectorsUtil.listSelectors(linkedinSelectors.skills).slice(0, 20)
  const hasPhoto = selectorsUtil.hasPhoto()

  return {
    url,
    locale,
    fullName,
    headline,
    about,
    location,
    experiences,
    skills,
    hasPhoto
  }
}

const emitSnapshot = (profile: ProfilePayload) => {
  chrome.runtime.sendMessage({ type: 'PROFILE_COLLECTED', payload: profile })
}

const initialize = async () => {
  await waitForPage()
  const profile = collectProfile()
  emitSnapshot(profile)
}

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (message?.type === 'COLLECT_PROFILE') {
    const profile = collectProfile()
    emitSnapshot(profile)
    sendResponse({ profile })
    return true
  }
  return undefined
})

initialize().catch((error) => {
  console.error('linkedin-optimizer: unable to collect profile', error)
})
