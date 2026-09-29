/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import FilterSection from '.';

// Мокируем все зависимости компонента
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn(),
}));

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}));

jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => {
    const result = args.filter(Boolean).join(' ');
    return result;
  }),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('../ItemContainer', () => ({
  Section: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="section">{children}</div>
  ),
}));

jest.mock('../Icon', () => ({
  FilterIcon: () => <span data-testid="filter-icon">FilterIcon</span>,
}));

jest.mock('../FilterCategory', () => ({
  __esModule: true,
  default: () => <div data-testid="filter-category">FilterCategory</div>,
}));

jest.mock('../Filter', () => ({
  __esModule: true,
  default: ({ category, onClose }: { category: any; onClose: () => void }) => (
    <div data-testid="filter">
      Filter Component - Category: {category}
      <button data-testid="filter-close-button" onClick={onClose}>
        Close Filter
      </button>
    </div>
  ),
}));

jest.mock('../Filter/components/Modal', () => ({
  __esModule: true,
  default: ({
    visible,
    onCancel,
    children,
    width,
  }: {
    visible: boolean;
    onCancel: () => void;
    children: React.ReactNode;
    width: string;
  }) => {
    if (!visible) return null;
    
    return (
      <div data-testid="modal" style={{ width }}>
        <div data-testid="modal-content">{children}</div>
        <button data-testid="modal-close-button" onClick={onCancel}>
          Close Modal
        </button>
      </div>
    );
  },
}));

jest.mock('./styles.module.scss', () => ({
  filterSection: 'filterSection',
  filterSection__item: 'filterSection__item',
  filterSection__item_search: 'filterSection__item_search',
  filterSection__search: 'filterSection__search',
  filterSection__filterButton: 'filterSection__filterButton',
}));

// Мокаем antd компоненты с фиксом для clear button
jest.mock('antd', () => {
  const originalModule = jest.requireActual('antd');
  
  const MockInputSearch = React.forwardRef(({
    placeholder,
    size,
    enterButton,
    onSearch,
    onChange,
    allowClear,
    value,
    className,
    ...props
  }: any, ref: any) => {
    const handleClear = () => {
      if (onChange) {
        const mockEvent = {
          target: {
            value: '',
          },
          currentTarget: {
            value: '',
          },
        };
        onChange(mockEvent);
      }
    };

    const handleSearchClick = () => {
      if (onSearch) {
        onSearch(value);
      }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      if (onChange) {
        const mockEvent = {
          target: {
            value: e.target.value,
          },
          currentTarget: {
            value: e.target.value,
          },
        };
        onChange(mockEvent);
      }
    };

    return (
      <div data-testid="input-search" className={className} {...props} ref={ref}>
        <input
          type="text"
          placeholder={placeholder}
          value={value || ''}
          onChange={handleInputChange}
          data-testid="search-input"
        />
        <button
          data-testid="search-button"
          onClick={handleSearchClick}
        >
          Search
        </button>
        {allowClear && value && (
          <button
            data-testid="clear-button"
            onClick={handleClear}
          >
            Clear
          </button>
        )}
      </div>
    );
  });

  const MockButton = ({
    type,
    icon,
    size,
    onClick,
    children,
    className,
    ...props
  }: any) => (
    <button
      data-testid="filter-button"
      className={className}
      onClick={onClick}
      data-type={type}
      data-size={size}
      {...props}
    >
      {icon}
      {children}
    </button>
  );

  return {
    ...originalModule,
    Input: {
      ...originalModule.Input,
      Search: MockInputSearch,
    },
    Button: MockButton,
  };
});

