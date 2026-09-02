import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay, withHttpTransferCacheOptions } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';

import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { provideTranslateService } from '@ngx-translate/core';
import { languageInterceptor } from './interceptors/language.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(
      withEventReplay(),
      withHttpTransferCacheOptions({
        filter: (req) => req.url.startsWith('/assets/'),
      }),
    ),
    provideHttpClient(withFetch(), withInterceptors([languageInterceptor, errorInterceptor])),
    provideTranslateService({
      fallbackLang: 'en',
      loader: provideTranslateHttpLoader({
        prefix: '/assets/',
        suffix: '.json'
      })
    })
  ]
};
