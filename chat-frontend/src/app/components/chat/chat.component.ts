import { Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnDestroy {

  private chatService = inject(ChatService);

  username: string = '';
  messageInput: string = '';
  usernameSubmitted: boolean = false;

  messages = this.chatService.messages;
  connected = this.chatService.connected;

  joinChat(): void {
    if (!this.username.trim()) return;
    this.usernameSubmitted = true;
    this.chatService.connect(this.username.trim());
  }

  sendMessage(): void {
    if (!this.messageInput.trim() || !this.connected()) return;
    this.chatService.sendMessage(this.username, this.messageInput.trim());
    this.messageInput = '';
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') this.sendMessage();
  }

  leaveChat(): void {
    this.chatService.disconnect();
    this.usernameSubmitted = false;
    this.username = '';
  }

  ngOnDestroy(): void {
    this.chatService.disconnect();
  }
}