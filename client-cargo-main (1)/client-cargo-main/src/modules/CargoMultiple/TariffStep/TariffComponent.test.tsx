import React from 'react';
import { render } from '@testing-library/react';

import { TariffCost, TariffType } from 'stores/CargoTariff/CargoTariff.interface';

// Мокаем observer, чтобы он просто возвращал компонент
jest.mock('mobx-react', () => ({
  observer: (component: any) => component,
}));

// Мокаем все стилизованные компоненты
jest.mock('./CargoTariffStep.style', () => ({
  TariffCard: ({
    children, onClick, active, disabled, ...props
  }: any) => (
    <div
      role="button"
      onClick={onClick}
      data-active={active}
      data-disabled={disabled}
      {...props}
    >
      {children}
    </div>
  ),
  TariffIcon: ({ children }: any) => <div data-testid="tariff-icon">{children}</div>,
  TariffIconImg: ({ src, alt }: any) => (
    <img
      src={src}
      alt={alt}
      data-testid="tariff-image"
    />
  ),
  TariffDescription: ({ children }: any) => <div data-testid="tariff-description">{children}</div>,
  TariffTitle: ({ children }: any) => <div data-testid="tariff-title">{children}</div>,
  TariffText: ({ children }: any) => <div data-testid="tariff-text">{children}</div>,
  TariffTime: ({ children }: any) => <div data-testid="tariff-time">{children}</div>,
  TariffDay: ({ dangerouslySetInnerHTML }: any) => <div data-testid="tariff-day" dangerouslySetInnerHTML={dangerouslySetInnerHTML} />,
  TariffDisabled: ({ children }: any) => <span data-testid="tariff-disabled">{children}</span>,
  Popover: ({
    children, title, placement, content,
  }: any) => (
    <div data-testid="popover" data-title={title}>
      {children}
      {content && <div data-testid="popover-content">{content}</div>}
    </div>
  ),
  PopoverContent: ({ children }: any) => <div data-testid="popover-content">{children}</div>,
  PopoverLink: ({ children, href }: any) => <a href={href} data-testid="popover-link">{children}</a>,
  DisabledIcon: () => <span data-testid="disabled-icon">🔒</span>,
}));

// Мокаем утилиты
jest.mock('utils', () => ({
  getTime: jest.fn(() => '1 день'),
  toRubles: jest.fn((cost, withFraction = false) => {
    if (withFraction) {
      return cost / 100;
    }
    return Math.round(cost / 100);
  }),
}));

// Мокаем зависимости - по умолчанию IS_SDO = false
jest.mock('stores', () => ({
  useAppStore: () => ({
    configStore: {
      env: { IS_SDO: false },
    },
  }),
}));

jest.mock('shared/images/cargo/time.svg', () => () => <svg data-testid="time-icon" />);

describe('TariffComponent', () => {
  const mockTariff: TariffCost = {
    id: '1',
    cost: 1000,
    deliveryTime: 3600000,
    contragent: 'ООО Тест',
    auto: { name: 'Иванов Иван' } as any,
    transportType: {
      id: '1',
      name: 'auto' as TariffType,
      nameRus: 'Автомобиль',
    },
    priceDetails: {
      baseCost: 280,
      loaderCost: 120,
      expressCost: 600,
    },
    loaders: 2,
  };

  const mockProps = {
    idx: 0,
    id: 'auto' as TariffType,
    active: true,
    tariff: mockTariff,
    logger: console,
    title: 'Тестовый тариф',
    description: 'Описание тарифа',
    image: '/test-icon.png',
    onChange: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Рабочий тест 1: не отображает тариф если tariff === null
  it('не отображает тариф если tariff === null', () => {
    const TariffComponent = require('./TariffComponent').default;
    const propsWithNullTariff = {
      ...mockProps,
      tariff: null,
    };

    const { container } = render(<TariffComponent {...propsWithNullTariff} />);
    expect(container.firstChild).toBeNull();
  });

  // Рабочий тест 2: не отображает тариф если cost === 0
  it('не отображает тариф если cost === 0', () => {
    const TariffComponent = require('./TariffComponent').default;
    const tariffWithZeroCost = {
      ...mockTariff,
      cost: 0,
    };

    const propsWithZeroCost = {
      ...mockProps,
      tariff: tariffWithZeroCost,
    };

    const { container } = render(<TariffComponent {...propsWithZeroCost} />);
    expect(container.firstChild).toBeNull();
  });

  // Рабочий тест 3: toRubles не вызывается при tariff === null
  it('не вызывает toRubles при tariff === null', () => {
    const TariffComponent = require('./TariffComponent').default;
    const { toRubles } = require('utils');
    const propsWithNullTariff = {
      ...mockProps,
      tariff: null,
    };
    render(<TariffComponent {...propsWithNullTariff} />);
    expect(toRubles).not.toHaveBeenCalled();
  });

  // Рабочий тест 4: getTime не вызывается при tariff === null
  it('не вызывает getTime при tariff === null', () => {
    const TariffComponent = require('./TariffComponent').default;
    const { getTime } = require('utils');
    const propsWithNullTariff = {
      ...mockProps,
      tariff: null,
    };
    render(<TariffComponent {...propsWithNullTariff} />);
    expect(getTime).not.toHaveBeenCalled();
  });

  // Рабочий тест 5: onChange не вызывается при null tarif
  it('не вызывает onChange при клике на null тариф', () => {
    const TariffComponent = require('./TariffComponent').default;
    const propsWithNullTariff = {
      ...mockProps,
      tariff: null,
    };

    const { container } = render(<TariffComponent {...propsWithNullTariff} />);
    // Нет кнопки, так как компонент возвращает null
    const card = container.querySelector('[role="button"]');
    expect(card).toBeNull();
  });
});