describe('Компонент FilterSection', () => {
  // Моковые данные
  let mockCargoStore: any;
  let originalUseState: any;
  let useStateMock: any;

  const mockUseAppStoreContext = jest.requireMock('shared/hooks/useAppStoreContext');
  
  const renderComponent = (category?: string) => {
    const mockUseParams = jest.requireMock('react-router-dom').useParams;
    mockUseParams.mockReturnValue({ category });
    
    return render(
      <BrowserRouter>
        <FilterSection />
      </BrowserRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();

    mockCargoStore = {
      cargoQueryFilters: { id: '' },
      setCargoFilterQueryProps: jest.fn(),
      getCargoSchedulerList: jest.fn(),
      getCargoOrderList: jest.fn(),
      getCargoOrderListDeferredPost: jest.fn(),
    };
    
    mockUseAppStoreContext.useAppStoreContext.mockReturnValue({
      cargoStore: mockCargoStore,
    });

    // Мокаем useState для отслеживания вызовов
    originalUseState = React.useState;
    useStateMock = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
    if (originalUseState) {
      React.useState = originalUseState;
    }
  });

  describe('Рендеринг компонента', () => {
    it('должен рендерить FilterCategory всегда', () => {
      renderComponent('template');
      expect(screen.getByTestId('filter-category')).toBeInTheDocument();
    });

    it('должен рендерить секцию с поиском и фильтром когда category определен', () => {
      renderComponent('template');
      
      expect(screen.getByTestId('input-search')).toBeInTheDocument();
      expect(screen.getByTestId('filter-button')).toBeInTheDocument();
      expect(screen.getByTestId('search-input')).toBeInTheDocument();
    });

    it('НЕ должен рендерить секцию с поиском и фильтром когда category undefined', () => {
      renderComponent();
      
      expect(screen.getByTestId('filter-category')).toBeInTheDocument();
      expect(screen.queryByTestId('input-search')).not.toBeInTheDocument();
      expect(screen.queryByTestId('filter-button')).not.toBeInTheDocument();
    });
  });

  describe('Поиск функциональность', () => {
    it('должен обновлять значение поиска при вводе', () => {
      renderComponent('template');
      
      const searchInput = screen.getByTestId('search-input') as HTMLInputElement;
      
      fireEvent.change(searchInput, { target: { value: 'test123' } });
      
      expect(searchInput.value).toBe('test123');
    });

    it('должен вызывать поиск при клике на кнопку поиска', () => {
      renderComponent('template');
      
      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'ABC123' } });
      
      const searchButton = screen.getByTestId('search-button');
      fireEvent.click(searchButton);
      
      expect(mockCargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        id: 'ABC123',
      });
      expect(mockCargoStore.getCargoSchedulerList).toHaveBeenCalled();
    });

    it('должен вызывать getCargoSchedulerList когда category = "template"', () => {
      renderComponent('template');
      
      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'TEST' } });
      
      const searchButton = screen.getByTestId('search-button');
      fireEvent.click(searchButton);
      
      expect(mockCargoStore.getCargoSchedulerList).toHaveBeenCalled();
      expect(mockCargoStore.getCargoOrderList).not.toHaveBeenCalled();
    });

    it('должен вызывать getCargoOrderListDeferredPost когда category != "template"', () => {
      renderComponent('order');

      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'TEST' } });

      const searchButton = screen.getByTestId('search-button');
      fireEvent.click(searchButton);

      expect(mockCargoStore.getCargoOrderListDeferredPost).toHaveBeenCalled();
      expect(mockCargoStore.getCargoSchedulerList).not.toHaveBeenCalled();
    });

    it('должен очищать фильтры когда значение поиска пустое', () => {
      renderComponent('template');
      
      // Сначала устанавливаем значение
      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'test' } });
      
      // Теперь очищаем
      fireEvent.change(searchInput, { target: { value: '' } });
      
      expect(mockCargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        id: undefined,
      });
      expect(mockCargoStore.getCargoSchedulerList).toHaveBeenCalled();
    });

    it('НЕ должен выполнять поиск если значение меньше 3 символов', () => {
      renderComponent('template');
      
      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'AB' } });
      
      const searchButton = screen.getByTestId('search-button');
      fireEvent.click(searchButton);
      
      expect(mockCargoStore.setCargoFilterQueryProps).not.toHaveBeenCalled();
      expect(mockCargoStore.getCargoSchedulerList).not.toHaveBeenCalled();
    });

    it('должен выполнять поиск если значение 3 или более символов', () => {
      renderComponent('template');
      
      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'ABC' } });
      
      const searchButton = screen.getByTestId('search-button');
      fireEvent.click(searchButton);
      
      expect(mockCargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        id: 'ABC',
      });
      expect(mockCargoStore.getCargoSchedulerList).toHaveBeenCalled();
    });

    it('должен обрезать пробелы при поиске', () => {
      renderComponent('template');
      
      const searchInput = screen.getByTestId('search-input');
      // Имитируем ввод с пробелами
      fireEvent.change(searchInput, { target: { value: '  ABC123  ' } });
      
      const searchButton = screen.getByTestId('search-button');
      fireEvent.click(searchButton);
      
      // Проверяем, что передается обрезанное значение
      expect(mockCargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        id: 'ABC123',
      });
    });

    it('должен очищать значение через кнопку clear', () => {
      renderComponent('template');
      
      // Сначала устанавливаем значение
      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'test value' } });
      
      // Нажимаем кнопку очистки
      const clearButton = screen.getByTestId('clear-button');
      fireEvent.click(clearButton);
      
      // Проверяем, что инпут очистился
      expect(searchInput).toHaveValue('');
    });
  });

  describe('Модальное окно фильтров', () => {
    it('должен открывать модальное окно при клике на кнопку фильтров', () => {
      renderComponent('template');
      
      const filterButton = screen.getByTestId('filter-button');
      fireEvent.click(filterButton);
      
      expect(screen.getByTestId('modal')).toBeInTheDocument();
      expect(screen.getByTestId('filter')).toBeInTheDocument();
    });

    it('должен закрывать модальное окно при клике на кнопку закрытия', () => {
      renderComponent('template');
      
      const filterButton = screen.getByTestId('filter-button');
      fireEvent.click(filterButton);
      
      expect(screen.getByTestId('modal')).toBeInTheDocument();
      
      const closeButton = screen.getByTestId('modal-close-button');
      fireEvent.click(closeButton);
      
      expect(screen.queryByTestId('modal')).not.toBeInTheDocument();
    });

    it('должен передавать правильную category в Filter компонент', () => {
      renderComponent('order');
      
      const filterButton = screen.getByTestId('filter-button');
      fireEvent.click(filterButton);
      
      expect(screen.getByTestId('filter')).toHaveTextContent('Filter Component - Category: order');
    });

    it('должен закрывать модальное окно через onClose из Filter', () => {
      renderComponent('template');
      
      const filterButton = screen.getByTestId('filter-button');
      fireEvent.click(filterButton);
      
      expect(screen.getByTestId('modal')).toBeInTheDocument();
      
      const filterCloseButton = screen.getByTestId('filter-close-button');
      fireEvent.click(filterCloseButton);
      
      expect(screen.queryByTestId('modal')).not.toBeInTheDocument();
    });
  });

