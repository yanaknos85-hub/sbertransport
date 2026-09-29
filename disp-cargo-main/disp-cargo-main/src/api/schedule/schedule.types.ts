import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { DriverSpecialityTypes } from 'constants/driver.constants';
import { TripTypes } from 'constants/app.constants';

export const ShiftsDriver = t.intersection([
  t.strict({
    id: tt.uuid,
    humanReadableId: t.string,
    firstName: t.string,
    lastName: t.string,
    online: t.boolean,
  }),
  t.partial({
    patronymic: t.string,
    driverSpeciality: ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes),
  }),
]);

export type ShiftsDriver = t.TypeOf<typeof ShiftsDriver>;

export const ShiftsVehicle = t.type({
  stateNumber: t.string,
  id: tt.uuid,
  active: t.boolean,
  vehicleType: ioTypeFromEnum<TripTypes>('DriverSpeciality', TripTypes),
});

export type ShiftsVehicle = t.TypeOf<typeof ShiftsVehicle>;

export const Shift = t.type({
  id: tt.uuid,
  active: t.boolean,
  driver: ShiftsDriver,
  vehicle: ShiftsVehicle,
  startDate: t.string,
  endDate: t.string,
});

export type Shift = t.TypeOf<typeof Shift>;

export const OneShiftsRequest = t.type({
  contractorId: tt.uuid,
  shiftId: tt.uuid,
});

export type OneShiftsRequest = t.TypeOf<typeof OneShiftsRequest>;
