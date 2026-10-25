import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ResourceCategoryPage } from '../resource-category-page';

@Component({
  selector: 'app-wellbeing',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './wellbeing.html',
  styleUrl: './wellbeing.css',
})
export class Wellbeing extends ResourceCategoryPage {
  protected categoryId = 'wellbeing';
}
