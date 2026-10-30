import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';

import { PageHeader } from './page-header';

@Component({
  imports: [PageHeader],
  template: `
    <app-page-header [title]="title" [description]="description" backLink="/induction" backLabel="INDUCTION.BACK">
      <span class="badge">Postgraduate</span>
    </app-page-header>
  `,
})
class Host {
  title = 'Welcome';
  description: string | undefined = 'Start here';
}

describe('PageHeader', () => {
  function render(changes: Partial<Host> = {}): HTMLElement {
    TestBed.configureTestingModule({
      imports: [Host],
      providers: [provideRouter([]), provideTranslateService()],
    });
    const fixture = TestBed.createComponent(Host);
    Object.assign(fixture.componentInstance, changes);
    fixture.detectChanges();
    return fixture.nativeElement;
  }

  it('shows the title, description and projected content', () => {
    const element = render();

    expect(element.querySelector('h1')?.textContent).toBe('Welcome');
    expect(element.querySelector('p.lead')?.textContent).toBe('Start here');
    expect(element.querySelector('.badge')?.textContent).toBe('Postgraduate');
  });

  it('links back to the given page', () => {
    const element = render();

    expect(element.querySelector('a')?.getAttribute('href')).toBe('/induction');
  });

  it('leaves out the description when there is none', () => {
    const element = render({ description: undefined });

    expect(element.querySelector('p.lead')).toBeNull();
  });
});
