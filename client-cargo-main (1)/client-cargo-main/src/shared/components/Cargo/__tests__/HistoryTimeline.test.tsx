import React from 'react';
import { render, screen } from '@testing-library/react';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';

import HistoryTimeline from '../HistoryTimeline/HistoryTimeline';

jest.mock('stores/Cargos/types', () => ({
  CargoHistoryTypeEnum: {
    PAST: 'PAST',
    NOW: 'NOW',
    FUTURE: 'FUTURE',
  },
}));

const { CargoHistoryTypeEnum } = jest.requireMock('stores/Cargos/types');

jest.mock('constants/CargoRequestStatuses.constants', () => {
  const Statuses = {
    CARGO_AWAITING_APPROVAL: 'CARGO_AWAITING_APPROVAL',
    CARGO_APPROVED: 'CARGO_APPROVED',
    CARGO_AWAITING_DATA: 'CARGO_AWAITING_DATA',
    CARGO_AWAITING_TRANSFER: 'CARGO_AWAITING_TRANSFER',
    CARGO_TRANSFER_FINISHED: 'CARGO_TRANSFER_FINISHED',
    CARGO_SHIPMENT_FINISHED: 'CARGO_SHIPMENT_FINISHED',
    CARGO_DELIVERY_CONFIRMATION_FINISHED: 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
    CARGO_CANCELED: 'CARGO_CANCELED',
  };

  const CargoRequestStatusesTitles: Record<string, string> = {
    CARGO_AWAITING_APPROVAL: 'На согласовании',
    CARGO_APPROVED: 'Согласовано',
    CARGO_AWAITING_DATA: 'Отправлен контрагенту',
    CARGO_AWAITING_TRANSFER: 'На сборе',
    CARGO_TRANSFER_FINISHED: 'Доставка',
    CARGO_SHIPMENT_FINISHED: 'Доставлено',
    CARGO_DELIVERY_CONFIRMATION_FINISHED: 'Завершено',
    CARGO_CANCELED: 'Отменено',
  };

  const FINAL_STATUSES = new Set<string>([
    Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
    Statuses.CARGO_CANCELED,
  ]);

  return {
    Statuses,
    CargoRequestStatusesTitles,
    FINAL_STATUSES,
  };
});

const { Statuses, CargoRequestStatusesTitles, FINAL_STATUSES } = jest.requireMock(
  'constants/CargoRequestStatuses.constants',
);

jest.mock('antd', () => {
  const MockTimelineItem = ({
    children,
    color,
    dot,
    className,
  }: {
    children?: React.ReactNode;
    color?: string;
    dot?: React.ReactNode;
    className?: string;
  }) => (
    <li data-testid="timeline-item" data-color={color} data-classname={className}>
      <div data-testid="timeline-dot">{dot}</div>
      <div data-testid="timeline-content">{children}</div>
    </li>
  );

  const MockTimeline = ({ children }: { children?: React.ReactNode }) => (
    <ul data-testid="timeline">{children}</ul>
  );

  return {
    Timeline: Object.assign(MockTimeline, { Item: MockTimelineItem }),
  };
});

jest.mock(
  '../HistoryTimeline/HistoryTimeline.module.scss',
  () => ({
    __esModule: true,
    default: {
      timelineItem: 'timelineItem',
      timelineItemLast: 'timelineItemLast',
      pastIcon: 'pastIcon',
      nowIcon: 'nowIcon',
      nowInnerCircle: 'nowInnerCircle',
      futureIcon: 'futureIcon',
      status: 'status',
      date: 'date',
    },
  }),
  { virtual: true },
);

jest.mock('shared/components/Images/view/menu 2.0/Question', () => ({
  __esModule: true,
  default: () => <span data-testid="question-icon" />,
}));

