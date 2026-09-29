import React, { FC, ReactElement } from 'react';
import moment from 'moment';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { DATE_FORMAT } from 'constants/constants.app';

import styles from './styles.module.scss';

export enum Periodicity {
  week = 'WEEK',
  month = 'MONTH',
  quarter = 'QUARTER',
}

interface Props {
  request: CargoRequestModel;
  isRegular?: boolean;
}

export const ListItemSubtitleMultiple: FC<Props> = (props): ReactElement => {
  const { request, isRegular } = props;
  const { period } = request;

  const dayOfWeek = period?.dayOfWeek.map(el => ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'][el - 1]);
  const weekOfMonth = period?.weekOfMonth.map(el => [1, 2, 3, 4][el - 1]);
  const monthOfQuartal = period?.monthOfQuartal ? period?.monthOfQuartal.map(el => [1, 2, 3][el - 1]) : '';

  let renderPhrase = '';
  if (period?.periodType === Periodicity.week) {
    renderPhrase = `каждую неделю по ${dayOfWeek}`;
  }
  if (period?.periodType === Periodicity.month) {
    renderPhrase = `каждый месяц по ${dayOfWeek} в ${weekOfMonth}-ю неделю`;
  }
  if (period?.periodType === Periodicity.quarter) {
    renderPhrase = `каждый квартал по ${dayOfWeek} в ${weekOfMonth}-ю неделю в ${monthOfQuartal}-ый месяц`;
  }

  return (
    <div>
      {isRegular ? (
        <>
          Происходит:
          {' '}
          {`${renderPhrase} до `}
          {' '}
          <span className={styles.regularDesiredDate}>{moment(request.nextDelivery).format('DD.MM.YY') || '-' }</span>
        </>
      ) : (
        <span className={styles.textDesiredDate}>
          Дата отправления:
          {' '}
          <span className={styles.desiredDate}>
            {moment(request?.desiredDate).format(`${DATE_FORMAT.BASE_REVERTED_DOTS} [в] ${DATE_FORMAT.TIME_SHORT}`)}
            {request.nextDelivery && `, доставка ${moment(request.nextDelivery).format(DATE_FORMAT.BASE_REVERTED_DOTS)}`}
          </span>
        </span>
      )}
    </div>
  );
};
