/* eslint-disable @typescript-eslint/no-explicit-any */
import { FormInstance } from 'antd';

import { ITripTariff, TExternalPrices, TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { TripLimit } from './hooks/useTripLimit';

export interface ITaxiClassesProps {
  tariffsInfo: ITripTariff[];
  externalPrices?: TExternalPrices[];
  isEditable: boolean;
  tripLimit?: TripLimit;
  value?: any;
  onChange?: any;
  form?: FormInstance;
  purpose?: string;
  isExternal?: boolean;
  externalTaxiClass?: string;
  availableTaxiClasses?: TaxiClassEnum[];
}
