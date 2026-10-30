import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PageHeader } from '../../components/page-header/page-header';
import { LoadState } from '../../components/load-state/load-state';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';
import { SmartLinkDirective } from '../../directives/smart-link.directive';

@Component({
  selector: 'app-services',
  imports: [PageHeader, LoadState, CommonModule, TranslateModule, SmartLinkDirective],
  templateUrl: './services.html',
  styleUrl: './services.css',
})
export class Services extends ResourceCategoryPage {
  protected categoryId = 'services';
}
