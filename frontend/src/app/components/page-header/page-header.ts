import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

// The blue banner at the top of content pages: a back link, the title and an optional description.
// Content placed inside <app-page-header> (e.g. a badge) is shown above the title.
@Component({
  selector: 'app-page-header',
  imports: [RouterLink, TranslateModule],
  templateUrl: './page-header.html',
  host: { class: 'd-block' },
})
export class PageHeader {
  title = input.required<string>();
  description = input<string>();
  backLink = input('/');
  backLabel = input('COMMON.NAV'); // translation key
}
