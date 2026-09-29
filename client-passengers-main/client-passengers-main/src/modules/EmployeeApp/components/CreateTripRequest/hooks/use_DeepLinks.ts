import { TCoordinates } from 'shared/models/geo/types';
import { ExternalProviderEnum, TaxiEnum } from 'stores/Trip/Trip.interface';

export const useDeepLinks = (
  externalProvider?: ExternalProviderEnum,
  subClass?: TaxiEnum,
  from?: TCoordinates,
  to?: TCoordinates
): {
  deeplinkYandex: string | null;
  deeplinkUber: string | null;
  deeplinkCitymobil: string | null;
} => {
  const deeplinkYandex
    = externalProvider === ExternalProviderEnum.YANDEX
      ? `https://taxi.yandex.ru/?utm_source&utm_medium&gfrom=${from?.latitude}%2C${from?.longitude}&gto=${to?.latitude}%2C${to?.longitude}&ref=${window.location.href}&level=50&city&tariff=${subClass}&referrer=appmetrica_tracking_id%3D1178268795219780156%26ym_tracking_id%3D17443706260962397846;`
      : null;
  const deeplinkUber
    = externalProvider === ExternalProviderEnum.UBER
      ? `https://taxi.yandex.ru/?utm_source&utm_medium&gfrom=${from?.latitude}%2C${from?.longitude}&gto=${to?.latitude}%2C${to?.longitude}&ref=${window.location.href}&level=50&city&tariff=${subClass}&referrer=appmetrica_tracking_id%3D1178268795219780156%26ym_tracking_id%3D17443706260962397846;`
      : null;
  const deeplinkCitymobil = externalProvider === ExternalProviderEnum.CITYMOBIL ? 'https://city-mobil.ru/' : null;

  return {
    deeplinkYandex,
    deeplinkUber,
    deeplinkCitymobil,
  };
};
