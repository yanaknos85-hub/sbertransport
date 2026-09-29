import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

const classicTaxi = [TaxiClassEnum.ECONOMY, TaxiClassEnum.COMFORT, TaxiClassEnum.BUSINESS];
export const taxiClassIsClassic = (taxiClass: TaxiClassEnum | undefined): boolean => !!(taxiClass && classicTaxi.includes(TaxiClassEnum[taxiClass]));
