import { renderHook } from '@testing-library/react-hooks';
import { useRoutesQuery } from '../useRoutesQuery';

// Мокаем зависимости
jest.mock('api/planner', () => ({
  useSearchRoutes: jest.fn(() => ({ data: [], isLoading: false })),
}));

jest.mock('modules/Planner/context/PlannerContext', () => ({
  usePlanner: jest.fn(() => ({
    routeFilters: {},
    sortingRouteProperty: 'createdAt',
    directionRouteAsc: true,
  })),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(() => ({
    plannerStore: {
      activeTab: 'planner',
      setRoutesListPageSize: { page: 0, size: 20 },
    },
  })),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

import { useOrganizationContext } from 'context/Organization.context';
import { useSearchRoutes } from 'api/planner';

describe('useRoutesQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('передает organizationId при isOrganization=true', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: true,
    });

    renderHook(() => useRoutesQuery());

    expect(useSearchRoutes).toHaveBeenCalledWith(
      expect.objectContaining({ organizationId: 'org-123' }),
      expect.anything()
    );
    expect(useSearchRoutes).not.toHaveBeenCalledWith(
      expect.objectContaining({ executorGroupIds: expect.anything() }),
      expect.anything()
    );
  });

  it('передает executorGroupIds при isOrganization=false', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: ['exec-1', 'exec-2'],
      isOrganization: false,
      emptyExecutorGroup: false,
    });

    renderHook(() => useRoutesQuery());

    expect(useSearchRoutes).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: ['exec-1', 'exec-2'],
        emptyExecutorGroup: false,
      }),
      expect.anything()
    );
    expect(useSearchRoutes).not.toHaveBeenCalledWith(
      expect.objectContaining({ organizationId: 'org-123' }),
      expect.anything()
    );
  });

  it('передает emptyExecutorGroup=true при пустом executorGroupId', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: false,
      emptyExecutorGroup: true,
    });

    renderHook(() => useRoutesQuery());

    expect(useSearchRoutes).toHaveBeenCalledWith(
      expect.objectContaining({ emptyExecutorGroup: true }),
      expect.anything()
    );
  });
});
describe('useRoutesQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('исключает EXECUTOR_GROUP_ALL_ID из executorGroupIds', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: ['allGroups', 'exec-1', 'exec-2'],
      isOrganization: false,
      emptyExecutorGroup: false,
    });

    renderHook(() => useRoutesQuery());

    expect(useSearchRoutes).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: ['exec-1', 'exec-2'],
        emptyExecutorGroup: false,
      }),
      expect.anything()
    );
  });

  it('передает пустой executorGroupIds и emptyExecutorGroup=true при «Без групп»', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: false,
      emptyExecutorGroup: true,
    });

    renderHook(() => useRoutesQuery());

    expect(useSearchRoutes).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: [],
        emptyExecutorGroup: true,
      }),
      expect.anything()
    );
  });
});
