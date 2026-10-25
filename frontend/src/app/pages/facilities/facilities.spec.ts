import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { ViewportScroller } from '@angular/common';
import { of } from 'rxjs';

import { Facilities } from './facilities';
import { ApiService } from '../../services/api.service';

describe('Facilities', () => {
  let component: Facilities;
  let fixture: ComponentFixture<Facilities>;
  let scroller: { scrollToAnchor: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    scroller = { scrollToAnchor: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Facilities],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: { getResources: vi.fn().mockReturnValue(of({ id: 'facilities' })) } },
        { provide: ViewportScroller, useValue: scroller },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Facilities);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the facilities resource category', () => {
    fixture.detectChanges();
    expect(component.resources()).toEqual({ id: 'facilities' });
  });

  it('highlights a location on the map and scrolls to it', () => {
    const event = { preventDefault: vi.fn() } as unknown as Event;

    component.highlightOnMap('Main Library', event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.activeMapLocation).toBe('Main Library');
    expect(scroller.scrollToAnchor).toHaveBeenCalledWith('campus-map');
  });
});
