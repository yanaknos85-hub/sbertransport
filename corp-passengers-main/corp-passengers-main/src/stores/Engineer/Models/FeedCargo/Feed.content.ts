import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { DeadlineState } from 'modules/Engineers/constants/Engineers.constants';

import { Passenger } from '../Feed/Feed.passenger';
import { Address } from '../Feed/Feed.addresses';

enum Source {
  WEB = 'WEB',
  HOME_CLICK = 'HOME_CLICK',
  WEB_MULTIPLE = 'WEB_MULTIPLE',
}

export const FeedCargoContent = t.intersection([
  t.type({
    transportType: t.string,
    id: tt.uuid,
    humanReadableId: t.string,
    status: t.string,
  }),
  t.partial({
    author: Passenger,
    approvalState: tt.nullable(t.string),
    approvalDate: tt.nullable(t.string),
    approvedBy: tt.nullable(Passenger),
    addresses: t.array(Address),
    creationTime: t.string,
    desiredDate: t.string,
    deadlineState: tt.nullable(ioTypeFromEnum('DeadlineState', DeadlineState)),
    deadline: tt.nullable(t.union([t.number, t.string])),
    sender: tt.nullable(t.string),
    recipient: tt.nullable(t.string),
    transferTime: tt.nullable(t.string),
    shipmentTime: tt.nullable(t.string),
    cargoTransportType: t.string,
    source: ioTypeFromEnum('Source', Source),
  }),
]);

export type FeedCargoContentType = t.TypeOf<typeof FeedCargoContent>;
