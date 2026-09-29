import { LimitSharing } from 'stores/Limits/Limit.interface';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import { TTaxiClass, TaxiClassEnum } from 'stores/Trip/Trip.interface';

const classicTaxi = [TaxiClassEnum.ECONOMY, TaxiClassEnum.COMFORT, TaxiClassEnum.COMFORT_PLUS, TaxiClassEnum.BUSINESS];
export const taxiClassIsClassic = (taxiClass: TaxiClassEnum | undefined): boolean => !!(taxiClass && classicTaxi.includes(TaxiClassEnum[taxiClass]));

const privilegedTaxi = [TaxiClassEnum.COMFORT, TaxiClassEnum.COMFORT_PLUS, TaxiClassEnum.BUSINESS];
export const isPrivilegedTaxiClass = (taxiClass: TTaxiClass | TaxiClassEnum | undefined): boolean => !!taxiClass && privilegedTaxi.includes(taxiClass as TaxiClassEnum);

export const calculateTaxiClassCost = (
  costs: TripPriceModel[],
  transportType: string | false | undefined,
  taxiClass?: string | undefined
): any => costs.find((x: TripPriceModel) => {
  if (x.transportType.name !== 'TAXI') {
    return x.transportType.name === transportType;
  }
  return x.taxiClass === taxiClass;
});

/**
 * Возвращает число процентов для строки состояния остатка по лимиту для видов транспорта
 * @param limitSharing
 */
export const getAvailablePercentage = (limitSharing?: LimitSharing | null): number => {
  if (limitSharing) {
    const { balance, sum } = limitSharing.limitSharingPerPeriodDTO || limitSharing;
    return sum ? Number(((balance * 100) / sum).toFixed(1)) : 0;
  }

  return 0;
};
