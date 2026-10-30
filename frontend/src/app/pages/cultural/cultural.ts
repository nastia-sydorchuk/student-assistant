import { Component } from '@angular/core';
import { PageHeader } from '../../components/page-header/page-header';
import { LoadState } from '../../components/load-state/load-state';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';

@Component({
  selector: 'app-cultural',
  imports: [PageHeader, LoadState, CommonModule, TranslateModule],
  templateUrl: './cultural.html',
  styleUrl: './cultural.css',
})
export class Cultural extends ResourceCategoryPage {
  protected categoryId = 'cultural';
}
