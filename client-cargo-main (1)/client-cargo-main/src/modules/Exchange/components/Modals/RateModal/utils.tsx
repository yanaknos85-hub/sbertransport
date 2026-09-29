import React, { ReactNode } from 'react';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';

import { FastIcon } from './icons/FastIcon';
import { InconvenientPlaceIcon } from './icons/InconvenientPlaceIcon';
import { LateIcon } from './icons/LateIcon';
import { PolitenessIcon } from './icons/PolitenessIcon';
import { RudenessIcon } from './icons/RudenessIcon';
import { WrinkledPackageIcon } from './icons/WrinkledPackageIcon';
import {
  EvaluationReasonsEnum, EvaluationReasonsNameEnum, RenderTitle, SVGProps
} from './types';

export const icons = [
  {
    type: EvaluationReasonsEnum.INCONVENIENT_PICKUP_LOCATION,
    name: EvaluationReasonsNameEnum.INCONVENIENT_PICKUP_LOCATION,
    icon: (props: SVGProps): ReactNode => <InconvenientPlaceIcon {...props} />,
  },
  {
    type: EvaluationReasonsEnum.WRINKLED_OR_TORN_PACKAGING,
    name: EvaluationReasonsNameEnum.WRINKLED_OR_TORN_PACKAGING,
    icon: (props: SVGProps): ReactNode => <WrinkledPackageIcon {...props} />,
  },
  {
    type: EvaluationReasonsEnum.RUDENESS,
    name: EvaluationReasonsNameEnum.RUDENESS,
    icon: (props: SVGProps): ReactNode => <RudenessIcon {...props} />,
  },
  {
    type: EvaluationReasonsEnum.LATE,
    name: EvaluationReasonsNameEnum.LATE,
    icon: (props: SVGProps): ReactNode => <LateIcon {...props} />,
  },
  {
    type: EvaluationReasonsEnum.FAST_SHIPPING,
    name: EvaluationReasonsNameEnum.FAST_SHIPPING,
    icon: (props: SVGProps): ReactNode => <FastIcon {...props} />,
  },
  {
    type: EvaluationReasonsEnum.POLITENESS,
    name: EvaluationReasonsNameEnum.POLITENESS,
    icon: (props: SVGProps): ReactNode => <PolitenessIcon {...props} />,
  },
];

export const renderRateTitle = (rate = 0): RenderTitle => {
  let rateTitle = '';
  let rateSubtitle = '';

  switch (true) {
    case rate <= 3:
      rateTitle = 'Плохо';
      rateSubtitle = 'Что было не так?';
      break;
    case rate > 3:
      rateTitle = 'Хорошо';
      rateSubtitle = 'Что особенно понравилось?';
      break;
    default:
      break;
  }
  return { rateTitle, rateSubtitle };
};

export const convertDate = (desiredDate: number | undefined): string => {
  const momentDate = moment(desiredDate);
  return momentDate.calendar(null, {
    lastDay: momentDate.format(`Вчера [в] ${DATE_FORMAT.TIME_FULL}`),
    sameDay: momentDate.format(`Сегодня [в] ${DATE_FORMAT.TIME_FULL}`),
    nextDay: momentDate.format(`Завтра [в] ${DATE_FORMAT.TIME_FULL}`),
    sameElse: momentDate.format(`${DATE_FORMAT.BASE_REVERTED_DOTS} [в] ${DATE_FORMAT.TIME_FULL}`),
  });
};
