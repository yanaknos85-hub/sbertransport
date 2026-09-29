import { removeIdFromUrl } from './removeIdFromUrl';
import type { UUID } from 'utils/io-ts';

describe('removeIdFromUrl', () => {
  describe('удаление UUID из конца URL', () => {
    test('должен удалить UUID в нижнем регистре из конца URL', () => {
      const url = '/api/cargo/550e8400-e29b-41d4-a716-446655440000' as UUID;
      const expected = '/api/cargo';
      expect(removeIdFromUrl(url)).toBe(expected);
    });

    test('должен удалить UUID в верхнем регистре из конца URL', () => {
      const url = '/api/cargo/550E8400-E29B-41D4-A716-446655440000' as UUID;
      const expected = '/api/cargo';
      expect(removeIdFromUrl(url)).toBe(expected);
    });

    test('должен удалить UUID в смешанном регистре из конца URL', () => {
      const url = '/api/cargo/550E8400-e29B-41D4-a716-446655440000' as UUID;
      const expected = '/api/cargo';
      expect(removeIdFromUrl(url)).toBe(expected);
    });

    test('должен удалить UUID с дефисами из конца URL', () => {
      const url = '/transport/123e4567-e89b-12d3-a456-426614174000' as UUID;
      const expected = '/transport';
      expect(removeIdFromUrl(url)).toBe(expected);
    });

    test('должен удалить короткий UUID (без префикса) из конца URL', () => {
      const url = '/page/a1b2c3d4-e5f6-7890-abcd-ef1234567890' as UUID;
      const expected = '/page';
      expect(removeIdFromUrl(url)).toBe(expected);
    });
  });

  describe('возврат исходной строки без изменений', () => {
    test('должен вернуть исходную строку, если UUID нет в конце', () => {
      const url = '/api/cargo/123' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/cargo/123');
    });

    test('должен вернуть исходную строку, если URL пустой', () => {
      const url = '' as UUID;
      expect(removeIdFromUrl(url)).toBe('');
    });

    test('должен вернуть исходную строку без слешей', () => {
      const url = 'some-string-without-slashes' as UUID;
      expect(removeIdFromUrl(url)).toBe('some-string-without-slashes');
    });

    test('должен вернуть исходную строку, если UUID в середине URL', () => {
      const url = '/api/550e8400-e29b-41d4-a716-446655440000/cargo' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/550e8400-e29b-41d4-a716-446655440000/cargo');
    });

    test('должен вернуть исходную строку, если UUID короче 36 символов', () => {
      const url = '/api/123e4567' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/123e4567');
    });

    test('должен вернуть исходную строку, если UUID длиннее 36 символов', () => {
      const url = '/api/123e4567-e89b-12d3-a456-426614174000-extra' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/123e4567-e89b-12d3-a456-426614174000-extra');
    });

    test('должен вернуть исходную строку, если после слеша не UUID', () => {
      const url = '/api/ Not-A-UUID' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/ Not-A-UUID');
    });
  });

  describe('edge cases', () => {
    test('должен удалить UUID, если URL состоит только из пути и UUID', () => {
      const url = '/550e8400-e29b-41d4-a716-446655440000' as UUID;
      expect(removeIdFromUrl(url)).toBe('');
    });

    test('должен сохранить query parameters после UUID', () => {
      const url = '/api/cargo/550e8400-e29b-41d4-a716-446655440000?param=value' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/cargo/550e8400-e29b-41d4-a716-446655440000?param=value');
    });

    test('должен удалить UUID после двойного слеша', () => {
      const url = '/api/cargo//550e8400-e29b-41d4-a716-446655440000' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/cargo/');
    });

    test('должен обработать UUID без дефисов (36 символов)', () => {
      const url = '/api/550e8400e29b41d4a716446655440000' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/550e8400e29b41d4a716446655440000');
    });

    test('должен обработать URL с UUID содержащим лишние символы в конце', () => {
      const url = '/api/cargo/550e8400-e29b-41d4-a716-446655440000/extra' as UUID;
      expect(removeIdFromUrl(url)).toBe('/api/cargo/550e8400-e29b-41d4-a716-446655440000/extra');
    });
  });
});