describe('HistoryTimeline', () => {
  const baseHistoryItem = {
    date: 1700000000000,
  };

  describe('цвет точки в зависимости от типа', () => {
    it('устанавливает зелёный цвет для типа PAST', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_AWAITING_DATA,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast={false}
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#10BF6A');
    });

    it('устанавливает оранжевый цвет для типа NOW', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_APPROVED,
            type: CargoHistoryTypeEnum.NOW,
          }}
          isLast={false}
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#FF9A32');
    });

    it('устанавливает серый цвет для типа FUTURE', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_TRANSFER_FINISHED,
            type: CargoHistoryTypeEnum.FUTURE,
          }}
          isLast={false}
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#D6D6D6');
    });

    it('устанавливает цвет по умолчанию для неизвестного типа', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_AWAITING_DATA,
            type: 'UNKNOWN' as any,
          }}
          isLast={false}
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#fff');
    });
  });

  describe('иконка точки в зависимости от типа', () => {
    it('отображает pastIcon с галочкой для типа PAST', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_AWAITING_DATA,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast={false}
        />,
      );

      const dot = screen.getByTestId('timeline-dot');
      expect(dot.querySelector('[class*="pastIcon"]')).not.toBeNull();
      expect(dot.querySelector('.anticon-check')).not.toBeNull();
    });

    it('отображает nowIcon с внутренним кругом для типа NOW', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_APPROVED,
            type: CargoHistoryTypeEnum.NOW,
          }}
          isLast={false}
        />,
      );

      const dot = screen.getByTestId('timeline-dot');
      expect(dot.querySelector('[class*="nowIcon"]')).not.toBeNull();
      expect(dot.querySelector('[class*="nowInnerCircle"]')).not.toBeNull();
    });

    it('отображает futureIcon для типа FUTURE', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_TRANSFER_FINISHED,
            type: CargoHistoryTypeEnum.FUTURE,
          }}
          isLast={false}
        />,
      );

      const dot = screen.getByTestId('timeline-dot');
      expect(dot.querySelector('[class*="futureIcon"]')).not.toBeNull();
      expect(dot.querySelector('.anticon-check')).toBeNull();
      expect(dot.querySelector('[class*="nowIcon"]')).toBeNull();
    });

    it('отображает futureIcon с иконкой вопроса для неизвестного типа', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_AWAITING_DATA,
            type: 'UNKNOWN' as any,
          }}
          isLast={false}
        />,
      );

      const dot = screen.getByTestId('timeline-dot');
      expect(dot.querySelector('[class*="futureIcon"]')).not.toBeNull();
      expect(screen.getByTestId('question-icon')).not.toBeNull();
    });
  });

  describe('принудительная подмена типа для финальных статусов', () => {
    it('подменяет NOW на PAST для статуса CARGO_DELIVERY_CONFIRMATION_FINISHED', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
            type: CargoHistoryTypeEnum.NOW,
          }}
          isLast
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#10BF6A');
      const dot = screen.getByTestId('timeline-dot');
      expect(dot.querySelector('[class*="pastIcon"]')).not.toBeNull();
      expect(dot.querySelector('.anticon-check')).not.toBeNull();
    });

    it('подменяет NOW на PAST для статуса CARGO_CANCELED', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_CANCELED,
            type: CargoHistoryTypeEnum.NOW,
          }}
          isLast
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#10BF6A');
      const dot = screen.getByTestId('timeline-dot');
      expect(dot.querySelector('[class*="pastIcon"]')).not.toBeNull();
      expect(dot.querySelector('.anticon-check')).not.toBeNull();
    });

    it('подменяет FUTURE на PAST для финального статуса', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
            type: CargoHistoryTypeEnum.FUTURE,
          }}
          isLast
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#10BF6A');
      expect(screen.getByTestId('timeline-dot').querySelector('.anticon-check')).not.toBeNull();
    });

    it('оставляет PAST как PAST для финального статуса', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast
        />,
      );

      expect(screen.getByTestId('timeline-item').getAttribute('data-color')).toBe('#10BF6A');
    });

    it('не подменяет тип для нефинального статуса', () => {
      const nonFinalStatuses = [
        Statuses.CARGO_AWAITING_APPROVAL,
        Statuses.CARGO_APPROVED,
        Statuses.CARGO_AWAITING_DATA,
        Statuses.CARGO_AWAITING_TRANSFER,
        Statuses.CARGO_TRANSFER_FINISHED,
        Statuses.CARGO_SHIPMENT_FINISHED,
      ];

      nonFinalStatuses.forEach(status => {
        expect(FINAL_STATUSES.has(status)).toBe(false);
      });
    });
  });

  describe('класс timelineItemLast в зависимости от isLast', () => {
    it('добавляет класс timelineItemLast когда isLast=true', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast
        />,
      );

      const className = screen.getByTestId('timeline-item').getAttribute('data-classname') ?? '';
      expect(className).toContain('timelineItemLast');
      expect(className).toContain('timelineItem');
    });

    it('не добавляет класс timelineItemLast когда isLast=false', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_AWAITING_DATA,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast={false}
        />,
      );

      const className = screen.getByTestId('timeline-item').getAttribute('data-classname') ?? '';
      expect(className).not.toContain('timelineItemLast');
      expect(className).toContain('timelineItem');
    });
  });

  describe('отображение содержимого', () => {
    it('отображает локализованное название статуса', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast
        />,
      );

      expect(
        screen.getByText(CargoRequestStatusesTitles[Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED]),
      ).toBeInTheDocument();
    });

    it('отображает дату в формате BASE_REVERTED_DOTS и TIME_FULL', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_APPROVED,
            type: CargoHistoryTypeEnum.NOW,
          }}
          isLast={false}
        />,
      );

      const expectedDate = moment(baseHistoryItem.date).format(
        `${DATE_FORMAT.BASE_REVERTED_DOTS} ${DATE_FORMAT.TIME_FULL}`,
      );
      expect(screen.getByText(expectedDate)).toBeInTheDocument();
    });

    it('передаёт статус в атрибут title контейнера', () => {
      render(
        <HistoryTimeline
          cargoHistoryType={{
            ...baseHistoryItem,
            status: Statuses.CARGO_TRANSFER_FINISHED,
            type: CargoHistoryTypeEnum.PAST,
          }}
          isLast={false}
        />,
      );

      const titleEl = document.querySelector(`div[title="${Statuses.CARGO_TRANSFER_FINISHED}"]`);
      expect(titleEl).not.toBeNull();
    });
  });
});
