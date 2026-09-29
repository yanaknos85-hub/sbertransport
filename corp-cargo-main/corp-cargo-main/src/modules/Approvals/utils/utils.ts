import { ApprovalSettings, ApprovalSettingsQuery } from 'stores/ApprovalSettings/ApprovalSettings.interface';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';
import { UUID } from 'utils/io-ts';
import uniqId from 'utils/uid';

import { Employee } from 'stores/Employee/Employee.interface';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';

import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { ApprovalsRecord, FormValues } from '../types/types';
import { ApprovalsNamesSubstring } from '../hooks/useColumns';

function getEmptyData(rowId: UUID, transportType: TransportTypes | undefined): ApprovalsRecord {
  const cellId = uniqId() as UUID;

  return {
    transportType,
    rowId,
    tripPurpose: { rowId },
    territory: { rowId, territory: 'any' },
    specificMinimalSum: [{ rowId, id: cellId }],
    specificTerritory: [{ rowId, id: cellId }],
  };
}

function mapSettingsToRecords(geoZones: GeoZones[], query: ApprovalSettings): ApprovalsRecord[] {
  const records: ApprovalsRecord[] = [];

  if (!query) {
    return records;
  }

  if (!query.purposeAndRegionItems || !query.purposeAndRegionItems.length) {
    const rowId = uniqId() as UUID;

    records.push({
      transportType: query.transportType,
      id: query.id,
      approvalActive: query.approvalActive,
      affirmativeActive: query.affirmativeActive,
      approvalDocumentCheck: query.approvalDocumentCheck,
      tripConfirmationActive: query.tripConfirmationActive,
      tripConfirmationDocumentCheck: query.tripConfirmationDocumentCheck,
      tripApprovalActive: query.tripApprovalActive,
      minimalSum: query.minCostToBeApproved,
      ...getEmptyData(rowId, query.transportType),
    });
  }

  if (query.purposeAndRegionItems && query.purposeAndRegionItems.length) {
    query.purposeAndRegionItems.forEach(p => {
      const geoZone = geoZones?.find(g => g.id === p.region?.id);

      const rowId = uniqId() as UUID;
      const territoryId = uniqId() as UUID;

      const tripPurpose: ApprovalsRecord['tripPurpose'] = { rowId, purposeId: p.tripPurpose.id };
      const specificMinimalSum: ApprovalsRecord['specificMinimalSum'] = [];
      const specificTerritory: ApprovalsRecord['specificTerritory'] = [];

      specificMinimalSum.push({
        rowId, id: territoryId, minimalSum: p.minCostToBeApproved,
      });
      specificTerritory.push({
        rowId, id: territoryId, territory: geoZone?.id ?? '',
      });

      records.push({
        transportType: query.transportType,
        id: query.id,
        rowId,
        specificMinimalSum,
        specificTerritory,
        tripPurpose,
        territory: { rowId, territory: p.region ? 'specific' : 'any' },
        minimalSum: query.minCostToBeApproved,
        approvalActive: query.approvalActive,
        affirmativeActive: query.affirmativeActive,
        approvalDocumentCheck: query.approvalDocumentCheck,
        tripConfirmationActive: query.tripConfirmationActive,
        tripConfirmationDocumentCheck: query.tripConfirmationDocumentCheck,
        tripApprovalActive: query.tripApprovalActive,
      });
    });
  }
  return records;
}

