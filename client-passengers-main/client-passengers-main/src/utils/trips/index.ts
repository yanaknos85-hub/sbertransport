import { TTaxiClass, TaxiClassEnum } from 'stores/Trip/Trip.interface';

const classicTaxi = [TaxiClassEnum.ECONOMY, TaxiClassEnum.COMFORT, TaxiClassEnum.COMFORT_PLUS, TaxiClassEnum.BUSINESS];
export const taxiClassIsClassic = (taxiClass: TaxiClassEnum | undefined): boolean => !!(taxiClass && classicTaxi.includes(TaxiClassEnum[taxiClass]));

const privilegedTaxi = [TaxiClassEnum.COMFORT, TaxiClassEnum.COMFORT_PLUS, TaxiClassEnum.BUSINESS];
export const isPrivilegedTaxiClass = (taxiClass: TTaxiClass | TaxiClassEnum | undefined): boolean => !!taxiClass && privilegedTaxi.includes(taxiClass as TaxiClassEnum);
