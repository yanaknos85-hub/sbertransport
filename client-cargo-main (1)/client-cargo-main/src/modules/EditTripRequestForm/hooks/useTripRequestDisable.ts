import { useEffect } from 'react';
import { useCurrentLimit, useTripLimitHolder } from 'shared/hooks/limit';
import { TCoordinates } from 'shared/models/geo/types';

import { LIMIT_SERVICE_TYPE } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import { Transport } from 'stores/Trip/Trip.interface';
import { TripRequestPurpose } from 'modules/EditTripRequestForm/hooks/useTripRequestPurpose';

import { usePersonalCars } from '../../CreateTripRequest/hooks/usePersonalCars';
import { calculateTripCost } from '../../CreateTripRequest/utils/utils';

export const useTripRequestDisable = ({
  tariffsCosts,
  tripPurpose,
  request,
  transport,
  isExternal,
  from,
  to,
}: {
  tariffsCosts: TripPriceModel[];
  tripPurpose: TripRequestPurpose;
  request?: TripRequestModel | null;
  transport?: Transport;
  isExternal?: boolean;
  from?: TCoordinates;
  to?: TCoordinates;
}): {
    disabled: boolean;
    getDisableReason: () => string;
    isNoPersonalCars: boolean;
  } => {
  const transportNormalized = transport?.split('-')[0] as Transport;
  const _tripCost = calculateTripCost(tariffsCosts, transportNormalized);
  const limitHolder = useTripLimitHolder(request);
  const limit = useCurrentLimit(limitHolder);
  const passengerLimit = limit?.find(x => x.limitServiceType === LIMIT_SERVICE_TYPE.PASSENGER);
  const _limit = passengerLimit?.sum;
  const _purpose = tripPurpose.purposeValidity.isValid;
  const { personalCars } = usePersonalCars();
  const isEmployeeHasPersonalCars = !!personalCars?.length;
  const isNoPersonalCars = transportNormalized === TransportTypeEnum.PERSONAL && !isEmployeeHasPersonalCars;

  const getDisableReason = (): string => {
    let errorMsg = '';
    if (!_tripCost) {
      errorMsg += 'Нет стоимости поездки. ';
    }
    if (!_limit) {
      errorMsg += 'Нет лимита. ';
    }
    if (_tripCost && _limit && _tripCost > _limit) {
      errorMsg += `Не хватает лимита:  ${_tripCost - _limit} `;
    }
    if (!_purpose) {
      errorMsg += 'Нет цели. ';
    }

    return errorMsg;
  };

  const externalDisabled = !isExternal || !from || !to;

  const disabled
    = request?.transportType === TransportTypeEnum.PUBLIC
      ? Boolean(!_limit || !_purpose)
      : Boolean(!_tripCost || !_limit || _tripCost > _limit || !_purpose);
  const disabledReason = getDisableReason();

  useEffect(() => {
    if (disabled) {
      // eslint-disable-next-line no-console
      console.log('Причина отключения кнопки заказа:', disabledReason);
    }
  }, [disabled, _tripCost, _limit, _purpose, disabledReason]);

  return {
    disabled: isExternal ? externalDisabled : disabled,
    getDisableReason,
    isNoPersonalCars,
  };
};
