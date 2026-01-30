import { Injectable, signal, computed } from '@angular/core';
import { Message } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class MessageSignals {
  // State signals
  private _messages = signal<Message[]>([]);
  private _loading = signal<boolean>(false);
  private _error = signal<string | null>(null);
  private _selectedMessage = signal<Message | null>(null);

  // Read-only computed signals
  readonly messages = this._messages.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly selectedMessage = this._selectedMessage.asReadonly();

  // Computed signals
  readonly messageCount = computed(() => this._messages().length);
  readonly hasMessages = computed(() => this._messages().length > 0);
  readonly hasError = computed(() => this._error() !== null);

  // Update methods
  setMessages(messages: Message[]): void {
    this._messages.set(messages);
  }

  addMessage(message: Message): void {
    this._messages.update(messages => [...messages, message]);
  }

  setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  setError(error: string | null): void {
    this._error.set(error);
  }

  selectMessage(message: Message | null): void {
    this._selectedMessage.set(message);
  }

  clearMessages(): void {
    this._messages.set([]);
  }
}
