import '@testing-library/jest-dom';
import 'reflect-metadata';

// Подавляем предупреждения о React.createElement для TButton
const originalError = console.error;
console.error = (...args: any[]) => {
  if (args[0]?.includes?.('React.createElement: type is invalid') && args[2]?.includes?.('ApprovalReasonModal')) {
    return;
  }
  if (args[0]?.includes?.('React does not recognize the')) {
    return;
  }
  originalError(...args);
};
