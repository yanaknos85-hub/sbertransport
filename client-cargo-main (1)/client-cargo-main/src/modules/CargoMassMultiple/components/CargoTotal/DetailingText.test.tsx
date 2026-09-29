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
import { DetailingText } from './DetailingText';

describe('DetailingText', () => {
  const mockProps = {
    courierTariff: { cost: 1000, count: 2 },
    title: 'Тестовый тариф',
    mb: '16px',
  };

  it('отображает заголовок тарифа', () => {
    render(<DetailingText {...mockProps} />);

    expect(screen.getByText('Тестовый тариф')).toBeInTheDocument();
  });

  it('отображает цену в рублях', () => {
    render(<DetailingText {...mockProps} />);

    expect(screen.getByText('₽')).toBeInTheDocument();
  });

  it('отображает xN когда count больше 1', () => {
    render(<DetailingText {...mockProps} />);

    expect(screen.getByText('x2')).toBeInTheDocument();
  });

  it('не отображает xN когда count равен 1', () => {
    const props = { ...mockProps, courierTariff: { cost: 1000, count: 1 } };
    render(<DetailingText {...props} />);

    expect(screen.queryByText('x1')).not.toBeInTheDocument();
  });

  it('передаёт mb в DetailingTextLine', () => {
    render(<DetailingText {...mockProps} />);

    expect(screen.getByTestId('mock-DetailingTextLine')).toHaveAttribute('data-mb', '16px');
  });

  it('передаёт правильный mb для 24px', () => {
    const props = { ...mockProps, mb: '24px' };
    render(<DetailingText {...props} />);

    expect(screen.getByTestId('mock-DetailingTextLine')).toHaveAttribute('data-mb', '24px');
  });
});
