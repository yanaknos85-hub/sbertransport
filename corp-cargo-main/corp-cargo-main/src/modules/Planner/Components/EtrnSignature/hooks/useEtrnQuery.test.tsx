/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from '@testing-library/react-hooks';

jest.mock('api/etrn-signature/etrn-signature', () => ({
  useSearchEtrn: jest.fn(() => ({ data: [], isLoading: false, refetch: jest.fn() })),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

import { useSearchEtrn } from 'api/etrn-signature/etrn-signature';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';
import { Tab } from 'modules/Planner/types';

import { useEtrnQuery } from './useEtrnQuery';

const mockSetEtrnPageSettings = jest.fn();
const basePlanner = {
  activeTab: Tab.etrn,
  setEtrnListPageSetting: { page: 0, size: 20 },
  setEtrnPageSettings: mockSetEtrnPageSettings,
};

describe('useEtrnQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSetEtrnPageSettings.mockClear();
    (useAppStoreContext as jest.Mock).mockReturnValue({ plannerStore: basePlanner });
    (useOrganizationContext as jest.Mock).mockReturnValue({ organizationId: 'org-1' });
  });

  test('должен передавать organizationId из useOrganizationContext в query', () => {
    renderHook(() => useEtrnQuery());
    expect(useSearchEtrn).toHaveBeenCalledWith(
      expect.objectContaining({ organizationId: 'org-1' }),
      expect.any(Object),
    );
  });

  test('должен передавать pageSetting из planner store', () => {
    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: { ...basePlanner, setEtrnListPageSetting: { page: 2, size: 50 } },
    });
    renderHook(() => useEtrnQuery());
    expect(useSearchEtrn).toHaveBeenCalledWith(
      expect.objectContaining({
        pageSetting: { page: 2, size: 50 },
      }),
      expect.any(Object),
    );
  });

  test('должен передавать enabled=true когда activeTab === Tab.etrn', () => {
    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: { ...basePlanner, activeTab: Tab.etrn },
    });
    renderHook(() => useEtrnQuery());
    expect(useSearchEtrn).toHaveBeenCalledWith(
      expect.any(Object),
      expect.objectContaining({ enabled: true }),
    );
  });

  test('должен передавать enabled=false когда activeTab !== Tab.etrn', () => {
    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: { ...basePlanner, activeTab: Tab.planner },
    });
    renderHook(() => useEtrnQuery());
    expect(useSearchEtrn).toHaveBeenCalledWith(
      expect.any(Object),
      expect.objectContaining({ enabled: false }),
    );
  });

  test('должен сбрасывать page на 0 при смене organizationId', () => {
    const { rerender } = renderHook(() => useEtrnQuery());
    expect(mockSetEtrnPageSettings).toHaveBeenCalledWith({ page: 0, size: 20 });

    mockSetEtrnPageSettings.mockClear();
    (useOrganizationContext as jest.Mock).mockReturnValue({ organizationId: 'org-2' });
    rerender();

    expect(mockSetEtrnPageSettings).toHaveBeenCalledWith({ page: 0, size: 20 });
  });

  test('не должен сбрасывать page при смене activeTab', () => {
    const { rerender } = renderHook(() => useEtrnQuery());
    expect(mockSetEtrnPageSettings).toHaveBeenCalledTimes(1);

    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: { ...basePlanner, activeTab: Tab.planner },
    });
    rerender();

    expect(mockSetEtrnPageSettings).toHaveBeenCalledTimes(1);
  });

  test('должен обновлять query при изменении setEtrnListPageSetting', () => {
    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: { ...basePlanner, setEtrnListPageSetting: { page: 0, size: 20 } },
    });
    const { rerender } = renderHook(() => useEtrnQuery());
    expect(useSearchEtrn).toHaveBeenLastCalledWith(
      expect.objectContaining({ pageSetting: { page: 0, size: 20 } }),
      expect.any(Object),
    );

    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: { ...basePlanner, setEtrnListPageSetting: { page: 0, size: 50 } },
    });
    rerender();

    expect(useSearchEtrn).toHaveBeenLastCalledWith(
      expect.objectContaining({ pageSetting: { page: 0, size: 50 } }),
      expect.any(Object),
    );
  });
});
