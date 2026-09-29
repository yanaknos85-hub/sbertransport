/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import { Pagination } from './Pagination';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';

// ========================
//        Моки
// ========================

jest.mock('antd', () => {
  interface MockSelectProps {
    value?: number;
    onChange?: (value: number) => void;
    open?: boolean;
  }

  interface MockPaginationProps {
    pageSize?: number;
    current?: number;
    total?: number;
    onChange?: (page: number) => void;
  }

  const MockSelect = ({ value, onChange, open }: MockSelectProps) => (
    <div data-testid="mock-select" data-open={String(!!open)}>
      <select
        data-testid="select-inner"
        value={value}
        onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
          onChange?.(Number(e.target.value))
        }
      >
        <option value={10}>10</option>
        <option value={20}>20</option>
        <option value={50}>50</option>
        <option value={100}>100</option>
      </select>
    </div>
  );

  const MockAntdPagination = ({ pageSize, current, total, onChange }: MockPaginationProps) => (
    <div
      data-testid="antd-pagination"
      data-page-size={pageSize}
      data-current={current}
      data-total={total}
    >
      <button data-testid="page-btn-1" onClick={() => onChange?.(1)}>1</button>
      <button data-testid="page-btn-2" onClick={() => onChange?.(2)}>2</button>
      <button data-testid="page-btn-3" onClick={() => onChange?.(3)}>3</button>
    </div>
  );

  return {
    __esModule: true,
    Pagination: MockAntdPagination,
    Select: MockSelect,
  };
});

jest.mock('./Pagination.module.scss', () => ({
  paginationContainer: 'paginationContainer',
  sizeSelector: 'sizeSelector',
  selectDescription: 'selectDescription',
  select: 'select',
  pagination: 'pagination',
}));

// ========================
//       Helpers
// ========================

const basePagination: PaginationParams = { page: 0, size: 10 };
const defaultSetPagination = jest.fn();

const renderPagination = (props: Partial<{
  pagination: PaginationParams;
  total: number;
  setPagination: jest.Mock;
}> = {}) => {
  const setPagination = props.setPagination ?? defaultSetPagination;
  return render(
    <Pagination
      pagination={props.pagination ?? basePagination}
      total={props.total ?? 100}
      setPagination={setPagination}
    />,
  );
};

// ========================
//        Тесты
// ========================

describe('Pagination', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('рендеринг', () => {
    it('должен рендерить antd Pagination', () => {
      renderPagination();
      expect(screen.getByTestId('antd-pagination')).toBeInTheDocument();
    });

    it('должен рендерить селект выбора размера страницы', () => {
      renderPagination();
      expect(screen.getByTestId('mock-select')).toBeInTheDocument();
    });

    it('должен показывать подпись "Показывать по"', () => {
      renderPagination();
      expect(screen.getByText('Показывать по')).toBeInTheDocument();
    });

    it('должен передавать в antd Pagination pageSize из pagination.size', () => {
      renderPagination({ pagination: { page: 0, size: 20 } });
      expect(screen.getByTestId('antd-pagination')).toHaveAttribute('data-page-size', '20');
    });

    it('должен передавать в antd Pagination current = pagination.page + 1', () => {
      renderPagination({ pagination: { page: 2, size: 10 } });
      expect(screen.getByTestId('antd-pagination')).toHaveAttribute('data-current', '3');
    });

    it('должен передавать в antd Pagination total', () => {
      renderPagination({ total: 250 });
      expect(screen.getByTestId('antd-pagination')).toHaveAttribute('data-total', '250');
    });

    it('должен передавать в Select value = pagination.size', () => {
      renderPagination({ pagination: { page: 0, size: 50 } });
      const inner = screen.getByTestId('select-inner') as HTMLSelectElement;
      expect(inner.value).toBe('50');
    });

    it('должен инициализировать селект в закрытом состоянии', () => {
      renderPagination();
      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('false');
    });
  });

  describe('открытие и закрытие селекта', () => {
    it('должен открывать селект по клику на контейнер выбора размера', () => {
      const { container } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.click(sizeSelector);

      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');
    });

    it('должен закрывать селект при повторном клике на контейнер', () => {
      const { container } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.click(sizeSelector);
      fireEvent.click(sizeSelector);

      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('false');
    });

    it('должен открывать селект по нажатию клавиши на контейнере', () => {
      const { container } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.keyDown(sizeSelector);

      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');
    });

    it('должен закрывать селект при mousedown вне контейнера', () => {
      const { container } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.click(sizeSelector);
      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');

      fireEvent.mouseDown(document.body);

      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('false');
    });

    it('НЕ должен закрывать селект при mousedown внутри контейнера', () => {
      const { container } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.click(sizeSelector);
      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');

      fireEvent.mouseDown(screen.getByText('Показывать по'));

      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');
    });

    it('НЕ должен закрывать селект при mousedown на .ant-select-dropdown', () => {
      const { container } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.click(sizeSelector);
      expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');

      const dropdown = document.createElement('div');
      dropdown.className = 'ant-select-dropdown';
      document.body.appendChild(dropdown);

      try {
        fireEvent.mouseDown(dropdown);
        expect(screen.getByTestId('mock-select').getAttribute('data-open')).toBe('true');
      } finally {
        document.body.removeChild(dropdown);
      }
    });

    it('должен удалять document listener при unmount с открытым селектом', () => {
      const removeSpy = jest.spyOn(document, 'removeEventListener');
      const { container, unmount } = renderPagination();
      const sizeSelector = container.querySelector('.sizeSelector') as HTMLElement;

      fireEvent.click(sizeSelector);

      unmount();

      expect(removeSpy).toHaveBeenCalledWith('mousedown', expect.any(Function));
      removeSpy.mockRestore();
    });
  });

  describe('смена страницы', () => {
    it('должен вызвать setPagination с page - 1 при клике на страницу', () => {
      const setPagination = jest.fn();
      renderPagination({ setPagination });

      fireEvent.click(screen.getByTestId('page-btn-2'));

      expect(setPagination).toHaveBeenCalledTimes(1);
      expect(setPagination).toHaveBeenCalledWith({ page: 1, size: 10 });
    });

    it('должен сохранять size при смене страницы', () => {
      const setPagination = jest.fn();
      renderPagination({
        pagination: { page: 0, size: 50 },
        setPagination,
      });

      fireEvent.click(screen.getByTestId('page-btn-3'));

      expect(setPagination).toHaveBeenCalledWith({ page: 2, size: 50 });
    });
  });

  describe('смена размера страницы', () => {
    it('должен вызвать setPagination с новым size при изменении селекта', () => {
      const setPagination = jest.fn();
      renderPagination({ setPagination });

      fireEvent.change(screen.getByTestId('select-inner'), {
        target: { value: '20' },
      });

      expect(setPagination).toHaveBeenCalledTimes(1);
      expect(setPagination).toHaveBeenCalledWith({ page: 0, size: 20 });
    });

    it('должен сохранять page при изменении size', () => {
      const setPagination = jest.fn();
      renderPagination({
        pagination: { page: 5, size: 10 },
        setPagination,
      });

      fireEvent.change(screen.getByTestId('select-inner'), {
        target: { value: '100' },
      });

      expect(setPagination).toHaveBeenCalledWith({ page: 5, size: 100 });
    });
  });
});
