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

describe('useRole', () => {
  beforeEach(() => {
    mockJwtDecode.mockReset();
  });

  describe('when token exists', () => {
    it('should return roles from decoded token', () => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJhZG1pbiIsInVzZXIiXX0.signature'; // Рандом, никак не используется
      mockJwtDecode.mockReturnValue({ roles: ['admin', 'user'] });

      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: mockToken,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toEqual(['admin', 'user']);
      expect(mockJwtDecode).toHaveBeenCalledWith(mockToken);
    });

    it('should return undefined when token.roles is undefined', () => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6dW5kZWZpbmVkfQ.signature';
      mockJwtDecode.mockReturnValue({ roles: undefined });

      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: mockToken,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toBeUndefined();
    });

    it('should handle case when jwtDecode returns object without roles property', () => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoibW9jayJ9.signature';
      mockJwtDecode.mockReturnValue({ user: 'mock' });

      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: mockToken,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toBeUndefined();
    });
  });

  describe('when token does not exist', () => {
    it('should return empty array when token is null', () => {
      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: null,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toEqual([]);
      expect(mockJwtDecode).not.toHaveBeenCalled();
    });

    it('should return empty array when token is undefined', () => {
      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {},
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toEqual([]);
      expect(mockJwtDecode).not.toHaveBeenCalled();
    });
  });

  describe('with different role combinations', () => {
    it('should handle single role', () => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJhZG1pbiJdXX0.signature';
      mockJwtDecode.mockReturnValue({ roles: ['admin'] });

      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: mockToken,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toEqual(['admin']);
    });

    it('should handle empty roles array', () => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6W119.signature';
      mockJwtDecode.mockReturnValue({ roles: [] });

      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: mockToken,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toEqual([]);
    });

    it('should handle many roles', () => {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJhIiwiYiIsImMiLCJkIiwiZSJdfQ.signature';
      mockJwtDecode.mockReturnValue({ roles: ['a', 'b', 'c', 'd', 'e'] });

      (require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock).mockReturnValue({
        authStore: {
          token: mockToken,
        },
      });

      const { result } = renderHook(() => {
        const { useRole } = require('./useRole');
        return useRole();
      });

      expect(result.current).toEqual(['a', 'b', 'c', 'd', 'e']);
    });
  });
});
