import { expect, test } from '@playwright/test';

// From rag-service/app/fake_llm.py
const CANNED_ANSWER = 'This is a canned answer from the fake LLM.';
const CANNED_SOURCE = 'https://www.hw.ac.uk/students/fake-llm-source';

test('the assistant replies with an answer and a source link', async ({ page }) => {
  await page.goto('/');

  // The widget opens by itself; clicking its button could close it again
  const input = page.locator('.chat-bubble input');
  await input.fill('When is enrolment?');
  await input.press('Enter');

  // Check the exact text: a failed request also shows up as a bot message
  const history = page.locator('#chat-history');
  await expect(history).toContainText(CANNED_ANSWER);
  await expect(history.locator(`a[href="${CANNED_SOURCE}"]`)).toBeVisible();
});
