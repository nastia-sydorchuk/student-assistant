import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { Cultural } from './cultural';
import { ApiService } from '../../services/api.service';

describe('Cultural', () => {
  let component: Cultural;
  let fixture: ComponentFixture<Cultural>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cultural],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: { getResources: vi.fn().mockReturnValue(of({ id: 'cultural' })) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Cultural);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the cultural resource category', () => {
    fixture.detectChanges();
    expect(component.resources()).toEqual({ id: 'cultural' });
  });
});
