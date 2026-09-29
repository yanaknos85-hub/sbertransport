import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Organization } from '../Organization';

// ==========================
// 1. ГЛОБАЛЬНЫЙ ПОЛИФИЛ FETCH
// ==========================
if (typeof global.fetch === 'undefined') {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({}),
      text: () => Promise.resolve(''),
      ok: true,
      status: 200,
    })
  ) as any;
}

// ==========================
// 2. МОКИ ЗАВИСИМОСТЕЙ
// ==========================
jest.mock('utils/useRole', () => ({
  useRole: jest.fn(),
}));

jest.mock('react-router-dom', () => ({
  useLocation: jest.fn(),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

jest.mock('constants/constants.app', () => ({
  rolesCanSwitchOrg: ['ADMIN', 'MANAGER'],
}));

jest.mock('shared/assets/svg/down-arrow.svg', () => ({
  ReactComponent: () => <svg data-testid="down-arrow-icon" />,
}));

jest.mock('../Organization.module.scss', () => ({
  container: 'container',
}));

jest.mock('shared/components/AccessControl', () => ({
  __esModule: true,
  default: ({ children, userPermissions, allowedPermissions }: any) => (
    <div
      data-testid="access-control"
      data-permissions={JSON.stringify(userPermissions)}
      data-allowed={JSON.stringify(allowedPermissions)}
    >
      {children}
    </div>
  ),
}));

jest.mock('shared/components/SelectOrganization', () => ({
  SelectOrganization: (props: any) => (
    <select
      data-testid="select-organization"
      defaultValue={props.defaultValue}
      onChange={(e) => props.onChange?.(e.target.value)}
    >
      <option value="org-1">Org 1</option>
      <option value="org-2">Org 2</option>
    </select>
  ),
}));

jest.mock('../../SelectExecutorGroup', () => ({
  SelectExecutorGroup: (props: any) => (
    <select
      data-testid="select-executor-group"
      defaultValue={props.defaultValue}
      onChange={(e) => props.onChange?.(e.target.value)}
    >
      <option value="exec-1">Exec 1</option>
      <option value="exec-2">Exec 2</option>
    </select>
  ),
}));

jest.mock('../../SelectFlag', () => ({
  SelectFlag: (props: any) => (
    <select
      data-testid="select-flag"
      defaultValue={props.defaultValue}
      onChange={() => props.onChange?.()}
    >
      {props.options?.map((opt: string) => (
        <option key={opt} value={opt}>{opt}</option>
      ))}
    </select>
  ),
}));

const mUseLocation = require('react-router-dom').useLocation as jest.Mock;
const mUseRole = require('utils/useRole').useRole as jest.Mock;
const mUseOrgCtx = require('context/Organization.context').useOrganizationContext as jest.Mock;

describe('Organization Component', () => {
  const mockSetOrganizationId = jest.fn();
  const mockSetExecutorGroupId = jest.fn();
  const mockSetIsOrganization = jest.fn();

  const defaultContextValue = {
    setOrganizationId: mockSetOrganizationId,
    organizationId: 'org-1',
    executorGroupId: 'exec-1',
    setExecutorGroupId: mockSetExecutorGroupId,
    isOrganization: true,
    setIsOrganization: mockSetIsOrganization,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mUseRole.mockReturnValue(['ADMIN']);
    mUseOrgCtx.mockReturnValue(defaultContextValue);
    mUseLocation.mockReturnValue({ pathname: '/some-page', search: '' });
  });

  describe('Access Control', () => {
    it('should pass userRoles and allowedPermissions to AccessControl', () => {
      mUseRole.mockReturnValue(['MANAGER']);
      render(<Organization />);

      const ac = screen.getByTestId('access-control');
      expect(ac).toHaveAttribute('data-permissions', JSON.stringify(['MANAGER']));
      expect(ac).toHaveAttribute('data-allowed', JSON.stringify(['ADMIN', 'MANAGER']));
    });
  });

  describe('Default View (non-report pages)', () => {
    beforeEach(() => {
      mUseLocation.mockReturnValue({ pathname: '/dashboard' });
    });

    it('should render only SelectOrganization when not on report page', () => {
      render(<Organization />);

      expect(screen.getByTestId('select-organization')).toBeInTheDocument();
      expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
      expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
    });

    it('should call setOrganizationId on organization change', () => {
      render(<Organization />);

      fireEvent.change(screen.getByTestId('select-organization'), {
        target: { value: 'org-2' },
      });

      expect(mockSetOrganizationId).toHaveBeenCalledWith('org-2');
    });
  });

  describe('New Header View (report pages)', () => {
    const reportPaths = [
      '/reports/registry/cargo/orders',
      '/reports/registry/cargo/routes',
      '/order-execution/list',
      '/multi-logistics/dashboard',
    ];

    reportPaths.forEach((path) => {
      describe(`pathname: ${path}`, () => {
        beforeEach(() => {
          mUseLocation.mockReturnValue({ pathname: path });
        });

        it('should render SelectFlag when not on template tab', () => {
          render(<Organization />);
          expect(screen.getByTestId('select-flag')).toBeInTheDocument();
        });

        it('should render SelectOrganization when isOrganization is true', () => {
          mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: true });
          render(<Organization />);

          expect(screen.getByTestId('select-organization')).toBeInTheDocument();
          expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
        });

        it('should render SelectExecutorGroup when isOrganization is false', () => {
          mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: false });
          render(<Organization />);

          expect(screen.getByTestId('select-executor-group')).toBeInTheDocument();
          expect(screen.queryByTestId('select-organization')).not.toBeInTheDocument();
        });

        it('should toggle isOrganization on SelectFlag change', () => {
          // Исправленный тест - проверяем что setIsOrganization вызывается с правильным значением
          render(<Organization />);

          fireEvent.change(screen.getByTestId('select-flag'));

          expect(mockSetIsOrganization).toHaveBeenCalledTimes(1);
          // Проверяем, что вызывается с boolean значением (не с функцией)
          expect(mockSetIsOrganization).toHaveBeenCalledWith(false);
        });

        it('should call setExecutorGroupId on executor group change', () => {
          mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: false });
          render(<Organization />);

          fireEvent.change(screen.getByTestId('select-executor-group'), {
            target: { value: 'exec-2' },
          });

          expect(mockSetExecutorGroupId).toHaveBeenCalledWith('exec-2');
        });
      });
    });
  });

  describe('Template Tab View', () => {
    it('should render only SelectOrganization on template tab regardless of isOrganization', () => {
      mUseLocation.mockReturnValue({ pathname: '/order-execution/template' });
      mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: false });

      render(<Organization />);

      expect(screen.getByTestId('select-organization')).toBeInTheDocument();
      expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
      expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
    });

    it('should NOT render SelectFlag on template tab', () => {
      mUseLocation.mockReturnValue({ pathname: '/reports/registry/cargo/orders/template' });

      render(<Organization />);

      expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
    });
  });

  describe('SelectFlag Default Value', () => {
    it('should show "По организации" when isOrganization is true', () => {
      mUseLocation.mockReturnValue({ pathname: '/order-execution' });
      mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: true });

      render(<Organization />);

      const flag = screen.getByTestId('select-flag');
      expect(flag).toHaveValue('По организации');
    });

    it('should show "По группам исполнителей" when isOrganization is false', () => {
      mUseLocation.mockReturnValue({ pathname: '/order-execution' });
      mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: false });

      render(<Organization />);

      const flag = screen.getByTestId('select-flag');
      expect(flag).toHaveValue('По группам исполнителей');
    });
  });

  describe('Card Tab View', () => {
    const etrnPaths = [
      '/order-execution/list?tab=etrn',
      '/multi-logistics/dashboard?tab=etrn',
      '/reports/registry/cargo/orders?tab=etrn',
      '/reports/registry/cargo/routes?tab=etrn',
    ];

    const setLocation = (path: string) => {
      const [pathname, search = ''] = path.split('?');
      mUseLocation.mockReturnValue({ pathname, search: search ? `?${search}` : '' });
    };

    etrnPaths.forEach((path) => {
      describe(`pathname: ${path}`, () => {
        beforeEach(() => {
          setLocation(path);
        });

        it('should render only SelectOrganization when isOrganization is true', () => {
          mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: true });
          render(<Organization />);

          expect(screen.getByTestId('select-organization')).toBeInTheDocument();
          expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
          expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
        });

        it('should render only SelectOrganization when isOrganization is false', () => {
          mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: false });
          render(<Organization />);

          expect(screen.getByTestId('select-organization')).toBeInTheDocument();
          expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
          expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
        });

        it('should NOT render SelectFlag', () => {
          render(<Organization />);

          expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
        });

        it('should call setOrganizationId on org change (not setExecutorGroupId)', () => {
          render(<Organization />);

          fireEvent.change(screen.getByTestId('select-organization'), {
            target: { value: 'org-2' },
          });

          expect(mockSetOrganizationId).toHaveBeenCalledWith('org-2');
          expect(mockSetExecutorGroupId).not.toHaveBeenCalled();
        });

        it('should NOT call setIsOrganization when changing org', () => {
          render(<Organization />);

          fireEvent.change(screen.getByTestId('select-organization'), {
            target: { value: 'org-2' },
          });

          expect(mockSetIsOrganization).not.toHaveBeenCalled();
        });
      });
    });

    it('should not affect default view when etrn-signature tab is on non-new-header page', () => {
      setLocation('/dashboard?tab=etrn');
      render(<Organization />);

      expect(screen.getByTestId('select-organization')).toBeInTheDocument();
      expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
      expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
    });

    it('should treat other tab query values normally (no etrn-signature behaviour)', () => {
      setLocation('/order-execution/list?tab=other');
      render(<Organization />);

      expect(screen.getByTestId('select-flag')).toBeInTheDocument();
      expect(screen.getByTestId('select-organization')).toBeInTheDocument();
    });

    it('should handle combined template + etrn-signature (only SelectOrganization)', () => {
      setLocation('/order-execution/template?tab=etrn');
      mUseOrgCtx.mockReturnValue({ ...defaultContextValue, isOrganization: false });
      render(<Organization />);

      expect(screen.getByTestId('select-organization')).toBeInTheDocument();
      expect(screen.queryByTestId('select-flag')).not.toBeInTheDocument();
      expect(screen.queryByTestId('select-executor-group')).not.toBeInTheDocument();
    });
  });
});
