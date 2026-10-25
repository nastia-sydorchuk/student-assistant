import { Component, ElementRef, ViewChild, PLATFORM_ID, SimpleChanges, Input, OnChanges, OnInit, inject, signal } from '@angular/core';
import { GoogleMapsModule, GoogleMap, MapDirectionsRenderer, MapDirectionsService } from '@angular/google-maps';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';
import { TranslateModule } from '@ngx-translate/core';
import { LoggerService } from '../../services/logger.service';

declare global {
  interface Window {
    __onGoogleMapsLoaded?: () => void;
  }
}

@Component({
  selector: 'app-map',
  imports: [GoogleMapsModule, MapDirectionsRenderer, CommonModule, TranslateModule],
  templateUrl: './map.html',
  styleUrl: './map.css',
})
export class Map implements OnInit, OnChanges {
  @ViewChild('searchContainer') searchContainer!: ElementRef;
  @ViewChild('originContainer') originContainer?: ElementRef;
  @ViewChild(GoogleMap) mapComponent!: GoogleMap;

  @Input() targetLocation: string | null = null;

  private platformId = inject(PLATFORM_ID);
  private directionsService = inject(MapDirectionsService);
  private logger = inject(LoggerService);

  apiLoaded = signal(false);
  center = signal<google.maps.LatLngLiteral>({ lat: 55.9097, lng: -3.3203 }); // HW
  zoom = 15;

  mapId = environment.GOOGLE_MAPS_MAP_ID;
  activeMarker: google.maps.marker.AdvancedMarkerElement | null = null;

  directionsResults = signal<google.maps.DirectionsResult | undefined>(undefined);
  routeError = signal('');
  originAutocompleteElement: google.maps.places.PlaceAutocompleteElement | null = null;
  showNavigation = signal(false);
  currentDestinationCoords = signal<google.maps.LatLngLiteral | null>(null);
  fallbackOrigin: google.maps.LatLngLiteral = { lat: 55.9125, lng: -3.3214 };

  autocompleteElement: google.maps.places.PlaceAutocompleteElement | null = null;

  mapOptions: google.maps.MapOptions = {
    mapTypeControl: false,
    streetViewControl: false,
    fullscreenControl: false,
    zoomControl: true
  };

