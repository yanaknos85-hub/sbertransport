import { renderHook } from '@testing-library/react-hooks';

describe('useTitle', () => {
  let originalTitle: string;

  beforeEach(() => {
    originalTitle = document.title;
  });

  afterEach(() => {
    document.title = originalTitle;
  });

  describe('when title is set', () => {
    it('should set document.title to the provided value', () => {
      renderHook(() => {
        const { useTitle } = require('./useTitle');
        useTitle('Test Title');
      });

      expect(document.title).toBe('Test Title');
    });

    it('should set document.title to another value', () => {
      renderHook(() => {
        const { useTitle } = require('./useTitle');
        useTitle('Another Title');
      });

      expect(document.title).toBe('Another Title');
    });

    it('should set document.title to empty string', () => {
      renderHook(() => {
        const { useTitle } = require('./useTitle');
        useTitle('');
      });

      expect(document.title).toBe('');
    });
  });

  describe('when deps array is provided', () => {
    it('should set document.title with deps parameter', () => {
      renderHook(() => {
        const { useTitle } = require('./useTitle');
        useTitle('Title with deps', [1, 2, 3]);
      });

      expect(document.title).toBe('Title with deps');
    });
  });
});
