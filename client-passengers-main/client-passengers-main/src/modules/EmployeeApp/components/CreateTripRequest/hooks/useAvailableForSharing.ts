import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

export const useAvalailableForSharing = (
  transport?: TransportTypeEnum
): {
  isAvailableForSharing: boolean;
} => {
  let isAvailableForSharing = false;

  if (transport) {
    isAvailableForSharing = [
      TransportTypeEnum.TAXI,
      TransportTypeEnum.PERSONAL,
      TaxiClassEnum.BUSINESS,
      TaxiClassEnum.ECONOMY,
      TaxiClassEnum.COMFORT,
      TaxiClassEnum.COMFORT_PLUS,
    ].includes(transport);
  }

  return {
    isAvailableForSharing,
  };
};
