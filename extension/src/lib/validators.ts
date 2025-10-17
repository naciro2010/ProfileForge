import { ProfileSnapshot } from '../types';

export function validateProfile(profile: ProfileSnapshot): string[] {
  const issues: string[] = [];

  if (!profile.headline?.trim()) {
    issues.push('Ajoutez un headline clair.');
  } else if (profile.headline.length > 220) {
    issues.push('Le headline dépasse 220 caractères.');
  }

  if (profile.about.trim().length < 200) {
    issues.push('La section À propos doit contenir au moins 200 caractères.');
  }

  if (profile.skills.length < 6) {
    issues.push('Ajoutez au moins 6 compétences pertinentes.');
  }

  profile.experiences.forEach((exp, index) => {
    if (!exp.role.trim() || !exp.company.trim()) {
      issues.push(`Complétez le titre et l'entreprise pour l'expérience ${index + 1}.`);
    }
    if (exp.achievements.trim().length < 50) {
      issues.push(`Ajoutez des réalisations chiffrées pour l'expérience ${index + 1}.`);
    }
  });

  return issues;
}

export function sanitizeSkills(value: string): string[] {
  return value
    .split(/[,\n]/)
    .map((skill) => skill.trim())
    .filter(Boolean)
    .slice(0, 20);
}
