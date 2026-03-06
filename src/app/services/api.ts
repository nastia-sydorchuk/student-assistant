import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InductionEvent, LevelData, WelcomePage } from '../models/welcome.model';
import { ResourceCategory } from '../models/resources.model';
import { CampusLife } from '../models/campus-life.model';
import { ChatRequest } from '../models/chat.model';

@Injectable({
  providedIn: 'root',
})
export class Api {
  private apiUrl = 'http://localhost:8080/api/event';

  constructor(private http: HttpClient) {}

  getWelcome(): Observable<WelcomePage> {
    return this.http.get<WelcomePage>('/api/pages/welcome');
  }

  getInductionEvents(): Observable<InductionEvent[]> {
    return this.http.get<InductionEvent[]>('/api/pages/welcome/induction-events');
  }

  getResources(): Observable<ResourceCategory[]> {
    return this.http.get<ResourceCategory[]>('/api/resources');
  }

  getFacilities(): Observable<CampusLife[]> {
    return this.http.get<CampusLife[]>('/api/campus-life');
  }

  getWelcomeByLevel(level: string): Observable<LevelData> {
    return this.http.get<LevelData>(`/api/pages/welcome/${level}`);
  }

  sendMessage(request: ChatRequest): Observable<any> {
    return this.http.post<ChatRequest>('http://localhost:8080/api/gemini/chat', request);
  }

  getGreeting(request: ChatRequest): Observable<any> {
    return this.http.post<ChatRequest>('http://localhost:8080/api/gemini/greeting', request);
  }
}
