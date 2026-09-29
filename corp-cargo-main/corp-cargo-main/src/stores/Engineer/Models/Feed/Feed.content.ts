import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { DeadlineState } from 'modules/Engineers/constants/Engineers.constants';
import { Passenger } from './Feed.passenger';
import { Contractor } from './Feed.contractor';
import { Address } from './Feed.addresses';
import { Expected } from './Feed.expected';
import { Fact } from './Feed.fact';
import { Driver } from './Feed.driver';
import { Vehicle } from './Feed.vehicle';
import { Department } from './Feed.department';
import { Limit } from './Feed.limit';
import { Purpose, RequestRating } from '../../../Trip/Trip.interface';

export const FeedContent = t.intersection([
  t.type({
    transportType: t.string,
    id: tt.uuid,
    humanReadableId: t.string,
    status: t.string,
    passenger: Passenger,
    expected: Expected,
    tariffId: tt.nullable(tt.uuid),
    driver: tt.nullable(Driver),
  }),
  t.partial({
    author: Passenger,
    passengers: tt.nullable(t.array(Passenger)),
    coopTrip: t.boolean,
    purpose: Purpose,
    magentaOrderId: tt.nullable(t.number),
    sharedRideId: t.string,
    approvalState: tt.nullable(t.string),
    approvalDate: tt.nullable(t.string),
    tripConfirmationDate: tt.nullable(t.string),
    requestRating: tt.nullable(RequestRating),
    requestOptions: tt.nullable(t.array(t.string)),
    resolution: tt.nullable(t.string),
    occupiedPlacesCount: tt.nullable(t.number),
    passengerCount: tt.nullable(t.number),
    tariffHumanReadableId: tt.nullable(t.string),
    fact: tt.nullable(Fact),
    approvedBy: tt.nullable(Passenger),
    vehicle: tt.nullable(Vehicle),
    department: tt.nullable(Department),
    dispatcherInfo: tt.nullable(t.string),
    limit: tt.nullable(Limit),
    departureAddress: t.string,
    destinationAddress: t.string,
    addresses: t.array(Address),
    distance: t.number,
    cost: t.number,
    intermediateAddresses: t.array(t.string),
    commentForDriver: tt.nullable(t.string),
    creationTime: t.string,
    desiredDate: t.string,
    deadlineState: tt.nullable(ioTypeFromEnum('DeadlineState', DeadlineState)),
    deadline: tt.nullable(t.union([t.number, t.string])),
    waitTime: t.number,
    taxiClass: tt.nullable(t.string),
    contractor: tt.nullable(Contractor),
    dateTimeRegistered: tt.nullable(t.string),
    factSearchTime: tt.nullable(t.number),
    tripStartTime: tt.nullable(t.string),
    tripFinishTime: tt.nullable(t.string),
  }),
]);

export type FeedContent = t.TypeOf<typeof FeedContent>;
