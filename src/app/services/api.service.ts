import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { InductionEvent, LevelData, Step } from '../models/welcome.model';
import { ResourceCategory } from '../models/resources.model';
import { ChatRequest, ChatResponse } from '../models/chat.model';
import { PortalCard } from '../models/portal-card.model';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private http = inject(HttpClient);

  getOnboardingSteps(): Observable<Step[]> {
    return this.http.get<Step[]>('/api/onboarding/steps');
  }

  getInductionEvents(): Observable<InductionEvent[]> {
    return this.http.get<InductionEvent[]>('/api/onboarding/induction-events');
  }

  getResources(category: string): Observable<ResourceCategory> {
    return this.http.get<ResourceCategory>(`/api/resources/${category}`);
  }

  getOnboardingConfigByLevel(level: string): Observable<LevelData> {
    return this.http.get<LevelData>(`/api/onboarding/${level}`);
  }

  sendMessage(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>('/api/gemini/chat', request);
  }

  getPortalCards(): Observable<PortalCard[]> {
    return this.http.get<PortalCard[]>('/api/portal-cards');
  }
}
