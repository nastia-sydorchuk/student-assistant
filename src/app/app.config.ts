import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { HttpClient, provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';

import { provideTranslateHttpLoader, TranslateHttpLoader } from '@ngx-translate/http-loader';
import { provideTranslateService, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { languageInterceptor } from './interceptors/language.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([languageInterceptor])),
    provideTranslateService({
      fallbackLang: 'en',
      loader: provideTranslateHttpLoader({
        prefix: '/assets/',
        suffix: '.json'
      })
    })
  ]
};
