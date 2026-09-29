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
