import { reject } from 'ramda';
import { AxiosError } from 'axios';
import moment from 'moment';

import { ILogger } from '@sber-sbertransport/mf-core';
import { AutoPark } from 'api/autopark/autopark.types';
import {
  ImportErrors, Vehicle, VehicleModel, VehiclesFilterFields, VehiclesSearchRequest
} from 'api/vehicles/vehicles.types';
import { TransportSearchRequest } from 'api/transport/transport.types';
import { Eco } from 'api/vehicles/vehicles.constants';

import {
  EcoClassDescription,
  ManufactureYear,
  VehicleInfoOptions
} from 'types/vehicles';
import { LabeledValue } from 'types/Types';

import { MIN_VEHICLE_MANUFACTURED_YEAR } from 'utils/fieldValidationRules/fieldValidationRules';
import { RangeNumberPartial } from 'utils/fieldValidationRules/types/Ranges';
import { sortLabelValue } from 'utils/sortLabeledValue';

export const transformStateNumber = (stateNumber: string | null | undefined, emptyValue = '-'): string => stateNumber
  ? stateNumber
    .split('')
    .map((x, i) => (i === 6 || i === 9 ? ` ${x}` : x))
    .join('')
    .trim()
  : emptyValue;

export const transformVin = (vin: string | null | undefined, emptyValue = '-'): string => vin
  ? vin
    .split('')
    .map((x, i) => (i === 3 || i === 9 || i === 10 ? `-${x}` : x))
    .join('')
  : emptyValue;

export const transformInsuranceNumber = (insuranceNumber: string | null | undefined, emptyValue = '-'): string => insuranceNumber
  ? insuranceNumber
    .split('')
    .map((x, i) => (i === 3 ? ` ${x}` : x))
    .join('')
  : emptyValue;

export const transformVehiclePassport = (passport: string | null | undefined, emptyValue = '-'): string => passport
  ? passport
    .split('')
    .map((x, i) => (i === 2 || i === 4 ? ` ${x}` : x))
    .join('')
  : emptyValue;

export const transformManufactureYear = (
  data: number | RangeNumberPartial | undefined
): ManufactureYear | undefined => {
  if (!data) {
    return undefined;
  }
  if (typeof data === 'number') {
    return { manufactureYear: data };
  }
  return {
    startDateManufactureYear: data.start ?? MIN_VEHICLE_MANUFACTURED_YEAR,
    endDateManufactureYear: data.end ?? +moment().format('gggg'),
  };
};

export const getVehicleModelInfo = (model: VehicleModel | undefined, emptyValue = '-'): string => model ? `${model.brand ?? '-'}, ${model.name ?? '-'}` : emptyValue;

export const getInExploitation = (inExploitation: boolean | null | undefined): string => inExploitation ? 'Да' : 'Нет';

export const getEcoClassDescription = (ecoClass: Eco | null | undefined, emptyValue = '-'): string => ecoClass ? EcoClassDescription[ecoClass] : emptyValue;

export const convertAutoParksToOptions = (autoParks: AutoPark[]): LabeledValue[] => (
  autoParks.map(item => ({ label: item.name, value: item.id }))
);

export const sleep = (n: number): Promise<void> => new Promise(resolve => setTimeout(resolve, n));

export const getUniqBrands = (vehicles: Vehicle[]): LabeledValue[] => {
  const uniqBrands = new Map<string, LabeledValue>();
  vehicles.forEach(({ model }) => uniqBrands.set(model.brand, { label: model.brand, value: model.brand }));
  const brandsOptions = [] as LabeledValue[];
  uniqBrands.forEach(item => brandsOptions.push(item));
  return brandsOptions.sort(sortLabelValue);
};

export const getUniqModels = (vehicles: Vehicle[]): VehicleModel[] => {
  const uniqModels = new Map<string | null, VehicleModel>();
  vehicles.forEach(({ model }) => uniqModels.set(model.id as string, model));
  const models = [] as VehicleModel[];
  uniqModels.forEach(item => models.push(item));
  return models;
};

