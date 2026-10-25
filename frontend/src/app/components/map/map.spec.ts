import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MapDirectionsService } from '@angular/google-maps';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { Map } from './map';

describe('Map', () => {
  let component: Map;
  let fixture: ComponentFixture<Map>;
  let directionsService: { route: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    directionsService = { route: vi.fn() };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any -- mocking the ambient `google` namespace for the test
    (globalThis as any).google = { maps: { TravelMode: { WALKING: 'WALKING' } } };

    await TestBed.configureTestingModule({
      imports: [Map],
      providers: [
        provideTranslateService(),
        { provide: MapDirectionsService, useValue: directionsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Map);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any -- mocking the ambient `google` namespace for the test
    delete (globalThis as any).google;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('stores the directions result on a successful route calculation', () => {
    const mockResult = { routes: [] } as unknown as google.maps.DirectionsResult;
    directionsService.route.mockReturnValue(of({ result: mockResult }));

    component.calculateRoute({ lat: 1, lng: 1 }, { lat: 2, lng: 2 });

    expect(component.directionsResults()).toBe(mockResult);
    expect(component.routeError()).toBe('');
  });

  it('surfaces a route error message when the request errors', () => {
    directionsService.route.mockReturnValue(throwError(() => new Error('routing failed')));

    component.calculateRoute({ lat: 1, lng: 1 }, { lat: 2, lng: 2 });

    expect(component.directionsResults()).toBeUndefined();
    expect(component.routeError()).toBe(
      'Could not calculate a walking route. Please try a different location.',
    );
  });

  it('surfaces a route error message when the response has no result', () => {
    directionsService.route.mockReturnValue(of({ result: undefined }));

    component.calculateRoute({ lat: 1, lng: 1 }, { lat: 2, lng: 2 });

    expect(component.routeError()).toBe(
      'Could not calculate a walking route. Please try a different location.',
    );
  });

  it('resolves the fallback origin when geolocation is unavailable', async () => {
    const originalGeolocation = navigator.geolocation;
    Object.defineProperty(navigator, 'geolocation', { value: undefined, configurable: true });

    const location = await component.getUserLocation();

    expect(location).toEqual(component.fallbackOrigin);

    Object.defineProperty(navigator, 'geolocation', { value: originalGeolocation, configurable: true });
  });

  it('resolves the live position when geolocation succeeds', async () => {
    const originalGeolocation = navigator.geolocation;
    const coords = { latitude: 10, longitude: 20 };
    Object.defineProperty(navigator, 'geolocation', {
      configurable: true,
      value: {
        getCurrentPosition: (success: PositionCallback) =>
          success({ coords } as GeolocationPosition),
      },
    });

    const location = await component.getUserLocation();

    expect(location).toEqual({ lat: 10, lng: 20 });

    Object.defineProperty(navigator, 'geolocation', { value: originalGeolocation, configurable: true });
  });

  it('resets navigation state on cancel', () => {
    component.showNavigation.set(true);
    component.directionsResults.set({ routes: [] } as unknown as google.maps.DirectionsResult);
    component.routeError.set('Could not calculate a walking route. Please try a different location.');
    component.originAutocompleteElement = {} as unknown as typeof component.originAutocompleteElement;

    component.cancelNavigation();

    expect(component.showNavigation()).toBe(false);
    expect(component.directionsResults()).toBeUndefined();
    expect(component.routeError()).toBe('');
    expect(component.originAutocompleteElement).toBeNull();
  });
});
