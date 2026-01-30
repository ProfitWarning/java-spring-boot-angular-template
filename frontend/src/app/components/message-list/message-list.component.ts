import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService } from '../../services/message.service';
import { MessageSignals } from '../../signals/message.signals';
import { Message } from '../../models/message.model';

@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.css'
})
export class MessageListComponent implements OnInit {
  constructor(
    private messageService: MessageService,
    public messageSignals: MessageSignals
  ) {}

  ngOnInit(): void {
    this.loadMessages();
  }

  loadMessages(): void {
    this.messageSignals.setLoading(true);
    this.messageSignals.setError(null);

    this.messageService.getMessages().subscribe({
      next: (messages) => {
        this.messageSignals.setMessages(messages);
        this.messageSignals.setLoading(false);
      },
      error: (error) => {
        this.messageSignals.setError(error.message);
        this.messageSignals.setLoading(false);
      }
    });
  }

  selectMessage(message: Message): void {
    this.messageSignals.selectMessage(message);
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString();
  }
}
