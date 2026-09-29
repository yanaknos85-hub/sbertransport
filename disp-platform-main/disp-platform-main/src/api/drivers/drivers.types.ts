
import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { createPagination } from 'utils/io-ts/pagination';
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
  t.partial({
    autoparkId: t.string,
  }),
]);

export type Driver = t.TypeOf<typeof Driver>;

export type DriverData = Omit<Driver, 'id' | 'humanReadableId'>;

export interface UseGetDriverProps {
  contractorId: string;
  driverId: string;
}

export const UseGetDriverResponse = Driver;

export type UseUpdateDriverRequest = DriverData;

export interface UseUpdateDriverProps {
  driverId: string;
  data: UseUpdateDriverRequest;
}

export const UseUpdateDriverResponse = t.string;

export type UseUpdateDriverResponse = t.TypeOf<typeof UseUpdateDriverResponse>;

export const UseCreateDriverResponse = Driver;
export const AllDrivers = t.array(Driver);

export const AllDriversResponse = createPagination(Driver);

export type UseGetDriverResponse = t.TypeOf<typeof UseCreateDriverResponse>;

export type UseCreateDriverResponse = t.TypeOf<typeof UseCreateDriverResponse>;
export type AllDrivers = t.TypeOf<typeof AllDrivers>;

export type AllDriversResponse = t.TypeOf<typeof AllDriversResponse>;

export const UseSearchDriversRating = t.strict({
  start: t.number,
  end: t.number,
});

export type UseSearchDriversRating = t.TypeOf<typeof UseSearchDriversRating>;

export const UseSearchDriversFilter = t.partial({
  driverHumanId: t.string,
  driverFullName: t.string,
  isActive: t.boolean,
  ratingRange: UseSearchDriversRating,
  driverLicenseClass: t.array(t.string),
  driverSpeciality: ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes),
});

export type UseSearchDriversFilter = t.TypeOf<typeof UseSearchDriversFilter>;

export const UseSearchDriversPageSettings = t.strict({
  page: t.number,
  size: t.number,
});

export const UseSearchDriversSortSettings = t.strict({
  property: t.string,
  directionAsc: t.boolean,
});

export const UseSearchDriversRequest = t.intersection([UseSearchDriversFilter, UseSearchDriversPageSettings]);

export type UseSearchDriversRequest = t.TypeOf<typeof UseSearchDriversRequest>;

export interface UseSearchDriversProps {
  contractorId: string;
  data: UseSearchDriversRequest;
  autoparkId?: string;
}

export const SearchDriversResponse = createPagination(Driver);

export type SearchDriversResponse = t.TypeOf<typeof SearchDriversResponse>;

export const UseGetDriverLicensesResponse = t.array(
  t.strict({
    name: t.string,
  })
);

export type UseGetDriverLicensesResponse = t.TypeOf<typeof UseGetDriverLicensesResponse>;

export const PassportError = t.type({
  passportFormatError: tt.nullable(t.string),
  errorDuplicatePassport: tt.nullable(t.string),
  passportAbsentError: tt.nullable(t.string),
});

export type PassportError = t.TypeOf<typeof PassportError>;

export const ImportErrors = t.type({
  lastNameError: tt.nullable(t.string),
  firstNameError: tt.nullable(t.string),
  patronymicError: tt.nullable(t.string),
  phoneError: tt.nullable(t.string),
  serviceLicenseError: tt.nullable(t.string),
  licenseClassesError: tt.nullable(t.string),
  driverLicenseNumberError: tt.nullable(t.string),
  passportError: tt.nullable(PassportError),
  errorImportDuplicatePassport: tt.nullable(t.string),
});

export type ImportErrors = t.TypeOf<typeof ImportErrors>;

export const ImportedDriver = t.intersection([
  t.type({
    contractorInfo: t.type({ id: tt.uuid, name: t.string }),
    firstName: t.string,
    lastName: t.string,
    driverLicenses: t.array(DriverLicense),
  }),
  t.partial({
    patronymic: t.string,
    contactPhone: t.string,
    serviceLicenseNumber: t.string,
    driverLicenseNumber: t.string,
    passport: t.string,
    errors: ImportErrors,
  }),
]);

export type ImportedDriver = t.TypeOf<typeof ImportedDriver>;

export const ImportedDrivers = t.strict({
  id: tt.uuid,
  filename: t.string,
  data: t.array(ImportedDriver),
});

export type ImportedDrivers = t.TypeOf<typeof ImportedDrivers>;
