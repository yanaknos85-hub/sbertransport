import { APIQueryResult, useAPI } from 'api';
import { AxiosError } from 'axios';
import {
  DIRECTORY_ACCESSIBLE_POSITION_ID,
  DIRECTORY_BRANDS_ALL,
  DIRECTORY_MODELS_ALL,
  DIRECTORY_TELEMATICS_ALL,
  DIRECTORY_USING_SUB_TYPE_ALL,
  DIRECTORY_USING_TYPE_ALL,
  DIRECTORY_VEHICLES_SEARCH
} from './directories.constants';
import {
  BrandsSearchResponse,
  ModelsSearchResponse,
  TelematicsSearchResponse,
  UsingAccessiblePositionIdSearchResponse,
  UsingSubTypeSearchResponse,
  UsingTypeSearchResponse,
  VehicleListSearchRequest,
  VehicleListSearchResponse,
  VehicleQuery
} from './directories.types';

declare module 'api' {
  interface Cache {
    modelsDirectory: { key: ['modelsDirectory', string]; value: ModelsSearchResponse };
    brandsDirectory: { key: ['brandsDirectory', string]; value: BrandsSearchResponse };
    telematicsDirectory: { key: ['telematicsDirectory', string]; value: TelematicsSearchResponse };
    usingTypeDirectory: { key: ['usingTypeDirectory']; value: UsingTypeSearchResponse };
    usingSubTypeDirectory: { key: ['usingSubTypeDirectory']; value: UsingSubTypeSearchResponse };
    usingAccessiblePositionIdDirectory: { key: ['usingAccessiblePositionIdDirectory']; value: UsingAccessiblePositionIdSearchResponse };
    vehicleDirectorySearch: { key: ['vehicleDirectorySearch', typeof VehicleListSearchRequest]; value: VehicleListSearchResponse };
  }
}

const queryConfig = { refetchOnMount: 'always' } as const;

export const useModelsDirectory = (
  searchValue: string,
  pageSetting = { page: 0, size: 1000 }
): APIQueryResult<ModelsSearchResponse | undefined, AxiosError | unknown> => useAPI(
  ['modelsDirectory', searchValue],
  ({ http, process }) => http
    .post<ModelsSearchResponse>(DIRECTORY_MODELS_ALL, {
      pageSetting,
      search: { title: searchValue },
    })
    .then(process.decodeResponseData(ModelsSearchResponse)),
  { suspense: false, ...queryConfig }
);

export const useBrandsDirectory = (
  searchValue: string,
  pageSetting = { page: 0, size: 20 }
): APIQueryResult<BrandsSearchResponse | undefined, AxiosError | unknown> => useAPI(
  ['brandsDirectory', searchValue],
  ({ http, process }) => http
    .post<BrandsSearchResponse>(DIRECTORY_BRANDS_ALL, {
      pageSetting,
      search: { title: searchValue },
    })
    .then(process.decodeResponseData(BrandsSearchResponse)),
  { suspense: false, ...queryConfig }
);

export const useTelematicsDirectory = (
  searchValue: string,
  pageSetting = { page: 0, size: 20 }
): APIQueryResult<TelematicsSearchResponse | undefined, AxiosError | unknown> => useAPI(
  ['telematicsDirectory', searchValue],
  ({ http, process }) => http
    .post<TelematicsSearchResponse>(DIRECTORY_TELEMATICS_ALL, {
      pageSetting,
      search: { title: searchValue },
    })
    .then(process.decodeResponseData(TelematicsSearchResponse)),
  { suspense: false, ...queryConfig }
);

export const useUsingTypeDirectory = (): APIQueryResult<
  UsingTypeSearchResponse | undefined, AxiosError | unknown
> => useAPI(
  ['usingTypeDirectory'],
  ({ http, process }) => http
    .post<UsingTypeSearchResponse>(DIRECTORY_USING_TYPE_ALL, {})
    .then(process.decodeResponseData(UsingTypeSearchResponse)),
  { suspense: false, ...queryConfig }
);

export const useUsingSubTypeDirectory = (
): APIQueryResult<UsingSubTypeSearchResponse | undefined, AxiosError | unknown> => useAPI(
  ['usingSubTypeDirectory'],
  ({ http, process }) => http
    .post<UsingSubTypeSearchResponse>(DIRECTORY_USING_SUB_TYPE_ALL, {})
    .then(process.decodeResponseData(UsingSubTypeSearchResponse)),
  { suspense: false, ...queryConfig }
);

export const useUsingAccessiblePositionIdDirectory = (
): APIQueryResult<UsingAccessiblePositionIdSearchResponse | undefined, AxiosError | unknown> => useAPI(
  ['usingAccessiblePositionIdDirectory'],
  ({ http, process }) => http
    .get<UsingAccessiblePositionIdSearchResponse>(DIRECTORY_ACCESSIBLE_POSITION_ID, {})
    .then(process.decodeResponseData(UsingAccessiblePositionIdSearchResponse)),
  { suspense: false, ...queryConfig }
);

export const useVehicleDirectorySearch = (
  query: typeof VehicleListSearchRequest
): APIQueryResult<VehicleListSearchResponse | undefined, AxiosError | unknown> => useAPI(
  ['vehicleDirectorySearch', query],
  ({ http, process }) => http
    .post<VehicleListSearchResponse>(DIRECTORY_VEHICLES_SEARCH, query)
    .then(process.decodeResponseData(VehicleListSearchResponse)),
  {
    suspense: false,
    enabled: (
      'vehicle' in query
      && !!(query.vehicle as VehicleQuery).brand
      && !!(query.vehicle as VehicleQuery).model
    ),
    refetchOnMount: false,
  }
);
