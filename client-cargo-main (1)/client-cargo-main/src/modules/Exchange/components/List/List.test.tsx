/**
 * Unit тесты для компонента List
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { List } from './List';

// Мокаем observer, чтобы он просто возвращал компонент
jest.mock('mobx-react', () => ({
  observer: (component: any) => component,
}));

// Мокаем зависимости на верхнем уровне
jest.mock('react-router-dom', () => ({
  useRouteMatch: jest.fn(() => ({ params: { type: 'incoming' } })),
}));

jest.mock('shared/hooks/useEmpContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('shared/hooks/useModal', () => ({
  useModalState: jest.fn(),
}));

jest.mock('../../api/filters', () => ({
  useGetFilters: jest.fn(),
}));

jest.mock('antd', () => ({
  List: ({ children, dataSource }: any) => (
    <ul data-testid="antd-list">
      {children}
    </ul>
  ),
  Pagination: ({ current, total, pageSize, onChange, onShowSizeChange }: any) => (
    <div data-testid="pagination">
      <button onClick={() => onChange(2)} data-testid="change-page">
        Change Page
      </button>
      <button onClick={() => onShowSizeChange(1, 20)} data-testid="change-size">
        Change Size
      </button>
      <span>Current: {current}</span>
      <span>Total: {total}</span>
      <span>PageSize: {pageSize}</span>
    </div>
  ),
}));

jest.mock('antd/lib/locale/ru_RU', () => ({
  Pagination: {},
}));

jest.mock('shared/EmptyFactory', () => ({
  EmptyRequestFilterListExchange: () => <div data-testid="empty-state">Нет данных</div>,
}));

jest.mock('../ListItem/listItem', () => ({
  ListItem: ({ request }: any) => <div data-testid={`item-${request.id}`}>{request.title}</div>,
}));

jest.mock('./Controls/Controls', () => ({
  Controls: ({ onShow, descriptionAddressFrom, descriptionAddressTo }: any) => (
    <div data-testid="controls">
      <button onClick={onShow} data-testid="show-modal">Показать фильтры</button>
      <span data-testid="address-from">{descriptionAddressFrom || '—'}</span>
      <span data-testid="address-to">{descriptionAddressTo || '—'}</span>
    </div>
  ),
}));

jest.mock('../Modals/FiltersModal/FiltersModal', () => ({
  FiltersModal: ({ visible, handleClose }: any) => (
    visible ? (
      <div data-testid="filters-modal">
        <button onClick={handleClose} data-testid="close-modal">Закрыть</button>
      </div>
    ) : null
  ),
}));

describe('List Component', () => {
  const createMockStore = (overrides: Record<string, any> = {}) => ({
    exchangeRequest: {
      totalElements: 100,
      content: [
        { id: '1', title: 'Request 1' },
        { id: '2', title: 'Request 2' },
        { id: '3', title: 'Request 3' },
      ],
      ...((overrides as any).exchangeRequest),
    },
    pageSetting: {
      page: 0,
      size: 10,
    },
    setPageSetting: jest.fn(),
    setSortSetting: jest.fn(),
    resetSettings: jest.fn(),
    ...overrides,
  });

  let mockModalActions: { show: jest.Mock; hide: jest.Mock };
  let useAppStoreContextMock: jest.Mock;
  let useModalStateMock: jest.Mock;
  let useGetFiltersMock: jest.Mock;

  beforeEach(() => {
    jest.clearAllMocks();
    mockModalActions = { show: jest.fn(), hide: jest.fn() };

    useAppStoreContextMock = require('shared/hooks/useEmpContext').useAppStoreContext as jest.Mock;
    useModalStateMock = require('shared/hooks/useModal').useModalState as jest.Mock;
    useGetFiltersMock = require('../../api/filters').useGetFilters as jest.Mock;

    useAppStoreContextMock.mockReturnValue({ exchangeStore: createMockStore() });
    useModalStateMock.mockReturnValue([false, mockModalActions]);
    useGetFiltersMock.mockReturnValue({ data: null, refetch: jest.fn() });
  });

  it('должен отрендерить список с данными', () => {
    render(<List />);

    expect(screen.getByTestId('controls')).toBeInTheDocument();
    expect(screen.getByTestId('item-1')).toBeInTheDocument();
    expect(screen.getByTestId('item-2')).toBeInTheDocument();
    expect(screen.getByTestId('item-3')).toBeInTheDocument();
  });

  it('должен показать пустое состояние, когда нет контента', () => {
    useAppStoreContextMock.mockReturnValue({
      exchangeStore: createMockStore({
        exchangeRequest: {
          totalElements: 0,
          content: [],
        },
      }),
    });

    render(<List />);

    expect(screen.getByTestId('empty-state')).toBeInTheDocument();
  });

  it('должен загрузить сохраненные фильтры при первом рендере', async () => {
    const savedFilters = {
      addressFrom: 'Москва',
      addressTo: 'Санкт-Петербург',
    };

    useGetFiltersMock.mockReturnValue({ data: savedFilters, refetch: jest.fn() });

    render(<List />);

    await waitFor(() => {
      expect(screen.getByTestId('address-from')).toHaveTextContent('Москва');
      expect(screen.getByTestId('address-to')).toHaveTextContent('Санкт-Петербург');
    });
  });

  it('должен показать "—" для пустых адресов', () => {
    render(<List />);

    expect(screen.getByTestId('address-from')).toHaveTextContent('—');
    expect(screen.getByTestId('address-to')).toHaveTextContent('—');
  });

  it('должен открыть модальное окно при клике', () => {
    render(<List />);

    fireEvent.click(screen.getByTestId('show-modal'));

    expect(mockModalActions.show).toHaveBeenCalled();
  });

  it('должен показать модальное окно когда visible=true', () => {
    useModalStateMock.mockReturnValue([true, mockModalActions]);

    render(<List />);

    expect(screen.getByTestId('filters-modal')).toBeInTheDocument();
  });

  it('должен обработать изменение страницы', () => {
    const mockStore = createMockStore();
    useAppStoreContextMock.mockReturnValue({
      exchangeStore: mockStore,
    });

    render(<List />);

    fireEvent.click(screen.getByTestId('change-page'));

    expect(mockStore.setPageSetting).toHaveBeenCalledWith({
      page: 1,
      size: 10,
    });
  });

  it('должен обработать изменение размера страницы', () => {
    const mockStore = createMockStore();
    useAppStoreContextMock.mockReturnValue({
      exchangeStore: mockStore,
    });

    render(<List />);

    fireEvent.click(screen.getByTestId('change-size'));

    expect(mockStore.setPageSetting).toHaveBeenCalledWith({
      page: 0,
      size: 20,
    });
  });

  it('должен отобразить пагинацию с правильными параметрами', () => {
    render(<List />);

    expect(screen.getByTestId('pagination')).toBeInTheDocument();
    expect(screen.getByText('Current: 1')).toBeInTheDocument();
    expect(screen.getByText('Total: 100')).toBeInTheDocument();
    expect(screen.getByText('PageSize: 10')).toBeInTheDocument();
  });

  it('не должен перезаписывать существующие адреса при повторном рендере', async () => {
    const savedFilters = {
      addressFrom: 'Москва',
      addressTo: 'СПб',
    };

    useGetFiltersMock.mockReturnValue({ data: savedFilters, refetch: jest.fn() });

    const { rerender, unmount } = render(<List />);

    await waitFor(() => {
      expect(screen.getByTestId('address-from')).toHaveTextContent('Москва');
    });

    useModalStateMock.mockReturnValue([false, mockModalActions]);
    useGetFiltersMock.mockReturnValue({ data: savedFilters, refetch: jest.fn() });

    rerender(<List />);

    await waitFor(() => {
      expect(screen.getByTestId('address-from')).toHaveTextContent('Москва');
    });

    unmount();
  });
});
