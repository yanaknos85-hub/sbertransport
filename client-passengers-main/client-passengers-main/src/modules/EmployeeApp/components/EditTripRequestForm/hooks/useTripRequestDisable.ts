
import { useEffect } from 'react';
import { StoreNames } from 'stores';

import { useGetFrequentlyPurpose } from 'api/purposes';
import { TripRequestPurpose } from 'modules/EmployeeApp/components/EditTripRequestForm/hooks/useTripRequestPurpose';
import { TCoordinates } from 'shared/models/geo/types';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import {
  BusEnum, GroupTransferClassesEnum, TaxiClassEnum, TaxiEnum, Transport
} from 'stores/Trip/Trip.interface';

import { calculateTripCost } from '../../CreateTripRequest/utils/utils';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

export const useTripRequestDisable = ({
  tariffsCosts,
  tripPurpose,
  request,
  transport,
  isExternal,
  from,
  to,
  subClass,
}: {
  tariffsCosts: TripPriceModel[];
  tripPurpose: TripRequestPurpose;
  request?: TripRequestModel | null;
  transport?: Transport;
  isExternal?: boolean;
  from?: TCoordinates;
  to?: TCoordinates;
  subClass?: TaxiEnum | BusEnum | GroupTransferClassesEnum;
}): {
    disabled: boolean;
    getDisableReason: () => string;
    isNoPersonalCars: boolean;
  } => {
  const defaultPurpose = useGetFrequentlyPurpose();
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  const transportNormalized = transport?.split('-')[0] as Transport;
  const isBus = transportNormalized === TaxiClassEnum.BUS;
  // eslint-disable-next-line no-underscore-dangle
  const _tripCost = isBus ? 1 : calculateTripCost(tariffsCosts, subClass || transportNormalized);

  // eslint-disable-next-line no-underscore-dangle
  const _purpose = tripPurpose.purposeValidity.isValid || defaultPurpose.data.id;
  const { personalCars } = tripStore;
  const isEmployeeHasPersonalCars = !!personalCars?.length;
  const isNoPersonalCars = transportNormalized === TransportTypeEnum.PERSONAL && !isEmployeeHasPersonalCars;

  const getDisableReason = (): string => {
    let errorMsg = '';
    if (!_tripCost) {
      errorMsg += 'Нет стоимости поездки. ';
    }
    if (!_purpose) {
      errorMsg += 'Нет цели. ';
    }

    return errorMsg;
  };

  const externalDisabled = !isExternal || !from || !to;

  const disabled
    = request?.transportType === TransportTypeEnum.PUBLIC ? Boolean(!_purpose) : Boolean(!_tripCost || !_purpose);
  const disabledReason = getDisableReason();

  useEffect(() => {
    if (disabled) {
      // eslint-disable-next-line no-console
      console.log('Причина отключения кнопки заказа:', disabledReason);
    }
  }, [disabled, _tripCost, _purpose, disabledReason]);

  return {
    disabled: isExternal ? externalDisabled : disabled,
    getDisableReason,
    isNoPersonalCars,
  };
};
