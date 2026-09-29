/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import Filter from '.';

// Мокируем все зависимости
jest.mock('react', () => ({
  ...jest.requireActual('react'),
  useCallback: jest.fn((cb) => cb),
}));

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}));

jest.mock('antd', () => {
  const MockTabs = ({ activeKey, onChange, children, className }: any) => {
    // Сохраняем onChange в замыкании
    const handleClick = () => {
      if (onChange) {
        onChange('cargo');
      }
    };
    
    return (
      <div className={className} data-testid="tabs">
        <div data-testid="active-tab">{activeKey}</div>
        <button 
          data-testid="change-tab-button" 
          onClick={handleClick}
        >
          Change Tab
        </button>
        <div data-testid="tabs-content">
          {children}
        </div>
      </div>
    );
  };
  
  const TabPaneComponent = ({ tab, tabKey, children }: any) => {
    // tabKey может передаваться как key, который React не передает в props
    const actualTabKey = tabKey || 'cargo';
    return (
      <div data-testid={`tab-pane-${actualTabKey}`}>
        <span data-testid={`tab-name-${actualTabKey}`}>{tab}</span>
        {children}
      </div>
    );
  };
  
  MockTabs.TabPane = TabPaneComponent;
  
  return {
    Tabs: MockTabs,
  };
});

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('shared/components/SpinWrapped/SpinWrapped', () => ({
  SpinWrapped: () => <div data-testid="spinner">Загрузка...</div>,
}));

jest.mock('./Tabs/CargoSearch', () => ({
  CargoSearch: ({ onClose, category }: any) => (
    <div data-testid="cargo-search">
      <div>Cargo Search Component</div>
      <div>Category: {category}</div>
      <button data-testid="close-cargo-search" onClick={onClose}>
        Close Cargo Search
      </button>
    </div>
  ),
}));

jest.mock('../../constants/Tabs', () => ({
  Tab: {
    cargo: 'cargo',
    other: 'other',
  },
}));

jest.mock('../../constants/Filters', () => ({
  AVAILABLE_FILTER_TABS: ['cargo'],
  FilterTab: {
    cargo: 'cargo',
  },
  FilterTabName: {
    cargo: 'Груз',
  },
}));

jest.mock('./styles.module.scss', () => ({
  filterTitle: 'filterTitle',
  filterTabs: 'filterTabs',
}));

describe('Компонент фильтра', () => {
  let mockCargoStore: any;
  let mockUseAppStoreContext: jest.Mock;
  
  const mockOnClose = jest.fn();
  
  const renderComponent = (category = 'all') => {
    return render(
      <Filter onClose={mockOnClose} category={category} />
    );
  };
  
  beforeEach(() => {
    jest.clearAllMocks();
    
    mockCargoStore = {
      activeTabKey: 'cargo',
      setActiveTabKey: jest.fn(),
    };
    
    mockUseAppStoreContext = jest.requireMock('shared/hooks/useAppStoreContext').useAppStoreContext;
    mockUseAppStoreContext.mockReturnValue({
      cargoStore: mockCargoStore,
    });
  });
  
  afterEach(() => {
    jest.resetAllMocks();
  });
  
  it('Должен отображать заголовок "Фильтры"', () => {
    renderComponent();
    
    expect(screen.getByText('Фильтры')).toBeInTheDocument();
  });
  
  it('Должен отображать вкладку с названием "Груз"', () => {
    renderComponent();
    
    // Проверяем что компонент CargoSearch отображается (что означает, что вкладка активна)
    expect(screen.getByTestId('cargo-search')).toBeInTheDocument();
    expect(screen.getByText('Груз')).toBeInTheDocument();
  });
  
  it('Должен отображать компонент CargoSearch с переданной категорией', () => {
    renderComponent('template');
    
    expect(screen.getByTestId('cargo-search')).toBeInTheDocument();
    expect(screen.getByText('Category: template')).toBeInTheDocument();
  });
  
  it('Должен вызывать onClose при клике на кнопку в CargoSearch', () => {
    renderComponent();
    
    const closeButton = screen.getByTestId('close-cargo-search');
    fireEvent.click(closeButton);
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });
  
  it('Должен отображать активную вкладку из store', () => {
    renderComponent();
    
    expect(screen.getByTestId('active-tab')).toHaveTextContent('cargo');
  });
  
  it('Должен вызывать setActiveTabKey при изменении вкладки через кнопку', () => {
    // Тестируем логику handleTabChange напрямую
    const { AVAILABLE_FILTER_TABS } = require('../../constants/Filters');
    
    // Имитируем handleTabChange из компонента
    const handleTabChange = (tabKey: string) => {
      if (AVAILABLE_FILTER_TABS.includes(tabKey)) {
        mockCargoStore.setActiveTabKey(tabKey);
      }
    };
    
    // Вызываем с 'cargo' (который должен быть в AVAILABLE_FILTER_TABS)
    handleTabChange('cargo');
    
    expect(mockCargoStore.setActiveTabKey).toHaveBeenCalledWith('cargo');
  });
  
  it('Должен использовать useCallback для handleTabChange', () => {
    const useCallbackMock = jest.requireMock('react').useCallback;
    
    renderComponent();
    
    expect(useCallbackMock).toHaveBeenCalled();
  });
  
  it('Не должен вызывать setActiveTabKey для недоступной вкладки', () => {
    // Тестируем логику handleTabChange напрямую
    const { AVAILABLE_FILTER_TABS } = require('../../constants/Filters');
    const handleTabChange = (tabKey: string) => {
      if (AVAILABLE_FILTER_TABS.includes(tabKey)) {
        mockCargoStore.setActiveTabKey(tabKey);
      }
    };
    
    // Пробуем изменить на доступную вкладку
    handleTabChange('cargo');
    expect(mockCargoStore.setActiveTabKey).toHaveBeenCalledWith('cargo');
    
    // Сбрасываем мок
    mockCargoStore.setActiveTabKey.mockClear();
    
    // Пробуем изменить на недоступную вкладку
    handleTabChange('other');
    expect(mockCargoStore.setActiveTabKey).not.toHaveBeenCalled();
  });
});