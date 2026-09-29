import { useMemo } from 'react';
import { useCargoTransportTypes, useCargoTripStatuses } from 'api/cargo-registry-search';
import { formatBaseDate } from 'utils/formatTime';
import { convertToRubles } from 'utils/convertToRubles';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatVolume } from 'utils/formatVolume';
import { RouteType, Row, StatusNames } from '../types';
import { VisibleFields } from '../constants';
import { emptySign } from 'constants/constants.app';
import { useOrganizations } from 'api/organizations';

export const useTransformedData = (data: RouteType[]): Row[] => {
  const { data: statuses } = useCargoTripStatuses();
  const { data: transportTypes } = useCargoTransportTypes();
  const {
    data: {
      organizationResponse: {
        content,
      },
    },
  } = useOrganizations();

  return useMemo(
    () => (data || []).map(item => {
      const weight = item.weight?.toString().replace('.', ',');
      const volume = formatVolume(item.volume).toString().replace('.', ',');
      const organizations = content
        .filter(({ id }) => (
          item.organizations.includes(id)
        ))
        .map(({ officialName }) => (
          officialName
        ))
        .join(', ');

      return {
        id: item.id || emptySign,
        [VisibleFields.humanReadableId]: item.humanReadableId || emptySign,
        [VisibleFields.status]: StatusNames[item.status] || emptySign,
        [VisibleFields.author]: item.author || emptySign,

        [VisibleFields.timeZone]: item.timeZone || emptySign,

        [VisibleFields.creationTime]: formatBaseDate(item.creationTime) || emptySign,
        [VisibleFields.desiredDate]: formatBaseDate(item.desiredDate) || emptySign,
        [VisibleFields.shipmentTime]: formatBaseDate(item.shipmentTime) || emptySign,

        [VisibleFields.weight]: weight || emptySign,
        [VisibleFields.volume]: volume || emptySign,

        [VisibleFields.distance]: item.distance || emptySign,
        [VisibleFields.planedRangeVisible]: item.plannedDistance || emptySign,

        [VisibleFields.cost]: convertToRubles(item.cost) || emptySign,
        [VisibleFields.actualCost]: convertToRubles(item.actualCost) || emptySign,
        [VisibleFields.waypointCountVisible]: item.waypointCount || emptySign,

        [VisibleFields.contractor]: item.contractorName || emptySign,
        [VisibleFields.capacity]: item.capacity || emptySign,

        [VisibleFields.organizations]: organizations.length ? organizations : emptySign,
        [VisibleFields.loaders]: item.loaders || emptySign,

        [VisibleFields.driver]: item.driver || emptySign,
        [VisibleFields.driverPhone]: formatPhoneNumber(item.driverPhone) || emptySign,
        [VisibleFields.carInfo]: item.carInfo || emptySign,
      };
    }),
    [data, statuses, transportTypes]
  );
};
