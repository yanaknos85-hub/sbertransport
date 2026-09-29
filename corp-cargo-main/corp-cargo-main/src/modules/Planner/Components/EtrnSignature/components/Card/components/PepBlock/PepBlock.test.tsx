import React from 'react';
import { render, screen } from '@testing-library/react';
import PepBlock from './PepBlock';

// ========================
//    Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        card: {
          pepBlock: {
            title: 'Основание ПЭП',
            name: 'Имя',
            role: 'Роль',
            date: 'Дата',
            id: 'ID',
          },
        },
      },
    },
  }),
}));

jest.mock('./styles.module.scss', () => ({
  wrapper: 'wrapper',
  title: 'title',
  row: 'row',
  field: 'field',
  label: 'label',
  value: 'value',
}));

// ========================
//  Тесты
// ========================

const mockTitle = {
  name: 'Иванов Иван Иванович',
  role: 'Генеральный директор',
  date: '2026-07-21T10:00:00',
  id: 'uuid-001',
};

describe('PepBlock', () => {
  it('рендерит заголовок "Основание ПЭП"', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('Основание ПЭП')).toBeInTheDocument();
  });

  it('рендерит значение name', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('Иванов Иван Иванович')).toBeInTheDocument();
  });

  it('рендерит значение role', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('Генеральный директор')).toBeInTheDocument();
  });

  it('рендерит значение date', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('2026-07-21T10:00:00')).toBeInTheDocument();
  });

  it('рендерит значение id', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('uuid-001')).toBeInTheDocument();
  });

  it('рендерит label name', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('Имя')).toBeInTheDocument();
  });

  it('рендерит label role', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('Роль')).toBeInTheDocument();
  });

  it('рендерит label date', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('Дата')).toBeInTheDocument();
  });

  it('рендерит label id', () => {
    render(<PepBlock title={mockTitle} />);
    expect(screen.getByText('ID')).toBeInTheDocument();
  });

  it('рендерит полную DOM-структуру', () => {
    const { container } = render(<PepBlock title={mockTitle} />);
    expect(container.querySelector('.wrapper')).toBeInTheDocument();
    expect(container.querySelector('.title')).toBeInTheDocument();
    expect(container.querySelector('.row')).toBeInTheDocument();
    // Четыре поля
    expect(container.querySelectorAll('.field')).toHaveLength(4);
    // Четыре label и четыре value
    expect(container.querySelectorAll('.label')).toHaveLength(4);
    expect(container.querySelectorAll('.value')).toHaveLength(4);
  });

  it('рендерит все данные в правильных полях', () => {
    const { container } = render(<PepBlock title={mockTitle} />);
    const fields = container.querySelectorAll('.field');
    expect(fields).toHaveLength(4);
    // В каждом поле есть label и value
    fields.forEach((field) => {
      expect(field.querySelector('.label')).toBeInTheDocument();
      expect(field.querySelector('.value')).toBeInTheDocument();
    });
  });
});
