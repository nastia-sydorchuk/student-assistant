import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InductionEvent, LevelData, Step, WelcomePage } from '../models/welcome.model';
import { ResourceCategory } from '../models/resources.model';
import { CampusLife } from '../models/campus-life.model';
import { ChatRequest } from '../models/chat.model';
import { PortalCard } from '../models/portal-card.model';

@Injectable({
  providedIn: 'root',
})
export class Api {
  private apiUrl = 'http://localhost:8080/api/event';

  constructor(private http: HttpClient) {}

  getOnboardingSteps(languageCode: string): Observable<Step[]> {
    const headers = new HttpHeaders().set('Accept-Language', languageCode);
    return this.http.get<Step[]>('/api/onboarding/steps', { headers });
  }

  getInductionEvents(languageCode: string): Observable<InductionEvent[]> {
    const headers = new HttpHeaders().set('Accept-Language', languageCode);
    return this.http.get<InductionEvent[]>('/api/onboarding/induction-events', { headers });
  }

  getResources(category: string, languageCode: string): Observable<ResourceCategory> {
    const headers = new HttpHeaders().set('Accept-Language', languageCode);
    return this.http.get<ResourceCategory>(`/api/resources/${category}`, { headers });
  }

  getOnboardingConfigByLevel(level: string, languageCode: string): Observable<LevelData> {
    const headers = new HttpHeaders().set('Accept-Language', languageCode);
    return this.http.get<LevelData>(`/api/onboarding/${level}`, { headers });
  }

  sendMessage(request: ChatRequest): Observable<any> {
    return this.http.post<ChatRequest>('http://localhost:8080/api/gemini/chat', request);
  }

  getPortalCards(languageCode: string): Observable<PortalCard[]> {
    const headers = new HttpHeaders().set('Accept-Language', languageCode);
    return this.http.get<PortalCard[]>('/api/portal-cards', { headers });
  }
}
