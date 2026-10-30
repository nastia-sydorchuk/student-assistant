import { Component, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

// The spinner and error alert shown above a page while its data loads or after it fails
@Component({
  selector: 'app-load-state',
  imports: [TranslateModule],
  templateUrl: './load-state.html',
})
export class LoadState {
  isLoading = input.required<boolean>();
  error = input.required<string>(); // translation key, or '' for no error
}
