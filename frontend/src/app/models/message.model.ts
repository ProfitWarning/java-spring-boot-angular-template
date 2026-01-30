export interface Message {
  id: number;
  content: string;
  createdAt: Date;
}

export interface CreateMessageCommand {
  content: string;
}

export interface MessageResponse {
  id: number;
  content: string;
  createdAt: string;
}
