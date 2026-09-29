import { renderHook } from '@testing-library/react-hooks';

jest.mock('api/planner', () => ({
  useSearchMonitorRoutes: jest.fn(() => ({ data: [], isLoading: false })),
}));

jest.mock('modules/Planner/context/PlannerContext', () => ({
  usePlanner: jest.fn(() => ({
    sortingRouteProperty: 'createdAt',
    directionRouteAsc: true,
    monitorFilters: {},
  })),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(() => ({
    plannerStore: {
      activeTab: 'journal',
      monitorFilters: {},
      setMonitorListPageSize: { page: 0, size: 20 },
    },
  })),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

import { useMonitorQuery } from './useMonitorQuery';
import { useOrganizationContext } from 'context/Organization.context';
import { useSearchMonitorRoutes } from 'api/planner';

describe('useMonitorQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useSearchMonitorRoutes as jest.Mock).mockReturnValue({ data: [], isLoading: false });
  });

  it('передает organizationId при isOrganization=true', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: true,
    });

    renderHook(() => useMonitorQuery());

    const lastCallArgs = (useSearchMonitorRoutes as jest.Mock).mock.calls[0];
    expect(lastCallArgs).toBeDefined();
    expect(lastCallArgs[0]).toMatchObject({ organizationId: 'org-123' });
    expect(lastCallArgs[0]).not.toHaveProperty('executorGroupIds');
    expect(lastCallArgs[0]).not.toHaveProperty('emptyExecutorGroup');
  });

  it('исключает EXECUTOR_GROUP_ALL_ID из executorGroupIds', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: ['allGroups', 'exec-1', 'exec-2'],
      isOrganization: false,
      emptyExecutorGroup: false,
    });

    renderHook(() => useMonitorQuery());

    const lastCallArgs = (useSearchMonitorRoutes as jest.Mock).mock.calls[0];
    expect(lastCallArgs).toBeDefined();
    expect(lastCallArgs[0]).toMatchObject({
      executorGroupIds: ['exec-1', 'exec-2'],
      emptyExecutorGroup: false,
    });
  });

  it('передает пустой executorGroupIds и emptyExecutorGroup=true при «Без групп»', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: false,
      emptyExecutorGroup: true,
    });

    renderHook(() => useMonitorQuery());

    const lastCallArgs = (useSearchMonitorRoutes as jest.Mock).mock.calls[0];
    expect(lastCallArgs).toBeDefined();
    expect(lastCallArgs[0]).toMatchObject({
      executorGroupIds: [],
      emptyExecutorGroup: true,
    });
  });
});
