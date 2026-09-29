import { FC } from 'react';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';
import { ReactComponent as LARGE_BUS } from './LARGE_BUS.svg';
import { ReactComponent as MIDDLE_BUS } from './MIDDLE_BUS.svg';
import { ReactComponent as SMALL_BUS } from './SMALL_BUS.svg';
import { ReactComponent as VIP_BUS } from './VIP_BUS.svg';

export const busIcons: Partial<Record<TaxiClassEnum, FC>> = {
  VIP_BUS,
  SMALL_BUS,
  MIDDLE_BUS,
  LARGE_BUS,
} as const;
