/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { renderHook, act } from '@testing-library/react-hooks';
import { useColumns } from './useColumns';
import { Modal } from 'antd';
import { useCancelTask } from 'api/cargo-registry-compensations-search';

jest.mock('antd', () => ({
  Modal: {
    confirm: jest.fn(),
  },
  Space: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Tooltip: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

jest.mock('api/cargo-registry-compensations-search', () => ({
  useCancelTask: jest.fn(),
}));

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Forms: {
        registryCargoCompensationsReports: {
          tableColumns: {
            status: 'Статус',
            url: 'URL',
            filterRequest: 'Параметры фильтра',
            creationTime: 'Дата создания',
            actions: 'Действия',
            download: 'Скачать',
            cancel: 'Отменить',
          },
          cancelModal: {
            confirmCancelTitle: 'Подтверждение отмены',
            yes: 'Да',
            no: 'Нет',
          },
        },
      },
    },
  }),
}));

jest.mock('components/DownloadButton', () => () => <div>DownloadButton</div>);

jest.mock('utils/formatFilterRequest', () => ({
  formatFilterRequest: () => (value: any) => JSON.stringify(value),
}));

jest.mock('@ant-design/icons', () => ({
  DownloadOutlined: () => <div data-testid="download-icon">DownloadIcon</div>,
  CloseCircleOutlined: ({
    onClick, style, disabled,
  }: any) => (
    <div
      data-testid="close-icon"
      onClick={onClick}
      style={style}
      data-disabled={disabled}
    >
      CloseIcon
    </div>
  ),
}));

jest.mock('../constants', () => ({
  FIELD_LABELS: {},
  TaskStatuses: {
    DONE: 'DONE',
    WAIT: 'WAIT',
    IN_PROGRESS: 'IN_PROGRESS',
  },
}));

jest.mock('constants/constants.app', () => ({
  emptySign: '-',
  DATE_FORMAT: {
    BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  },
}));

jest.mock('constants/constants.api', () => ({
  CARGO_COMPENSATION: '/api/cargo-compensation/',
}));

jest.mock('moment', () => {
  return {
    utc: (date: any) => ({
      format: () => {
        if (Array.isArray(date) && date.length >= 3) {
          const [year, month, day] = date;
          return `${day.toString().padStart(2, '0')}.${(month).toString().padStart(2, '0')}.${year}`;
        }
        return '01.01.2024';
      },
    }),
  };
});

import { TaskStatuses } from '../constants';

