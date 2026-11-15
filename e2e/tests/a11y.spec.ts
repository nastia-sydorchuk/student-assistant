import AxeBuilder from '@axe-core/playwright';
import { expect, Page, test } from '@playwright/test';

// Every route in frontend/src/app/app.routes.ts ('home' is the landing page again)
const ROUTES = [
  '/',
  '/registration',
  '/services',
  '/academic',
  '/facilities',
  '/cultural',
  '/induction',
  '/induction/undergraduate',
  '/induction/postgraduate',
  '/wellbeing',
];

// WCAG 2.2 A and AA; axe's best-practice rules are left out so the build only fails on real criteria
const WCAG_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'];

async function waitForContent(page: Page) {
  await expect(page.locator('h1').first()).toBeVisible();
  await expect(page.locator('app-load-state .spinner-border')).toHaveCount(0);
}

function axe(page: Page) {
  return (
    new AxeBuilder({ page })
      .withTags(WCAG_TAGS)
      // The Google Map is a third-party widget we can't change
      .exclude('google-map')
  );
}

function describeViolations(violations: Awaited<ReturnType<AxeBuilder['analyze']>>['violations']) {
  return violations
    .map((v) => `${v.id} (${v.impact}): ${v.help}\n${v.nodes.map((n) => `  ${n.target.join(' ')}`).join('\n')}`)
    .join('\n\n');
}

for (const route of ROUTES) {
  test(`${route} has no WCAG A/AA violations`, async ({ page }) => {
    await page.goto(route);
    await waitForContent(page);

    // The chat widget opens by itself, so this also covers the empty chat
    const { violations } = await axe(page).analyze();
    expect(describeViolations(violations)).toBe('');
  });
}

test('the chat has no WCAG A/AA violations after an answer with sources', async ({ page }) => {
  await page.goto('/');
  await waitForContent(page);

  const input = page.locator('.chat-bubble input');
  await input.fill('When is enrolment?');
  await input.press('Enter');
  await expect(page.locator('#chat-history a').first()).toBeVisible();

  const { violations } = await axe(page).include('app-chat-widget').analyze();
  expect(describeViolations(violations)).toBe('');
});
