import { TaxiClass, ServiceType } from 'constants/trips.constants';

/** Маппинг: вид сервиса → список taxiClass */
export const serviceTypeToTaxiClasses: Record<ServiceType, TaxiClass[]> = {
  [ServiceType.TAXI]: [TaxiClass.ECONOMY, TaxiClass.COMFORT, TaxiClass.COMFORT_PLUS, TaxiClass.BUSINESS],
  [ServiceType.TRANSFER]: [TaxiClass.GROUP_TRANSFER],
};

/** Опции для выпадающего списка в модалке фильтров */
export const serviceTypeOptions = [
  { label: 'Такси', value: ServiceType.TAXI },
  { label: 'Трансфер', value: ServiceType.TRANSFER },
];
