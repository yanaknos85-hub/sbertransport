import { throwAxiosErrorMessage } from './throwAxiosErrorMessage';

describe('throwAxiosErrorMessage', () => {
  describe('успешный сценарий - строка с сообщением об ошибке', () => {
    test('для ошибки с response.message "Something went wrong" должен выбросить ошибку с сообщением "Something went wrong"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Something went wrong' }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('Something went wrong');
    });

    test('для ошибки с response.message "Validation failed" должен выбросить ошибку с сообщением "Validation failed"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Validation failed' }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('Validation failed');
    });

    test('для ошибки с response.message пустая строка должен выбросить ошибку с пустым сообщением', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: '' }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('');
    });
  });

  describe('сценарий JSON с вложенным объектом message', () => {
    test('для ошибки с response.message "Error in field" должен выбросить ошибку с сообщением "Error in field"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Error in field' }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('Error in field');
    });
  });

  describe('объект message вместо строки', () => {
    test('для ошибки с response.message как число 123 должен выбросить ошибку с "123"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 123 }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('123');
    });

    test('для ошибки с response.message как объект должен выбросить ошибку с "[object Object]"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: { nested: 'object' } }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('[object Object]');
    });
  });

  describe('некорректный JSON в response', () => {
    test('для ошибки с некорректным JSON должен выбросить ошибку парсинга', () => {
      const error = {
        request: {
          response: 'invalid json string',
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow();
    });

    test('для ошибки с пустым response должен выбросить ошибку парсинга', () => {
      const error = {
        request: {
          response: '',
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow();
    });

    test('для ошибки с response как null должен выбросить ошибку парсинга', () => {
      const error = {
        request: {
          response: null,
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow();
    });
  });

  describe('корректная структура AxiosError', () => {
    test('для ошибки с isAxiosError: true и response.message должен выбросить ошибку', () => {
      const error = {
        isAxiosError: true,
        request: {
          response: JSON.stringify({ message: 'Axios error occurred' }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('Axios error occurred');
    });

    test('для ошибки с response.status 500 и response.message должен выбросить ошибку', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Internal Server Error' }),
          status: 500,
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('Internal Server Error');
    });

    test('для ошибки с response.status 400 и response.message должен выбросить ошибку', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Bad Request' }),
          status: 400,
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('Bad Request');
    });
  });

  describe('проверка типа выбрасываемой ошибки', () => {
    test('должен выбрасывать Error (не AxiosError)', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Test error' }),
        },
      } as any;

      try {
        throwAxiosErrorMessage(error);
      } catch (e) {
        expect(e).toBeInstanceOf(Error);
        expect((e as Error).message).toBe('Test error');
      }
    });

    test('сообщение ошибки должно совпадать с response.message', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: 'Custom message from server' }),
        },
      } as any;

      try {
        throwAxiosErrorMessage(error);
      } catch (e) {
        expect((e as Error).message).toBe('Custom message from server');
      }
    });
  });

  describe('пограничные случаи', () => {
    test('для ошибки с response.message как true должен выбросить ошибку с "true"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: true }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('true');
    });

    test('для ошибки с response.message как false должен выбросить ошибку с "false"', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: false }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('false');
    });

    test('для ошибки с response.message как undefined должен выбросить ошибку с пустым сообщением', () => {
      const error = {
        request: {
          response: JSON.stringify({ message: undefined }),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('');
    });

    test('для ошибки без response.message должен выбросить ошибку с пустым сообщением', () => {
      const error = {
        request: {
          response: JSON.stringify({}),
        },
      } as any;

      expect(() => throwAxiosErrorMessage(error)).toThrow('');
    });
  });
});
