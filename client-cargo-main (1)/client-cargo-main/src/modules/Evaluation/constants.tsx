import React, { ReactNode } from 'react';

import { FastIcon } from './static/icons/FastIcon';
import { InconvenientPlaceIcon } from './static/icons/InconvenientPlaceIcon';
import { LateIcon } from './static/icons/LateIcon';
import { PolitenessIcon } from './static/icons/PolitenessIcon';
import { RudenessIcon } from './static/icons/RudenessIcon';
import { WrinkledPackageIcon } from './static/icons/WrinkledPackageIcon';
import { EvaluationReasonsEnum, EvaluationReasonsNameEnum, SVGProps } from './types';

export const NOTIFICATION_MESSAGE = 'Спасибо за Ваш отзыв!';
export const NOTIFICATION_DESCRIPTION = 'Оценка успешно отправлена';
export const REQUEST_STATUS_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED';
export const CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED';

export const SERVICE_STATUS_OKAY = 200;

export const MIDDLE_RATE = 3;

export const icons = {
  cargo: {
    negative: [
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
    ],
    positive: [
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
    ],
  },
};
