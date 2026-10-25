import { Directive, ElementRef, HostListener, Renderer2, effect, inject, input } from '@angular/core';
import { Router } from '@angular/router';

@Directive({
  selector: 'a[appSmartLink]',
})
export class SmartLinkDirective {
  readonly url = input<string | null | undefined>(null, { alias: 'appSmartLink' });

  private readonly router = inject(Router);
  private readonly el = inject<ElementRef<HTMLAnchorElement>>(ElementRef);
  private readonly renderer = inject(Renderer2);

  constructor() {
    effect(() => {
      const url = this.url();
      const anchor = this.el.nativeElement;

      if (!url) {
        this.renderer.removeAttribute(anchor, 'href');
        this.renderer.removeAttribute(anchor, 'target');
        this.renderer.removeAttribute(anchor, 'rel');
        return;
      }

      this.renderer.setAttribute(anchor, 'href', url);

      if (this.isExternal(url)) {
        this.renderer.setAttribute(anchor, 'target', '_blank');
        this.renderer.setAttribute(anchor, 'rel', 'noopener noreferrer');
      } else {
        this.renderer.removeAttribute(anchor, 'target');
        this.renderer.removeAttribute(anchor, 'rel');
      }
    });
  }

  @HostListener('click', ['$event'])
  onClick(event: MouseEvent): void {
    const url = this.url();

    // External links and modified clicks (open in new tab/window, middle click, etc.)
    // fall through to the browser's default anchor behavior.
    if (!url || this.isExternal(url) || event.defaultPrevented || event.button !== 0
      || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
      return;
    }

    event.preventDefault();
    this.router.navigateByUrl(url);
  }

  private isExternal(url: string): boolean {
    return url.startsWith('http');
  }
}
