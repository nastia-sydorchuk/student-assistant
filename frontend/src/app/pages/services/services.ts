import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';
import { SmartLinkDirective } from '../../directives/smart-link.directive';

@Component({
  selector: 'app-services',
  imports: [RouterLink, CommonModule, TranslateModule, SmartLinkDirective],
  templateUrl: './services.html',
  styleUrl: './services.css',
})
export class Services extends ResourceCategoryPage {
  protected categoryId = 'services';
}
