import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { Services } from './services';
import { ApiService } from '../../services/api.service';

describe('Services', () => {
  let component: Services;
  let fixture: ComponentFixture<Services>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Services],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: { getResources: vi.fn().mockReturnValue(of({ id: 'services' })) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Services);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the services resource category', () => {
    fixture.detectChanges();
    expect(component.resources()).toEqual({ id: 'services' });
  });
});
