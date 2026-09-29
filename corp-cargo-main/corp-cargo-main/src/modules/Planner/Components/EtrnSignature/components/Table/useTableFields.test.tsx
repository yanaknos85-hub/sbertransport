/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import { renderHook } from '@testing-library/react-hooks';

import { useTableFields } from './useTableFields';
import { EtrnStatus } from 'modules/Planner/Components/EtrnSignature/constants';

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Planner: {
        Etrn: {
          numberEtrn: 'Номер ЭТрН',
          status: 'Статус',
          sla: 'Срок',
          currentTitle: 'Титул',
          senderName: 'Отправитель',
          receiverName: 'Получатель',
          carrierName: 'Перевозчик',
        },
      },
    },
  }),
}));

jest.mock('modules/Planner/Components/EtrnSignature/components/HeaderWithIcon', () => ({
  HeaderWithIcon: ({ title, tooltip }: any) => (
    <span data-testid="header-icon" data-title={title} data-has-tooltip={String(Boolean(tooltip))}>
      {title}
    </span>
  ),
}));

jest.mock('modules/Planner/Components/EtrnSignature/styles.module.scss', () => ({
  etrnTable__link: 'etrnTable__link',
  etrnTable__statusBadge: 'statusBadge',
  etrnTable__statusGreen: 'statusGreen',
  etrnTable__statusGray: 'statusGray',
}));

describe('useTableFields', () => {
  test('должен возвращать массив из 7 колонок', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    expect(result.current).toHaveLength(7);
  });

  test('humanReadableId должен рендерить значение из record в div с классом etrnTable__link', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const record = { humanReadableId: 'ETRN-001' };
    const html = render(<>{result.current[0].render!(record.humanReadableId, record as any, 0)}</>);
    const link = html.container.querySelector('.etrnTable__link');
    expect(link).toBeInTheDocument();
    expect(link).toHaveTextContent('ETRN-001');
  });

  test('status с зелёным статусом READY_FOR_BANK_ACTION должен применить класс statusGreen', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const record = { status: EtrnStatus.READY_FOR_BANK_ACTION };
    const html = render(<>{result.current[1].render!(record.status, record as any, 0)}</>);
    const badge = html.container.querySelector('.statusBadge');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('statusGreen');
  });

  test('status с серым статусом IDENTIFIED должен применить класс statusGray', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const record = { status: EtrnStatus.IDENTIFIED };
    const html = render(<>{result.current[1].render!(record.status, record as any, 0)}</>);
    const badge = html.container.querySelector('.statusBadge');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('statusGray');
  });

  test('status должен отображать имя статуса из EtrnStatusNames', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const record = { status: EtrnStatus.WAIT_CONDITIONS };
    const html = render(<>{result.current[1].render!(record.status, record as any, 0)}</>);
    expect(html.container.querySelector('.statusBadge')).toHaveTextContent('Ожидание проверки условий');
  });

  test('колонка numberEtrn должна рендерить HeaderWithIcon с tooltip', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[0].title as any}</>);
    const header = html.container.querySelector('[data-testid="header-icon"]');
    expect(header).toBeInTheDocument();
    expect(header).toHaveAttribute('data-title', 'Номер ЭТрН');
    expect(header).toHaveAttribute('data-has-tooltip', 'true');
  });

  test('колонка sla должна рендерить HeaderWithIcon с tooltip', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[2].title as any}</>);
    const header = html.container.querySelector('[data-testid="header-icon"]');
    expect(header).toHaveAttribute('data-title', 'Срок');
    expect(header).toHaveAttribute('data-has-tooltip', 'true');
  });

  test('колонка currentTitle должна рендерить HeaderWithIcon с tooltip', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[3].title as any}</>);
    const header = html.container.querySelector('[data-testid="header-icon"]');
    expect(header).toHaveAttribute('data-title', 'Титул');
    expect(header).toHaveAttribute('data-has-tooltip', 'true');
  });

  test('колонка status должна рендерить строку заголовка без HeaderWithIcon', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    expect(result.current[1].title).toBe('Статус');
  });

  test('колонка senderName должна рендерить строку заголовка без HeaderWithIcon', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    expect(result.current[4].title).toBe('Отправитель');
  });

  test('render колонки sla должен форматировать ISO в DD.MM.YYYY HH:mm', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[2].render!('2026-08-08T13:00:00', {} as any, 0)}</>);
    expect(html.container.firstChild?.nodeName).toBe('DIV');
    expect(html.container).toHaveTextContent('08.08.2026 13:00');
  });

  test('render колонки sla должен показывать emptySign для пустого значения', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[2].render!('', {} as any, 0)}</>);
    expect(html.container.firstChild?.nodeName).toBe('DIV');
    expect(html.container).toHaveTextContent('-');
  });

  test('render колонки currentTitle должен показывать emptySign для пустого значения', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[3].render!('', {} as any, 0)}</>);
    expect(html.container.firstChild?.nodeName).toBe('DIV');
    expect(html.container).toHaveTextContent('-');
  });

  test('render колонки senderName должен рендерить value в div', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[4].render!('OOO Ромашка', {} as any, 0)}</>);
    expect(html.container).toHaveTextContent('OOO Ромашка');
  });

  test('render колонки senderName должен показывать emptySign для пустого значения', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[4].render!('', {} as any, 0)}</>);
    expect(html.container.firstChild?.nodeName).toBe('DIV');
    expect(html.container).toHaveTextContent('-');
  });

  test('render колонки receiverName должен показывать emptySign для пустого значения', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[5].render!('', {} as any, 0)}</>);
    expect(html.container.firstChild?.nodeName).toBe('DIV');
    expect(html.container).toHaveTextContent('-');
  });

  test('render колонки carrierName должен показывать emptySign для пустого значения', () => {
    const { result } = renderHook(() => useTableFields({ onOpenCard: jest.fn() }));
    const html = render(<>{result.current[6].render!('', {} as any, 0)}</>);
    expect(html.container.firstChild?.nodeName).toBe('DIV');
    expect(html.container).toHaveTextContent('-');
  });
});
