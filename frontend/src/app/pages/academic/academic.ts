import { Component } from '@angular/core';
import { PageHeader } from '../../components/page-header/page-header';
import { LoadState } from '../../components/load-state/load-state';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../shared/resource-category-page';

@Component({
  selector: 'app-academic',
  imports: [PageHeader, LoadState, CommonModule, TranslateModule],
  templateUrl: './academic.html',
  styleUrl: './academic.css',
})
export class Academic extends ResourceCategoryPage {
  protected categoryId = 'academic';
}
