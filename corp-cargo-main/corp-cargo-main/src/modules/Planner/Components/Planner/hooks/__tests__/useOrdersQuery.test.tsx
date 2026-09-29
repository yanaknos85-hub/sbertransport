import { renderHook } from '@testing-library/react-hooks';
import { useOrdersQuery } from '../useOrdersQuery';

// Мокаем зависимости
jest.mock('api/planner', () => ({
  useSearchOrders: jest.fn(() => ({ data: [], isLoading: false })),
}));

jest.mock('modules/Planner/context/PlannerContext', () => ({
  usePlanner: jest.fn(() => ({
    orderFilters: {},
    sortingOrderProperty: 'createdAt',
    directionOrderAsc: true,
  })),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(() => ({
    plannerStore: {
      activeTab: 'planner',
      setOrdersListPageSize: { page: 0, size: 20 },
    },
  })),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

import { useOrganizationContext } from 'context/Organization.context';
import { useSearchOrders } from 'api/planner';

describe('useOrdersQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('передает organizationId при isOrganization=true', () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: true,
    });

    renderHook(() => useOrdersQuery());

    expect(useSearchOrders).toHaveBeenCalledWith(
      expect.objectContaining({ organizationId: 'org-123' }),
      expect.anything()
    );
    expect(useSearchOrders).not.toHaveBeenCalledWith(
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

    renderHook(() => useOrdersQuery());

    expect(useSearchOrders).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: ['exec-1', 'exec-2'],
        emptyExecutorGroup: false,
      }),
      expect.anything()
    );
    expect(useSearchOrders).not.toHaveBeenCalledWith(
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

    renderHook(() => useOrdersQuery());

    expect(useSearchOrders).toHaveBeenCalledWith(
      expect.objectContaining({ emptyExecutorGroup: true }),
      expect.anything()
    );
  });
});

describe('useOrdersQuery', () => {
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

    renderHook(() => useOrdersQuery());

    expect(useSearchOrders).toHaveBeenCalledWith(
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

    renderHook(() => useOrdersQuery());

    expect(useSearchOrders).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: [],
        emptyExecutorGroup: true,
      }),
      expect.anything()
    );
  });
});
