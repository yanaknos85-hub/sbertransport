import { renderHook } from '@testing-library/react-hooks';

// Mock shared/hooks/useEmpContext
jest.mock('shared/hooks/useEmpContext', () => ({
  useAppStoreContext: jest.fn(),
}));

// Mock ./Misc
const mockJwtDecode = jest.fn();
jest.mock('./Misc', () => ({
  jwtDecode: mockJwtDecode,
}));

// Mock document.cookie
let mockCookie = '';
Object.defineProperty(document, 'cookie', {
  get: () => mockCookie,
  set: (value: string) => {
    mockCookie = value;
  },
});

describe('useTokenCookie', () => {
  beforeEach(() => {
    mockCookie = '';
  });

  describe('useToken', () => {
    it('should return token from authStore when it exists', () => {
      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: 'mocked-token',
        },
      });

      const { result } = renderHook(() => {
        const useToken = require('./useTokenCookie').default;
        return useToken();
      });

      expect(result.current).toBe('mocked-token');
    });

    it('should return null when token does not exist', () => {
      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: null,
        },
      });

      const { result } = renderHook(() => {
        const useToken = require('./useTokenCookie').default;
        return useToken();
      });

      expect(result.current).toBeNull();
    });

    it('should return undefined when authStore does not have token property', () => {
      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {},
      });

      const { result } = renderHook(() => {
        const useToken = require('./useTokenCookie').default;
        return useToken();
      });

      expect(result.current).toBeUndefined();
    });
  });

  describe('useTokenCookieExpiration', () => {
    const mockToken
      = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAwMzYwMH0.mock-signature';

    beforeEach(() => {
      mockJwtDecode.mockReset();
    });

    it('should return expireTime for isSigma = true', () => {
      // Token with iat=1700000000, exp=1700003600 => expireTime = 3600 - 30 = 3570
      mockJwtDecode.mockReturnValue({ iat: 1700000000, exp: 1700003600 });

      const { result } = renderHook(() => {
        const { useTokenCookieExpiration } = require('./useTokenCookie');
        return useTokenCookieExpiration(mockToken, true);
      });

      expect(result.current).toBe(3570);
      expect(mockJwtDecode).toHaveBeenCalledWith(mockToken);
    });

    it('should return expireTime for isSigma = false', () => {
      mockJwtDecode.mockReturnValue({ iat: 1700000000, exp: 1700003600 });

      const { result } = renderHook(() => {
        const { useTokenCookieExpiration } = require('./useTokenCookie');
        return useTokenCookieExpiration(mockToken, false);
      });

      expect(result.current).toBe(3570);
    });

    it('should set document.cookie when isSigma = true', () => {
      mockJwtDecode.mockReturnValue({ iat: 1700000000, exp: 1700003600 });

      renderHook(() => {
        const { useTokenCookieExpiration } = require('./useTokenCookie');
        useTokenCookieExpiration(mockToken, true);
      });

      expect(mockCookie).toContain('token=');
      expect(mockCookie).toContain('expires=');
      expect(mockCookie).toContain('path=/');
    });

    it('should not set document.cookie when isSigma = false', () => {
      mockJwtDecode.mockReturnValue({ iat: 1700000000, exp: 1700003600 });

      renderHook(() => {
        const { useTokenCookieExpiration } = require('./useTokenCookie');
        useTokenCookieExpiration(mockToken, false);
      });

      expect(mockCookie).toBe('');
    });
  });
});
