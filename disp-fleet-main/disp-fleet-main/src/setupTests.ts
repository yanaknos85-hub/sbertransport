import '@testing-library/jest-dom';

// Мок для WebSocket, так как jsdom не поддерживает его полностью
global.WebSocket = class WebSocket {
  readyState = 0;
  onopen: (() => void) | null = null;
  onclose: (() => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onerror: (() => void) | null = null;

  constructor(_url: string) {
    // Mock implementation
  }

  send(_data: string | ArrayBuffer | Blob | ArrayBufferView) {
    // Mock implementation
  }

  close(_code?: number, _reason?: string) {
    // Mock implementation
  }
} as unknown as typeof WebSocket;