  async ngOnInit(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      try {
        await this.loadGoogleMapsApi();

        await google.maps.importLibrary('maps');
        await google.maps.importLibrary('places');
        await google.maps.importLibrary('marker');
        await google.maps.importLibrary('routes');

        this.apiLoaded.set(true);

        setTimeout(() => this.initAutocomplete(), 0);
      } catch (error) {
        this.logger.error("Error loading Google Maps", error);
      }
      } else {
        this.logger.log('Skipping Google Maps load during Server-Side Rendering.');
      }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['targetLocation'] && this.targetLocation) {
      this.searchAndDisplayLocation(this.targetLocation);
    }
  }

  loadGoogleMapsApi(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (typeof google !== 'undefined' && typeof google.maps?.importLibrary === 'function') {
        resolve();
        return;
      }

      window.__onGoogleMapsLoaded = () => resolve();

      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${environment.GOOGLE_MAPS_API_KEY}&v=quarterly&language=en&region=GB&loading=async&callback=__onGoogleMapsLoaded`;
      script.async = true;
      script.defer = true;

      script.onerror = (error) => reject(error);

      document.head.appendChild(script);
    });
  }

  private async processSelectedPlace(place: google.maps.places.Place) {
    if (!place.location) {
      this.logger.warn("No location data available for:", place.displayName);
      return;
    }

    const newPos = { lat: place.location.lat(), lng: place.location.lng() };
    this.currentDestinationCoords.set(newPos);

    if (this.showNavigation()) {
      this.startNavigation();
    } else {
      this.center.set(newPos);
      if (this.mapComponent && this.mapComponent.googleMap) {
        this.mapComponent.googleMap.panTo(newPos);
        this.mapComponent.googleMap.setZoom(19);
      }
    }

    if (this.activeMarker) {
      this.activeMarker.map = null;
    }
    const markerLib = await google.maps.importLibrary("marker") as google.maps.MarkerLibrary;
    this.activeMarker = new markerLib.AdvancedMarkerElement({
      map: this.mapComponent.googleMap,
      position: newPos,
      title: place.displayName
    });

    this.logger.log(`Successfully mapped: ${place.displayName}`);
  }

  async initAutocomplete() {
    await google.maps.importLibrary("places");
    const autocompleteElement = new google.maps.places.PlaceAutocompleteElement({
      locationRestriction: {
        north: 55.916,
        south: 55.904,
        east: -3.308,
        west: -3.335
      },
      includedRegionCodes: ['gb'],
      includedPrimaryTypes: ['establishment', 'premise']
    });
    this.autocompleteElement = autocompleteElement;

    this.searchContainer.nativeElement.appendChild(autocompleteElement);

    autocompleteElement.addEventListener('gmp-select', async (event) => {
      const place = event.placePrediction.toPlace();

      await place.fetchFields({ fields: ['displayName', 'location', 'viewport', 'types', 'primaryType'] });
      await this.processSelectedPlace(place);
    });
  }

  async searchAndDisplayLocation(searchQuery: string) {
    const { Place } = await google.maps.importLibrary("places") as google.maps.PlacesLibrary;

    if (this.autocompleteElement) {
      this.autocompleteElement.value = searchQuery;
    }

    const request = {
      textQuery: searchQuery,
      fields: ['displayName', 'location', 'viewport'],
      locationBias: { north: 55.916, south: 55.904, east: -3.308, west: -3.335 }
    };

    try {
      const { places } = await Place.searchByText(request);

      if (places && places.length > 0) {
        await this.processSelectedPlace(places[0]);
      } else {
        this.logger.warn(`Google could not find a match for: ${searchQuery}`);
      }
    } catch (error) {
      this.logger.error("Programmatic search failed", error);
    }
  }

  calculateRoute(origin: google.maps.LatLngLiteral, destination: google.maps.LatLngLiteral) {
    const request: google.maps.DirectionsRequest = {
      origin: origin,
      destination: destination,
      travelMode: google.maps.TravelMode.WALKING
    };

    this.routeError.set('');

    this.directionsService.route(request).subscribe({
      next: (response) => {
        if (response.result) {
          this.logger.log("Route calculated successfully!");
          this.directionsResults.set(response.result);
        } else {
          this.logger.warn("Google returned an empty route.");
          this.routeError.set('Could not calculate a walking route. Please try a different location.');
        }
      },
      error: (err) => {
        this.logger.error("Directions API failed:", err);
        this.routeError.set('Could not calculate a walking route. Please try a different location.');
      }
    });
  }

  async startNavigation() {
    if (!this.currentDestinationCoords()) return;

    this.showNavigation.set(true);

    setTimeout(async () => {
      await this.initOriginAutocomplete();
      const liveLocation = await this.getUserLocation();

      if (this.originAutocompleteElement) {
        this.originAutocompleteElement.value = "Your Current Location";
      }

      this.calculateRoute(liveLocation, this.currentDestinationCoords()!);
    }, 50);
  }

  cancelNavigation() {
    this.showNavigation.set(false);
    this.directionsResults.set(undefined);
    this.routeError.set('');
    this.originAutocompleteElement = null;

    const destination = this.currentDestinationCoords();
    if (destination && this.mapComponent) {
       this.mapComponent.googleMap?.panTo(destination);
       this.mapComponent.googleMap?.setZoom(19);
    }
  }

  async initOriginAutocomplete() {
    if (!this.originContainer) return;

    const originAutocompleteElement = new google.maps.places.PlaceAutocompleteElement({
      locationRestriction: {
        north: 55.916,
        south: 55.904,
        east: -3.308,
        west: -3.335
      },
      includedRegionCodes: ['gb'],
      includedPrimaryTypes: ['establishment', 'premise']
    });
    this.originAutocompleteElement = originAutocompleteElement;
    this.originContainer.nativeElement.appendChild(originAutocompleteElement);

    originAutocompleteElement.addEventListener('gmp-select', async (event) => {
      const place = event.placePrediction.toPlace();
      await place.fetchFields({ fields: ['location'] });

      const destination = this.currentDestinationCoords();
      if (place.location && destination) {
        const customOrigin = { lat: place.location.lat(), lng: place.location.lng() };
        this.calculateRoute(customOrigin, destination);
      }
    });
  }

  getUserLocation(): Promise<google.maps.LatLngLiteral> {
    return new Promise((resolve) => {
      if (typeof navigator !== 'undefined' && navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => resolve({ lat: position.coords.latitude, lng: position.coords.longitude }),
          () => resolve(this.fallbackOrigin),
          { enableHighAccuracy: true, timeout: 5000 }
        );
      } else {
        resolve(this.fallbackOrigin);
      }
    });
  }
}
