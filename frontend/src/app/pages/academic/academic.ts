import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';

@Component({
  selector: 'app-academic',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './academic.html',
  styleUrl: './academic.css',
})
export class Academic extends ResourceCategoryPage {
  protected categoryId = 'academic';
}
