import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const SharedRideSettingsByType = t.type({
  id: tt.uuid,
  positions: t.array(t.type({ id: tt.uuid })),
  attributes: t.array(t.type({ id: tt.uuid })),
  employees: t.array(t.type({ id: tt.uuid })),
});

export const SharedRideSettingsType = t.string;

export const SharedRideSettings = t.type({
  id: tt.uuid,
  organizationId: tt.uuid,
  transportType: t.string,
  settings: t.record(SharedRideSettingsType, SharedRideSettingsByType),
  economyIndicationYellowRangeLowerBorder: t.number,
  economyIndicationYellowRangeUpperBorder: t.number,
});

export type SharedRideSettingsByType = t.TypeOf<typeof SharedRideSettingsByType>;
export type SharedRideSettingsType = t.TypeOf<typeof SharedRideSettingsType>;
export type SharedRideSettings = t.TypeOf<typeof SharedRideSettings>;

export type SharedRideSettingsQueryInput = Omit<PartialBy<SharedRideSettings, 'id'>, 'settings'> & {
  settings: Record<SharedRideSettingsType, PartialBy<SharedRideSettingsByType, 'id'>>;
};
