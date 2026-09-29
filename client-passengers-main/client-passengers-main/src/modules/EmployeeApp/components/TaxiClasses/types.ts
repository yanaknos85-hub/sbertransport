/* eslint-disable @typescript-eslint/no-explicit-any */

import { FormInstance } from 'antd';
import { Dispatch } from 'react';

import { ITripTariff, TExternalPrices, TaxiClassEnum } from 'stores/Trip/Trip.interface';
import Process from '../Evaluation/Constants/Process';

import { TripLimit } from './hooks/useTripLimit';

export interface ITaxiClassesProps {
  tariffsInfo: ITripTariff[];
  externalPrices?: TExternalPrices[];
  isEditable: boolean;
  setProcess?: Dispatch<Process>;
  tripLimit?: TripLimit;
  value?: any;
  onChange?: any;
  form?: FormInstance;
  purpose?: string;
  isExternal?: boolean;
  externalTaxiClass?: string;
  availableTaxiClasses?: TaxiClassEnum[];
}
