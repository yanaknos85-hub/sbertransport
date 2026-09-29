import * as t from 'io-ts';

export const SchedulerTableContent = t.intersection([
  t.type({
    humanReadableId: t.string,
    status: t.string,
    tariffType: t.string,
    nextRequestDate: t.string,
    lastRequestDate:t.string,
    author: t.string,
    authorMobilePhone: t.string,
    carrier: t.string,
    senderAddress: t.string,
    sender: t.string,
    senderPhone: t.string,
    senderOrganization: t.string,
    recipientAddress: t.string,
    recipient: t.string,
    recipientPhone: t.string,
    recipientOrganization: t.string,
    plannedRange: t.number,
    plannedPrice: t.number,
    cargoType: t.string,
    loaders: t.number,
    weight: t.number,
    volume: t.number,
    creationTime: t.string,
    comment: t.string,
  }),
  t.partial({
    period: t.string,
    countRequests: t.number,
    countRequestsInRoute: t.number,
  }),
]);

export type SchedulerTableContentType = t.TypeOf<typeof SchedulerTableContent>;
