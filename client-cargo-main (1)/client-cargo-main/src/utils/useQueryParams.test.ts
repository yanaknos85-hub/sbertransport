import { renderHook } from '@testing-library/react-hooks';

// Mock react-router-dom
const mockUseLocation = jest.fn();
jest.mock('react-router-dom', () => ({
  useLocation: mockUseLocation,
}));

// Mock qs
const mockQsParse = jest.fn();
jest.mock('qs', () => ({
  parse: mockQsParse,
}));

describe('useQueryParams', () => {
  beforeEach(() => {
    mockUseLocation.mockReset();
    mockQsParse.mockReset();
  });

  describe('when location.search is empty', () => {
    it('should return empty object', () => {
      mockUseLocation.mockReturnValue({ search: '' });
      mockQsParse.mockReturnValue({});

      const { result } = renderHook(() => {
        const { useQueryParams } = require('./useQueryParams');
        return useQueryParams();
      });

      expect(result.current).toEqual({});
      expect(mockQsParse).toHaveBeenCalledWith('', { ignoreQueryPrefix: true });
    });
  });

  describe('when location.search has query parameters', () => {
    it('should parse query parameters correctly', () => {
      mockUseLocation.mockReturnValue({ search: '?id=123&name=test' });
      mockQsParse.mockReturnValue({ id: '123', name: 'test' });

      const { result } = renderHook(() => {
        const { useQueryParams } = require('./useQueryParams');
        return useQueryParams();
      });

      expect(result.current).toEqual({ id: '123', name: 'test' });
      expect(mockQsParse).toHaveBeenCalledWith('?id=123&name=test', {
        ignoreQueryPrefix: true,
      });
    });
  });

  describe('when location.search has array parameters', () => {
    it('should parse array parameters correctly', () => {
      mockUseLocation.mockReturnValue({ search: '?ids=1&ids=2&ids=3' });
      mockQsParse.mockReturnValue({ ids: ['1', '2', '3'] });

      const { result } = renderHook(() => {
        const { useQueryParams } = require('./useQueryParams');
        return useQueryParams();
      });

      expect(result.current).toEqual({ ids: ['1', '2', '3'] });
      expect(mockQsParse).toHaveBeenCalledWith('?ids=1&ids=2&ids=3', {
        ignoreQueryPrefix: true,
      });
    });
  });

  describe('when location.search has complex parameters', () => {
    it('should parse nested objects correctly', () => {
      mockUseLocation.mockReturnValue({ search: '?filter[name]=test&filter[age]=25' });
      mockQsParse.mockReturnValue({ filter: { name: 'test', age: '25' } });

      const { result } = renderHook(() => {
        const { useQueryParams } = require('./useQueryParams');
        return useQueryParams();
      });

      expect(result.current).toEqual({ filter: { name: 'test', age: '25' } });
      expect(mockQsParse).toHaveBeenCalledWith('?filter[name]=test&filter[age]=25', {
        ignoreQueryPrefix: true,
      });
    });
  });
});
