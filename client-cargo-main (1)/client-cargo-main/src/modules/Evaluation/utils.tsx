import React from 'react';
import { notification } from 'antd';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { CargoRequestStatusesType } from '../../constants/CargoRequestStatuses.constants';
import { CARGO_SHIPMENT_FINISHED, REQUEST_STATUS_CONFIRMATION_FINISHED } from './constants';
import { ReactComponent as SuccessIcon } from './static/icons/successIcon.svg';
import {
  EvaluationIcons, EvaluationReasonsEnum, OpenNotification, RenderedIcons,
  RenderTitle
} from './types';

import styles from './item.module.scss';

export const openNotification = ({
  placement, message, description,
}: OpenNotification): void => {
  notification.info({
    message,
    description,
    placement,
    icon: <SuccessIcon />,
    className: styles.notification,
  });

  setTimeout(() => null, 2000);
};

export const renderIcons = (
  transportType: TransportTypeEnum | undefined,
  reasons: EvaluationReasonsEnum[],
  renderedIcons: RenderedIcons
  // eslint-disable-next-line consistent-return
): any => {
  if (transportType === 'DEDICATED' || transportType === 'INTERREGIONAL' || transportType === 'COURIER') {
    return [...renderedIcons.cargo.negative, ...renderedIcons.cargo.positive].filter(icon => reasons.includes(icon.type)
    );
  }
};

// eslint-disable-next-line consistent-return
export const getImages = (type: string | undefined, images: RenderedIcons): EvaluationIcons => {
  // TODO переделать, когда появятся значения для других типов транспорта
  if (type === 'DEDICATED' || type === 'INTERREGIONAL' || type === 'COURIER') {
    return images.cargo;
  }

  return images.cargo;
};

export const renderRateTitle = (rate = 0): RenderTitle => {
  let rateTitle = '';
  let rateSubtitle = '';

  switch (true) {
    case rate === 0:
      rateTitle = 'Ваша оценка';
      break;
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

export const renderButtonsBlock = (status?: CargoRequestStatusesType): boolean => {
  switch (status) {
    case REQUEST_STATUS_CONFIRMATION_FINISHED:
      return true;
    case CARGO_SHIPMENT_FINISHED:
      return true;
    default:
      return false;
  }
};

export const isFinishedShipment = (status?: CargoRequestStatusesType): boolean => status === CARGO_SHIPMENT_FINISHED;
