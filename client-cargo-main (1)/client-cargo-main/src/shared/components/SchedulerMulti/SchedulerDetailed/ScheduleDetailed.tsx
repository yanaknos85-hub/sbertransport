import React, { FC } from 'react';
import moment from 'moment';

import { PeriodType } from 'stores/Cargos/types';

import { FieldType } from '../../../form/Field/Field';
import FormField from '../../../form/FormField/FormField';
import { FieldName, Periodicity, PeriodicityLabel } from '../constants';
import RowButtons from '../RowButtons/RowButtons';
import { Title } from './ScheduleDetailed.style';

interface Props {
  period?: PeriodType;
}

export const ScheduleDetailed: FC<Props> = props => {
  const { period } = props;

  if (!period) {
    return null;
  }

  const field = {
    [FieldName.periodicity]: {
      label: 'Периодичность',
      name: FieldName.periodicity,
      type: FieldType.input,
      params: {
        defaultValue: PeriodicityLabel[period.periodType],
        disabled: period,
      },
    },
  };

  const dayOfWeek = period.dayOfWeek.map(el => ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'][el - 1]);
  const weekOfMonth = period.weekOfMonth.map(el => [1, 2, 3, 4][el - 1]);
  const monthOfQuartal = period?.monthOfQuartal ? period?.monthOfQuartal.map(el => [1, 2, 3][el - 1]) : '';

  let renderPhrase = '';
  if (period.periodType === Periodicity.week) {
    renderPhrase = `каждую неделю по ${dayOfWeek}`;
  }
  if (period.periodType === Periodicity.month) {
    renderPhrase = `каждый месяц по ${dayOfWeek} в ${weekOfMonth}-ю неделю`;
  }
  if (period.periodType === Periodicity.quarter) {
    renderPhrase = `каждый квартал по ${dayOfWeek} в ${weekOfMonth}-ю неделю в ${monthOfQuartal}-ый месяц`;
  }

  return (
    <div>
      <Title>График доставки</Title>
      <div>
        <FormField {...field[FieldName.periodicity]} style={{ cursor: period ? 'not-allowed' : 'pointer' }} />
        {!!period.dayOfWeek?.length && (
          <RowButtons
            label="Дни недели"
            names={['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс']}
            values={[1, 2, 3, 4, 5, 6, 7]}
            isPeriod={true}
            period={period.dayOfWeek}
          />
        )}
        {!!period.weekOfMonth?.length && (
          <RowButtons
            label="Неделя месяца"
            values={[1, 2, 3, 4]}
            isPeriod={true}
            period={period.weekOfMonth}
          />
        )}
        {!!period.monthOfQuartal?.length && (
          <RowButtons
            label="Месяц"
            values={[1, 2, 3]}
            isPeriod={true}
            period={period.monthOfQuartal}
          />
        )}
        <p>
          Происходит
          {' '}
          {`${renderPhrase} до `}
          <span style={{ color: '#10BF6A' }}>{moment(period.endDate).format('DD.MM.YY')}</span>
        </p>
      </div>
    </div>
  );
};
