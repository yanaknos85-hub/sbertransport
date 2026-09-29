import { Dispatch, SetStateAction } from 'react';
import { TablePaginationConfig } from 'antd/lib/table';
import { SorterResult } from 'antd/es/table/interface';

import { UUID } from 'utils/io-ts';
import {
  ImportedDriver, AllDrivers, UseSearchDriversProps, SearchDriversResponse
} from 'api/drivers/drivers.types';

export enum DRIVER_ACTIVE {
  ACTIVE = 'ACTIVE',
  NOT_ACTIVE = 'NOT_ACTIVE',
}

export interface DriversProps {
  id: string;
  humanReadableId?: string;
  lastName: string;
  firstName: string;
  patronymic?: string;
  contactPhone?: string;
  licenseClasses?: string;
  isActive?: string;
  rating: number | string;
  serviceLicenseNumber?: string;
}

export interface DriversContextType {
  contractorId: UUID;
  drivers: AllDrivers;
  driversSearchResponse: SearchDriversResponse;
  isFetchingDrivers: boolean;
  driversProps: UseSearchDriversProps;
  setDriversProps: Dispatch<SetStateAction<UseSearchDriversProps>>;
  isFetchingSearchDrivers: boolean;
}

export interface Pagination {
  handlePageChange: (
    pagination: TablePaginationConfig,
    _: unknown,
    sorter: SorterResult<DriversProps> | SorterResult<DriversProps>[],
  ) => void;
}

export interface CheckedImportDrivers {
  totalErrorCounter: number;
  errorCounters: Record<string, number>;
  incorrectDrivers: ImportedDriver[];
}
