import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import { createPagination, PaginationParams } from 'utils/io-ts/pagination';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { WaybillStatus } from './waybill.constants';

const WaybillStatuses = ioTypeFromEnum<WaybillStatus>('WaybillStatus', WaybillStatus);

const UuidResponse = t.type({
  uuid: tt.uuid,
});
export type TUuidResponse = t.TypeOf<typeof UuidResponse>;

export const Driver = t.intersection([
  t.type({
    firstName: t.string,
    lastName: t.string,
    personnelNumber: t.string,
    mobilePhone: t.string,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);
export type Driver = t.TypeOf<typeof Driver>;

export const DrivingLicense = t.type({
  number: t.number,
  series: t.string,
  issueDate: t.string,
});
export type DrivingLicense = t.TypeOf<typeof DrivingLicense>;

export const CreateFirstTitleRequest = t.type({
  ewbUuid: tt.uuid,
  startDate: t.string,
  finishDate: t.string,
  transportationType: t.string,
  communicationType: t.string,
  tariffDepartmentId: tt.uuid,
  transportId: tt.uuid,
  driverId: tt.uuid,
});
export type TCreateFirstTitleRequest = t.TypeOf<typeof CreateFirstTitleRequest>;

export const CreateFirstTitleResponse = t.type({
  humanReadableId: t.string,
  fileName: t.string,
  content: t.string,
  creationTime: t.string,
});
export type TCreateFirstTitleResponse = t.TypeOf<typeof CreateFirstTitleResponse>;

const TitleSendRequest = t.intersection([
  CreateFirstTitleResponse,
  t.type({
    firstTitleForm: CreateFirstTitleRequest,
    ewbUuid: tt.uuid,
    titleType: t.string,
    signature: t.string,
  }),
]);
export type TTitleSendRequest = t.TypeOf<typeof TitleSendRequest>;

const CloseRequest = t.type({
  id: tt.uuid,
  value: t.number,
  fuelLitreage: t.number,
});
export type TCloseRequest = t.TypeOf<typeof CloseRequest>;

export const Period = t.type({
  start: t.number,
  end: t.number,
});
export type TPeriod = t.TypeOf<typeof Period>;

export interface WaybillFilters extends PaginationParams {
  searchText?: string;
  stateNumber?: string;
  requestStatusSet?: WaybillStatus[];
  creationTime?: TPeriod;
  finishTime?: TPeriod;
  contractorIds?: tt.UUID[];
  autoparkIds?: tt.UUID[];
}

export const Transport = t.type({
  brand: t.string,
  model: t.string,
  stateNumber: t.string,
});
export type Transport = t.TypeOf<typeof Transport>;

export const WaybillSearchItem = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  organizationName: t.string,
  startDate: t.string,
  finishDate: t.string,
  status: WaybillStatuses,
  medicSuccess: t.boolean,
  telemechSuccess: t.boolean,
  transport: Transport,
  driverFullName: t.string,
});
export type WaybillSearchItem = t.TypeOf<typeof WaybillSearchItem>;

const WaybillSearchResponse = createPagination(WaybillSearchItem);
export type TWaybillSearchResponse = t.TypeOf<typeof WaybillSearchResponse>;

export const Attorney = t.type({
  attorneyNumber: tt.uuid,
  issueDate: t.string,
  creationSystem: t.string,
});
export type Attorney = t.TypeOf<typeof Attorney>;

export const Author = t.intersection([
  t.type({
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
    mobilePhone: t.string,
  }),
  t.type({
    attorney: Attorney,
  }),
]);
export type Author = t.TypeOf<typeof Author>;

export const Medic = t.type({
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  position: t.string,
  medicSuccess: t.boolean,
  decisionTime: t.string,
});
export type Medic = t.TypeOf<typeof Medic>;

export const Telemechanic = t.type({
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  position: t.string,
  telemechSuccess: t.boolean,
  decisionTime: t.string,
  mileage: t.number,
});
export type Telemechanic = t.TypeOf<typeof Telemechanic>;

export const Waybill = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: t.string,
    ewbUuid: tt.uuid,
    creationDate: t.string,
    startDate: t.string,
    finishDate: t.string,
    regionCode: t.string,
    msrn: t.string,
    tin: t.string,
    organizationName: t.string,
    status: WaybillStatuses,
  }),
  t.type({
    transport: t.intersection([
      Transport,
      t.type({
        transportType: t.string,
        odometerOut: t.union([t.number, t.null]),
        fuelLitreageOut: t.union([t.number, t.null]),
        fuelTankVolume: t.number,
      }),
    ]),
    driver: Driver,
    drivingLicense: DrivingLicense,
    author: Author,
    medic: t.union([Medic, t.null]),
    telemechOut: t.union([Telemechanic, t.null]),
    telemechIn: t.union([Telemechanic, t.null]),
  }),
]);
export type Waybill = t.TypeOf<typeof Waybill>;
