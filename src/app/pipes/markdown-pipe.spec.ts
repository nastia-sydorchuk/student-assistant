import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MarkdownPipe } from './markdown-pipe';

@Component({
  selector: 'app-markdown-host-test',
  imports: [MarkdownPipe],
  template: `<span [innerHTML]="text | markdown"></span>`,
})
class MarkdownHostTestComponent {
  text = '';
}

describe('MarkdownPipe', () => {
  it('create an instance', () => {
    const pipe = new MarkdownPipe();
    expect(pipe).toBeTruthy();
  });

  it('strips <script> tags when bound via [innerHTML]', async () => {
    await TestBed.configureTestingModule({
      imports: [MarkdownHostTestComponent],
    }).compileComponents();

    const fixture: ComponentFixture<MarkdownHostTestComponent> = TestBed.createComponent(MarkdownHostTestComponent);
    fixture.componentInstance.text = 'Hello <script>window.__pwned = true;</script> world';
    fixture.detectChanges();

    const span = fixture.debugElement.query(By.css('span')).nativeElement as HTMLElement;

    expect(span.innerHTML).not.toContain('<script');
    expect((window as any).__pwned).toBeUndefined();
  });

  it('strips inline event-handler attributes when bound via [innerHTML]', async () => {
    await TestBed.configureTestingModule({
      imports: [MarkdownHostTestComponent],
    }).compileComponents();

    const fixture: ComponentFixture<MarkdownHostTestComponent> = TestBed.createComponent(MarkdownHostTestComponent);
    fixture.componentInstance.text = '<img src="x" onerror="window.__pwned = true">';
    fixture.detectChanges();

    const span = fixture.debugElement.query(By.css('span')).nativeElement as HTMLElement;

    expect(span.innerHTML).not.toContain('onerror');
    expect((window as any).__pwned).toBeUndefined();
  });
});
