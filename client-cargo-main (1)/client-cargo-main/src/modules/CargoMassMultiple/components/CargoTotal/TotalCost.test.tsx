import React from 'react';
import { render, screen } from '@testing-library/react';

// Мокаем TTypography ДО импорта компонента
jest.mock('shared/components/Cargo/TTypography', () => {
  const TTypography = ({ children, ...props }: any) => {
    if (Array.isArray(children)) {
      return (
        <div data-testid="mock-TTypography" data-props={JSON.stringify(props)}>
          {children.map((child, i) => (
            <span key={i} data-child={typeof child === 'string' ? child : 'non-string'}>
              {child}
            </span>
          ))}
        </div>
      );
    }
    return (
      <div data-testid="mock-TTypography" data-props={JSON.stringify(props)}>
        {children}
      </div>
    );
  };
  return TTypography;
});

// Мокаем styled-components
jest.mock('./CargoTotal.style', () => ({
  Container: ({ children }: any) => <div data-testid="mock-Container">{children}</div>,
  Detailing: ({ children }: any) => <div data-testid="mock-Detailing">{children}</div>,
  DetailingTextLine: ({ children, mb }: any) => (
    <div data-testid="mock-DetailingTextLine" data-mb={mb}>{children}</div>
  ),
  Total: ({ children }: any) => <div data-testid="mock-Total">{children}</div>,
}));

// Импортируем после моков
import { TotalCost } from './TotalCost';

describe('TotalCost', () => {
  const mockProps = {
    allCost: 10000,
    title: 'Общая стоимость',
  };

  it('отображает заголовок', () => {
    render(<TotalCost {...mockProps} />);

    expect(screen.getByText('Общая стоимость')).toBeInTheDocument();
  });

  it('отображает цену в рублях (allCost / 100)', () => {
    render(<TotalCost {...mockProps} />);

    expect(screen.getByText('₽')).toBeInTheDocument();
  });

  it('отображает правильную цену для разных значений', () => {
    const props = { ...mockProps, allCost: 5500 };
    render(<TotalCost {...props} />);

    expect(screen.getByText('55')).toBeInTheDocument();
  });

  it('отображает ноль для пустой стоимости', () => {
    const props = { ...mockProps, allCost: 0 };
    render(<TotalCost {...props} />);

    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('отображает дробную часть стоимости', () => {
    const props = { ...mockProps, allCost: 1234 };
    render(<TotalCost {...props} />);

    expect(screen.getByText('12.34')).toBeInTheDocument();
  });

  it('отображает DetailingTextLine контейнер', () => {
    render(<TotalCost {...mockProps} />);

    expect(screen.getByTestId('mock-DetailingTextLine')).toBeInTheDocument();
  });
});
