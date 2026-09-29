/* eslint-disable @typescript-eslint/no-explicit-any */
import React, {
  FC, useEffect, useMemo, useState
} from 'react';
import { observer } from 'mobx-react';
import { constFalse } from 'fp-ts/lib/function';
import { Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';

import {
  formatPhoneNumber, getFormatDate, getTimeString, serializeRubles
} from 'utils';
import { formatName } from 'utils/formatName';
import { UUID } from 'utils/io-ts';
import { getCoopTripName } from 'utils/reportsUtils';

import { TripRequestModel } from 'stores/Trip/models';
import { Employee } from '@sber-sbertransport/mf-core';
import { TransportTypes, TransportTypeLongDescriptions, TripStatusesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripResponse } from 'stores/Registry/Registry.interface';

import { Button } from 'shared/components/Button/Button';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import { Fields } from 'modules/TripDetailed/types/types';
import { TripInProgressStatuses } from 'modules/TripDetailed/DetailedViewStatuses';
import {
  useChangeRequestStatus,
  useDriverAssignedStatusWithCarInfo,
  useStartPersonalTrip
} from 'api/trip-detailed-view';
import { useChangeCargoRequestStatus } from 'api/engineer';
import { useTariff } from 'api/tariffs';
import { useSingleContractor } from 'api/contractors';
import { useGroupTransferTransportClasses } from 'api/passengers-transport-classes';

import { OrderTitle } from 'modules/OrderExecution/constants/General';
import { STATUSES } from 'modules/OrderExecution/constants/Statuses';
import { VALUE_NOT_FOUND } from 'modules/OrderExecution/constants/General';
import personFullNameGeneration from 'modules/OrderExecution/utils/CarService/personFullNameGeneration';
import { IPersonInfo } from 'modules/OrderExecution/interfaces/CarService/General';
import {
  getAddressString,
  getGroupTransferChildSeatsString,
  getIntermediateAddressString,
  formatNumber
} from 'modules/OrderExecution/utils/common';
import { ServiceEnum } from 'modules/Departments/DelegatesPage/constants/EmployeeApp.constants';
import { zoneTime } from 'utils/times';
import { formatFullName } from 'utils/formatFullName';
import { DATE_FORMAT, sTransport } from 'constants/constants.app';

import { StatusSelect } from '../StatusSelect';
import CompensationDocument from './Compensation';
import OrderHead from '../Header';
import OrderStatus from '../../OrderStatus';
import { Section, Item } from '../../ItemContainer';

import { EditOutlined } from '@ant-design/icons';
import * as routes from 'constants/constants.routes';
import '../styles.scss';

interface OrderDetailedProps {
  data: TripResponse;
  className: string;
  update: () => void;
}

const ShowOrder: FC<OrderDetailedProps> = props => {
  const [form] = useForm();
  const {
    data, className, update,
  } = props;
  const { approvedBy } = data;

  const [isEditMode, setIsEditMode] = useState(constFalse);
  const isTaxiTransportType = data.transportType === TransportTypes.TAXI;
  const isPersonalTransportType = data.transportType === TransportTypes.PERSONAL;
  const isGroupTransferType = data.transportType === TransportTypes.GROUP_TRANSFER;
  const { passengerStore } = useAppStoreContext();

  const currentStatus = STATUSES.find(item => item.name === String(data.status));

  const tariff = useTariff(
    { transTypeId: data.transportType.toLowerCase(), tariffId: data.tariffId as UUID },
    { enabled: isTaxiTransportType }
  ).data;
  const contractor = useSingleContractor(
    (isGroupTransferType ? data.contractorId : tariff?.contractorId) as UUID,
    { enabled: isTaxiTransportType || isGroupTransferType }
  ).data;

  const [changeRequestStatus, { isLoading: isChangeStatusLoading }] = useChangeRequestStatus();
  const [changeCargoRequestStatus, { isLoading: isChangeCargoStatusLoading }] = useChangeCargoRequestStatus();
  const [
    driverAssignedStatusWithCarInfo,
    { isLoading: isDriverAssignedLoading },
  ] = useDriverAssignedStatusWithCarInfo();
  const [startPersonalTrip, { isLoading: isStartTripLoading }] = useStartPersonalTrip();
  const { data: groupTransferClasses } = useGroupTransferTransportClasses();
  const getGroupTransferClassName = (groupTransferClass: string | undefined) => {
    if (!groupTransferClass) return VALUE_NOT_FOUND;

    return groupTransferClasses.find(item => item.value === groupTransferClass)?.rusName || VALUE_NOT_FOUND;
  };

  const transportServiceName = TransportTypeLongDescriptions[data.transportType as TransportTypes];

  const isAttachedDocument = data.transportCompensation?.some(el => 'attachedDocumentId' in el);

  const isLoading
    = isChangeStatusLoading || isChangeCargoStatusLoading || isDriverAssignedLoading || isStartTripLoading;

  useEffect(() => {
    form.resetFields();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data, form.resetFields]);

  const title = useMemo(
    () => (
      <OrderHead caption={`Заявка ${data.humanReadableId}`} className={{ title: 'headerTitle' }}>
        <div className={`${className}__edit-button`} onClick={() => setIsEditMode(true)}>
          <EditOutlined className="editIcon" />
        </div>
      </OrderHead>
    ),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [data.humanReadableId]
  );

  const saveRequest = (value: Fields) => {
    if (value.status !== data.status) {
      if (!value.vehicleInfo) {
        const [, personalTripStart] = TripInProgressStatuses;

        if (value.status === personalTripStart) {
          const startTripParams = {
            requestId: data.id,
            latitude: 0,
            longitude: 0,
          };

          startPersonalTrip(startTripParams).then(() => {
            setIsEditMode(false);
            update();
          });
          return;
        }
        if (value.status.startsWith('CARGO_')) {
          value.status
          && changeCargoRequestStatus({ requestId: data.id, status: value.status }).then(() => {
            setIsEditMode(false);
            update();
          });
        } else {
          value.status
          && changeRequestStatus({ requestId: data.id, status: value.status }).then(() => {
            setIsEditMode(false);
            update();
          });
        }
        return;
      }

      if (value.vehicleInfo) {
        driverAssignedStatusWithCarInfo({
          requestId: data.id,
          vehicleInfo: { vehicleInfo: value.vehicleInfo },
        }).then(() => {
          setIsEditMode(false);
          update();
        });
      }
    }
  };

  const cancelEdit = () => {
    setIsEditMode(false);
    form.resetFields();
  };

  useEffect(() => {
    if (data.payRequestIds?.length) {
      const transportType = data.transportType === TransportTypes.PERSONAL
        ? TransportTypes.PUBLIC : TransportTypes.PERSONAL;
      passengerStore.getRelatedOrder(data.payRequestIds[0] as UUID, transportType);
    }

    data.coopTrip && passengerStore.getSharedTrip(data.id as UUID);
  }, []);

  useEffect(() => {
    return () => {
      passengerStore.resetRelatedOrder();
      passengerStore.resetSharedTrip();
      passengerStore.resetOrder();
    };
  }, []);

  const joinedPassengers: Employee[] | undefined = data.coopTrip
    ? data.joinedPassengers
    && data.transportType === TransportTypes.PERSONAL
      ? data.joinedPassengers?.filter(el => el.userId !== data.passenger.userId)
      : data.joinedPassengers?.concat(data.passenger)
    : data.joinedPassengers?.length
      ? data.joinedPassengers?.concat(data.passenger)
      : data.transportType === TransportTypes.PERSONAL ? [] : [data.passenger];

  const countJoinedPassengers = data.coopTrip
    ? data.joinedPassengers
    && data.transportType === TransportTypes.PERSONAL
      ? joinedPassengers?.filter(el => el.userId !== data.passenger.userId).length
      : data.joinedPassengers?.concat(data.passenger).length
    : data.joinedPassengers?.length
      ? data.joinedPassengers?.concat(data.passenger).length
      : data.transportType === TransportTypes.PERSONAL ? 0 : [data.passenger]?.length;

  const passengers = data.transportType === TransportTypes.TAXI
    ? data.coopTrip
      // eslint-disable-next-line @stylistic/max-len
      ? passengerStore.sharedTrip?.requests.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.TAXI_AWAITING_APPROVAL)
      : []
    // eslint-disable-next-line @stylistic/max-len
    : passengerStore.sharedTrip?.requests.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.PERSONAL_AWAITING_APPROVAL);

  // eslint-disable-next-line @stylistic/max-len
  const activePassengers = passengerStore.sharedTrip?.requests.filter(el => el?.status !== TripStatusesEnum.PERSONAL_CANCELLED && el?.status !== TripStatusesEnum.TAXI_CANCELLED);

  const countActivePassengers = data.coopTrip
  // eslint-disable-next-line @stylistic/max-len
    ? activePassengers?.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.PERSONAL_AWAITING_APPROVAL).length
    : 0;

  return (
    <Form
      form={form}
      initialValues={data}
      onFinish={saveRequest}
    >
      <Section className={`${className}__head`} title={title}>
        <Item title={OrderTitle.humanReadableId}>{data.humanReadableId}</Item>
        <Item title={OrderTitle.status}>
          {isEditMode ? (
            <Form.Item name="status">
              <StatusSelect status={data.status} transportType={data.transportType} />
            </Form.Item>
          ) : (
            <OrderStatus
              status={currentStatus}
              transportType={data.transportType}
              isSharedRideSub={data.coopTrip && data.sharedRideOwner === false}
              tripId={data.id}
              statusCodeDescription={data.statusCodeDescription}
              approver={approvedBy ? {
                id: approvedBy.id,
                fullName: formatFullName(approvedBy.firstName, approvedBy.lastName, approvedBy.patronymic),
              } : null}
            />
          )}
        </Item>
        {!isGroupTransferType
        && <Item title={OrderTitle.transportType}>{transportServiceName}</Item>}
        {isGroupTransferType
        && <Item title={OrderTitle.groupTransferClass}>{getGroupTransferClassName(data.groupTransferClass)}</Item>}
        {isGroupTransferType
        && <Item title={OrderTitle.territoryOrg}>{data.author?.organizationName || VALUE_NOT_FOUND}</Item>}
        <Item title={OrderTitle.creationTime}>{getFormatDate(data.creationTime)}</Item>
        <Item title={OrderTitle.desiredTripTime}>{getFormatDate(data.desiredDate)}</Item>
        {!isGroupTransferType
        && (
        <Item title={OrderTitle.deadlineTime}>
          {
            zoneTime(data.deadline, DATE_FORMAT.DATE_WITH_TIME_SEPARATED_BY_COMMAS, data?.timeZone)
          }
        </Item>
        )}
        {!isGroupTransferType
        && <Item title={OrderTitle.approvalTime}>{getFormatDate(data.approvalDate)}</Item>}
        {[TransportTypes.PERSONAL, TransportTypes.PUBLIC].includes(data.transportType) && (
          <Item title={OrderTitle.finalApprovalTime}>{getFormatDate(data.orderPaymentFormationStartDate)}</Item>
        )}
        {!isGroupTransferType
        && <Item title={OrderTitle.approverName}>{formatName(approvedBy as Employee)}</Item>}
      </Section>

      {!isGroupTransferType
      && (
      <Section title={OrderTitle.creator}>
        <Item title={OrderTitle.creatorName}>{formatName(data.author as Employee)}</Item>
        <Item title={OrderTitle.creatorPhone}>{formatPhoneNumber(data.author?.mobilePhone)}</Item>
        <Item title={OrderTitle.personPositionName}>{data.author?.positionName || VALUE_NOT_FOUND}</Item>
        <Item title={OrderTitle.personDepartmentName}>{data.author?.departmentName || VALUE_NOT_FOUND}</Item>
      </Section>
      )}
      {!isGroupTransferType
      && (isTaxiTransportType || isPersonalTransportType) && (
        <Section title={(
          <div>
            {data.transportType === TransportTypes.TAXI ? OrderTitle.passengers : OrderTitle.passengersWithMe}
            <span className="informationPassengersCount">{countJoinedPassengers}</span>
          </div>
        )}
        >
          {joinedPassengers?.reverse().map((el, index) => (
            <div className="informationPassengersList" key={index}>
              <span>{formatName(el)}</span>
              <span>{`тел.: ${formatPhoneNumber(el.mobilePhone)}`}</span>
            </div>
          )
          )}
        </Section>
      )}
      {!isGroupTransferType
      && isPersonalTransportType && (
        <Section title={(
          <div>
            {OrderTitle.passengersJoined}
            <span className="informationPassengersCount">{countActivePassengers}</span>
          </div>
        )}
        >
          {passengers?.map((el, index) => (
            <div className="informationPassengersList" key={index}>
              <span>{formatName(el.employee)}</span>
              <span>{`тел.: ${formatPhoneNumber(el.employee.mobilePhone)}`}</span>
            </div>
          )
          )}
        </Section>
      )}
      {!isGroupTransferType
      && (
      <Section title={OrderTitle.trip}>
        <Item title={OrderTitle.tripCost}>{serializeRubles(data.expected?.cost) || VALUE_NOT_FOUND}</Item>
        <Item title={OrderTitle.tripDistance}>{`${data.expected?.distance} км`}</Item>
        <Item title={OrderTitle.tripTime}>{getTimeString(data.expected?.time)}</Item>
        {data.transportType !== TransportTypes.CARSHARING && data.transportType !== TransportTypes.GROUP_TRANSFER && (
        <Item title={OrderTitle.tripType}>{getCoopTripName(data.coopTrip)}</Item>
        )}
        <Item title={OrderTitle.relatedApplication}>
          {passengerStore.relatedOrder && data.payRequestIds?.length
            ? (
              <a
                className="relatedOrder"
                href={`${routes.ORDER_EXECUTION_PASSENGERS}/${data.transportType === TransportTypes.PERSONAL
                  ? ServiceEnum.public : ServiceEnum.personal}/${passengerStore.relatedOrder.id}`}
              >
                {passengerStore.relatedOrder.humanReadableId}
              </a>
            )
            : VALUE_NOT_FOUND}
        </Item>
      </Section>
      )}

      {isGroupTransferType && (
      <>
        <Section title={OrderTitle.trip}>
          <Item title={OrderTitle.groupTransferDepartureAddress}>{getAddressString(data.expected.waypoints[0])}</Item>
          <Item title={OrderTitle.groupTransferIntermediateAddresses}>
            {getIntermediateAddressString(data.expected.waypoints)}
          </Item>
          <Item title={OrderTitle.groupTransferNumberOfPoints}>
            {data.expected.waypoints.length || VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.groupTransferPlannedRent}>{VALUE_NOT_FOUND}</Item>
          {' '}
          {/* пока без бека */}
          <Item title={OrderTitle.groupTransferArrivalAddress}>
            {getAddressString(data.expected.waypoints[data.expected.waypoints.length - 1])}
          </Item>
          <Item title={OrderTitle.groupTransferOrderTime}>{getFormatDate(data.desiredDate)}</Item>
          <Item title={OrderTitle.groupTransferOrderCloseTime}>{VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.groupTransferDeadline}>{getFormatDate(data.deadline)}</Item>
          <Item title={OrderTitle.groupTransferArrivalTime}>{VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.groupTransferDeadlineViolation}>{VALUE_NOT_FOUND}</Item>
        </Section>

        <Section title={OrderTitle.person}>
          <Item title={OrderTitle.primaryPassenger}>{personFullNameGeneration(data.passenger as IPersonInfo)}</Item>
          <Item title={OrderTitle.primaryPassengerPhone}>{formatPhoneNumber(data.passenger?.mobilePhone)}</Item>
          <Item title={OrderTitle.secondaryPassenger}>{data.information?.addContactFIO || VALUE_NOT_FOUND}</Item>
          {' '}
          <Item title={OrderTitle.secondaryPassengerPhone}>
            {formatPhoneNumber(data.information?.addContactPhone) || VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.passengersNumber}>{data?.passengerCount || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.tripPurpose}>{data.purpose ? data.purpose.label : VALUE_NOT_FOUND}</Item>
        </Section>

        <Section title={OrderTitle.groupTransferAttributes}>
          <Item title={OrderTitle.numberFlight}>{data.information?.numberFlight || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.dateFlight}>{getFormatDate(data.information?.dateFlight)}</Item>
          <Item title={OrderTitle.groupTransferLuggage}>
            {data.information?.bugs ? data.information?.bugsComment : VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.groupTransferOversizedLuggage}>
            {data.information?.bugsOversized ? data.information?.bugsOversizedComment : VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.groupTransferChildSeats}>
            {getGroupTransferChildSeatsString(data.information?.childSeatDetails)}
          </Item>
          <Item title={OrderTitle.groupTransferDesiredVehicle}>
            {data.information?.typeVehicle || VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.groupTransferAnimal}>
            {data.information?.animal ? data.information?.animalComment : VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.phoneHotel}>{data.information?.phoneHotel || VALUE_NOT_FOUND}</Item>
        </Section>

        <Section title={OrderTitle.creatorStructureTitle}>
          <Item title={OrderTitle.creatorOrganization}>{data.author?.organizationName || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.creatorDepartment}>{data.author?.departmentName || VALUE_NOT_FOUND}</Item>
        </Section>

        <Section title={OrderTitle.planFactTitle}>
          <Item title={OrderTitle.contractor}>{contractor?.name || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.vehicleData}>
            {`${data.driverInfo?.vehicleInfo || VALUE_NOT_FOUND} ${data.driverInfo?.registrationNumber || VALUE_NOT_FOUND}`}
          </Item>
          <Item title={OrderTitle.driverName}>{data.driverInfo?.driverName || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.driverPhone}>{data.driverInfo?.driverPhone || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.preliminaryDistanceKm}>{formatNumber(data.expected.distance)}</Item>
          <Item title={OrderTitle.actualDistanceKm}>{VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.prelimiaryAmount}>{formatNumber(data.expected.cost / 100)}</Item>
          <Item title={OrderTitle.actualAmount}>{VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.preliminaryRentTime}>{VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.actualTripTime}>{VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.actualWaitingTime}>{VALUE_NOT_FOUND}</Item>
        </Section>

        <Section title={OrderTitle.orderCreatorSectionTitle}>
          <Item title={OrderTitle.orderCreator}>{formatName(data.author as Employee)}</Item>
          <Item title={OrderTitle.tripRating}>{data.requestRating?.rating || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.tripRatingComment}>{data.requestRating?.ratingComment || VALUE_NOT_FOUND}</Item>
        </Section>
      </>
      )}

      {data.transportType === TransportTypes.PUBLIC && isAttachedDocument && (
        <Section title="Файлы">
          <Item>
            <CompensationDocument request={new TripRequestModel(data as any)} />
          </Item>
        </Section>
      )}

      {isTaxiTransportType && (
        <Section title={OrderTitle.agent}>
          <Item title={OrderTitle.contractor}>
            {contractor?.name ? contractor.name : !tariff?.contractorId ? sTransport : VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.car}>
            {(data.driverInfo && `${data.driverInfo.vehicleInfo || ''} ${data.driverInfo.registrationNumber || ''}`)
            || VALUE_NOT_FOUND}
          </Item>
          <Item title={OrderTitle.driver}>{data.driverInfo?.driverName || VALUE_NOT_FOUND}</Item>
          <Item title={OrderTitle.tariffHumanReadableId}>{tariff?.humanReadableId || VALUE_NOT_FOUND}</Item>
        </Section>
      )}

      {data.commentForDriver && (
        <Section title={OrderTitle.comment}>
          <Item>{data.commentForDriver}</Item>
        </Section>
      )}

      {isEditMode && (
        <div className={`${className}__actions`}>
          <Button type="text" onClick={cancelEdit}>
            Отмена
          </Button>
          <Button
            htmlType="submit"
            type="primary"
            loading={isLoading}
          >
            Сохранить
          </Button>
        </div>
      )}
    </Form>
  );
};

export default observer(ShowOrder);
