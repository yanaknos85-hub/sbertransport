import { renderHook } from '@testing-library/react-hooks';
import { useLocation } from 'react-router-dom';
import { useCargoRoute } from './useCargoRoute';

jest.mock('react-router-dom', () => ({
  useLocation: jest.fn(),
}));

const mockUseLocation = useLocation as jest.Mock;

describe('useCargoRoute', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('returns all false for an empty pathname', () => {
    mockUseLocation.mockReturnValue({ pathname: '', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current).toEqual({
      isCargoOrderExecution: false,
      isCargoOrders: false,
      isCargoMultiLogistics: false,
      isCargoRoutes: false,
      isCargoAny: false,
      isEtrnTab: false,
    });
  });

  it('returns all false for an unrelated path', () => {
    mockUseLocation.mockReturnValue({ pathname: '/client/home', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoAny).toBe(false);
    expect(result.current.isCargoOrderExecution).toBe(false);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('detects order-execution/cargo page', () => {
    mockUseLocation.mockReturnValue({ pathname: '/client/order-execution/cargo', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoOrderExecution).toBe(true);
    expect(result.current.isCargoAny).toBe(true);
    expect(result.current.isCargoOrders).toBe(false);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('excludes order-execution/cargo/template from cargoOrderExecution', () => {
    mockUseLocation.mockReturnValue({ pathname: '/client/order-execution/cargo/template', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoOrderExecution).toBe(false);
    expect(result.current.isCargoAny).toBe(false);
  });

  it('detects reports/registry/cargo/orders page', () => {
    mockUseLocation.mockReturnValue({ pathname: '/client/reports/registry/cargo/orders', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoOrders).toBe(true);
    expect(result.current.isCargoAny).toBe(true);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('detects reports/registry/cargo/routes page', () => {
    mockUseLocation.mockReturnValue({ pathname: '/client/reports/registry/cargo/routes', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoRoutes).toBe(true);
    expect(result.current.isCargoAny).toBe(true);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('detects cargo/multi-logistics page', () => {
    mockUseLocation.mockReturnValue({ pathname: '/client/cargo/multi-logistics', search: '' });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoMultiLogistics).toBe(true);
    expect(result.current.isCargoAny).toBe(true);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('detects multi-logistics ?tab=etrn', () => {
    mockUseLocation.mockReturnValue({
      pathname: '/client/cargo/multi-logistics',
      search: '?tab=etrn',
    });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoMultiLogistics).toBe(true);
    expect(result.current.isEtrnTab).toBe(true);
  });

  it('does not flag etrn for other tabs in multi-logistics', () => {
    mockUseLocation.mockReturnValue({
      pathname: '/client/cargo/multi-logistics',
      search: '?tab=journal',
    });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoMultiLogistics).toBe(true);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('does not flag etrn for non-multi-logistics path', () => {
    mockUseLocation.mockReturnValue({
      pathname: '/client/order-execution/cargo',
      search: '?tab=etrn',
    });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current.isCargoMultiLogistics).toBe(false);
    expect(result.current.isEtrnTab).toBe(false);
  });

  it('handles undefined pathname gracefully', () => {
    mockUseLocation.mockReturnValue({ pathname: undefined });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current).toEqual({
      isCargoOrderExecution: false,
      isCargoOrders: false,
      isCargoMultiLogistics: false,
      isCargoRoutes: false,
      isCargoAny: false,
      isEtrnTab: false,
    });
  });

  it('handles null pathname gracefully', () => {
    mockUseLocation.mockReturnValue({ pathname: null });

    const { result } = renderHook(() => useCargoRoute());

    expect(result.current).toEqual({
      isCargoOrderExecution: false,
      isCargoOrders: false,
      isCargoMultiLogistics: false,
      isCargoRoutes: false,
      isCargoAny: false,
      isEtrnTab: false,
    });
  });
});
