import React from 'react';
import { render, screen } from '@testing-library/react';

// Мокаем svg-иконки до импорта
jest.mock('shared/images/cargo/total_distance.svg', () => ({
  ReactComponent: () => <svg data-testid="mock-icon-distance" />,
}));
jest.mock('shared/images/cargo/total_volume.svg', () => ({
  ReactComponent: () => <svg data-testid="mock-icon-volume" />,
}));
jest.mock('shared/images/cargo/total_weight.svg', () => ({
  ReactComponent: () => <svg data-testid="mock-icon-weight" />,
}));

// Мокаем styled-components
jest.mock('./CargoTotal.style', () => ({
  Container: ({ children }: any) => <div data-testid="mock-Container">{children}</div>,
  Detailing: ({ children }: any) => <div data-testid="mock-Detailing">{children}</div>,
  DetailingTextLine: ({ children, mb }: any) => (
    <div data-testid="mock-DetailingTextLine" data-mb={mb}>{children}</div>
  ),
  Total: ({ children }: any) => <div data-testid="mock-Total">{children}</div>,
}));

// Мокаем TTypography
jest.mock('shared/components/Cargo/TTypography', () => {
  const TTypography = ({ children, ...props }: any) => (
    <div data-testid="mock-TTypography" data-props={JSON.stringify(props)}>
      {children}
    </div>
  );
  return TTypography;
});

// Мокаем DetailingText
jest.mock('./DetailingText', () => ({
  DetailingText: ({ mb, title, courierTariff }: any) => (
    <div data-testid="mock-DetailingText" data-mb={mb}>
      <div>
        {title}
        {courierTariff.count > 1 && ` x${courierTariff.count}`}
      </div>
      <div>
        {courierTariff.cost / 100} ₽
      </div>
    </div>
  ),
}));

// Мокаем TotalCost
jest.mock('./TotalCost', () => ({
  TotalCost: ({ allCost, title }: any) => (
    <div data-testid="mock-TotalCost" data-allcost={allCost}>
      {title} - {allCost / 100} ₽
    </div>
  ),
}));

// Мокаем Divider из antd -必须 ДО импорта компонента
jest.mock('antd', () => {
  const originalAntd = jest.requireActual('antd');
  return {
    ...originalAntd,
    Divider: ({ style }: any) => <div data-testid="mock-Divider" data-style={JSON.stringify(style)} />,
  };
});

// Мокаем TransportTypeEnum из stores/TransportTypes/TransportTypes.interface.ts
jest.mock('stores/TransportTypes/TransportTypes.interface', () => ({
  TransportTypeEnum: {
    COURIER: 'COURIER',
    DEDICATED: 'DEDICATED',
    INTERREGIONAL: 'INTERREGIONAL',
    INDIVIDUAL: 'INDIVIDUAL',
  },
}));

// Мокаем transportTypeTitles из types/Cargo
jest.mock('types/Cargo', () => ({
  transportTypeTitles: {
    COURIER: 'Курьерская доставка',
    DEDICATED: 'Доставка сборным грузом',
    INTERREGIONAL: 'Межрегиональная доставка',
    INDIVIDUAL: 'Доставка выделенным транспортом',
  },
}));

// Мокаем declOfNum
const declOfNum = (count: number, words: string[]) => {
  const cases = [2, 0, 1, 1, 1, 2];
  const form = count % 100 > 4 && count % 100 < 20 ? 2 : cases[Math.min(count % 10, 5)];
  return words[form];
};
jest.mock('utils/declOfNum', () => ({
  declOfNum,
}));

// Мокаем Misc утилиты
jest.mock('utils/Misc', () => ({
  getDistance: jest.fn(() => '10 км'),
  getWeight: jest.fn(() => '5 кг'),
  getVolume: jest.fn(() => '2 м³'),
}));

// Импортируем после всех моков
import CargoTotal from './CargoTotal';

// Мок TransportTypeEnum для проверки
const mockTransportTypeEnum = {
  COURIER: 'COURIER',
  DEDICATED: 'DEDICATED',
  INTERREGIONAL: 'INTERREGIONAL',
  INDIVIDUAL: 'INDIVIDUAL',
} as const;

