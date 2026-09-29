import React from 'react';
import { render, screen } from '@testing-library/react';
import ParticipantBlock from './ParticipantBlock';

// ========================
//    Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        card: {
          participants: 'Участники',
          senderLabel: 'Отправитель',
          receiverLabel: 'Получатель',
          carrierLabel: 'Перевозчик',
        },
      },
    },
  }),
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
  sender: 'ООО Грузоотправитель',
  receiver: 'АО Грузополучатель',
  contractor: 'ООО ТК Перевозчик',
};

describe('ParticipantBlock', () => {
  it('рендерит заголовок "Участники"', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('Участники')).toBeInTheDocument();
  });

  it('рендерит значение отправителя', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('ООО Грузоотправитель')).toBeInTheDocument();
  });

  it('рендерит значение получателя', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('АО Грузополучатель')).toBeInTheDocument();
  });

  it('рендерит значение перевозчика', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('ООО ТК Перевозчик')).toBeInTheDocument();
  });

  it('рендерит label отправителя', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('Отправитель')).toBeInTheDocument();
  });

  it('рендерит label получателя', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('Получатель')).toBeInTheDocument();
  });

  it('рендерит label перевозчика', () => {
    render(<ParticipantBlock data={mockData} />);
    expect(screen.getByText('Перевозчик')).toBeInTheDocument();
  });

  it('рендерит 3 img с правильными иконками', () => {
    const { container } = render(<ParticipantBlock data={mockData} />);
    const imgs = container.querySelectorAll('img');
    expect(imgs).toHaveLength(3);
    expect(imgs[0]).toHaveAttribute('alt', 'closedBox');
    expect(imgs[1]).toHaveAttribute('alt', 'openBox');
    expect(imgs[2]).toHaveAttribute('alt', 'truck');
    // Все иконки имеют одинаковые размеры
    expect(imgs[0]).toHaveAttribute('width', '24');
    expect(imgs[0]).toHaveAttribute('height', '24');
    expect(imgs[1]).toHaveAttribute('width', '24');
    expect(imgs[1]).toHaveAttribute('height', '24');
    expect(imgs[2]).toHaveAttribute('width', '24');
    expect(imgs[2]).toHaveAttribute('height', '24');
  });

  it('рендерит полную DOM-структуру', () => {
    const { container } = render(<ParticipantBlock data={mockData} />);
    expect(container.querySelector('.block')).toBeInTheDocument();
    expect(container.querySelector('.title')).toBeInTheDocument();
    expect(container.querySelector('.row')).toBeInTheDocument();
    // Три поля для трёх участников
    expect(container.querySelectorAll('.field')).toHaveLength(3);
    // label и value для каждого поля
    expect(container.querySelectorAll('.label')).toHaveLength(3);
    expect(container.querySelectorAll('.value')).toHaveLength(3);
  });

  it('рендерит все данные в правильных полях', () => {
    const { container } = render(<ParticipantBlock data={mockData} />);
    const fields = container.querySelectorAll('.field');
    expect(fields).toHaveLength(3);
    // В каждом поле есть label и value
    fields.forEach((field) => {
      expect(field.querySelector('.label')).toBeInTheDocument();
      expect(field.querySelector('.value')).toBeInTheDocument();
    });
  });
});
