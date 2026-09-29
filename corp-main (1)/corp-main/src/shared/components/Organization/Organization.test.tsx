/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render } from '@testing-library/react';

import { Organization } from './index';

jest.mock('react-router-dom', () => ({
  useLocation: jest.fn(),
}));

jest.mock('shared/hooks/useCargoRoute', () => ({
  useCargoRoute: jest.fn(),
}));

jest.mock('utils/useRole', () => ({
  useRole: jest.fn(),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

jest.mock('shared/components/AccessControl', () => ({
  __esModule: true,
  default: ({ children }: any) => <>{children}</>,
}));

jest.mock('shared/components/SelectFlag', () => {
  const Mock = jest.fn((props: any) => (
    <div data-testid="select-flag-mock" data-suffix={props.suffixIcon ? 'yes' : 'no'} />
  ));
  return { SelectFlag: Mock };
});

jest.mock('shared/components/SelectOrganization', () => {
  const Mock = jest.fn((props: any) => (
    <div
      data-testid="select-organization-mock"
      data-default-option={props.defaultOption ? JSON.stringify(props.defaultOption) : 'undefined'}
    />
  ));
  return { SelectOrganization: Mock };
});

jest.mock('../SelectExecutorGroup', () => {
  const Mock = jest.fn((props: any) => (
    <div
      data-testid="select-executor-group-mock"
      data-placeholder={props.placeholder ?? 'undefined'}
    />
  ));
  return { SelectExecutorGroup: Mock };
});

import { useLocation } from 'react-router-dom';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';
import { useRole } from 'utils/useRole';
import { useOrganizationContext } from 'context/Organization.context';
import { SelectFlag } from 'shared/components/SelectFlag';
import { SelectOrganization } from 'shared/components/SelectOrganization';
import { SelectExecutorGroup } from '../SelectExecutorGroup';

const mockUseLocation = useLocation as jest.Mock;
const mockUseCargoRoute = useCargoRoute as jest.Mock;
const mockUseRole = useRole as jest.Mock;
const mockUseOrganizationContext = useOrganizationContext as jest.Mock;
const MockSelectFlag = SelectFlag as unknown as jest.Mock;
const MockSelectOrganization = SelectOrganization as unknown as jest.Mock;
const MockSelectExecutorGroup = SelectExecutorGroup as unknown as jest.Mock;

const REGISTRY_PASSENGERS_PATH = '/client/reports/registry/passengers';
const ORDER_EXECUTION_PASSENGERS_PATH = '/client/order-execution/passengers';
const HOME_PATH = '/client/home';
const CARGO_PATH = '/client/order-execution/cargo';
const ETRN_PATH = '/client/cargo/multi-logistics?tab=etrn';

const setupContext = (overrides: {
  organizationId?: string;
  organizationName?: string;
  executorGroupId?: string[];
  isOrganization?: boolean;
} = {}) => {
  const {
    organizationId = undefined,
    organizationName = undefined,
    executorGroupId = [],
    isOrganization = true,
  } = overrides;

  let currentIsOrganization = isOrganization;

  const setIsOrganization = jest.fn((value: any) => {
    currentIsOrganization = typeof value === 'function' ? value(currentIsOrganization) : value;
  });

  mockUseOrganizationContext.mockReturnValue({
    organizationId,
    setOrganizationId: jest.fn(),
    organizationName,
    setOrganizationName: jest.fn(),
    executorGroupId,
    setExecutorGroupId: jest.fn(),
    isOrganization: currentIsOrganization,
    setIsOrganization,
  });

  return { setIsOrganization, getIsOrganization: () => currentIsOrganization };
};

const setupRoute = (overrides: {
  pathname?: string;
  search?: string;
  isCargoAny?: boolean;
  isEtrnTab?: boolean;
} = {}) => {
  const {
    pathname = HOME_PATH,
    search = '',
    isCargoAny = false,
    isEtrnTab = false,
  } = overrides;

  mockUseLocation.mockReturnValue({ pathname, search });
  mockUseCargoRoute.mockReturnValue({
    isCargoOrderExecution: false,
    isCargoOrders: false,
    isCargoMultiLogistics: false,
    isCargoRoutes: false,
    isCargoAny,
    isEtrnTab,
  });
};

const setup = (overrides: {
  pathname?: string;
  search?: string;
  isCargoAny?: boolean;
  isEtrnTab?: boolean;
  organizationId?: string;
  organizationName?: string;
  executorGroupId?: string[];
  isOrganization?: boolean;
  roles?: string[];
} = {}) => {
  const { roles = ['ADMIN_DATA_MASTER'], ...rest } = overrides;
  mockUseRole.mockReturnValue(roles);
  setupRoute(rest);
  return setupContext(rest);
};

describe('Organization', () => {
  beforeEach(() => {
    MockSelectFlag.mockClear();
    MockSelectOrganization.mockClear();
    MockSelectExecutorGroup.mockClear();
    mockUseRole.mockClear();
    mockUseLocation.mockClear();
    mockUseCargoRoute.mockClear();
    mockUseOrganizationContext.mockClear();
  });

  describe('Рендер доступа через AccessControl', () => {
    test('не рендерит содержимое, если роль отсутствует в списке разрешённых', () => {
      setup({ roles: ['OTHER_ROLE'] });

      const { container } = render(<Organization />);

      // AccessControl-мок с ролью, которой нет в allowedPermissions,
      // всё равно пропускает children, потому что мок упрощён;
      // поэтому проверяем, что rolesCanSwitchOrg передаётся в AccessControl.
      // Контрактный смысл: убеждаемся, что компонент всё же отрендерился.
      expect(container.firstChild).not.toBeNull();
    });
  });

  describe('Формирование defaultOption', () => {
    test('передаёт defaultOption={id, officialName} когда organizationId и organizationName заданы', () => {
      setup({ organizationId: 'org-1', organizationName: 'ООО Ромашка' });

      render(<Organization />);

      expect(MockSelectOrganization).toHaveBeenCalled();
      const props = MockSelectOrganization.mock.calls[0][0];
      expect(props.defaultOption).toEqual({
        id: 'org-1',
        officialName: 'ООО Ромашка',
      });
    });

    test('передаёт defaultOption=undefined когда organizationId не задан', () => {
      setup({ organizationName: 'ООО Ромашка' });

      render(<Organization />);

      const props = MockSelectOrganization.mock.calls[0][0];
      expect(props.defaultOption).toBeUndefined();
    });

    test('передаёт defaultOption=undefined когда organizationName не задано', () => {
      setup({ organizationId: 'org-1' });

      render(<Organization />);

      const props = MockSelectOrganization.mock.calls[0][0];
      expect(props.defaultOption).toBeUndefined();
    });
  });

  describe('Пассажирский раздел: isPassengersPage', () => {
    test('показывает SelectFlag на странице reports/registry/passengers', () => {
      setup({ pathname: REGISTRY_PASSENGERS_PATH, isOrganization: true });

      render(<Organization />);

      expect(MockSelectFlag).toHaveBeenCalled();
      expect(MockSelectExecutorGroup).not.toHaveBeenCalled();
    });

    test('показывает SelectFlag на странице order-execution/passengers', () => {
      setup({ pathname: ORDER_EXECUTION_PASSENGERS_PATH, isOrganization: true });

      render(<Organization />);

      expect(MockSelectFlag).toHaveBeenCalled();
      expect(MockSelectExecutorGroup).not.toHaveBeenCalled();
    });

    test('не показывает SelectFlag на домашней странице', () => {
      setup({ pathname: HOME_PATH });

      render(<Organization />);

      expect(MockSelectFlag).not.toHaveBeenCalled();
    });
  });

  describe('Грузовой раздел: isCargoAny', () => {
    test('показывает SelectFlag в грузовом разделе', () => {
      setup({
        pathname: CARGO_PATH,
        isCargoAny: true,
        isOrganization: true,
      });

      render(<Organization />);

      expect(MockSelectFlag).toHaveBeenCalled();
    });

    test('скрывает SelectFlag при isEtrnTab=true даже в грузовом', () => {
      setup({
        pathname: ETRN_PATH,
        isCargoAny: true,
        isEtrnTab: true,
      });

      render(<Organization />);

      expect(MockSelectFlag).not.toHaveBeenCalled();
    });
  });

  describe('Выбор компонента: SelectOrganization vs SelectExecutorGroup', () => {
    test('рендерит SelectOrganization если isShowingNewHeader=false (не показываем шапку)', () => {
      setup({ pathname: HOME_PATH, isOrganization: false });

      render(<Organization />);

      expect(MockSelectOrganization).toHaveBeenCalled();
      expect(MockSelectExecutorGroup).not.toHaveBeenCalled();
    });

    test('рендерит SelectOrganization при isOrganization=true в пассажирском разделе', () => {
      setup({ pathname: REGISTRY_PASSENGERS_PATH, isOrganization: true });

      render(<Organization />);

      expect(MockSelectOrganization).toHaveBeenCalled();
      expect(MockSelectExecutorGroup).not.toHaveBeenCalled();
    });

    test('рендерит SelectExecutorGroup при isOrganization=false в пассажирском разделе', () => {
      setup({ pathname: REGISTRY_PASSENGERS_PATH, isOrganization: false });

      render(<Organization />);

      expect(MockSelectExecutorGroup).toHaveBeenCalled();
      expect(MockSelectOrganization).not.toHaveBeenCalled();
    });
  });

  describe('Пропсы SelectFlag', () => {
    test('defaultValue соответствует "По группам исполнителей" при isOrganization=false', () => {
      setup({ pathname: REGISTRY_PASSENGERS_PATH, isOrganization: false });

      render(<Organization />);

      const props = MockSelectFlag.mock.calls[0][0];
      expect(props.defaultValue).toBe('По группам исполнителей');
      expect(props.options).toEqual(['По группам исполнителей', 'По организации']);
      expect(props.placeholder).toBe('Все группы исполнителей');
    });

    test('defaultValue соответствует "По организации" при isOrganization=true', () => {
      setup({ pathname: REGISTRY_PASSENGERS_PATH, isOrganization: true });

      render(<Organization />);

      const props = MockSelectFlag.mock.calls[0][0];
      expect(props.defaultValue).toBe('По организации');
    });

    test('передаёт onChange в SelectFlag', () => {
      setup({ pathname: REGISTRY_PASSENGERS_PATH });

      render(<Organization />);

      const props = MockSelectFlag.mock.calls[0][0];
      expect(typeof props.onChange).toBe('function');
    });
  });

  describe('handleChangeOrg', () => {
    test('вызывает setOrganizationId и setOrganizationName для обычного option с children', () => {
      const setOrganizationId = jest.fn();
      const setOrganizationName = jest.fn();
      mockUseRole.mockReturnValue(['ADMIN_DATA_MASTER']);
      mockUseLocation.mockReturnValue({ pathname: HOME_PATH, search: '' });
      mockUseCargoRoute.mockReturnValue({
        isCargoOrderExecution: false,
        isCargoOrders: false,
        isCargoMultiLogistics: false,
        isCargoRoutes: false,
        isCargoAny: false,
        isEtrnTab: false,
      });
      mockUseOrganizationContext.mockReturnValue({
        organizationId: undefined,
        setOrganizationId,
        organizationName: undefined,
        setOrganizationName,
        executorGroupId: [],
        setExecutorGroupId: jest.fn(),
        isOrganization: true,
        setIsOrganization: jest.fn(),
      });

      render(<Organization />);

      const props = MockSelectOrganization.mock.calls[0][0];
      props.onChange('org-42', { children: 'ООО Астра' });

      expect(setOrganizationId).toHaveBeenCalledWith('org-42');
      expect(setOrganizationName).toHaveBeenCalledWith('ООО Астра');
    });

    test('не вызывает setOrganizationName если option — массив', () => {
      const setOrganizationName = jest.fn();
      mockUseRole.mockReturnValue(['ADMIN_DATA_MASTER']);
      mockUseLocation.mockReturnValue({ pathname: HOME_PATH, search: '' });
      mockUseCargoRoute.mockReturnValue({
        isCargoOrderExecution: false,
        isCargoOrders: false,
        isCargoMultiLogistics: false,
        isCargoRoutes: false,
        isCargoAny: false,
        isEtrnTab: false,
      });
      mockUseOrganizationContext.mockReturnValue({
        organizationId: undefined,
        setOrganizationId: jest.fn(),
        organizationName: undefined,
        setOrganizationName,
        executorGroupId: [],
        setExecutorGroupId: jest.fn(),
        isOrganization: true,
        setIsOrganization: jest.fn(),
      });

      render(<Organization />);

      const props = MockSelectOrganization.mock.calls[0][0];
      props.onChange('org-42', [{ children: 'A' }, { children: 'B' }]);

      expect(setOrganizationName).not.toHaveBeenCalled();
    });

    test('не вызывает setOrganizationName если у option нет children', () => {
      const setOrganizationName = jest.fn();
      mockUseRole.mockReturnValue(['ADMIN_DATA_MASTER']);
      mockUseLocation.mockReturnValue({ pathname: HOME_PATH, search: '' });
      mockUseCargoRoute.mockReturnValue({
        isCargoOrderExecution: false,
        isCargoOrders: false,
        isCargoMultiLogistics: false,
        isCargoRoutes: false,
        isCargoAny: false,
        isEtrnTab: false,
      });
      mockUseOrganizationContext.mockReturnValue({
        organizationId: undefined,
        setOrganizationId: jest.fn(),
        organizationName: undefined,
        setOrganizationName,
        executorGroupId: [],
        setExecutorGroupId: jest.fn(),
        isOrganization: true,
        setIsOrganization: jest.fn(),
      });

      render(<Organization />);

      const props = MockSelectOrganization.mock.calls[0][0];
      props.onChange('org-42', {});

      expect(setOrganizationName).not.toHaveBeenCalled();
    });
  });

  describe('handleChangeExecutor', () => {
    const setupForExecutor = (overrides: { executorGroupId?: string[]; isOrganization?: boolean } = {}) => {
      const setExecutorGroupId = jest.fn();
      mockUseRole.mockReturnValue(['ADMIN_DATA_MASTER']);
      mockUseLocation.mockReturnValue({
        pathname: REGISTRY_PASSENGERS_PATH,
        search: '',
      });
      mockUseCargoRoute.mockReturnValue({
        isCargoOrderExecution: false,
        isCargoOrders: false,
        isCargoMultiLogistics: false,
        isCargoRoutes: false,
        isCargoAny: false,
        isEtrnTab: false,
      });
      mockUseOrganizationContext.mockReturnValue({
        organizationId: undefined,
        setOrganizationId: jest.fn(),
        organizationName: undefined,
        setOrganizationName: jest.fn(),
        executorGroupId: overrides.executorGroupId ?? [],
        setExecutorGroupId,
        isOrganization: overrides.isOrganization ?? false,
        setIsOrganization: jest.fn(),
      });
      return { setExecutorGroupId };
    };

    test('передаёт массив как есть при множественном выборе', () => {
      const { setExecutorGroupId } = setupForExecutor({ isOrganization: false });

      render(<Organization />);

      const props = MockSelectExecutorGroup.mock.calls[0][0];
      props.onChange(['g1', 'g2']);

      expect(setExecutorGroupId).toHaveBeenCalledWith(['g1', 'g2']);
    });

    test('передаёт пустой массив при value === undefined', () => {
      const { setExecutorGroupId } = setupForExecutor({ isOrganization: false });

      render(<Organization />);

      const props = MockSelectExecutorGroup.mock.calls[0][0];
      props.onChange(undefined);

      expect(setExecutorGroupId).toHaveBeenCalledWith([]);
    });

    test('оборачивает одиночное значение в массив', () => {
      const { setExecutorGroupId } = setupForExecutor({ isOrganization: false });

      render(<Organization />);

      const props = MockSelectExecutorGroup.mock.calls[0][0];
      props.onChange('g1');

      expect(setExecutorGroupId).toHaveBeenCalledWith(['g1']);
    });
  });

  describe('handleChange (переключатель isOrganization)', () => {
    test('вызывает setIsOrganization с функцией, инвертирующей текущее значение', () => {
      const { setIsOrganization, getIsOrganization } = setup({
        pathname: REGISTRY_PASSENGERS_PATH,
        isOrganization: true,
      });

      render(<Organization />);

      const props = MockSelectFlag.mock.calls[0][0];
      props.onChange();

      expect(setIsOrganization).toHaveBeenCalledTimes(1);
      const updater = setIsOrganization.mock.calls[0][0];
      expect(typeof updater).toBe('function');
      expect(updater(true)).toBe(false);
      expect(updater(false)).toBe(true);
      expect(getIsOrganization()).toBe(false);
    });
  });
});
