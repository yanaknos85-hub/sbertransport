import React, { ReactElement } from 'react';
import moment from 'moment';
import Courier from 'shared/components/Images/cargo/Courier';
import Dedicated from 'shared/components/Images/cargo/Dedicated';
import Interregional from 'shared/components/Images/cargo/Interregional';
import Question from 'shared/components/Images/view/menu 2.0/Question';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { DATE_FORMAT } from 'constants/constants.app';

export const setIcon = (transportType?: TransportTypeEnum): ReactElement => {
  switch (transportType) {
    case TransportTypeEnum.COURIER:
      return <Courier />;
    case TransportTypeEnum.DEDICATED:
    case TransportTypeEnum.INDIVIDUAL:
      return <Dedicated />;
    case TransportTypeEnum.INTERREGIONAL:
      return <Interregional />;
    default:
      return <Question />;
  }
};

export const getTariffText = (requestData?: CargoRequestModel): string => (requestData?.transportType ? TransportTypeTitlesEnum[requestData.transportType] : 'Не определен');
// (requestData?.express ? ' (экспресс)' : '(стандарт)'); TODO временно убрано до введения типа экспресс

export const convertDate = (desiredDate: number | undefined): string => {
  const momentDate = moment(desiredDate);
  return momentDate.calendar(null, {
    lastDay: momentDate.format(`Вчера [в] ${DATE_FORMAT.TIME_FULL}`),
    sameDay: momentDate.format(`Сегодня [в] ${DATE_FORMAT.TIME_FULL}`),
    nextDay: momentDate.format(`Завтра [в] ${DATE_FORMAT.TIME_FULL}`),
    sameElse: momentDate.format(`${DATE_FORMAT.BASE_REVERTED_DOTS} [в] ${DATE_FORMAT.TIME_FULL}`),
  });
};
