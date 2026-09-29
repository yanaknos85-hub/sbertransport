import { useMemo } from 'react';
import moment from 'moment';
import { useTranslation } from 'i18n';

import { YandexTaxiRequestStatusTitles } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';
import { useProfile } from 'api/profile';
import { useGetAvailableTransportTypes } from 'api/transport-types';

import { DATE_FORMAT, TripRequestTitles, VALUE_NOT_FOUND } from 'constants/constants.app';
import { autoApprovalTitle } from '../constants';

import { TFraudMonitoringDetailsResponse } from 'modules/FraudMonitoring/fraudMonitoring.interface';

import { getTimeString } from 'utils';
import { fullNameLastFirstPat } from 'utils/employee';
import { getTripStatusTitle } from 'utils/getTripStatusTitle';
import { getTransportTypeName } from 'utils/transport';

export const useInformationGroups = (response: TFraudMonitoringDetailsResponse) => {
  const {
    t: {
      fraudMonitoring: { details },
    },
  } = useTranslation();

  const { organizationId } = useProfile().data;
  const allTransportTypes = useGetAvailableTransportTypes(organizationId).data;
  const getTransportTypeTitle = getTransportTypeName(allTransportTypes);

  const groups = useMemo(
    () => [
      {
        groupName: details.generalInfo,
        fields: [
          {
            title: details.humanReadableId,
            value: response.humanReadableId,
          },
          {
            title: details.transportType,
            value: getTransportTypeTitle(response.transportType),
          },
          {
            title: details.costCenter,
            value: response.costCenter,
          },
          {
            title: details.status,
            value: getTripStatusTitle(response.status, { ...YandexTaxiRequestStatusTitles, ...TripRequestTitles }),
          },
          {
            title: details.departmentCode,
            value: response.departmentCode,
          },
        ],
      },
      {
        groupName: details.passengerInfo,
        fields: [
          {
            title: details.passengerPersonnelNumber,
            value: response.passenger.personnelNumber,
          },
          {
            title: details.passangerName,
            value: fullNameLastFirstPat({
              firstName: response.passenger.firstName,
              lastName: response.passenger.lastName,
              patronymic: response.passenger.patronymic,
            }),
          },
          {
            title: details.purpose,
            value: response.purpose.purpose,
          },
        ],
      },
      {
        groupName: details.tripInfo,
        fields: [
          {
            title: details.desiredDate,
            value: response.desiredDate
              ? moment(response.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)
              : VALUE_NOT_FOUND,
          },
          {
            title: details.approveDate,
            value: response.approvalDate
              ? moment(response.approvalDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)
              : VALUE_NOT_FOUND,
          },
          {
            title: details.departureAddress,
            value: response.departureAddress,
          },
          {
            title: details.waypointsCount,
            value: response.waypointsCount,
          },
          {
            title: details.destinationAddress,
            value: response.destinationAddress,
          },
          {
            title: details.endDate,
            value: response.tripEndDate
              ? moment(response.tripEndDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)
              : VALUE_NOT_FOUND,
          },
        ],
      },
      {
        groupName: details.intermediateAddresses,
        groupNameDescription: details.intermediateAddressesWaitTime,
        fields: response.intermediateAddresses.map(({ address, waitTime }) => ({
          title: address,
          value: getTimeString(waitTime),
        })),
      },
      {
        groupName: details.plannedAndActualInfo,
        fields: [
          {
            title: details.expectedCost,
            value: response.plannedCost,
          },
          {
            title: details.expectedDistance,
            value: response.distance.toFixed(3),
          },
          {
            title: details.costToCompensate,
            value: response.actualCost,
          },
        ],
      },
      {
        groupName: details.additionalInfo,
        fields: [
          {
            title: details.approverName,
            value: fullNameLastFirstPat({
              firstName: response.approver?.firstName,
              lastName: response.approver?.lastName,
              patronymic: response.approver?.patronymic,
            }),
          },
          {
            title: details.autoApproval,
            value: autoApprovalTitle[String(response.autoApproval)],
          },
          {
            title: details.approverPersonnelNumber,
            value: response.approver?.personnelNumber,
          },
        ],
      },
    ],
    [response, details, allTransportTypes]
  );

  return groups;
};
