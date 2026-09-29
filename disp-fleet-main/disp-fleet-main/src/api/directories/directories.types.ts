import { PageSettings } from 'api/transport/transport.types';
import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';

// Models
export const ModelsItem = t.type({
  id: tt.uuid,
  title: t.string,
  brand: t.type({
    id: tt.uuid,
    title: t.string,
  }),
});
export type ModelsItem = t.TypeOf<typeof ModelsItem>;

export const ModelsSearchResponse = createPagination(ModelsItem);
export type ModelsSearchResponse = t.TypeOf<typeof ModelsSearchResponse>;

// Brands
export const BrandsItem = t.type({
  id: tt.uuid,
  title: t.string,
});
export type BrandsItem = t.TypeOf<typeof BrandsItem>;

export const BrandsSearchResponse = createPagination(BrandsItem);
export type BrandsSearchResponse = t.TypeOf<typeof BrandsSearchResponse>;

// Telematics
export const TelematicsItem = t.type({
  id: tt.uuid,
  imei: t.string,
  title: t.string,
});
export type TelematicsItem = t.TypeOf<typeof TelematicsItem>;

export const TelematicsSearchResponse = createPagination(TelematicsItem);
export type TelematicsSearchResponse = t.TypeOf<typeof TelematicsSearchResponse>;

// Using Type
export const UsingTypeItem = t.type({
  id: tt.uuid,
  title: t.string,
});
export type UsingTypeItem = t.TypeOf<typeof UsingTypeItem>;

export const UsingTypeSearchResponse = createPagination(UsingTypeItem);
export type UsingTypeSearchResponse = t.TypeOf<typeof UsingTypeSearchResponse>;

// Using Sub Type
export const UsingSubTypeItem = t.type({
  id: tt.uuid,
  title: t.string,
  type: UsingTypeItem,
});
export type UsingSubTypeItem = t.TypeOf<typeof UsingSubTypeItem>;

export const UsingSubTypeSearchResponse = createPagination(UsingSubTypeItem);
export type UsingSubTypeSearchResponse = t.TypeOf<typeof UsingSubTypeSearchResponse>;

// Using Accessible PositionId
export const UsingAccessiblePositionIdItem = t.type({
  id: tt.uuid,
  title: t.string,
});
export type UsingAccessiblePositionIdItem = t.TypeOf<typeof UsingAccessiblePositionIdItem>;

export const UsingAccessiblePositionIdSearchResponse = createPagination(UsingAccessiblePositionIdItem);
export type UsingAccessiblePositionIdSearchResponse = t.TypeOf<typeof UsingAccessiblePositionIdSearchResponse>;

// Vehicle List Search
export const VehicleQuery = t.partial({
  brand: tt.uuid,
  model: tt.uuid,
  bodyType: tt.uuid,
  manufactureYear: t.number,
  manufacturePeriod: t.string,
});
export type VehicleQuery = t.TypeOf<typeof VehicleQuery>;

export const EngineQuery = t.partial({
  engineType: tt.uuid,
  drive: tt.uuid,
  transmissionType: tt.uuid,
  fuelTankVolume: t.number,
  enginePower: t.number,
});
export type EngineQuery = t.TypeOf<typeof EngineQuery>;

export const VehicleListSearchRequest = t.intersection([
  t.type({
    vehicle: VehicleQuery,
    engine: EngineQuery,
  }),
  t.partial({
    pageSetting: PageSettings,
  }),
]);
export type VehicleListSearchRequest = t.TypeOf<typeof VehicleListSearchRequest>;

export const VehicleListContentItem = t.type({
  id: tt.uuid,
  brand: t.string,
  model: t.string,
  engineType: t.string,
  engineCapacity: t.number,
  enginePower: t.number,
  fuelType: t.string,
  fuelTankVolume: t.number,
  mudguardInstalled: t.boolean,
  spareWheelHolderInstalled: t.boolean,
  drive: t.string,
  bodyType: t.string,
  transmissionType: t.string,
  manufacturePeriod: t.string,
  weight: t.number,
  dimensions: t.string,
});
export type VehicleListContentItem = t.TypeOf<typeof VehicleListContentItem>;

export const VehicleListSearchTruncatedContent = createPagination(VehicleListContentItem);
export type VehicleListSearchTruncatedContent = t.TypeOf<typeof VehicleListSearchTruncatedContent>;

export const FilterItem = t.type({
  id: tt.uuid,
  title: t.string,
});
export type FilterItem = t.TypeOf<typeof FilterItem>;

export const VehicleFilters = t.type({
  bodyType: t.array(FilterItem),
  engineType: t.array(FilterItem),
  transmissionType: t.array(FilterItem),
  driveType: t.array(FilterItem),
  engineCapacity: t.array(t.number),
  enginePower: t.array(t.number),
  manufacturePeriod: t.array(t.string),
});
export type VehicleFilters = t.TypeOf<typeof VehicleFilters>;

export const VehicleListSearchTruncatedResponse = t.type({
  totalElements: t.number,
  totalPages: t.number,
  size: t.number,
  number: t.number,
  numberOfElements: t.number,
});
export type VehicleListSearchTruncatedResponse = t.TypeOf<typeof VehicleListSearchTruncatedResponse>;

export const VehicleListSearchResponse = t.intersection([
  VehicleListSearchTruncatedResponse,
  t.type({
    content: VehicleListSearchTruncatedContent,
    filters: VehicleFilters,
  }),
]);
export type VehicleListSearchResponse = t.TypeOf<typeof VehicleListSearchResponse>;
