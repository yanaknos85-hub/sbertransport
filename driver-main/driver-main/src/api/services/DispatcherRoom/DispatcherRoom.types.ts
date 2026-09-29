import { TripTypes } from 'constants/trips.constants';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/io-ts/ioTypeFromEnum';

export enum DriverSpecialityTypes {
  Passenger = 'PASSENGER',
  Cargo = 'CARGO',
  Both = 'BOTH',
}

/** Данные о себе */
export const DriverSelf = t.intersection([
  t.type({
    active: t.boolean,
    attributes: t.array(t.string),
    consent: t.boolean,
    contactPhone: t.string,
    contractorId: tt.uuid,
    driverLicenses: t.array(t.string),
    driverSpeciality: ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes),
    email: t.string,
    firstName: t.string,
    humanReadableId: t.string,
    id: tt.uuid,
    lastName: t.string,
    online: t.boolean,
    rating: t.number,
    phoneConfirmed: t.boolean,
  }),
  t.partial({
    patronymic: t.string,
    driverLicenseNumber: t.string,
  }),
]);
export type DriverSelf = t.TypeOf<typeof DriverSelf>;

/** Данные о своем авто */
export const VehicleSelf = t.type({
  active: t.boolean,
  autopark: t.type({
    id: tt.uuid,
    name: t.string,
    active: t.boolean,
    contractor: t.type({
      id: tt.uuid,
      name: t.string,
      autoassign: t.boolean,
      technicalAccountOwner: t.string,
      technicalAccountOwnerEmail: t.string,
      tin: t.string,
    }),
  }),
  id: tt.uuid,
  inExploitation: t.boolean,
  model: t.partial({
    brand: t.string,
    name: t.string,
  }),
  stateNumber: t.string,
  vehicleType: ioTypeFromEnum<TripTypes>('TripTypes', TripTypes),
});
export type VehicleSelf = t.TypeOf<typeof VehicleSelf>;

/** Данные о контрагенте водителя */
export const ContractorSelf = t.intersection([
  t.type({
    name: t.string,
    tin: t.string,
    digitId: t.number,
    id: tt.uuid,
    humanReadableId: t.string,
    autoassign: t.boolean,
    technicalAccountOwnerEmail: t.string,
    technicalAccountOwner: t.string,
  }),
  t.partial({
    mainDispatcher: t.type({
      id: tt.uuid,
      lastName: t.string,
      firstName: t.string,
      patronymic: t.string,
      humanReadableId: t.string,
      phone: t.string,
      email: t.string,
      contractorId: tt.uuid,
      autoassign: t.boolean,
      consent: t.boolean,
      phoneConfirmed: t.boolean,
    }),
  }),
]);
export type ContractorSelf = t.TypeOf<typeof ContractorSelf>;

/** Данные о блокировке подтверждение номера */
export const BlockPhoneConfirmation = t.intersection([
  t.type({
    blocked: t.boolean,
  }),
  t.partial({
    nextAllowTime: t.string,
  }),
]);
export type BlockPhoneConfirmation = t.TypeOf<typeof BlockPhoneConfirmation>;
