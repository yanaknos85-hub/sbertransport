import React from 'react';
import { render, screen } from '@testing-library/react';

// Мокаем TTypography ДО импорта компонента
jest.mock('shared/components/Cargo/TTypography', () => {
  const TTypography = ({ children, ...props }: any) => {
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
import CargoTotalItem from './CargoTotalItem';

describe('CargoTotalItem', () => {
  const mockImg = <svg data-testid="mock-img" />;

  it('отображает изображение', () => {
    render(<CargoTotalItem img={mockImg} data={10} description="тест" />);

    expect(screen.getByTestId('mock-img')).toBeInTheDocument();
  });

  it('отображает данные как число', () => {
    render(<CargoTotalItem img={mockImg} data={10} description="тест" />);

    expect(screen.getByText('10')).toBeInTheDocument();
  });

  it('отображает данные как строку', () => {
    render(<CargoTotalItem img={mockImg} data="10 кг" description="тест" />);

    expect(screen.getByText('10 кг')).toBeInTheDocument();
  });

  it('отображает описание', () => {
    render(<CargoTotalItem img={mockImg} data={10} description="тест" />);

    expect(screen.getByText('тест')).toBeInTheDocument();
  });

  it('отображает unit если передан', () => {
    render(<CargoTotalItem img={mockImg} data={10} units="кг" description="тест" />);

    // Используем предикат для поиска элемента, содержащего текст
    expect(screen.getByText((content) => content.startsWith('10'))).toBeInTheDocument();
    expect(screen.getByText((content) => content.includes('кг'))).toBeInTheDocument();
  });

  it('отображает tag если передан', () => {
    const tag = <span data-testid="custom-tag">tag</span>;
    render(<CargoTotalItem img={mockImg} data={10} description="тест" tag={tag} />);

    expect(screen.getByTestId('custom-tag')).toBeInTheDocument();
  });
});
