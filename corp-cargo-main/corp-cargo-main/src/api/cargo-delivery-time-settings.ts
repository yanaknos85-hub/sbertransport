import { TypeAtKey, useAPI, useAPIMutation } from './index';
import { CARGO_DELIVERY_TIME_SETTINGS } from '../constants/constants.api';
import {
  CargoDeliveryTimeSettingsArray,
  CargoDeliveryTimeSettingsArrayType
} from '../stores/CargoDeliveryTimeSettings/CargoDeliveryTimeSettings.interface';

declare module 'api' {
  interface Cache {
    cargoDeliveryTimeSettingsArray: {
      key: ['cargoDeliveryTimeSettingsArray'];
      value: {
        cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType;
      };
    };
  }
}

type CacheItem = TypeAtKey<['cargoDeliveryTimeSettingsArray']>;

const raw2cache = (cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType): CacheItem => ({
  cargoDeliveryTimeSettingsArray,
});

export const useGetCargoDeliveryTimeSettings = () => useAPI(['cargoDeliveryTimeSettingsArray'], ({ http, process }) => http
  .get<CargoDeliveryTimeSettingsArrayType>(CARGO_DELIVERY_TIME_SETTINGS)
  .then(process.decodeResponseData(CargoDeliveryTimeSettingsArray))
  .then(raw2cache)
);

export const useUpdateCargoDeliveryTimeSettings = () => useAPIMutation(
  (
    { http },
    { cargoDeliveryTimeSettingsArray }: { cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType }
  ) => http.post(CARGO_DELIVERY_TIME_SETTINGS, cargoDeliveryTimeSettingsArray),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DeadlineSettings.messages.updateSuccess);
      cache.refetchQueries(['cargoDeliveryTimeSettingsArray']);
    },
  }
);
