import { Injectable, signal } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({ providedIn: 'root' })
export class ChatService {

  private client!: Client;
  private subscription!: StompSubscription;

  // Signals instead of BehaviorSubject — works with zoneless Angular
  messages = signal<ChatMessage[]>([]);
  connected = signal<boolean>(false);

  connect(username: string): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,

      onConnect: () => {
        this.connected.set(true);

        this.subscription = this.client.subscribe(
          '/topic/public',
          (stompMessage) => {
            const message: ChatMessage = JSON.parse(stompMessage.body);
            this.messages.update(msgs => [...msgs, message]);
          }
        );

        this.client.publish({
          destination: '/app/chat.addUser',
          body: JSON.stringify({
            sender: username,
            type: 'JOIN',
            content: '',
            timestamp: ''
          })
        });
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connected.set(false);
      },

      onDisconnect: () => {
        this.connected.set(false);
      }
    });

    this.client.activate();
  }

  sendMessage(sender: string, content: string): void {
    if (!this.client || !this.client.connected) {
      console.warn('STOMP client not connected');
      return;
    }

    this.client.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        sender,
        content,
        type: 'CHAT',
        timestamp: ''
      })
    });
  }

  disconnect(): void {
    if (this.subscription) this.subscription.unsubscribe();
    if (this.client) this.client.deactivate();
    this.connected.set(false);
    this.messages.set([]);
  }
}