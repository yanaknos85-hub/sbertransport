import React from 'react';
import { render, screen } from '@testing-library/react';
import CargoAndRoute from './CargoAndRoute';

// ========================
//    Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        card: {
          cargo: 'Груз',
          cargoLabel: 'Груз',
          routeLabel: 'Маршрут',
        },
      },
    },
  }),
}));

jest.mock('antd', () => ({
  Modal: ({ children, visible }: { children: React.ReactNode; visible: boolean }) =>
    visible ? <div data-testid="antd-modal">{children}</div> : null,
}));

jest.mock('./styles.module.scss', () => ({
  block: 'block',
  title: 'title',
  row: 'row',
  field: 'field',
  label: 'label',
  value: 'value',
}));

// ========================
//  Тесты
// ========================

const mockData = {
  cargo: 'Оборудование, 5 мест',
  route: 'Екатеринбург — Москва',
};

describe('CargoAndRoute', () => {
  it('рендерит заголовок и label для груза', () => {
    render(<CargoAndRoute data={mockData} />);
    const { queryAllByText } = screen;
    const cargos = queryAllByText('Груз');
    // Заголовок + label
    expect(cargos).toHaveLength(2);
  });

  it('рендерит значение груза', () => {
    render(<CargoAndRoute data={mockData} />);
    expect(screen.getByText('Оборудование, 5 мест')).toBeInTheDocument();
  });

  it('рендерит значение маршрута', () => {
    render(<CargoAndRoute data={mockData} />);
    expect(screen.getByText('Екатеринбург — Москва')).toBeInTheDocument();
  });

  it('рендерит label "Маршрут"', () => {
    render(<CargoAndRoute data={mockData} />);
    expect(screen.getByText('Маршрут')).toBeInTheDocument();
  });

  it('рендерит img с иконкой груза', () => {
    const { container } = render(<CargoAndRoute data={mockData} />);
    const img = container.querySelector('img');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('width', '24');
    expect(img).toHaveAttribute('height', '24');
    expect(img).toHaveAttribute('alt', 'cargo');
  });

  it('рендерит img с иконкой маршрута', () => {
    const { container } = render(<CargoAndRoute data={mockData} />);
    const imgs = container.querySelectorAll('img');
    expect(imgs).toHaveLength(2);
    expect(imgs[1]).toHaveAttribute('alt', 'route');
  });

  it('рендерит все поля с правильными данными', () => {
    const { container } = render(<CargoAndRoute data={mockData} />);
    // Проверка что все элементы отрендерились
    expect(container.querySelector('.block')).toBeInTheDocument();
    expect(container.querySelector('.title')).toBeInTheDocument();
    expect(container.querySelector('.row')).toBeInTheDocument();
    expect(container.querySelectorAll('.field')).toHaveLength(2);
    expect(screen.getByText('Оборудование, 5 мест')).toBeInTheDocument();
    expect(screen.getByText('Екатеринбург — Москва')).toBeInTheDocument();
    expect(screen.getByText('Маршрут')).toBeInTheDocument();
  });
});