const mapFromValuesToApprovalsQuery = (
  values: FormValues,
  profile: Employee,
  purposes: Record<string, TripPurpose>,
  geoZones: GeoZones[]
): ApprovalSettingsQuery => {
  const prepared: ApprovalSettingsQuery = {};

  const keys = Object.keys(values);

  prepared.organizationId = profile.organizationId;
  prepared.purposeAndRegionItems = [];

  const territoryTypes = {} as Record<string, string>;
  keys.forEach(key => {
    const vString = key.toString();
    const isTerritory = vString.includes(ApprovalsNamesSubstring.territory);

    if (isTerritory) {
      const rowId = key.toString().split('_')[1] as UUID;
      territoryTypes[rowId] = values[key];
    }
  });

  // сложный разбор развесистого дерева data из form, возможно легче использовать state из родителя
  // напомню что мы работаем с целой таблицей, а не со строкой как раньше
  keys.forEach((k: keyof FormValues) => {
    const vString = k.toString();
    const isTripPurpose = vString.includes(ApprovalsNamesSubstring.tripPurpose);
    const isMinSum = vString.includes(ApprovalsNamesSubstring.minimalSum);
    const isSpecificTerritory = vString.includes(ApprovalsNamesSubstring.specificTerritory);

    if (k === 'type') {
      prepared.transportType = values[k];
      return;
    }

    if (k === 'isRequired') {
      prepared.approvalActive = values[k];
      return;
    }

    if (k === 'minimalSum') {
      prepared.minCostToBeApproved = values[k];
      return;
    }

    if (k === 'approvalDocumentCheck') {
      prepared.approvalDocumentCheck = values[k];
      return;
    }

    if (k === 'affirmativeActive') {
      prepared.affirmativeActive = values[k];
      return;
    }

    if (k === 'tripConfirmationActive') {
      prepared.tripConfirmationActive = values[k];
      return;
    }

    if (k === 'tripConfirmationDocumentCheck') {
      prepared.tripConfirmationDocumentCheck = values[k];
      return;
    }

    if (k === 'tripApprovalActive') {
      prepared.tripApprovalActive = values[k];
      return;
    }

    if (isTripPurpose) {
      const rowId = k.toString().split('_')[1] as UUID;
      const items = prepared.purposeAndRegionItems;
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const purpose = purposes[values[k as any]];

      // массив prepared не пуст, такой rowId уже есть
      if (items && items.length && items.find(p => p.rowId === rowId)) {
        // eslint-disable-next-line @stylistic/max-len
        prepared.purposeAndRegionItems = prepared.purposeAndRegionItems?.map(p => p.rowId === rowId ? { ...p, purposeId: purpose?.id } : p
        );
        return;
      }

      // массив prepared пуст ИЛИ массив prepared не пуст, но такого rowId нет
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      prepared.purposeAndRegionItems?.push({ purposeId: purpose?.id, rowId } as any);
    }

    if (isSpecificTerritory) {
      const rowId = k.toString().split('_')[1] as UUID;
      const items = prepared.purposeAndRegionItems;
      const isAnyTerritory = territoryTypes[rowId] === 'any';

      const geoIds = isAnyTerritory ? undefined : Object.keys(values[k]);

      if (geoIds && geoIds.length) {
        geoIds.forEach(geoId => {
          const geo = geoZones.find(g => g.id === values[k][geoId]) as GeoZones;

          // массив prepared не пуст, такой rowId уже есть
          if (items && items.length && items.find(p => p.rowId === rowId)) {
            // eslint-disable-next-line @stylistic/max-len
            prepared.purposeAndRegionItems = prepared.purposeAndRegionItems?.map(p => p.rowId === rowId ? { ...p, regionId: geo?.id } : p
            );
            return;
          }
          prepared.purposeAndRegionItems?.map(p => (p.rowId === rowId ? { ...p, regionId: geo.id } : p));
        });
      } else {
        prepared.purposeAndRegionItems = items?.map(p => (p.rowId === rowId ? { ...p, regionId: undefined } : p));
        return;
      }
    }

    if (isMinSum) {
      const rowId = k.toString().split('_')[1] as UUID;
      const items = prepared?.purposeAndRegionItems;
      const costs = Object.keys(values[k]);

      if (costs && costs.length) {
        costs.forEach(costId => {
          const cost = values[k][costId];

          // массив prepared не пуст, такой rowId уже есть
          if (items && items.length > 0 && items.find(p => p.rowId === rowId)) {
            // eslint-disable-next-line @stylistic/max-len
            prepared.purposeAndRegionItems = prepared?.purposeAndRegionItems?.map(p => p.rowId === rowId ? { ...p, minCostToBeApproved: cost } : p
            );

            return;
          }
          prepared.purposeAndRegionItems?.map(p => p.rowId === rowId ? {
            ...p, minCostToBeApproved: cost, territoryId: costId, rowId,
          } : p
          );
        });
      }
    }
  });
  return prepared;
};
export { getEmptyData, mapSettingsToRecords, mapFromValuesToApprovalsQuery };
