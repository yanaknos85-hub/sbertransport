// import type { UUID } from 'utils/io-ts';

// export interface Sort {
//   sorted: boolean;
//   unsorted: boolean;
//   empty: boolean;
// }

// export interface Pageable {
//   offset: number;
//   sort: Sort;
//   pageNumber: number;
//   pageSize: number;
//   paged: boolean;
//   unpaged: boolean;
// }

// export interface Transport {
//   brand: string;
//   model: string;
//   stateNumber: string;
// }

// export interface ListItem {
//   id: UUID;
//   humanReadableId: string;
//   organizationName: string;
//   startDate: string;
//   finishDate: string;
//   status: WaybillStatus;
//   medicSuccess: boolean;
//   telemechSuccess: boolean;
//   organizationId: UUID;
//   transport: Transport;
//   driverFullName: string;
// }

// export interface SearchResponse {
//   totalElements: number;
//   totalPages: number;
//   size: number;
//   number: number;
//   sort: Sort;
//   first: boolean;
//   pageable: Pageable;
//   numberOfElements: number;
//   last: boolean;
//   empty: boolean;
//   content: ListItem[];
// }

// export interface Filter {
//   searchText?: string;
//   humanReadableId?: string;
//   stateNumber?: string;
//   organizationId?: string;
//   requestStatusSet?: string[];
//   creationTime?: {
//     start?: number;
//     end?: number;
//   };
//   finishTime?: {
//     start?: number;
//     end?: number;
//   };
// }

// export interface Pagination {
//   sortSetting?: SortSetting<string>;
//   pageSetting?: PageSetting;
// }

// export interface SearchQuery extends Filter, Pagination {}

// interface Driver {
//   firstName: string;
//   lastName: string;
//   patronymic: string;
//   personnelNumber: string;
//   mobilePhone: string;
// }

// interface DrivingLicense {
//   series: string;
//   number: number;
//   issueDate: string;
// }

// interface Attorney {
//   attorneyNumber: UUID;
//   issueDate: string;
//   creationSystem: string;
// }

// interface Author {
//   firstName: string;
//   lastName: string;
//   patronymic: string;
//   mobilePhone: string;
//   attorney: Attorney;
// }

// interface Medic {
//   firstName: string;
//   lastName: string;
//   patronymic: string;
//   position: string;
//   medicSuccess: boolean;
//   decisionTime: string;
// }

// interface Telemechanic {
//   firstName: string;
//   lastName: string;
//   patronymic: string;
//   position: string;
//   telemechSuccess: boolean;
//   decisionTime: string;
//   mileage: number;
// }

// export interface Waybill {
//   id: UUID;
//   humanReadableId: string;
//   ewbUuid: UUID;
//   creationDate: string;
//   startDate: string;
//   finishDate: string;
//   regionCode: string;
//   msrn: string;
//   tin: string;
//   organizationName: string;
//   status: WaybillStatus;
//   transport: Transport & {
//     transportType: string;
//     odometerOut: number | null;
//     fuelLitreageOut: number | null;
//     fuelTankVolume: number;
//   };
//   driver: Driver;
//   drivingLicense: DrivingLicense;
//   author: Author;
//   medic: Medic | null;
//   telemechOut: Telemechanic | null;
//   telemechIn: Telemechanic | null;
// }

export interface CreationFormValues {
  ewbUuid: string;
  startDate: string;
  finishDate: string;
  organizationName: string;
  organizationRegionCode: string;
  organizationMsrn: string;
  organizationTin: string;
  ownerPhoneNumber: string;
  transportId: string;
  stateNumber: string;
  brand: string;
  model: string;
  transportType: string;
  driverLicenseId: string;
  driverId: string;
  driverFullName: string;
  driverPersonnelNumber: string;
  driverTin: number | null;
  driverOrganizationId: string;
  driverOrganizationName: string;
  driverDepartmentName: string;
  driverDepartmentId: string;
  drivingLicenseSeries: string;
  drivingLicenseNumber: string;
  drivingLicenseIssueDate: string;
}

export interface CreationPayload {
  humanReadableId: string;
  ewbDate: string;
  ewbId: string;
  startDate: string;
  finishDate: string;
  transportationType: string;
  communicationType: string;
  organizationName: string;
  ogrn: string;
  tin: string;
  organization: {
    organizationName: string;
    subjectCode: string;
    ogrn: string;
    tin: string;
  };
  transport: {
    stateNumber: string;
    brand: string;
    model: string;
    type: string;
  };
  driver: {
    fullName: string;
    personnelNumber: string;
    number: string;
    series: string;
    issueDate: string;
  };
}

// export interface IWaybillService {
//   searchAllWaybills: (query: SearchQuery) => Promise<SearchResponse | void>;
//   searchSelfWaybills: (query: SearchQuery) => Promise<SearchResponse | void>;
//   searchWaybills: (query: SearchQuery) => Promise<SearchResponse | void>;
//   getWaybill: (id: UUID) => Promise<Waybill | void>;
// }

// export interface IWaybillStore {
//   filter: Filter;
//   pagination: Pagination;
//   filterCounter: number;
//   waybillsListConfig: SearchResponse | null;
//   waybill: Waybill | null;
//   userRoles: string[];

//   searchWaybills(): void;
//   getWaybill(id: UUID): void;
//   clearWaybill(): void;
//   setPagination(pagination: Pagination): void;
//   setFilter(filter: Filter): void;
//   resetFiltersWithApply(): void;
//   resetPageSetting(): void;
// }
