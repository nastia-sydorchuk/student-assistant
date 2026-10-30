import { Component } from '@angular/core';
import { PageHeader } from '../../components/page-header/page-header';
import { LoadState } from '../../components/load-state/load-state';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../shared/resource-category-page';

@Component({
  selector: 'app-wellbeing',
  imports: [PageHeader, LoadState, CommonModule, TranslateModule],
  templateUrl: './wellbeing.html',
  styleUrl: './wellbeing.css',
})
export class Wellbeing extends ResourceCategoryPage {
  protected categoryId = 'wellbeing';
}
