import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';

@Component({
  selector: 'app-services',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './services.html',
  styleUrl: './services.css',
})
export class Services extends ResourceCategoryPage {
  protected categoryId = 'services';
}
