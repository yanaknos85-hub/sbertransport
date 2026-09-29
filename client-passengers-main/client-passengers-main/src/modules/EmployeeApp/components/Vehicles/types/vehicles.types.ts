import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { Ownership } from '../constants/vehicles.constants';
import { UsedFileFormatEnum } from 'stores/Trip/Trip.interface';

const IOFile = t.partial({
  fileName: t.string,
  fileSize: t.number,
  fileFormat: ioTypeFromEnum<UsedFileFormatEnum>('UsedFileFormatEnum', UsedFileFormatEnum),
  id: t.number,
});

export type File = t.TypeOf<typeof IOFile>;

export const IODriverLic = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    number: t.string,
    seria: t.string,
    issue: t.string,
    placeIssue: t.string,
    categoria: t.string,
    issueDateDocument: t.string,
    finalTimeDocument: t.string,
  }),
  IOFile,
]);

export const IOOSago = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    carId: tt.uuid,
    seria: t.string,
    number: t.string,
    startTimeDocument: t.string,
    finalTimeDocument: t.string,
  }),
  IOFile,
]);

export const IOPassportTS = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    carId: tt.uuid,
    vin: t.string,
    engineVolume: t.number,
    enginePower: t.string,
  }),
  IOFile,
]);

export const IOMarriageCert = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    number: t.string,
    seria: t.string,
    issueDateDocument: t.string,
  }),
  IOFile,
]);

export const IOAgreementPdn = t.intersection([
  t.type({
    id: tt.uuid,
    carId: tt.uuid,
    employeeId: tt.uuid,
  }),
  IOFile,
]);

export const IODocuments = t.partial({
  driverLic: IODriverLic,
  osago: IOOSago,
  passportTs: IOPassportTS,
  marriageCertificate: IOMarriageCert,
  agreementPdn: IOAgreementPdn,
});

export const IOVehicle = t.intersection([
  t.type({
    id: t.string,
    color: t.string,
    transportType: t.string,
    brandName: t.string,
    model: t.string,
    registrationNumber: t.string,
    engineVolume: t.number,
    insuranceNumber: t.string,
    passengerSeatsCount: t.number,
    ownerInfo: ioTypeFromEnum<Ownership>('Ownership', Ownership),
    persDataAccept: t.boolean,
    documents: IODocuments,
  }),
  t.partial({
    employeeId: t.string,
    registrationCertificate: t.string,
  }),
]);

export type Vehicle = t.TypeOf<typeof IOVehicle>;
