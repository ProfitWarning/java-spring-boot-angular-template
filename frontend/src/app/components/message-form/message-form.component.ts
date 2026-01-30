import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessageService } from '../../services/message.service';
import { MessageSignals } from '../../signals/message.signals';
import { CreateMessageCommand } from '../../models/message.model';

@Component({
  selector: 'app-message-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './message-form.component.html',
  styleUrl: './message-form.component.css'
})
export class MessageFormComponent {
  content = signal<string>('');
  submitting = signal<boolean>(false);
  submitError = signal<string | null>(null);

  constructor(
    private messageService: MessageService,
    private messageSignals: MessageSignals
  ) {}

  onSubmit(): void {
    const contentValue = this.content().trim();
    
    if (!contentValue) {
      this.submitError.set('Message content cannot be empty');
      return;
    }

    this.submitting.set(true);
    this.submitError.set(null);

    const command: CreateMessageCommand = {
      content: contentValue
    };

    this.messageService.createMessage(command).subscribe({
      next: (message) => {
        this.messageSignals.addMessage(message);
        this.content.set('');
        this.submitting.set(false);
      },
      error: (error) => {
        this.submitError.set(error.message);
        this.submitting.set(false);
      }
    });
  }
}
