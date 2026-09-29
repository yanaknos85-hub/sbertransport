import { getFilenameFromHeader } from './downloads';

// Мок content-disposition
jest.mock('content-disposition', () => ({
  parse: jest.fn((str: string) => {
    // Парсим строку вручную для тестов
    const params: Record<string, string> = {};
    str.split(';').forEach(part => {
      const [key, value] = part.trim().split('=');
      if (key && value) {
        params[key] = value.replace(/"/g, '');
      }
    });
    return { parameters: params };
  }),
}));

describe('downloads', () => {
  describe('getFilenameFromHeader', () => {
    test('должен извлекать имя файла из content-disposition header', () => {
      const response = {
        headers: {
          'content-disposition': 'attachment; filename="report.pdf"',
        },
      } as any;

      const filename = getFilenameFromHeader(response);
      expect(filename).toBe('report.pdf');
    });

    test('должен возвращать пустую строку если нет content-disposition header', () => {
      const response = {
        headers: {},
      } as any;

      const filename = getFilenameFromHeader(response);
      expect(filename).toBe('');
    });

    test('должен возвращать пустую строку если content-disposition header пустой', () => {
      const response = {
        headers: {
          'content-disposition': '',
        },
      } as any;

      const filename = getFilenameFromHeader(response);
      expect(filename).toBe('');
    });

    test('должен извлекать имя файла с кавычками', () => {
      const response = {
        headers: {
          'content-disposition': "attachment; filename=\"файл с пробелами.docx\"",
        },
      } as any;

      const filename = getFilenameFromHeader(response);
      expect(filename).toBe('файл с пробелами.docx');
    });

    test('должен извлекать имя файла без кавычек', () => {
      const response = {
        headers: {
          'content-disposition': 'attachment; filename=report.xlsx',
        },
      } as any;

      const filename = getFilenameFromHeader(response);
      expect(filename).toBe('report.xlsx');
    });

    test('должен извлекать имя файла с расширением', () => {
      const response = {
        headers: {
          'content-disposition': 'attachment; filename="data_2024_05_08.csv"',
        },
      } as any;

      const filename = getFilenameFromHeader(response);
      expect(filename).toBe('data_2024_05_08.csv');
    });
  });
});
