/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, waitFor } from '@testing-library/react';
import { act } from 'react-dom/test-utils';

import { SelectExecutorGroup } from './index';

jest.mock('react-router-dom', () => ({
  useLocation: jest.fn(),
}));

jest.mock('api/executor-group', () => ({
  useGetExecutorGroups: jest.fn(),
  ExecutorGroups: jest.fn(),
}));

jest.mock('shared/hooks/useCargoRoute', () => ({
  useCargoRoute: jest.fn(),
}));

jest.mock('shared/components/Select', () => ({
  Select: jest.fn(({ children }: any) => (
    <div data-testid="new-select-mock">{children}</div>
  )),
}));

jest.mock('antd', () => {
  const Option = ({ value, children }: any) => (
    <option value={value} data-testid="antd-option">
      {children}
    </option>
  );
  const Select = jest.fn(({ children }: any) => (
    <div data-testid="select-antd-mock">{children}</div>
  ));
  (Select as any).Option = Option;
  return { Select };
});

import { useLocation } from 'react-router-dom';
import { useGetExecutorGroups } from 'api/executor-group';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';
import { Select as AntdSelect } from 'antd';
import { Select as NewSelect } from 'shared/components/Select';

const mockUseLocation = useLocation as jest.Mock;
const mockUseGetExecutorGroups = useGetExecutorGroups as jest.Mock;
const mockUseCargoRoute = useCargoRoute as jest.Mock;

const ANTD_SELECT = AntdSelect as unknown as jest.Mock;
const NEW_SELECT = NewSelect as unknown as jest.Mock;

const mockGroups = [
  {
    id: 'g1', name: 'Группа 1', active: true,
  },
  {
    id: 'g2', name: 'Группа 2', active: true,
  },
  {
    id: 'g3', name: 'Группа 3', active: false,
  },
];

const setupMocks = (overrides: {
  isCargo?: boolean;
  data?: any;
  isLoading?: boolean;
} = {}) => {
  const {
    isCargo = false, data, isLoading = false,
  } = overrides;
  mockUseLocation.mockReturnValue({ pathname: '/client/home', search: '' });
  mockUseCargoRoute.mockReturnValue({
    isCargoAny: isCargo,
    isCargoOrderExecution: false,
    isCargoOrders: false,
    isCargoMultiLogistics: false,
    isCargoRoutes: false,
    isEtrnTab: false,
  });
  mockUseGetExecutorGroups.mockReturnValue({
    data: 'data' in overrides ? data : { content: mockGroups },
    isLoading,
  });
};

const getLastAntdSelectProps = () => {
  const calls = ANTD_SELECT.mock.calls;
  return calls[calls.length - 1][0];
};

const getLastNewSelectProps = () => {
  const calls = NEW_SELECT.mock.calls;
  return calls[calls.length - 1][0];
};

const fireSelectChange = (value: any) => {
  const props = getLastAntdSelectProps();
  act(() => {
    props.onChange(value, undefined);
  });
};