describe('useColumns', () => {
  const mockCancelTask = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useCancelTask as jest.Mock).mockReturnValue([mockCancelTask, { isLoading: false }]);
  });

  it('должен возвращать массив колонок', () => {
    const { result } = renderHook(() => useColumns());

    expect(Array.isArray(result.current)).toBe(true);
    expect(result.current.length).toBe(5);

    const keys = result.current.map(col => col.key);
    expect(keys).toEqual(['status', 'url', 'filterRequest', 'creationTime', 'actions', undefined]);
  });

  it('должен корректно рендерить filterRequest', () => {
    const { result } = renderHook(() => useColumns());
    const filterColumn = result.current.find(col => col.key === 'filterRequest');

    expect(filterColumn).toBeDefined();
    expect(filterColumn).toHaveProperty('render');

    const mockFilterRequest = { field1: 'value1', field2: 'value2' };
    const rendered = filterColumn?.render?.(mockFilterRequest, {} as any, 0);

    expect(rendered).toBe(JSON.stringify(mockFilterRequest));
  });

  it('должен корректно форматировать creationTime', () => {
    const { result } = renderHook(() => useColumns());
    const creationColumn = result.current.find(col => col.key === 'creationTime');

    expect(creationColumn).toBeDefined();
    expect(creationColumn).toHaveProperty('render');

    const mockCreationTime = [2024, 3, 15];
    const rendered = creationColumn?.render?.(mockCreationTime, {} as any, 0);

    expect(typeof rendered).toBe('string');
    expect(rendered).not.toBe('-');
  });

  it('должен возвращать emptySign для некорректного creationTime', () => {
    const { result } = renderHook(() => useColumns());
    const creationColumn = result.current.find(col => col.key === 'creationTime');

    const invalidDates = [
      null,
      undefined,
      [2024],
      [2024, 3],
      'invalid',
    ];

    invalidDates.forEach(date => {
      const rendered = creationColumn?.render?.(date as any, {} as any, 0);
      expect(rendered).toBe('-');
    });
  });

  describe('колонка actions', () => {
    it('должен показывать кнопку загрузки для статуса DONE', () => {
      const { result } = renderHook(() => useColumns());
      const actionsColumn = result.current.find(col => col.key === 'actions');

      expect(actionsColumn).toBeDefined();
      expect(actionsColumn).toHaveProperty('render');

      const mockRecord = {
        id: '1',
        status: TaskStatuses.DONE,
        url: '/test.xlsx',
      };

      const rendered = actionsColumn?.render?.('', mockRecord as any, 0);

      expect(rendered).toBeDefined();
    });

    it('должен показывать кнопку отмены для статуса WAIT', () => {
      const { result } = renderHook(() => useColumns());
      const actionsColumn = result.current.find(col => col.key === 'actions');

      const mockRecord = {
        id: '2',
        status: TaskStatuses.WAIT,
      };

      const rendered = actionsColumn?.render?.('', mockRecord as any, 0);

      expect(rendered).toBeDefined();
    });

    it('должен показывать кнопку отмены для статуса IN_PROGRESS', () => {
      const { result } = renderHook(() => useColumns());
      const actionsColumn = result.current.find(col => col.key === 'actions');

      const mockRecord = {
        id: '3',
        status: TaskStatuses.IN_PROGRESS,
      };

      const rendered = actionsColumn?.render?.('', mockRecord as any, 0);

      expect(rendered).toBeDefined();
    });

    it('не должен показывать кнопки для других статусов', () => {
      const { result } = renderHook(() => useColumns());
      const actionsColumn = result.current.find(col => col.key === 'actions');

      const mockRecord = {
        id: '4',
        status: 'OTHER_STATUS',
      };

      const rendered = actionsColumn?.render?.('', mockRecord as any, 0);

      expect(rendered).toBeDefined();
    });
  });

  describe('handleCancelTask', () => {
    it('должен открывать модальное окно подтверждения при клике на отмену', () => {
      const { result } = renderHook(() => useColumns());
      const actionsColumn = result.current.find(col => col.key === 'actions');

      expect(actionsColumn).toBeDefined();

      const mockRecord = { id: '5', status: TaskStatuses.WAIT };

      const rendered = actionsColumn?.render?.('', mockRecord as any, 0) as any;

      expect(rendered).toBeDefined();
      expect(rendered.props).toBeDefined();
      expect(Modal.confirm).not.toHaveBeenCalled();
    });

    it('должен вызывать cancelTask при подтверждении', async () => {
      const { result } = renderHook(() => useColumns());
      const actionsColumn = result.current.find(col => col.key === 'actions');
      expect(actionsColumn).toBeDefined();
      const mockRecord = { id: '6', status: TaskStatuses.WAIT };
      const renderedCell = actionsColumn?.render?.('', mockRecord as any, 0);
      const cancelIcon = (renderedCell as any).props.children[1].props.children;
      act(() => {
        cancelIcon.props.onClick();
      });

      expect(Modal.confirm).toHaveBeenCalledTimes(1);

      const confirmCall = (Modal.confirm as jest.Mock).mock.calls[0][0];
      await act(async () => {
        await confirmCall.onOk();
      });

      expect(mockCancelTask).toHaveBeenCalledWith('6');
    });
  });

  it('должен обновляться при изменении isLoading', () => {
    const { result, rerender } = renderHook(() => useColumns());

    const firstResult = result.current;

    (useCancelTask as jest.Mock).mockReturnValue([mockCancelTask, { isLoading: true }]);
    rerender();

    expect(result.current).not.toBe(firstResult);
  });
});
