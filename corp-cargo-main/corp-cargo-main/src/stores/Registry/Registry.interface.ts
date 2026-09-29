import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import {
  DetailedPublicCompensation,
  Expected,
  Purpose
} from '../PublicRegistry/models/PublicRegistry.interface';
import {
  Passenger, RequestRating, TaxiClass, TaxiOptions
} from '../Trip/Trip.interface';
import { TransportTypes } from '../TransportTypes/TransportTypes.interface';
import { InfoContractor } from 'api/reports';

export const PersonalCarDetailed = t.partial({
  id: tt.nullable(t.string),
  engineVolume: tt.nullable(t.number),
  ownerInfo: tt.nullable(t.string),
  registrationNumber: tt.nullable(t.string),
  brandName: tt.nullable(t.string),
  model: tt.nullable(t.string),
  insuranceNumber: tt.nullable(t.string),
  registrationCertificate: tt.nullable(t.string),
});

export const DriverInfo = t.partial({
  vehicleInfo: tt.nullable(t.string),
  registrationNumber: tt.nullable(t.string),
  driverName: tt.nullable(t.string),
});

export type PersonalCarDetailed = t.TypeOf<typeof PersonalCarDetailed>;
export type DriverInfo = t.TypeOf<typeof DriverInfo>;

export const TripResponse = t.intersection([
  t.type({
    id: tt.uuid,
    expected: Expected,
    transportType: ioTypeFromEnum<TransportTypes>('TransportTypes', TransportTypes),
    passenger: Passenger,
  }),
  t.partial({
    author: Passenger,
    driverInfo: DriverInfo,
    passengers: t.array(Passenger),
    taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass),
    passengerCount: t.number,
    tariffId: tt.uuid,
    deadline: t.number,
    desiredDate: t.number,
    creationDate: t.number,
    tripConfirmationDate: t.number,
    purpose: Purpose,
    commentForDriver: t.string,
    humanReadableId: t.string,
    magentaOrderId: t.number,
    sharedRideId: t.string,
    passenger: Passenger,
    approvedBy: Passenger,
    approvalState: t.string,
    approvalDate: t.number,
    orderPaymentFormationStartDate: t.number,
    status: t.string,
    creationTime: t.number,
    occupiedPlacesCount: t.number,
    requestOptions: t.array(ioTypeFromEnum<TaxiOptions>('TaxiOptions', TaxiOptions)),
    requestRating: tt.nullable(RequestRating),
    coopTrip: t.boolean,
    contractor: InfoContractor,
    compensationType: t.string,
    personalCar: PersonalCarDetailed,
    tariffName: t.string,
    transportCompensation: t.array(DetailedPublicCompensation),
  }),
]);

export type TripResponse = t.TypeOf<typeof TripResponse>;
