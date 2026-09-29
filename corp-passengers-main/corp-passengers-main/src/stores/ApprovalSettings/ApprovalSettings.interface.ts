import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';

export const PurposeItemQuery = t.intersection([
  t.type({
    minCostToBeApproved: tt.money,
  }),
  // Косяки бэка
  t.partial({
    regionId: tt.uuid,
    purposeId: tt.uuid,
  }),
  // ONLY FOR INNER USAGE
  t.partial({
    rowId: tt.uuid,
    id: tt.uuid,
    territoryId: tt.uuid,
  }),
]);

export const ApprovalSettingsQuery = t.partial({
  organizationId: tt.uuid,
  approvalActive: t.boolean,
  minCostToBeApproved: tt.money,
  transportType: ioTypeFromEnum<TransportTypes>('transportType', TransportTypes),
  purposeAndRegionItems: t.array(PurposeItemQuery),
  approvalDocumentCheck: t.boolean,
  affirmativeActive: t.boolean,
  tripConfirmationActive: t.boolean,
  tripConfirmationDocumentCheck: t.boolean,
  tripApprovalActive: t.boolean,
});
export const PurposeItem = t.intersection([
  t.type({
    tripPurpose: t.type({ id: tt.uuid, label: t.string }),
    minCostToBeApproved: tt.money,
  }),
  // ONLY FOR INNER USAGE
  t.partial({
    region: GeoZones,
    rowId: tt.uuid,
    territoryId: tt.uuid,
  }),
]);

export const ApprovalSettings = t.partial({
  organizationId: tt.uuid,
  approvalActive: t.boolean,
  minCostToBeApproved: tt.money,
  transportType: ioTypeFromEnum<TransportTypes>('transportType', TransportTypes),
  purposeAndRegionItems: t.array(PurposeItem),
  id: tt.uuid,
  approvalDocumentCheck: t.boolean,
  affirmativeActive: t.boolean,
  tripConfirmationActive: t.boolean,
  tripConfirmationDocumentCheck: t.boolean,
  tripApprovalActive: t.boolean,
});

export type PurposeItemQuery = t.TypeOf<typeof PurposeItemQuery>;
export type ApprovalSettingsQuery = t.TypeOf<typeof ApprovalSettingsQuery>;

export type PurposeItem = t.TypeOf<typeof PurposeItem>;
export type ApprovalSettings = t.TypeOf<typeof ApprovalSettings>;
