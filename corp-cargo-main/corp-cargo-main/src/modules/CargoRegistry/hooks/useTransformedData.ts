import { useMemo } from 'react';
import { TripInfo } from 'stores/CargoRegistry/CargoRegistry.interface';
import { useCargoTransportTypes, useCargoTripStatuses } from 'api/cargo-registry-search';
import { getTripStatus } from 'utils/reportsUtils';
import { formatBaseDate, formatBaseTime, formatTimeDate } from 'utils/formatTime';
import { convertToRubles } from 'utils/convertToRubles';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatVolume } from 'utils/formatVolume';
import { Row } from '../types';
import { VisibleFields } from '../constants';
import { emptySign } from 'constants/constants.app';

export const useTransformedData = (data: TripInfo[]): Row[] => {
  const { data: statuses } = useCargoTripStatuses();
  const { data: transportTypes } = useCargoTransportTypes();

  return useMemo(
    () => (data || []).map(item => {
      const expectedDistance = item?.expected?.distance?.toFixed(1).toString().replace('.', ',');
      const weight = item.weight != null ? item.weight.toFixed(3).toString().replace('.', ',') : undefined;
      const volume = formatVolume(item.volume).toString().replace('.', ',');
      const waypointsCount = item?.expected?.waypointsCount?.toString();
      const economy = item?.economy
        ? convertToRubles(item?.economy)?.toString().replace('.', ',')
        : item?.economy === 0
          ? '0'
          : emptySign;
      const cost = item?.expected?.cost ? (
        (item.expected.cost / 100)
          .toFixed(2)
          .toString()
          .replace('.', ',')
        ) : emptySign;
      const actualCost = item?.actualCost ? (
        convertToRubles(item.actualCost)
          .toFixed(2)
          .toString()
          .replace('.', ',')
        ) : emptySign;
      const actualDistance = item?.actualDistance?.toFixed(1).toString().replace('.', ',') || emptySign;

      return {
        id: item.id || emptySign,
        [VisibleFields.humanReadableId]: item.humanReadableId,
        [VisibleFields.status]: getTripStatus(statuses, item.status, '-'),
        [VisibleFields.transportType]: transportTypes.find(s => s.name === item.transportType)?.rusName || '-',
        [VisibleFields.desiredDate]: formatTimeDate(item.desiredDate, emptySign),
        [VisibleFields.contractor]: item.contractor?.name || item.contractor?.id,
        [VisibleFields.author]: item.author?.fio || emptySign,
        [VisibleFields.expectedCost]: cost,
        [VisibleFields.expectedDistance]: expectedDistance || emptySign,
        [VisibleFields.sender]: item?.sender?.fio || emptySign,
        [VisibleFields.recipient]: item?.recipient?.fio || emptySign,
        [VisibleFields.senderAddress]: item.senderAddress || emptySign,
        [VisibleFields.recipientAddress]: item.recipientAddress || emptySign,
        [VisibleFields.waypointsCount]: waypointsCount || emptySign,
        [VisibleFields.volume]: volume || emptySign,
        [VisibleFields.weight]: weight || emptySign,
        [VisibleFields.senderOrganization]: item.senderOrganization || emptySign,
        [VisibleFields.recipientOrganization]: item.recipientOrganization || emptySign,
        [VisibleFields.creationDate]: formatBaseDate(item.creationTime, emptySign),
        [VisibleFields.creationTime]: formatBaseTime(item.creationTime, emptySign),
        [VisibleFields.authorPhone]: formatPhoneNumber(item?.author?.mobilePhone) || emptySign,
        [VisibleFields.senderPhone]: formatPhoneNumber(item?.sender?.mobilePhone) || emptySign,
        [VisibleFields.recipientPhone]: formatPhoneNumber(item?.recipient?.mobilePhone) || emptySign,
        [VisibleFields.source]: item?.source || emptySign,
        [VisibleFields.routeNumber]: item?.cargoTripId || emptySign,
        [VisibleFields.templateNumber]: item?.templateNumber || emptySign,
        [VisibleFields.evaluation]:
            item.status === 'CARGO_DELIVERY_CONFIRMATION_FINISHED'
              ? item?.evaluation?.rating?.toString() || emptySign
              : emptySign,
        [VisibleFields.costCenter]: item?.costCenter || emptySign,
        [VisibleFields.plannedDeliveryDate]: formatBaseDate(item?.plannedDeliveryDate, emptySign),
        [VisibleFields.economy]: economy || emptySign,
        [VisibleFields.shipmentTime]: formatBaseDate(item?.shipmentTime, emptySign),
        [VisibleFields.transferTime]: formatBaseDate(item?.transferTime, emptySign),
        [VisibleFields.deadlineDate]: item?.deadlineDate || emptySign,
        [VisibleFields.authorPersonnelNumber]: item?.author?.personnelNumber || emptySign,
        [VisibleFields.actualCost]: actualCost,
        [VisibleFields.actualDistance]: actualDistance,
      };
    }),
    [data, statuses, transportTypes]
  );
};
