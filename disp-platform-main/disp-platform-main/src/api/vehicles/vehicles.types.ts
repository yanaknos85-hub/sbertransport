import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { createPagination } from 'utils/io-ts/pagination';

import { AutoPark } from 'api/autopark/autopark.types';
import { VehiclesSearch } from 'types/vehicles';
import { TripTypes } from 'constants/app.constants';
import { Eco } from './vehicles.constants';
import { TypeAtKey } from '../index';

export const EcoClass = t.type({
  name: t.string,
  rusName: t.string,
});

export const VehiclesRangeNumber = t.partial({
  start: t.number,
  end: t.number,
});

export type VehiclesRangeNumber = t.TypeOf<typeof VehiclesRangeNumber>;

export const VehicleAdditional = t.partial({
  semitrailerNumber: t.string,
  volume: t.number,
  length: t.number,
  width: t.number,
  height: t.number,
});

export const VehicleModel = t.intersection([
  t.type({
    brand: t.string,
  }),
  t.partial({
    year: tt.nullable(t.number),
    id: tt.nullable(tt.uuid),
    name: t.string,
  }),
]);

export const VehiclesFiltersData = t.partial({
  autopark: t.string,
  stateNumber: t.string,
  manufactureYear: t.number,
  startDateManufactureYear: t.number,
  endDateManufactureYear: t.number,
  transmissionType: t.string,
  brand: t.string,
  model: t.string,
  vehicleType: ioTypeFromEnum<TripTypes>('VehicleType', TripTypes),
});

export const Sort = t.strict({
  unsorted: t.boolean,
  sorted: t.boolean,
  empty: t.boolean,
});

export const VehiclesSearchRequest = t.intersection([
  VehiclesFiltersData,
  t.strict({
    page: t.number,
    size: t.number,
  }),
]);

export type VehiclesSearchRequest = t.TypeOf<typeof VehiclesSearchRequest>;

export const VehiclesFilterFields = t.partial({
  autoparkId: t.string,
  stateNumber: t.string,
  manufactureYear: t.union([t.number, VehiclesRangeNumber]),
  transmissionType: t.string,
  brand: t.string,
  model: t.string,
});

export type VehicleModel = t.TypeOf<typeof VehicleModel>;
export type VehiclesFilterFields = t.TypeOf<typeof VehiclesFilterFields>;

export const Vehicle = t.intersection([
  t.type({
    id: tt.uuid,
    stateNumber: t.string,
    inExploitation: t.boolean,
    model: VehicleModel,
    autopark: AutoPark,
    vehicleType: ioTypeFromEnum<TripTypes>('VehicleType', TripTypes),
  }),
  t.partial({
    passport: t.string,
    vin: t.string,
    mileage: t.number,
    color: t.string,
    manufactureYear: t.number,
    maxAllowedWeight: t.number,
    chassisType: t.string,
    transmissionType: t.string,
    bodyType: t.string,
    engineType: t.string,
    insuranceNumber: t.string,
    ecoClass: ioTypeFromEnum<Eco>('Eco', Eco),
    fuelConsumption: t.number,
    packageClass: t.string,
    active: t.boolean,
    vehicleAdditional: VehicleAdditional,
    autoparkId: t.string,
  }),
]);

export const VehicleCard = t.intersection([
  t.type({
    nameContractor: t.string,
    autoparkDTO: AutoPark,
  }),
  t.partial({
    vehicleDTO: Vehicle,
  }),
]);

export const VehiclesSearchResponse = createPagination(Vehicle);

export type VehiclesSearchResponse = t.TypeOf<typeof VehiclesSearchResponse>;

export interface CacheVehicles {
  vehicles: Vehicle[];
  byId: Record<string, Vehicle>;
}

export interface CacheAllSearchedVehicles {
  response: VehiclesSearchResponse;
  byId: Record<string, Vehicle>;
}

export interface VehicleSearchParams {
  page?: number;
  size?: number;
  stateNumber?: string;
  isActive?: boolean;
  inExploitation?: boolean;
  autopark?: string;
}

export type CacheAllSearchedVehicle = TypeAtKey<['allVehiclesSearch', VehiclesSearch]>;

export const StateNumberError = t.type({
  stateNumberFormatError: tt.nullable(t.string),
  stateNumberDuplicateError: tt.nullable(t.string),
  stateNumberAbsentError: tt.nullable(t.string),
});

export type StateNumberError = t.TypeOf<typeof StateNumberError>;

export const VinError = t.type({
  vinFormatError: tt.nullable(t.string),
  vinDuplicateError: tt.nullable(t.string),
  vinAbsentError: tt.nullable(t.string),
});

export type VinError = t.TypeOf<typeof VinError>;

export const BrandError = t.type({
  brandFormatError: tt.nullable(t.string),
  brandAbsentError: tt.nullable(t.string),
});

export type BrandError = t.TypeOf<typeof BrandError>;

export const ModelNameError = t.type({
  modelNameFormatError: tt.nullable(t.string),
  modelNameAbsentError: tt.nullable(t.string),
});

export type ModelNameError = t.TypeOf<typeof ModelNameError>;

export const InsuranceNumberError = t.type({
  insuranceNumberFormatError: tt.nullable(t.string),
  insuranceNumberAbsentError: tt.nullable(t.string),
});

export type InsuranceNumberError = t.TypeOf<typeof InsuranceNumberError>;

export const FuelConsumptionError = t.type({
  fuelConsumptionFormatError: tt.nullable(t.string),
  fuelConsumptionAbsentError: tt.nullable(t.string),
});

export type FuelConsumptionError = t.TypeOf<typeof FuelConsumptionError>;

export const PackageClassError = t.type({
  packageClassFormatError: tt.nullable(t.string),
  packageClassAbsentError: tt.nullable(t.string),
});

export type PackageClassError = t.TypeOf<typeof PackageClassError>;

export const ImportErrors = t.type({
  stateNumberError: tt.nullable(StateNumberError),
  vinError: tt.nullable(VinError),
  brandError: tt.nullable(BrandError),
  modelNameError: tt.nullable(ModelNameError),
  insuranceNumberError: tt.nullable(InsuranceNumberError),
  colorError: tt.nullable(t.string),
  fuelConsumptionError: tt.nullable(FuelConsumptionError),
  ecoClassError: tt.nullable(t.string),
  packageClassError: tt.nullable(PackageClassError),
  manufactureYearError: tt.nullable(t.string),
  errorImportDuplicateStateNumber: tt.nullable(t.string),
  errorImportDuplicateVin: tt.nullable(t.string),
});

export type ImportErrors = t.TypeOf<typeof ImportErrors>;

export const ImportedVehicle = t.intersection([
  t.type({
    autoparkInfo: t.type({ id: tt.uuid, name: t.string }),
    model: t.partial({ brand: t.string, name: t.string }),
  }),
  t.partial({
    stateNumber: t.string,
    vin: t.string,
    insuranceNumber: t.string,
    color: t.string,
    fuelConsumption: t.number,
    ecoClass: ioTypeFromEnum<Eco>('Eco', Eco),
    packageClass: t.string,
    manufactureYear: t.number,
    inExploitation: t.boolean,
    errors: ImportErrors,
  }),
]);

export type ImportedVehicle = t.TypeOf<typeof ImportedVehicle>;

export const ImportedVehicles = t.strict({
  id: tt.uuid,
  filename: t.string,
  data: t.array(ImportedVehicle),
});

export type ImportedVehicles = t.TypeOf<typeof ImportedVehicles>;

export type EcoClass = t.TypeOf<typeof EcoClass>;
export type Vehicle = t.TypeOf<typeof Vehicle>;
