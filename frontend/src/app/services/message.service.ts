import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map, retry } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Message, CreateMessageCommand, MessageResponse } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private apiUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  getMessages(): Observable<Message[]> {
    return this.http.get<MessageResponse[]>(this.apiUrl).pipe(
      map(responses => responses.map(this.mapResponseToMessage)),
      retry(2),
      catchError(this.handleError)
    );
  }

  getMessageById(id: number): Observable<Message> {
    return this.http.get<MessageResponse>(`${this.apiUrl}/${id}`).pipe(
      map(this.mapResponseToMessage),
      retry(2),
      catchError(this.handleError)
    );
  }

  createMessage(command: CreateMessageCommand): Observable<Message> {
    return this.http.post<MessageResponse>(this.apiUrl, command).pipe(
      map(this.mapResponseToMessage),
      catchError(this.handleError)
    );
  }

  private mapResponseToMessage(response: MessageResponse): Message {
    return {
      id: response.id,
      content: response.content,
      createdAt: new Date(response.createdAt)
    };
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Server returned code ${error.status}, body was: ${error.error}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
