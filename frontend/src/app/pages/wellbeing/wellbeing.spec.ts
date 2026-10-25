import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { Wellbeing } from './wellbeing';
import { ApiService } from '../../services/api.service';

describe('Wellbeing', () => {
  let component: Wellbeing;
  let fixture: ComponentFixture<Wellbeing>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Wellbeing],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: { getResources: vi.fn().mockReturnValue(of({ id: 'wellbeing' })) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Wellbeing);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the wellbeing resource category', () => {
    fixture.detectChanges();
    expect(component.resources()).toEqual({ id: 'wellbeing' });
  });
});
