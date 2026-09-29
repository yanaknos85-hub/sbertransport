import { FormInstance } from 'antd';

import { ITripTariff, TExternalPrices } from 'stores/Trip/Trip.interface';

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
}
