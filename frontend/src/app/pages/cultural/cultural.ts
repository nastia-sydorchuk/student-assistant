import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';

@Component({
  selector: 'app-cultural',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './cultural.html',
  styleUrl: './cultural.css',
})
export class Cultural extends ResourceCategoryPage {
  protected categoryId = 'cultural';
}
