import { ChangeDetectorRef, Component } from '@angular/core';
import { Api } from '../../services/api';
import { InductionEvent } from '../../models/welcome.model';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-induction',
  imports: [RouterLink, CommonModule],
  templateUrl: './induction.html',
  styleUrl: './induction.css',
})
export class Induction {
  inductionEvents: InductionEvent[] = [];
  isLoadingEvents = true;
  eventsError = '';
  
  constructor(private service: Api, private cdr: ChangeDetectorRef) { }
  
  ngOnInit() {
    this.fetchInductionEvents();
  }

  fetchInductionEvents() {
    this.isLoadingEvents = true;

    this.service.getInductionEvents().subscribe({
      next: (data) => {
        this.inductionEvents = data;
        this.isLoadingEvents = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching events:', error);
        this.eventsError = 'Unable to load induction events at this time. Please try again later.';
        this.isLoadingEvents = false;
      }
    });
  }
}
