import { Component, inject } from '@angular/core';
import { PageHeader } from '../../components/page-header/page-header';
import { LoadState } from '../../components/load-state/load-state';
import { Map } from '../../components/map/map';
import { CommonModule, ViewportScroller } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';

@Component({
  selector: 'app-facilities',
  imports: [PageHeader, LoadState, Map, CommonModule, TranslateModule],
  templateUrl: './facilities.html',
  styleUrl: './facilities.css',
})
export class Facilities extends ResourceCategoryPage {
  protected categoryId = 'facilities';

  private scroller = inject(ViewportScroller);

  activeMapLocation: string | null = null;

  highlightOnMap(location: string, event: Event) {
    event.preventDefault();
    this.activeMapLocation = location;
    this.scroller.scrollToAnchor('campus-map'); 
  }
}
