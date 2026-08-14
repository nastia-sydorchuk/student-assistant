import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { LanguageService } from '../services/language.service';

export const languageInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith('/api/')) {
    return next(req);
  }

  const languageService = inject(LanguageService);

  return next(req.clone({
    setHeaders: { 'Accept-Language': languageService.getCurrentLanguage() }
  }));
};
