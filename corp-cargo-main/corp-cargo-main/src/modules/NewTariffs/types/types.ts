import { TariffJson } from 'stores/Tariffs/Tariffs.interface';
import { Dispatch, SetStateAction } from 'react';
import { SelectOption } from '../../TripPurposes/types/types';

export interface FilterProps {
  acceptFilters: (values: Record<string, string>) => void;
}

export interface TariffFormProps {
  goToTariffsList: () => void;
  deactivateTariff?: () => void;
  organizationsOptions: SelectOption[];
  contractorsOptions: SelectOption[];
  transportServiceTypes: SelectOption[];
  regionsOptions: SelectOption[];
  contractorTariffId: string | undefined;
  transportValue: string;
  setTransportValue: any;
  taxiClassOptions: SelectOption[];
  createTariff?: any;
  updateTariff?: any;
  initialId?: string;
  initialValues?: TariffJson;
  transportType?: string;
  compensationOptions: SelectOption[];
  busy: boolean;
  setBusy: Dispatch<SetStateAction<boolean>>;
}

export interface PersonalTariffTab {
  rideCostPerKmInit?: number;
  rideCostPerMinInit?: number;
  distanceIncludedInit?: number;
  minRideDistanceCostInit?: number;
  timeIncludedInit?: number;
  minRideTimeCostInit?: number;
}

export interface TaxiTariffTab {
  rideCostPerKmInit?: number;
  rideCostPerMinInit?: number;
}
