import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { SmartLinkDirective } from './smart-link.directive';

@Component({
  selector: 'app-smart-link-host-test',
  imports: [SmartLinkDirective],
  template: `<a [appSmartLink]="url" class="btn">Go</a>`,
})
class SmartLinkHostTestComponent {
  url: string | null | undefined = '/registration';
}

describe('SmartLinkDirective', () => {
  let fixture: ComponentFixture<SmartLinkHostTestComponent>;
  let anchor: HTMLAnchorElement;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SmartLinkHostTestComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(SmartLinkHostTestComponent);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  });

  it('sets a plain href and routes internal links through the Router on click', () => {
    fixture.componentInstance.url = '/registration';
    fixture.detectChanges();
    anchor = fixture.debugElement.query(By.css('a')).nativeElement as HTMLAnchorElement;

    expect(anchor.getAttribute('href')).toBe('/registration');
    expect(anchor.hasAttribute('target')).toBe(false);

    anchor.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, button: 0 }));

    expect(router.navigateByUrl).toHaveBeenCalledWith('/registration');
  });

  it('sets target/rel for external links and lets the browser handle the click', () => {
    fixture.componentInstance.url = 'https://example.com';
    fixture.detectChanges();
    anchor = fixture.debugElement.query(By.css('a')).nativeElement as HTMLAnchorElement;

    expect(anchor.getAttribute('href')).toBe('https://example.com');
    expect(anchor.getAttribute('target')).toBe('_blank');
    expect(anchor.getAttribute('rel')).toBe('noopener noreferrer');

    const event = new MouseEvent('click', { bubbles: true, cancelable: true, button: 0 });
    anchor.dispatchEvent(event);

    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(event.defaultPrevented).toBe(false);
  });

  it('does not intercept modified clicks on internal links', () => {
    fixture.componentInstance.url = '/registration';
    fixture.detectChanges();
    anchor = fixture.debugElement.query(By.css('a')).nativeElement as HTMLAnchorElement;

    anchor.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, button: 0, ctrlKey: true }));

    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('removes href/target/rel when the url is missing', () => {
    fixture.componentInstance.url = undefined;
    fixture.detectChanges();
    anchor = fixture.debugElement.query(By.css('a')).nativeElement as HTMLAnchorElement;

    expect(anchor.hasAttribute('href')).toBe(false);
    expect(anchor.hasAttribute('target')).toBe(false);
    expect(anchor.hasAttribute('rel')).toBe(false);
  });
});
