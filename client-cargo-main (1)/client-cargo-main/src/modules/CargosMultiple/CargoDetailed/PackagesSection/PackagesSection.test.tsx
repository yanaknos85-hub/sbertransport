import React from 'react';
import { render, screen } from '@testing-library/react';

// Мокаем styled-components компоненты (простые div с data-testid)
jest.mock('./PackagesSection.style', () => ({
  Wrapper: ({ children, ...props }: { children: React.ReactNode; [key: string]: any }) => (
    <div data-testid="mock-packages-wrapper" {...props}>
      {children}
    </div>
  ),
  Item: ({ children, ...props }: { children: React.ReactNode; [key: string]: any }) => (
    <div data-testid="mock-packages-item" {...props}>
      {children}
    </div>
  ),
}));

// Импортируем компонент ПОСЛЕ всех моков
import { PackagesSection } from './PackagesSection';

describe('PackagesSection', () => {
  const mockPackages = [
    { id: '1', name: 'Коробка', count: 5 },
    { id: '2', name: 'Ящик', count: 2 },
    { id: '3', name: 'Пакет', count: 10 },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('не рендерится если packages === undefined', () => {
    render(<PackagesSection />);

    expect(screen.queryByTestId('mock-packages-wrapper')).not.toBeInTheDocument();
  });

  it('не рендерится если packages.length === 0', () => {
    render(<PackagesSection packages={[]} />);

    expect(screen.queryByTestId('mock-packages-wrapper')).not.toBeInTheDocument();
  });

  it('рендерится если есть пакеты', () => {
    render(<PackagesSection packages={mockPackages} />);

    expect(screen.getByTestId('mock-packages-wrapper')).toBeInTheDocument();
  });

  it('отображает правильное количество items', () => {
    render(<PackagesSection packages={mockPackages} />);

    const items = screen.getAllByTestId('mock-packages-item');
    expect(items).toHaveLength(3);
  });

  it('отображает имя пакета и количество в правильном формате', () => {
    render(<PackagesSection packages={mockPackages} />);

    expect(screen.getByText('Коробка x5')).toBeInTheDocument();
    expect(screen.getByText('Ящик x2')).toBeInTheDocument();
    expect(screen.getByText('Пакет x10')).toBeInTheDocument();
  });

  it('использует item.id как ключ для каждого элемента', () => {
    render(<PackagesSection packages={mockPackages} />);

    const items = screen.getAllByTestId('mock-packages-item');
    expect(items[0]).toHaveTextContent('Коробка x5');
    expect(items[1]).toHaveTextContent('Ящик x2');
    expect(items[2]).toHaveTextContent('Пакет x10');
  });

  it('правильная структура DOM (wrapper содержит items)', () => {
    render(<PackagesSection packages={mockPackages} />);

    const wrapper = screen.getByTestId('mock-packages-wrapper');
    const items = wrapper.querySelectorAll('[data-testid="mock-packages-item"]');
    expect(items).toHaveLength(3);
  });
});
