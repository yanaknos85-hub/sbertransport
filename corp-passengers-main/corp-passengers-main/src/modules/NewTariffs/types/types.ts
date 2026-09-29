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
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setTransportValue: any;
  taxiClassOptions: SelectOption[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  createTariff?: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateTariff?: any;
  initialId?: string;
  initialValues?: TariffJson;
  transportType?: string;
  compensationOptions: SelectOption[];
  busy: boolean;
  setBusy: Dispatch<SetStateAction<boolean>>;
}

export interface PersonalTariffTab {
  initialValues?: TariffJson;
  isFormEditDisabled?: boolean;
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
