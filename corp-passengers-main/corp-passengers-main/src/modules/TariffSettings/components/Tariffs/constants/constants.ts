import { TariffsHanbookTitles } from 'modules/NewTariffs/constants/Tariffs.constants';
import { LabeledValue } from 'utils/Types';

export enum Modes {
  Passengers = 'passengers',
  Cargo = 'cargo',
}

export const tariffStatusOptions: LabeledValue[] = [
  { label: TariffsHanbookTitles.statusActive, value: 'true' },
  { label: TariffsHanbookTitles.statusInactive, value: 'false' },
];

export const taxiClassOptions = [
  { label: 'Эконом', value: 'ECONOMY' },
  { label: 'Комфорт', value: 'COMFORT' },
  { label: 'Комфорт+', value: 'COMFORT_PLUS' },
  { label: 'Бизнес', value: 'BUSINESS' },
  { label: 'Служебный', value: 'OFFICIAL' },
  { label: 'Автобус до 9 мест', value: 'VIP_BUS' },
  { label: 'Автобус от 10 до 21 места', value: 'SMALL_BUS' },
  { label: 'Автобус от 22 до 41 места', value: 'MIDDLE_BUS' },
  { label: 'Автобус от 42 до 55 места', value: 'LARGE_BUS' },
  { label: 'VIP', value: 'TRANSFER_VIP' },
  { label: 'Выбор автомобиля', value: 'TRANSFER_CAR_CHOICE' },
];