describe('Синхронизация с store', () => {
  it('должен инициализировать значение поиска из store', async () => {
    // Устанавливаем начальное значение в store
    mockCargoStore.cargoQueryFilters.id = 'test-id-from-store';
    
    await act(async () => {
      renderComponent('template');
    });
    
    const searchInput = screen.getByTestId('search-input') as HTMLInputElement;
    // Проверяем что значение загрузилось из store
    expect(searchInput.value).toBe('test-id-from-store');
  });
  
  it('должен обновлять значение при изменении через поиск', async () => {
    await act(async () => {
      renderComponent('template');
    });
    
    const searchInput = screen.getByTestId('search-input') as HTMLInputElement;
    const searchButton = screen.getByTestId('search-button');
    
    // Вводим значение и выполняем поиск
    fireEvent.change(searchInput, { target: { value: 'test-search' } });
    fireEvent.click(searchButton);
    
    // Проверяем что store обновился
    expect(mockCargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      id: 'test-search',
    });
    
    // Проверяем что значение в инпуте сохранилось
    expect(searchInput.value).toBe('test-search');
  });
  
  it('должен очищать значение при нажатии clear', async () => {
    // Начинаем с непустого значения
    mockCargoStore.cargoQueryFilters.id = 'initial-value';
    
    await act(async () => {
      renderComponent('template');
    });
    
    const searchInput = screen.getByTestId('search-input') as HTMLInputElement;
    // Должен быть clear button, т.к. значение не пустое
    const clearButton = screen.getByTestId('clear-button');
    
    fireEvent.click(clearButton);
    
    // Проверяем что инпут очистился
    expect(searchInput.value).toBe('');
    // Проверяем что store очистился
    expect(mockCargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      id: undefined,
    });
  });
});

  describe('Кнопка фильтров', () => {
    it('должен отображать иконку фильтра', () => {
      renderComponent('template');
      
      const filterButton = screen.getByTestId('filter-button');
      expect(screen.getByTestId('filter-icon')).toBeInTheDocument();
      expect(filterButton).toHaveTextContent('Фильтры');
    });

    it('должен иметь правильные атрибуты', () => {
      renderComponent('template');
      
      const filterButton = screen.getByTestId('filter-button');
      expect(filterButton).toHaveAttribute('data-type', 'primary');
      expect(filterButton).toHaveAttribute('data-size', 'large');
    });
  });
});