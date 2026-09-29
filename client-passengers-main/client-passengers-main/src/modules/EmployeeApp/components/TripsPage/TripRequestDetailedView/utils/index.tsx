import { Steps } from 'antd';
import moment from 'moment-timezone';
import React, { useMemo } from 'react';

import { formatRubles } from 'utils';

import {
  RequestStatus,
  useGetRequestCarsharingStatusesList,
  useGetRequestGroupTransferStatusesList,
  useGetRequestPersonalStatusesList,
  useGetRequestPublicStatusesList,
  useGetRequestTaxiStatusesList
} from 'api/trip-requests';

import { DATE_FORMAT } from 'constants/constants.app';

import { TripPrefTitlesEnum } from 'modules/EmployeeApp/components/CreateTripRequest/constants/preferences.enum';
import { useTripPurposeList } from 'modules/EmployeeApp/components/EditTripRequestForm/hooks/useTripPurposeList';
import { TripRequestFieldsTitles, TripTypes } from 'modules/EmployeeApp/shared/TripRequestDict.enum';

import { TransportCompensations, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { zoneTime } from 'utils/trips/times';

import {
  TimelineCarsharingStatuses,
  TimelinePersonalStatuses,
  TimelinePublicStatuses,
  TimelineTaxiStatuses
} from '../timelineStatuses';
import { busClasses } from '../../../TaxiClasses/TaxiClassesConfig';

import styles from '../styles.module.scss';
import CompensationDocument from 'modules/EmployeeApp/components/ApprovalPage/ApprovalDetailedView/ApprovalDetailedRequestView/Compensation';

const { Step } = Steps;

export const chooseCorrectStatusesList = (transportType: TransportTypeEnum | undefined) => {
  switch (transportType) {
    case TransportTypeEnum.TAXI:
      return useGetRequestTaxiStatusesList;
    case TransportTypeEnum.PERSONAL:
      return useGetRequestPersonalStatusesList;
    case TransportTypeEnum.PUBLIC:
      return useGetRequestPublicStatusesList;
    case TransportTypeEnum.CARSHARING:
      return useGetRequestCarsharingStatusesList;
    case TransportTypeEnum.GROUP_TRANSFER:
      return useGetRequestGroupTransferStatusesList;
    default:
      return useGetRequestTaxiStatusesList;
  }
};

const getCorrectStatusList = (
  transportType: TransportTypeEnum | undefined
): {
  statuses: string[];
}[] => {
  switch (transportType) {
    case TransportTypeEnum.TAXI:
      return TimelineTaxiStatuses;
    case TransportTypeEnum.PERSONAL:
      return TimelinePersonalStatuses;
    case TransportTypeEnum.PUBLIC:
      return TimelinePublicStatuses;
    case TransportTypeEnum.CARSHARING:
      return TimelineCarsharingStatuses;
    default:
      return [];
  }
};

export function useDescriptionData({
  request,
  delegates,
  tripFromCoop,
}: {
  request?: TripRequestModel;
  delegates: string[] | string;
  tripFromCoop?: TripFromCoopModel;
}): {
    timeline: { label: string; steps: JSX.Element };
    descriptionFields: [string, string | number | JSX.Element | undefined][];
    activeStatus?: RequestStatus;
  } {
  const tripPurposeList = useTripPurposeList(request);
  const statusList = chooseCorrectStatusesList(request?.transportType)().data;

  const ft = TripRequestFieldsTitles;
  const preferencesString = request?.requestOptions
    ? request.requestOptions
      .map((element: string) => TripPrefTitlesEnum[element as keyof typeof TripPrefTitlesEnum])
      .join(', ')
    : '';
  const absentText = 'отсутствует';
  const activeStatusEntity = (request?.status && statusList.find(status => status.name === request.status));

  const renderStatusWithSteps = (): JSX.Element => {
    const statusListMapped = getCorrectStatusList(request?.transportType);
    // @ts-ignore
    const activeStatus = activeStatusEntity?.rusName || '';
    const activeStatusIndex = statusListMapped.findIndex(element => element.statuses.includes(activeStatus));

    return (
      <div className={styles.statusContainer}>
        <Steps
          className={styles.steps}
          size="small"
          responsive
          labelPlacement="horizontal"
          current={activeStatusIndex}
          direction="vertical"
          style={{ marginTop: '30px' }}
        >
          {statusListMapped.map((item, index) => (
            <Step
              title={item.statuses.includes(activeStatus) ? activeStatus : item.statuses.join(' - ')}
              key={`${item.statuses.join('-')}-${index + 1}`}
            />
          ))}
        </Steps>
      </div>
    );
  };

  const approvedByWithDelegates = useMemo(() => {
    const approvedByString = request?.approvedBy.shortName || '-';
    const delegatesString = Array.isArray(delegates) ? `(делегаты: ${delegates.join(', ')})` : '';
    return `${approvedByString} ${delegatesString}`;
  }, [request?.approvedBy, delegates]);

  const renderCreationTime = (): string => request?.creationTime ? moment(request.creationTime).format(DATE_FORMAT.DATE_WITH_TIME) : 'не известна';

  const renderPurpose = (): string => `${tripPurposeList?.getById(request?.purpose.id)?.label}` ?? 'не выбрана';

  const isBus = busClasses.includes(request?.taxiClass as TaxiClassEnum);

  const { kpi } = tripFromCoop || {};

  const isSaving = kpi?.ordersKpi && kpi?.ordersKpi.length > 1;

  const isSuburb = request?.transportCompensation?.[0]?.transportType?.publicCompensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION;

  return {
    timeline: { label: ft.status, steps: renderStatusWithSteps() },
    activeStatus: activeStatusEntity,
    descriptionFields: [
      [ft.id, request?.humanReadableId],
      [ft.approvedBy, approvedByWithDelegates],
      [ft.passenger, request?.passenger.shortName],
      [ft.creationTime, renderCreationTime()],
      [ft.desiredDate, zoneTime(request?.desiredDate, DATE_FORMAT.DATE_WITH_TIME, request?.timeZone)],
      [ft.expectedTime, request?.expected.getTimeString()],
      [ft.expectedCost,
        tripFromCoop
          ? (
            <div>
              <span>
                {kpi?.ordersKpi[0].orderPriceKop && formatRubles(kpi?.ordersKpi[0].orderPriceKop / 100)}
              </span>

              {
              isSaving && (
                <>
                  <span style={{ textDecoration: 'line-through', padding: '0 10px' }}>
                    {kpi?.totalCost && formatRubles(kpi?.totalCost / 100)}
                  </span>
                  <span>
                    (Совместная поездка)
                  </span>
                </>
              )
            }
            </div>
          )
          : formatRubles(request?.costInRubles),
      ],
      [ft.expectedDistance, request?.expected.getDistanceString()],
      [ft.passengerCount, isBus ? request?.passengerCount : (tripFromCoop?.passengers ?? request?.passengerCount)],
      [ft.tripType, request?.coopTrip ? TripTypes.shared : TripTypes.single],
      [ft.author, request?.author.shortName],
      [ft.purpose, renderPurpose()],
      [ft.transportType, request?.transportTypeString],
      [ft.contractor,
        request?.driverInfo
          ? (
            <>
              {request?.driverInfo?.driverName}
            &nbsp;
              <a href={`tel:${request?.driverInfo?.driverPhone}`}>
                {request?.driverInfo?.driverPhone}
              </a>
            </>
          )
          : absentText,
      ],
      [ft.preferences, preferencesString || 'отсутствуют'],
      [ft.commentary, request?.commentForDriver ? request.commentForDriver : absentText],
      ['', isSuburb ? <CompensationDocument request={request as TripRequestModel} /> : ''],
    ],
  };
}
