import { Dispatch, SetStateAction } from 'react';
import { FormInstance } from 'antd/lib/form';

import { AutoPark, AutoParks, CacheParks } from 'api/autopark/autopark.types';
import {
  CacheAllSearchedVehicle,
  Vehicle,
  VehicleModel,
  VehiclesSearchRequest,
  VehiclesSearchResponse
} from 'api/vehicles/vehicles.types';
import { LabeledValue } from './Types';
import { UUID } from 'utils/io-ts';

export const EcoClassDescription = {
  EURO_0: 'Евро-0',
  EURO_1: 'Евро-1',
  EURO_2: 'Евро-2',
  EURO_3: 'Евро-3',
  EURO_4: 'Евро-4',
  EURO_5: 'Евро-5',
  EURO_6: 'Евро-6',
  EURO_7: 'Евро-7',
};

export interface VehicleInfoOptions {
  transmissionTypeOptions: LabeledValue[];
  brandsOptions: LabeledValue[];
  models: VehicleModel[];
}

export interface VehiclesSearch {
  contractorId: string | UUID;
  data: VehiclesSearchRequest;
}

export interface ManufactureYear {
  manufactureYear?: number;
  startDateManufactureYear?: number;
  endDateManufactureYear?: number;
}

export interface VehiclesFilterProps {
  className: string;
  responseAutoParks: CacheParks;
  vehiclesProps: VehiclesSearch;
  setVehiclesProps: Dispatch<SetStateAction<VehiclesSearch>>;
  responseAllVehicles: CacheAllSearchedVehicle;
  isFetchingSearchVehicles?: boolean;
  selectDropDown: string;
}

export interface VehiclesTableProps {
  vehiclesSearchResponse: VehiclesSearchResponse;
  vehiclesProps: VehiclesSearch;
  setVehiclesProps: Dispatch<SetStateAction<VehiclesSearch>>;
  isFetchingSearchVehicles?: boolean;
}

type DescriptionsField = [string, string | number | undefined | null];

export type UseDescriptions = DescriptionsField[];

export interface RouterProps {
  autoparkId: UUID;
  vehicleId: UUID;
}

export interface VehicleCardFormProps {
  className?: string;
  form: FormInstance;
  editMode?: boolean;
  autoParks: AutoPark[];
  selectDropDown: string;
}

export interface VehiclesContextType {
  contractorId: UUID;
  responseAutoParks: AutoParks;
  vehiclesProps: VehiclesSearch;
  setVehiclesProps: React.Dispatch<React.SetStateAction<VehiclesSearch>>;
  vehiclesSearchResponse: VehiclesSearchResponse;
  isFetchingSearchVehicles: boolean;
  vehicles: CacheAllSearchedVehicle;
}

export interface VehicleCardProps {
  editMode: boolean;
  setEditMode: Dispatch<SetStateAction<boolean>>;
  vehicleCard: Vehicle;
}

export interface TSelectOption {
  value: string | number;
  label: string;
}
