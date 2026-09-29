import { DRIVER_LICENSES } from 'constants/driver.constants';
import { Driver, ImportErrors, UseGetDriverLicensesResponse } from 'api/drivers/drivers.types';
import { DRIVER_ACTIVE_DESCRIPTIONS } from 'constants/driver.constants';
import { LabeledValue } from 'types/Types';
import { DriversProps } from 'types/drivers';
import { formatDriverRating } from 'utils/formatDriverRating';

export const transformPatronymic = (patronymic: string | undefined, emptyValue = '-'): string => (
  patronymic ?? emptyValue
);

export const getDriverLicenseOptions = (driverLicenses: UseGetDriverLicensesResponse): LabeledValue[] => (
  driverLicenses?.map(({ name }) => ({ value: name, label: name })) || []
);

export const getDriverLicenses = (driverLicenses: DRIVER_LICENSES[] | undefined, emptyValue = '-'): string => (
  driverLicenses ? driverLicenses.join(', ') : emptyValue
);

export const getDriverActivity = (isActive: boolean | undefined, emptyValue = '-'): string => {
  if (isActive === undefined) {
    return emptyValue;
  }

  return isActive ? DRIVER_ACTIVE_DESCRIPTIONS.ACTIVE : DRIVER_ACTIVE_DESCRIPTIONS.NOT_ACTIVE;
};

export const getDriverData = (drivers: Driver[] | undefined): DriversProps[] | undefined => {
  if (!drivers) return undefined;

  return drivers.map(driver => ({
    ...driver,
    licenseClasses: getDriverLicenses(driver.driverLicenses),
    rating: formatDriverRating(driver.rating),
    isActive: getDriverActivity(driver.active),
  }));
};

export const getCurrentErrors = (errors: ImportErrors | null, emptyValue = []): (string | null | undefined)[] => {
  if (!errors) return emptyValue;

  const {
    lastNameError,
    firstNameError,
    patronymicError,
    phoneError,
    serviceLicenseError,
    licenseClassesError,
    driverLicenseNumberError,
    passportError,
    errorImportDuplicatePassport,
  } = errors;

  return [
    lastNameError,
    firstNameError,
    patronymicError,
    phoneError,
    serviceLicenseError,
    licenseClassesError,
    driverLicenseNumberError,
    passportError?.errorDuplicatePassport,
    passportError?.passportFormatError,
    passportError?.passportAbsentError,
    errorImportDuplicatePassport,
  ];
};
