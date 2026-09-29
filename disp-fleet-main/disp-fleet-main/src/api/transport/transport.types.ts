import { TypeAtKey } from 'api';
import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';

export const PageSettings = t.type({
  page: t.number,
  size: t.number,
});

export enum TransportStatus {
  IN_USE = 'IN_USE',
  NOT_IN_USE = 'NOT_IN_USE',
}

export const StatusesNames: Record<TransportStatus, string> = {
  [TransportStatus.IN_USE]: 'В эксплуатации',
  [TransportStatus.NOT_IN_USE]: 'Выведен из эксплуатации',
};

export const TransportSearchRequest = t.intersection([
  t.type({
    page: PageSettings,
  }),
  t.partial({
    searchText: t.string,
    contractorIds: t.array(tt.uuid),
    autoparkId: tt.uuid,
    status: t.array(t.string),
    brand: t.string,
    model: t.string,
    year: t.number,
  }),
]);

export type TransportSearchRequest = t.TypeOf<typeof TransportSearchRequest>;

export const TransportShort = t.intersection([
  t.type({
    id: tt.uuid,
    stateNumber: t.string,
    status: t.keyof(TransportStatus),
    vinCode: t.string,
  }),
  t.partial({
    contractorId: t.string,
    autoparkId: t.string,
  }),
]);

export type TransportShort = t.TypeOf<typeof TransportShort>;

export const TransportSearchResponse = createPagination(TransportShort);

export type TransportSearchResponse = t.TypeOf<typeof TransportSearchResponse>;

export interface CacheSearchTransport {
  response: TransportSearchResponse;
  byId: Record<string, TransportShort>;
}

export type CacheSearchedTransport = TypeAtKey<['transportSearch', TransportSearchRequest]>;

const Documents = t.type({
  certificateNumber: t.string,
  certificateIssuedDate: t.number,
  vehicleType: t.string,
});

const DocumentsFull = t.intersection([
  Documents,
  t.type({
    passportNumber: t.string,
    passportIssuedDate: t.number,
    brandByPassport: t.string,
    modelByPassport: t.string,
  }),
]);

const Location = t.type({
  exploitationStart: t.number,
  locationAddress: t.string,
  parkingAddress: t.string,
});

const Vehicle = t.type({
  id: tt.uuid,
  vinCode: t.string,
  assetNumber: t.string,
  inventoryNumber: tt.nullable(t.string),
  bodyNumber: tt.nullable(t.string),
  chassisNumber: tt.nullable(t.string),
});

const Engine = t.type({
  engineType: t.string,
  engineCapacity: t.number,
  enginePower: t.number,
  fuelType: t.string,
  cityConsumptionRate: t.number,
  countryConsumptionRate: t.number,
  hybridConsumptionRate: t.number,
});

export const GeneralFields = t.type({
  height: t.number,
  width: t.number,
  length: t.number,
  weight: t.number,
  maxWeight: t.number,
  fuelTankVolume: t.number,
});

export const ServiceFields = t.type({
  serviceIntervalDays: t.number,
  serviceIntervalMileage: t.number,
  serviceAuthorizationDays: t.number,
  serviceAuthorizationMileage: t.number,
});

const General = t.intersection([
  GeneralFields,
  t.type({
    bodyColor: t.string,
    category: t.string,
    categoryName: t.string,
    spareWheelHolderInstalled: t.boolean,
    mudguardInstalled: t.boolean,
    telematics: tt.nullable(t.string),
    telematicsId: tt.nullable(tt.uuid),
    frontWheelSize: t.string,
    rearWheelSize: t.string,
  }),
]);

export const Transport = t.type({
  id: tt.uuid,
  stateNumber: t.string,
  brand: t.string,
  model: t.string,
  year: t.string,
  currentMileage: t.number,
  vehicle: t.intersection([
    Vehicle,
    t.type({
      status: t.string,
      manufacturer: t.string,
      type: t.string,
      subtype: t.string,
      drive: t.string,
      ecologicalClass: t.string,
      bodyType: t.string,
      transmissionType: t.string,
      manufacturePeriod: t.string,
    }),
  ]),
  location: t.intersection([
    Location,
    t.type({
      exploitationEnd: t.number,
    }),
  ]),
  contractorId: tt.uuid,
  autoparkId: tt.uuid,
  comment: t.string,
  accessiblePositionId: t.string,
  documents: DocumentsFull,
  engine: Engine,
  general: General,
  service: ServiceFields,
  balanceUnitNumber: t.string,
  facility: t.string,
  equipmentUnitSystemNumber: t.string,
});
export type Transport = t.TypeOf<typeof Transport>;

export const CreateTransportRequest = t.type({
  contractorId: tt.uuid,
  autoparkId: tt.uuid,
  location: Location,
  documents: DocumentsFull,
  comment: t.string,
  accessiblePositionId: t.string,
  balanceUnitNumber: t.string,
  facility: t.string,
  equipmentUnitSystemNumber: t.string,
  vehicle: t.intersection([
    Vehicle,
    t.type({
      stateNumber: t.string,
      year: t.number,
      currentMileage: t.number,
      subtypeId: tt.uuid,
      bodyColor: t.string,
      telematicsId: tt.nullable(tt.uuid),
    }),
  ]),
});

export type CreateTransportRequest = t.TypeOf<typeof CreateTransportRequest>;

export const DeleteRequest = t.type({
  exploitationEnd: t.number,
});
export type DeleteRequest = t.TypeOf<typeof DeleteRequest>;

export const EditTransportRequest = t.intersection([
  t.type({
    contractorId: tt.uuid,
    autoparkId: tt.uuid,
    location: Location,
    comment: t.string,
    documents: Documents,
    vehicle: t.intersection([
      Vehicle,
      t.type({
        stateNumber: t.string,
        telematicsId: tt.nullable(tt.uuid),
        currentMileage: t.number,
        subtypeId: t.string,
      }),
    ]),
    balanceUnitNumber: t.string,
    facility: t.string,
    equipmentUnitSystemNumber: t.string,
  }),
  t.partial({
    accessiblePositionId: t.string,
  }),
]);
export type EditTransportRequest = t.TypeOf<typeof EditTransportRequest>;
