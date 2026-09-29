/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { FC } from 'react';

import { ReactComponent as CheckboxAll } from 'shared/components/Images/evaluationIcons/personal/CheckboxAll.svg';
import { ReactComponent as CheckboxSeveral } from 'shared/components/Images/evaluationIcons/personal/CheckboxSeveral.svg';
import { ReactComponent as Smile0 } from 'shared/components/Images/evaluationIcons/personal/Smile0.svg';
import { ReactComponent as SpeedFast } from 'shared/components/Images/evaluationIcons/personal/SpeedFast.svg';
import { ReactComponent as SpeedSlow } from 'shared/components/Images/evaluationIcons/personal/SpeedSlow.svg';
import { ReactComponent as CarProfile } from 'shared/components/Images/evaluationIcons/taxi/CarProfile.svg';

import { ReactComponent as CarTop } from 'shared/components/Images/evaluationIcons/taxi/CarTop.svg';
import { ReactComponent as Dirt } from 'shared/components/Images/evaluationIcons/taxi/Dirt.svg';
import { ReactComponent as Smile100 } from 'shared/components/Images/evaluationIcons/taxi/Smile100.svg';
import { ReactComponent as Smile50 } from 'shared/components/Images/evaluationIcons/taxi/Smile50.svg';
import { ReactComponent as Stars } from 'shared/components/Images/evaluationIcons/taxi/Stars.svg';
import { ReactComponent as Time } from 'shared/components/Images/evaluationIcons/taxi/Time.svg';

import PersonalCarIcon from 'shared/components/Images/orders/pers.png';
import BusIcon from 'shared/components/Images/orders/public.png';
import TaxiIcon from 'shared/components/Images/orders/taxi.png';
import { TransportType } from 'stores/TransportTypes/TransportTypes.interface';
import { IIcons } from '../types';

const Taxi: FC<any> = () => (
  <img
    alt="taxi"
    src={TaxiIcon}
    style={{
      width: 16.5, height: 9.41, margin: '0 6.59px 0 0',
    }}
  />
);

const Bus: FC<any> = () => (
  <img
    alt="bus"
    src={BusIcon}
    style={{
      width: 16.5, height: 9.41, margin: '0 6.59px 0 0',
    }}
  />
);

const PersonalCar: FC<any> = () => (
  <img
    alt="bus"
    src={PersonalCarIcon}
    style={{
      width: 16.5, height: 9.41, margin: '0 6.59px 0 0',
    }}
  />
);

export const icons: Partial<Record<TransportType, IIcons>> = {
  TAXI: {
    advantages: {
      CarProfile,
      Stars,
      Smile50,
      Smile100,
    },
    drawbacks: {
      CarTop,
      Dirt,
      Time,
      Smile0,
    },
    title: Taxi,
  },
  PERSONAL: {
    advantages: {
      CheckboxAll,
      Smile50,
      SpeedFast,
    },
    drawbacks: {
      CheckboxSeveral,
      Smile0,
      SpeedSlow,
    },
    title: PersonalCar,
  },
  PUBLIC: {
    advantages: {
      CheckboxAll,
      Smile50,
      SpeedFast,
    },
    drawbacks: {
      CheckboxSeveral,
      Smile0,
      SpeedSlow,
    },
    title: Bus,
  },
} as const;