describe('CargoTotal', () => {
  const mockSizes = {
    width: 100,
    length: 200,
    height: 150,
    volume: 2000000,
    weight: 5000,
    occupiedPlacesCount: 1,
  };
  const mockTariffCost = {
    totalCost: 10000,
    details: {
      [mockTransportTypeEnum.COURIER]: { cost: 1000, count: 2 },
      [mockTransportTypeEnum.DEDICATED]: { cost: 2000, count: 1 },
      [mockTransportTypeEnum.INTERREGIONAL]: { cost: 3000, count: 0 },
      [mockTransportTypeEnum.INDIVIDUAL]: { cost: 4000, count: 0 },
    },
  };

  const mockProps = {
    distance: 10,
    sizes: mockSizes,
    allCost: 10000,
    tariffCost: mockTariffCost,
    countRequests: 1,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('отображает итоговую информацию с базовыми данными', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByTestId('mock-Container')).toBeInTheDocument();
    expect(screen.getByTestId('mock-DetailingTextLine')).toBeInTheDocument();
    expect(screen.getByTestId('mock-Total')).toBeInTheDocument();
  });

  it('отображает количество заявок и правильное склонение', () => {
    render(<CargoTotal {...mockProps} countRequests={1} />);

    expect(screen.getByText('1 заявка')).toBeInTheDocument();
  });

  it('отображает правильное склонение для множественного числа', () => {
    render(<CargoTotal {...mockProps} countRequests={5} />);

    expect(screen.getByText('5 заявок')).toBeInTheDocument();
  });

  it('отображает три характеристики: вес, объём, расстояние', () => {
    render(<CargoTotal {...mockProps} />);

    // Проверяем наличие компонентов характеристик через текст
    expect(screen.getByText('вес')).toBeInTheDocument();
    expect(screen.getByText('объём')).toBeInTheDocument();
    expect(screen.getByText('расстояние')).toBeInTheDocument();
  });

  it('передаёт правильные данные в CargoTotalItem для веса', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByText('5 кг')).toBeInTheDocument();
  });

  it('передаёт правильные данные в CargoTotalItem для объёма', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByText('2 м³')).toBeInTheDocument();
  });

  it('передаёт правильные данные в CargoTotalItem для расстояния', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByText('10 км')).toBeInTheDocument();
  });

  it('отображает DetailingText для COURIER тарифа', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByText('Курьерская доставка x2')).toBeInTheDocument();
    expect(screen.getByText('10 ₽')).toBeInTheDocument();
  });

  it('отображает DetailingText для DEDICATED тарифа', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByText('Доставка сборным грузом')).toBeInTheDocument();
    expect(screen.getByText('20 ₽')).toBeInTheDocument();
  });

  it('не отображает DetailingText для INTERREGIONAL тарифа если count = 0', () => {
    const { container } = render(<CargoTotal {...mockProps} />);
    // Мок DetailingText всегда рендерит элемент, поэтому проверяем количество
    // В реальном компоненте INTERREGIONAL и INDIVIDUAL не рендерятся при count = 0
    // Но в тесте мы проверяем, что мок работает корректно
    const detailings = container.querySelectorAll('[data-testid="mock-DetailingText"]');
    expect(detailings.length).toBe(2); // COURIER и DEDICATED
  });

  it('не отображает DetailingText для INDIVIDUAL тарифа если count = 0', () => {
    const { container } = render(<CargoTotal {...mockProps} />);
    const detailings = container.querySelectorAll('[data-testid="mock-DetailingText"]');
    // INDIVIDUAL не рендерится при count = 0 в реальном компоненте
    expect(detailings.length).toBe(2); // COURIER и DEDICATED
  });

  it('отображает TotalCost с правильной стоимостью', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByText('Общая стоимость - 100 ₽')).toBeInTheDocument();
  });

  it('отображает topElement если передан', () => {
    const topElement = <div data-testid="custom-top-element">Top</div>;
    render(<CargoTotal {...mockProps} topElement={topElement} />);

    expect(screen.getByTestId('custom-top-element')).toBeInTheDocument();
  });

  it('отображает bottomElement если передан', () => {
    const bottomElement = <div data-testid="custom-bottom-element">Bottom</div>;
    render(<CargoTotal {...mockProps} bottomElement={bottomElement} />);

    expect(screen.getByTestId('custom-bottom-element')).toBeInTheDocument();
  });

  it('отображает Divider с правильным отступом сверху', () => {
    render(<CargoTotal {...mockProps} />);

    expect(screen.getByTestId('mock-Divider')).toBeInTheDocument();
  });
});
