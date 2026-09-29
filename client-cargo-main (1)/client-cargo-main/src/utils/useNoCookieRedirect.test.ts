import { renderHook } from '@testing-library/react-hooks';

// Mock @sber-sbertransport/mf-core
jest.mock('@sber-sbertransport/mf-core', () => ({
  useHistory: jest.fn(),
}));

// Mock universal-cookie
const mockCookiesGet = jest.fn();
jest.mock('universal-cookie', () => {
  return class Cookies {
    constructor() {
      (this as any).get = mockCookiesGet;
    }
  };
});

describe('useNoCookieRedirect', () => {
  let mockHistoryPush: jest.Mock;

  beforeEach(() => {
    mockHistoryPush = jest.fn();
    mockCookiesGet.mockReset();

    (require('@sber-sbertransport/mf-core').useHistory as jest.Mock).mockReturnValue({
      push: mockHistoryPush,
    });
  });

  describe('when token cookie exists', () => {
    it('should not redirect to /', () => {
      mockCookiesGet.mockReturnValue('mocked-token-value');

      renderHook(() => {
        const useNoCookieRedirect = require('./useNoCookieRedirect').default;
        useNoCookieRedirect('token');
      });

      expect(mockCookiesGet).toHaveBeenCalledWith('token');
      expect(mockHistoryPush).not.toHaveBeenCalled();
    });
  });

  describe('when token cookie does not exist', () => {
    it('should redirect to /', () => {
      mockCookiesGet.mockReturnValue(undefined);

      renderHook(() => {
        const useNoCookieRedirect = require('./useNoCookieRedirect').default;
        useNoCookieRedirect('token');
      });

      expect(mockCookiesGet).toHaveBeenCalledWith('token');
      expect(mockHistoryPush).toHaveBeenCalledWith('/');
    });

    it('should redirect to / when cookie value is null', () => {
      mockCookiesGet.mockReturnValue(null);

      renderHook(() => {
        const useNoCookieRedirect = require('./useNoCookieRedirect').default;
        useNoCookieRedirect('token');
      });

      expect(mockCookiesGet).toHaveBeenCalledWith('token');
      expect(mockHistoryPush).toHaveBeenCalledWith('/');
    });
  });

  describe('with different cookie names', () => {
    it('should check for the specified cookie name', () => {
      mockCookiesGet.mockReturnValue(undefined);

      renderHook(() => {
        const useNoCookieRedirect = require('./useNoCookieRedirect').default;
        useNoCookieRedirect('auth-token');
      });

      expect(mockCookiesGet).toHaveBeenCalledWith('auth-token');
      expect(mockHistoryPush).toHaveBeenCalledWith('/');
    });
  });
});
