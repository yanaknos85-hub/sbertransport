import React from 'react';
import { render } from '@testing-library/react';
import { useLocation } from 'react-router-dom';

import { Organization } from '../Organization';
import { SelectExecutorGroup } from '../../SelectExecutorGroup';
import { useOrganizationContext } from 'context/Organization.context';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';

if (typeof global.fetch === 'undefined') {
  global.fetch = jest.fn(() => Promise.resolve({ ok: true, status: 200 })) as unknown as typeof fetch;
}

jest.mock('react-router-dom', () => ({
  useLocation: jest.fn(),
  useHistory: jest.fn(),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

jest.mock('shared/hooks/useCargoRoute', () => ({
  useCargoRoute: jest.fn(),
}));

jest.mock('utils/useRole', () => ({
  useRole: jest.fn(() => []),
}));

jest.mock('shared/components/AccessControl', () => ({
  __esModule: true,
  default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

jest.mock('shared/components/SelectOrganization', () => ({
  SelectOrganization: () => <div data-testid="select-organization" />,
}));

jest.mock('shared/components/SelectFlag', () => ({
  SelectFlag: () => <div data-testid="select-flag" />,
}));

jest.mock('../../SelectExecutorGroup', () => ({
  SelectExecutorGroup: jest.fn(() => <div data-testid="select-executor-group" />),
}));

const mockUseLocation = useLocation as jest.Mock;
const mockUseOrganizationContext = useOrganizationContext as jest.Mock;
const mockUseCargoRoute = useCargoRoute as jest.Mock;
const mockSelectExecutorGroup = SelectExecutorGroup as unknown as jest.Mock;

const baseOrgContext = {
  organizationId: 'org-1',
  organizationName: 'Org Name',
  setOrganizationId: jest.fn(),
  setOrganizationName: jest.fn(),
  executorGroupId: [],
  setExecutorGroupId: jest.fn(),
  emptyExecutorGroup: false,
  setEmptyExecutorGroup: jest.fn(),
  allExecutorGroups: false,
  setAllExecutorGroups: jest.fn(),
  isOrganization: false,
  setIsOrganization: jest.fn(),
  isCargo: true,
};

describe('Organization', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseLocation.mockReturnValue({ pathname: '/client/order-execution/cargo' });
    mockUseOrganizationContext.mockReturnValue({ ...baseOrgContext });
    mockUseCargoRoute.mockReturnValue({
      isCargoOrderExecution: true,
      isCargoOrders: false,
      isCargoMultiLogistics: false,
      isCargoRoutes: false,
      isCargoAny: true,
    });
  });

  it('передаёт флаги empty/all и обработчики в SelectExecutorGroup', () => {
    const ctx = {
      ...baseOrgContext,
      executorGroupId: ['g-1'],
      emptyExecutorGroup: true,
      allExecutorGroups: false,
    };
    mockUseOrganizationContext.mockReturnValue(ctx);

    render(<Organization />);

    expect(mockSelectExecutorGroup).toHaveBeenCalledTimes(1);
    const props = mockSelectExecutorGroup.mock.calls[0][0];

    expect(props.value).toEqual(['g-1']);
    expect(props.emptyActive).toBe(true);
    expect(props.allActive).toBe(false);
    expect(props.onEmptyChange).toBe(ctx.setEmptyExecutorGroup);
    expect(props.onAllChange).toBe(ctx.setAllExecutorGroups);
    expect(props.onChange).toEqual(expect.any(Function));
    expect(props.placeholder).toBe('Все группы исполнителей');
  });

  it('пробрасывает placeholder в SelectExecutorGroup', () => {
    render(<Organization />);

    const props = mockSelectExecutorGroup.mock.calls[0][0];
    expect(props.placeholder).toBe('Все группы исполнителей');
  });
});