describe('SelectExecutorGroup', () => {
  beforeEach(() => {
    ANTD_SELECT.mockClear();
    NEW_SELECT.mockClear();
    mockUseGetExecutorGroups.mockClear();
    mockUseCargoRoute.mockClear();
    mockUseLocation.mockClear();
    setupMocks();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Рендер списка опций', () => {
    test('отображает только активные группы исполнителей', () => {
      const { getAllByTestId } = render(<SelectExecutorGroup onListChange={jest.fn()} />);

      const options = getAllByTestId('antd-option');
      expect(options).toHaveLength(2);
      expect(options[0]).toHaveTextContent('Группа 1');
      expect(options[1]).toHaveTextContent('Группа 2');
    });

    test('не падает и рендерит пустой список когда данные не пришли', () => {
      setupMocks({ data: null });
      const { getByTestId, queryAllByTestId } = render(<SelectExecutorGroup />);

      expect(getByTestId('select-antd-mock')).toBeInTheDocument();
      expect(queryAllByTestId('antd-option')).toHaveLength(0);
    });
  });

  describe('Состояние загрузки', () => {
    test('передаёт loading=true в Select при isLoading=true', () => {
      setupMocks({ isLoading: true });
      render(<SelectExecutorGroup />);

      expect(getLastAntdSelectProps().loading).toBe(true);
    });

    test('передаёт loading=false в Select при isLoading=false', () => {
      render(<SelectExecutorGroup />);

      expect(getLastAntdSelectProps().loading).toBe(false);
    });
  });

  describe('Выбор компонента Select', () => {
    test('использует SelectAntd по умолчанию (isNewDesign=false)', () => {
      const { getByTestId, queryByTestId } = render(
        <SelectExecutorGroup isNewDesign={false} />
      );

      expect(getByTestId('select-antd-mock')).toBeInTheDocument();
      expect(queryByTestId('new-select-mock')).not.toBeInTheDocument();
    });

    test('использует NewSelect при isNewDesign=true', () => {
      const { getByTestId, queryByTestId } = render(
        <SelectExecutorGroup isNewDesign={true} />
      );

      expect(getByTestId('new-select-mock')).toBeInTheDocument();
      expect(queryByTestId('select-antd-mock')).not.toBeInTheDocument();
    });
  });

  describe('Параметр isHome', () => {
    test('не применяет CSS-класс selectFieldExecutor когда isHome=true', () => {
      const { container } = render(<SelectExecutorGroup isHome={true} />);

      const antdMock = container.querySelector('[data-testid="select-antd-mock"]') as HTMLElement;
      expect(antdMock.className).not.toContain('selectFieldExecutor');
    });

    test('передаёт placeholder="Группы исполнителей" когда isHome=true', () => {
      render(<SelectExecutorGroup isHome={true} />);

      const props = getLastAntdSelectProps();
      expect(props.placeholder).toBe('Группы исполнителей');
    });

    test('передаёт placeholder=false когда isHome=false', () => {
      render(<SelectExecutorGroup isHome={false} />);

      const props = getLastAntdSelectProps();
      expect(props.placeholder).toBe(false);
    });

    test('передаёт maxTagCount="responsive" когда isHome=true', () => {
      render(<SelectExecutorGroup isHome={true} />);

      const props = getLastAntdSelectProps();
      expect(props.maxTagCount).toBe('responsive');
    });

    test('передаёт maxTagCount=undefined когда isHome=false', () => {
      render(<SelectExecutorGroup isHome={false} />);

      const props = getLastAntdSelectProps();
      expect(props.maxTagCount).toBeUndefined();
    });
  });

  describe('Интеграция с useCargoRoute', () => {
    test('передаёт isCargo=true в useGetExecutorGroups для грузового раздела', () => {
      setupMocks({ isCargo: true });
      render(<SelectExecutorGroup />);

      expect(mockUseGetExecutorGroups).toHaveBeenCalledWith(
        { page: 0, size: 200 },
        true
      );
    });

    test('передаёт isCargo=false в useGetExecutorGroups для негрузового раздела', () => {
      setupMocks({ isCargo: false });
      render(<SelectExecutorGroup />);

      expect(mockUseGetExecutorGroups).toHaveBeenCalledWith(
        { page: 0, size: 200 },
        false
      );
    });
  });

  describe('Фильтрация значений из defaultValue', () => {
    test('отображает только валидные ID, отфильтровывая отсутствующие', () => {
      render(<SelectExecutorGroup defaultValue={['g1', 'g-unknown', 'g2']} />);

      const props = getLastAntdSelectProps();
      expect(props.value).toEqual(['g1', 'g2']);
    });

    test('корректно обрабатывает пустой массив defaultValue', () => {
      render(<SelectExecutorGroup defaultValue={[]} />);

      const props = getLastAntdSelectProps();
      expect(props.value).toEqual([]);
    });

    test('корректно обрабатывает defaultValue=undefined', () => {
      render(<SelectExecutorGroup defaultValue={undefined as any} />);

      const props = getLastAntdSelectProps();
      expect(props.value).toEqual([]);
    });
  });

  describe('Синхронизация defaultValue через useEffect', () => {
    test('обновляет внутреннее значение при изменении defaultValue', async () => {
      const { rerender } = render(<SelectExecutorGroup defaultValue={['g1']} />);

      expect(getLastAntdSelectProps().value).toEqual(['g1']);

      rerender(<SelectExecutorGroup defaultValue={['g2']} />);

      await waitFor(() => {
        expect(getLastAntdSelectProps().value).toEqual(['g2']);
      });
    });
  });

  describe('Вызов callback onListChange', () => {
    test('вызывает onListChange со списком активных групп при получении данных', () => {
      const onListChange = jest.fn();
      render(<SelectExecutorGroup onListChange={onListChange} />);

      expect(onListChange).toHaveBeenCalledWith(mockGroups);
    });

    test('не вызывает onListChange если данные отсутствуют', () => {
      setupMocks({ data: null });
      const onListChange = jest.fn();
      render(<SelectExecutorGroup onListChange={onListChange} />);

      expect(onListChange).not.toHaveBeenCalled();
    });
  });

  describe('Обработка изменений handleChange', () => {
    test('вызывает props.onChange с новым значением при изменении', () => {
      const onChange = jest.fn();
      render(<SelectExecutorGroup defaultValue={['g1']} onChange={onChange} />);

      fireSelectChange(['new-id-1', 'new-id-2']);

      expect(onChange).toHaveBeenCalledWith(['new-id-1', 'new-id-2'], undefined);
    });

    test('передаёт undefined в props.onChange при очистке', () => {
      const onChange = jest.fn();
      render(<SelectExecutorGroup defaultValue={['g1']} onChange={onChange} />);

      fireSelectChange(undefined);

      // handleChange передаёт в props.onChange тот же newValue, что получил.
      // Внутри он сам преобразует в [] через setFullValue(newValue || []).
      expect(onChange).toHaveBeenCalledWith(undefined, undefined);
    });

    test('после очистки внутреннее значение становится пустым', async () => {
      render(<SelectExecutorGroup defaultValue={['g1']} />);

      expect(getLastAntdSelectProps().value).toEqual(['g1']);

      fireSelectChange(undefined);

      await waitFor(() => {
        expect(getLastAntdSelectProps().value).toEqual([]);
      });
    });

    test('после выбора валидных значений отображает их в value', async () => {
      render(<SelectExecutorGroup defaultValue={['g1']} />);

      // Используем валидные ID из mockGroups (g1, g2)
      fireSelectChange(['g1', 'g2']);

      await waitFor(() => {
        expect(getLastAntdSelectProps().value).toEqual(['g1', 'g2']);
      });
    });

    test('не вызывает props.onChange если он не передан', () => {
      render(<SelectExecutorGroup defaultValue={['g1']} />);

      // Не должно бросить ошибку
      expect(() => fireSelectChange(['x'])).not.toThrow();
    });
  });

  describe('Передача filterOption и filterSort в Select', () => {
    test('SelectAntd получает filterOption и filterSort функции', () => {
      render(<SelectExecutorGroup isNewDesign={false} />);

      const props = getLastAntdSelectProps();
      expect(typeof props.filterOption).toBe('function');
      expect(typeof props.filterSort).toBe('function');
    });

    test('filterOption находит опции по подстроке', () => {
      render(<SelectExecutorGroup isNewDesign={false} />);

      const props = getLastAntdSelectProps();
      const option = { children: 'Группа 1' };
      expect(props.filterOption('1', option)).toBe(true);
      expect(props.filterOption('xyz', option)).toBe(false);
    });

    test('filterSort сортирует опции по алфавиту без учёта регистра', () => {
      render(<SelectExecutorGroup isNewDesign={false} />);

      const props = getLastAntdSelectProps();
      const a = { children: 'бананы' };
      const b = { children: 'Апельсины' };
      expect(props.filterSort(a, b)).toBeGreaterThan(0);
      expect(props.filterSort(b, a)).toBeLessThan(0);
    });

    test('NewSelect также получает filterOption и filterSort', () => {
      render(<SelectExecutorGroup isNewDesign={true} />);

      const props = getLastNewSelectProps();
      expect(typeof props.filterOption).toBe('function');
      expect(typeof props.filterSort).toBe('function');
    });
  });

  describe('Режим и общие пропсы', () => {
    test('включает режим multiple, showSearch, allowClear, showArrow', () => {
      render(<SelectExecutorGroup isNewDesign={false} />);

      const props = getLastAntdSelectProps();
      expect(props.mode).toBe('multiple');
      expect(props.showSearch).toBe(true);
      expect(props.allowClear).toBe(true);
      expect(props.showArrow).toBe(true);
    });
  });

  describe('Передача внешних пропсов в Select', () => {
    test('пробрасывает кастомный suffixIcon', () => {
      const suffixIcon = <span data-testid="custom-suffix">x</span>;
      render(<SelectExecutorGroup suffixIcon={suffixIcon} />);

      const props = getLastAntdSelectProps();
      expect(props.suffixIcon).toBe(suffixIcon);
    });
  });
});
