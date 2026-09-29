/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}));

jest.mock('antd', () => ({
  Table: ({
    rowKey, columns, dataSource, pagination,
  }: any) => (
    <div
      data-testid="etrn-table"
      data-row-key={rowKey}
      data-pagination={String(pagination)}
      data-cols-length={String(columns?.length ?? 0)}
      data-datasource={JSON.stringify(dataSource ?? [])}
    />
  ),
  message: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

jest.mock('shared/hooks/useTableConfig', () => ({
  useTableConfig: jest.fn(() => ({ x: 1000 })),
}));

jest.mock('./components/Table/useTableFields', () => ({
  useTableFields: jest.fn(() => [
    { dataIndex: 'col1' },
    { dataIndex: 'col2' },
  ]),
}));

jest.mock('./hooks/useEtrnQuery', () => ({
  useEtrnQuery: jest.fn(),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Planner: { Etrn: {} },
      Etrn: {
        sign: { success: 'Подписано' },
        card: {
          tabs: {
            general: 'Общие',
            documents: 'Документы',
            checks: 'Проверки',
            history: 'История',
          },
        },
      },
    },
  }),
}));

jest.mock('shared/components/EmptyView', () => ({
  EmptyView: ({ title }: { title: string }) => (
    <div data-testid="etrn-empty" data-title={title} />
  ),
}));

jest.mock('shared/components/SpinWrapped/SpinWrapped', () => ({
  __esModule: true,
  default: () => <div data-testid="spin-wrapped" />,
}));

jest.mock('./components/Filters/EtrnFilters', () => ({
  EtrnFilters: () => <div data-testid="etrn-filters" />,
}));

jest.mock('./components/Card/EtrnModal', () => ({
  __esModule: true,
  default: ({ cardId, visible }: { cardId: string; visible: boolean }) =>
    visible
      ? <div data-testid="etrn-modal-stub" data-card-id={cardId} />
      : null,
}));

jest.mock('modules/Planner/Pagination/Pagination', () => ({
  Pagination: ({ pagination, total, setPagination }: any) => (
    <div
      data-testid="pagination"
      data-page={pagination?.page}
      data-size={pagination?.size}
      data-total={total}
      data-set-pagination={String(typeof setPagination === 'function')}
    />
  ),
}));

jest.mock('./styles.module.scss', () => ({
  etrnTable: 'etrnTable',
  etrnTable__row: 'etrnTable__row',
  etrnTable__pagination: 'etrnTable__pagination',
}));

jest.mock('modules/Planner/Components/Monitor/utils', () => ({
  tableScrollConfiguration: { x: 1000 },
}));

import { useEtrnQuery } from './hooks/useEtrnQuery';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import EtrnSignature from './EtrnSignature';

let mockData: any;
let mockSetEtrnPageSettings: jest.Mock;
let mockRefetch: jest.Mock;

describe('EtrnSignature', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSetEtrnPageSettings = jest.fn();
    mockRefetch = jest.fn();
    mockData = undefined;
    (useEtrnQuery as jest.Mock).mockImplementation(() => ({ data: mockData, refetch: mockRefetch }));
    (useAppStoreContext as jest.Mock).mockReturnValue({
      plannerStore: {
        setEtrnListPageSetting: { page: 1, size: 25 },
        setEtrnPageSettings: mockSetEtrnPageSettings,
      },
    });
  });

  test('должен рендерить EtrnFilters', () => {
    render(<EtrnSignature />);
    expect(screen.getByTestId('etrn-filters')).toBeInTheDocument();
  });

  test('должен показывать EmptyView когда data === undefined', () => {
    mockData = undefined;
    render(<EtrnSignature />);
    expect(screen.getByTestId('etrn-empty')).toBeInTheDocument();
    expect(screen.queryByTestId('etrn-table')).not.toBeInTheDocument();
    expect(screen.queryByTestId('pagination')).not.toBeInTheDocument();
  });

  test('должен показывать EmptyView когда data.content === undefined', () => {
    mockData = { content: undefined, totalElements: 0 };
    render(<EtrnSignature />);
    expect(screen.getByTestId('etrn-empty')).toBeInTheDocument();
    expect(screen.queryByTestId('etrn-table')).not.toBeInTheDocument();
    expect(screen.queryByTestId('pagination')).not.toBeInTheDocument();
  });

  test('должен показывать EmptyView когда data.content пустой', () => {
    mockData = { content: [], totalElements: 0 };
    render(<EtrnSignature />);
    expect(screen.getByTestId('etrn-empty')).toBeInTheDocument();
    expect(screen.queryByTestId('etrn-table')).not.toBeInTheDocument();
  });

  test('должен рендерить Table когда data.content содержит элементы', () => {
    mockData = { content: [{ id: '1' }, { id: '2' }], totalElements: 2 };
    render(<EtrnSignature />);
    expect(screen.getByTestId('etrn-table')).toBeInTheDocument();
    expect(screen.queryByTestId('etrn-empty')).not.toBeInTheDocument();
  });

  test('Table должен получать dataSource из data.content', () => {
    mockData = { content: [{ id: '1' }, { id: '2' }], totalElements: 2 };
    render(<EtrnSignature />);
    const table = screen.getByTestId('etrn-table');
    expect(table).toHaveAttribute('data-datasource', JSON.stringify([{ id: '1' }, { id: '2' }]));
  });

  test('Table должен получать columns из useTableFields', () => {
    mockData = { content: [{ id: '1' }], totalElements: 1 };
    render(<EtrnSignature />);
    const table = screen.getByTestId('etrn-table');
    expect(table).toHaveAttribute('data-cols-length', '2');
  });

  test('Pagination должен получать page и size из plannerStore.setEtrnListPageSetting', () => {
    mockData = { content: [{ id: '1' }], totalElements: 100 };
    render(<EtrnSignature />);
    const pagination = screen.getByTestId('pagination');
    expect(pagination).toHaveAttribute('data-page', '1');
    expect(pagination).toHaveAttribute('data-size', '25');
  });

  test('Pagination должен получать total из data.totalElements', () => {
    mockData = { content: [{ id: '1' }], totalElements: 100 };
    render(<EtrnSignature />);
    const pagination = screen.getByTestId('pagination');
    expect(pagination).toHaveAttribute('data-total', '100');
  });

  test('Pagination должен получать setPagination из plannerStore.setEtrnPageSettings', () => {
    mockData = { content: [{ id: '1' }], totalElements: 1 };
    render(<EtrnSignature />);
    const pagination = screen.getByTestId('pagination');
    expect(pagination).toHaveAttribute('data-set-pagination', 'true');
  });
});
