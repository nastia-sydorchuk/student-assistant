import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';

import { LoadState } from './load-state';

describe('LoadState', () => {
  function render(isLoading: boolean, error: string): HTMLElement {
    TestBed.configureTestingModule({
      imports: [LoadState],
      providers: [provideTranslateService()],
    });
    const fixture = TestBed.createComponent(LoadState);
    fixture.componentRef.setInput('isLoading', isLoading);
    fixture.componentRef.setInput('error', error);
    fixture.detectChanges();
    return fixture.nativeElement;
  }

  it('shows a spinner while loading', () => {
    const element = render(true, '');

    expect(element.querySelector('.spinner-border')).not.toBeNull();
    expect(element.querySelector('[role="alert"]')).toBeNull();
  });

  it('shows the error as an alert', () => {
    const element = render(false, 'COMMON.ERROR');

    expect(element.querySelector('.spinner-border')).toBeNull();
    expect(element.querySelector('[role="alert"]')?.textContent?.trim()).toBe('COMMON.ERROR');
  });

  it('shows nothing once loaded without an error', () => {
    const element = render(false, '');

    expect(element.textContent?.trim()).toBe('');
  });
});
