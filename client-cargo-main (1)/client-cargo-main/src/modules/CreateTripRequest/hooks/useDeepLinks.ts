import { FormInstance } from 'antd';
import { TCoordinates } from 'shared/models/geo/types';

import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

export const useDeepLinks = (
  form: FormInstance,
  from?: TCoordinates,
  to?: TCoordinates
): {
  deeplinkYandex: string | null;
  deeplinkUber: string | null;
  deeplinkCitymobil: string | null;
} => {
  const economyExternal = form.getFieldValue(`externalPrices_${TaxiClassEnum.ECONOMY}`);
  const comfortExternal = form.getFieldValue(`externalPrices_${TaxiClassEnum.COMFORT}`);
  const businessExternal = form.getFieldValue(`externalPrices_${TaxiClassEnum.BUSINESS}`);

  const economyExternalProvider = economyExternal?.split(`_${TaxiClassEnum.ECONOMY}`)[0];
  const comfortExternalProvider = comfortExternal?.split(`_${TaxiClassEnum.COMFORT}`)[0];
  const businessExternalProvider = businessExternal?.split(`_${TaxiClassEnum.BUSINESS}`)[0];

  let tariff;

  if (economyExternalProvider) {
    tariff = TaxiClassEnum.ECONOMY;
  }

  if (comfortExternalProvider) {
    tariff = TaxiClassEnum.COMFORT;
  }

  if (businessExternalProvider) {
    tariff = TaxiClassEnum.BUSINESS;
  }

  const provider = economyExternalProvider || comfortExternalProvider || businessExternalProvider;

  const deeplinkYandex
    = provider === TaxiClassEnum.YANDEX
      ? `https://taxi.yandex.ru/?utm_source&utm_medium&gfrom=${from?.latitude}%2C${from?.longitude}&gto=${to?.latitude}%2C${to?.longitude}&ref=yoursiteru&level=50&city&tariff=${tariff}&referrer=appmetrica_tracking_id%3D1178268795219780156%26ym_tracking_id%3D17443706260962397846;`
      : null;
  const deeplinkUber
    = provider === TaxiClassEnum.UBER
      ? `https://taxi.yandex.ru/?utm_source&utm_medium&gfrom=${from?.latitude}%2C${from?.longitude}&gto=${to?.latitude}%2C${to?.longitude}&ref=yoursiteru&level=50&city&tariff=${tariff}&referrer=appmetrica_tracking_id%3D1178268795219780156%26ym_tracking_id%3D17443706260962397846;`
      : null;
  const deeplinkCitymobil = provider === TaxiClassEnum.CITYMOBIL ? 'https://city-mobil.ru/' : null;

  return {
    deeplinkYandex,
    deeplinkUber,
    deeplinkCitymobil,
  };
};
