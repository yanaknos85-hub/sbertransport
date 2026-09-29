import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useLocation } from 'react-router-dom';

import { SelectExecutorGroup } from './index';
import { useGetExecutorGroups } from 'api/executor-group';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';

jest.mock('react-router-dom', () => ({
  useLocation: jest.fn(),
}));

jest.mock('api/executor-group', () => ({
  useGetExecutorGroups: jest.fn(),
}));

jest.mock('shared/hooks/useCargoRoute', () => ({
  useCargoRoute: jest.fn(),
}));

const mockUseLocation = useLocation as jest.Mock;
const mockUseGetExecutorGroups = useGetExecutorGroups as jest.Mock;
const mockUseCargoRoute = useCargoRoute as jest.Mock;

const makeUser = () => userEvent.setup({ pointerEventsCheck: 0 });

const setCargoRoute = () => mockUseCargoRoute.mockReturnValue({
  isCargoOrderExecution: true,
  isCargoOrders: false,
  isCargoMultiLogistics: false,
  isCargoRoutes: false,
  isCargoAny: true,
});

const setNonCargoRoute = () => mockUseCargoRoute.mockReturnValue({
  isCargoOrderExecution: false,
  isCargoOrders: false,
  isCargoMultiLogistics: false,
  isCargoRoutes: false,
  isCargoAny: false,
});

describe('SelectExecutorGroup — спец-опции (TRANSPORT-43949)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseLocation.mockReturnValue({ pathname: '/client/order-execution/cargo' });
    mockUseGetExecutorGroups.mockReturnValue({
      data: { content: [] },
      isLoading: false,
    });
  });

  it('рендерит селект в грузовом разделе', () => {
    setCargoRoute();

    render(<SelectExecutorGroup />);

    expect(document.querySelector('.ant-select')).toBeInTheDocument();
  });

  it('выбор «Без групп» → onChange([]), onEmptyChange(true), onAllChange(false)', async () => {
    setCargoRoute();
    const onChange = jest.fn();
    const onEmptyChange = jest.fn();
    const onAllChange = jest.fn();
    const user = makeUser();

    render(
      <SelectExecutorGroup
        onChange={onChange}
        onEmptyChange={onEmptyChange}
        onAllChange={onAllChange}
      />
    );

    await user.click(screen.getByRole('combobox'));
    const item = await screen.findByText('Без групп');
    await user.click(item);

    await waitFor(() => {
      expect(onChange).toHaveBeenCalledWith([]);
    });
    expect(onEmptyChange).toHaveBeenCalledWith(true);
    expect(onAllChange).toHaveBeenCalledWith(false);
  });

  it('выбор «Все группы» → onChange([]), onEmptyChange(false), onAllChange(true)', async () => {
    setCargoRoute();
    const onChange = jest.fn();
    const onEmptyChange = jest.fn();
    const onAllChange = jest.fn();
    const user = makeUser();

    render(
      <SelectExecutorGroup
        onChange={onChange}
        onEmptyChange={onEmptyChange}
        onAllChange={onAllChange}
      />
    );

    await user.click(screen.getByRole('combobox'));
    const item = await screen.findByText('Все группы');
    await user.click(item);

    await waitFor(() => {
      expect(onChange).toHaveBeenCalledWith([]);
    });
    expect(onEmptyChange).toHaveBeenCalledWith(false);
    expect(onAllChange).toHaveBeenCalledWith(true);
  });

  it('выбор реальной группы → onChange([id])', async () => {
    setCargoRoute();
    mockUseGetExecutorGroups.mockReturnValue({
      data: {
        content: [
          {
            id: 'g-1', name: 'Группа 1', active: true,
          },
          {
            id: 'g-2', name: 'Группа 2', active: true,
          },
        ],
      },
      isLoading: false,
    });

    const onChange = jest.fn();
    const user = makeUser();

    render(<SelectExecutorGroup onChange={onChange} />);

    await user.click(screen.getByRole('combobox'));
    const item = await screen.findByText('Группа 1');
    await user.click(item);

    await waitFor(() => {
      expect(onChange).toHaveBeenCalledWith(['g-1']);
    });
  });

  it('при emptyActive=true показывает «Без групп» как выбранное значение', () => {
    setCargoRoute();

    const { container } = render(<SelectExecutorGroup emptyActive onChange={jest.fn()} />);

    const items = container.querySelectorAll('.ant-select-selection-item');
    const labels = Array.from(items).map(el => el.textContent);
    expect(labels).toContain('Без групп');
  });

  it('при allActive=true показывает «Все группы» как выбранное значение', () => {
    setCargoRoute();

    const { container } = render(<SelectExecutorGroup allActive onChange={jest.fn()} />);

    const items = container.querySelectorAll('.ant-select-selection-item');
    const labels = Array.from(items).map(el => el.textContent);
    expect(labels).toContain('Все группы');
  });

  it('не показывает спец-опции вне грузового раздела', () => {
    setNonCargoRoute();

    const { container } = render(<SelectExecutorGroup />);

    // Никаких скрытых options в DOM (antd не рендерит их пока не открыт)
    expect(container.querySelector('.ant-select-selection-item')).not.toBeInTheDocument();
  });
});
