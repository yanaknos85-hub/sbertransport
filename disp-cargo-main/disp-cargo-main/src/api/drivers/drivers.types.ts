
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { DriverSpecialityTypes, DRIVER_LICENSES } from 'constants/driver.constants';

export enum DRIVER_EXPERIENCE {
  LESS_THEN_FIVE = 'LESS_THEN_FIVE',
  FIVE_TO_TEN = 'FIVE_TO_TEN',
  MORE_THEN_TEN = 'MORE_THEN_TEN',
}

const DriverLicense = t.intersection([
  t.strict({
    licenseClass: t.string,
  }),
  t.partial({
    id: tt.uuid,
  }),
]);

export type DriverLicense = t.TypeOf<typeof DriverLicense>;

export const DriverShort = t.intersection([
  t.strict({
    id: tt.uuid,
    // humanReadableId: t.string, // FixMe вернуть обязательное поле после доработки с бека
    firstName: t.string,
    lastName: t.string,
  }),
  t.partial({
    humanReadableId: t.string, // FixMe вернуть обязательное поле после доработки с бека
    patronymic: t.string,
    contactPhone: t.string,
    rating: t.number,
    driverLicenseNumber: t.string,
    serviceLicenseNumber: t.string,
    experience: ioTypeFromEnum<DRIVER_EXPERIENCE>('DRIVER_EXPERIENCE', DRIVER_EXPERIENCE),
    active: t.boolean,
    email: t.string,
    attributes: t.array(t.type({ name: t.string })),
    passport: t.string,
    driverLicenses: t.array(ioTypeFromEnum<DRIVER_LICENSES>('DRIVER_LICENSES', DRIVER_LICENSES)),
  }),
]);

export type DriverShort = t.TypeOf<typeof DriverShort>;

export const Driver = t.intersection([
  DriverShort,
  t.strict({
    driverSpeciality: ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes),
  }),
]);

export type Driver = t.TypeOf<typeof Driver>;

export type DriverData = Omit<Driver, 'id' | 'humanReadableId'>;

export interface UseGetDriverProps {
  contractorId: string;
  driverId: string;
}

export const UseGetDriverResponse = Driver;

export const UseCreateDriverResponse = Driver;

export type UseGetDriverResponse = t.TypeOf<typeof UseCreateDriverResponse>;

export type UseCreateDriverResponse = t.TypeOf<typeof UseCreateDriverResponse>;
