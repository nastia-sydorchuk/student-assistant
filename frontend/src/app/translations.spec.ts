import en from '../assets/en.json';
import es from '../assets/es.json';
import fr from '../assets/fr.json';
import de from '../assets/de.json';

interface Translations {
  [key: string]: string | Translations;
}

// Flattens nested translations to dotted keys, e.g. "DASHBOARD.ERROR"
function keys(translations: Translations, prefix = ''): string[] {
  return Object.entries(translations).flatMap(([key, value]) =>
    typeof value === 'string' ? [prefix + key] : keys(value, `${prefix}${key}.`),
  );
}

// A key missing from one language shows up on screen as the raw key, e.g. "DASHBOARD.ERROR"
describe('translation files', () => {
  const english = keys(en).sort();

  it.each([
    ['es', es],
    ['fr', fr],
    ['de', de],
  ])('%s has exactly the same keys as English', (_language, translations) => {
    expect(keys(translations).sort()).toEqual(english);
  });
});