export const getUniqTransmissionTypes = (vehicles: Vehicle[]): LabeledValue[] => {
  const uniqTransmissionTypes = new Map<string, LabeledValue>();
  vehicles.forEach(vehicle => {
    vehicle.transmissionType
    && uniqTransmissionTypes.set(vehicle.transmissionType, {
      label: vehicle.transmissionType,
      value: vehicle.transmissionType,
    });
  });
  const transmissionTypeOptions = [] as LabeledValue[];
  uniqTransmissionTypes.forEach(item => transmissionTypeOptions.push(item));
  return transmissionTypeOptions.sort(sortLabelValue);
};

export const convertVehiclesToOptions = (vehicles: Vehicle[]): VehicleInfoOptions => ({
  transmissionTypeOptions: getUniqTransmissionTypes(vehicles),
  brandsOptions: getUniqBrands(vehicles),
  models: getUniqModels(vehicles),
});

export const getCreateUpdateVehicleErrors = (err: AxiosError, logger: ILogger): void => {
  if (err.isAxiosError && err.response && err.response.status === 409) {
    logger.toMessage('error', err.response.data.message);
  }
};

export const getCurrentErrors = (errors: ImportErrors | null, emptyValue = []): (string | null | undefined)[] => {
  if (!errors) {
    return emptyValue;
  }
  const {
    stateNumberError,
    vinError,
    brandError,
    modelNameError,
    insuranceNumberError,
    colorError,
    fuelConsumptionError,
    ecoClassError,
    packageClassError,
    manufactureYearError,
    errorImportDuplicateStateNumber,
    errorImportDuplicateVin,
  } = errors;

  return [
    stateNumberError?.stateNumberDuplicateError,
    stateNumberError?.stateNumberAbsentError,
    stateNumberError?.stateNumberFormatError,
    vinError?.vinDuplicateError,
    vinError?.vinAbsentError,
    vinError?.vinFormatError,
    brandError?.brandFormatError,
    brandError?.brandAbsentError,
    modelNameError?.modelNameFormatError,
    modelNameError?.modelNameAbsentError,
    insuranceNumberError?.insuranceNumberFormatError,
    insuranceNumberError?.insuranceNumberAbsentError,
    colorError,
    fuelConsumptionError?.fuelConsumptionFormatError,
    fuelConsumptionError?.fuelConsumptionAbsentError,
    ecoClassError,
    packageClassError?.packageClassFormatError,
    packageClassError?.packageClassAbsentError,
    manufactureYearError,
    errorImportDuplicateStateNumber,
    errorImportDuplicateVin,
  ];
};

export const checkChangedParams = (prev: string | undefined, next: string | undefined): boolean => {
  if ((!prev && !next) || (prev && !next)) {
    return false;
  }
  return prev !== next;
};

export const isNeedToCheck = (prev: string | undefined, next: string | undefined, inExploitation: boolean): boolean => (
  inExploitation || checkChangedParams(prev, next)
);

export const checkStateNumber = (stateNumber: string, id: string | undefined, vehicles: Vehicle[]): boolean => (
  !!vehicles.find(item => (id ? item.stateNumber === stateNumber && item.id !== id : item.stateNumber === stateNumber))
);

export const checkVin = (vin: string, id: string | undefined, vehicles: Vehicle[]): boolean => (
  !!vehicles.find(item => (id ? item.vin === vin && id && item.id !== id : item.vin === vin))
);

export const checkPassport = (passport: string | undefined, id: string | undefined, vehicles: Vehicle[]): boolean => {
  if (!passport) {
    return false;
  }

  return !!vehicles.find(item => id ? item.passport === passport && id && item.id !== id : item.passport === passport
  );
};

export const getDupleErrorMessage = (errors: string[], logger: ILogger): void => {
  errors.forEach(message => logger.toMessage('error', message));
};

export const processRequestParams = (data: VehiclesFilterFields) => {
  const { stateNumber, autoparkId } = data;

  const params = {
    ...data,
    autopark: autoparkId,
    stateNumber: stateNumber?.replaceAll(' ', ''),
  };

  delete params.autoparkId;

  return reject(x => typeof x === 'undefined' || x === '')(params) as VehiclesSearchRequest;
};

export const processRequestTransportParams = (query: TransportSearchRequest) => {
  let data = JSON.parse(JSON.stringify(TransportSearchRequest.encode(query)));

  data['page'] = { page: data.page, size: data.size };

  delete data['size'];

  if (query?.status?.length) {
    data = { ...data, status: query.status[0] };
  }

  return data;
};
