import { AvailableStatus } from 'stores/StatusTypes/StatusTypes.interface';
import { TransportTypeDescriptions, TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TripStatus } from 'api/travel-status';
import { Employee } from 'stores/Employee/Employee.interface';
import { TariffJson } from 'stores/Tariffs/Tariffs.interface';
import { Department } from 'stores/Engineer/Models/Feed/Feed.department';
import { UUID } from 'utils/io-ts';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import { fullVehicleInfo, fullNameWithPhoneNumber } from 'utils/employee';
import { FeedContent } from 'stores/Engineer/Models/Feed/Feed.content';
import { LabeledValue } from 'utils';
import { TripRequestReport } from 'stores/PersonalSearch/PersonalSearch.interface';
import { TransportStatuses } from 'modules/ServiceMetrics/TransportStatuses';
import {
  cancelCodes,
  CanceledStatuses,
  DetailViewStatuses,
  PaymentAwaitingStatuses,
  TripFinishedStatuses
} from '../DetailedViewStatuses';
import { ApprovalSettings } from 'stores/ApprovalSettings/ApprovalSettings.interface';

export const getAvailableStatuses = (
  transportType: string | undefined,
  status: string | undefined,
  statuses: AvailableStatus[],
  approvalSettings?: ApprovalSettings
): { label: string; value: string }[] => {
  const indexedStatuses = statuses.map(item => {
    DetailViewStatuses.forEach((element, index) => {
      if (element.indexOf(item.name) !== -1) {
        item.index = index;
      }
    });

    return item;
  });

  const currentStatus: { status?: string; index?: number } = {
    status,
    index: indexedStatuses.find(({ name }) => status === name)?.index || 0,
  };

  const showedStatuses = (item: AvailableStatus) => {
    // TODO на бэке нельзя уйти на предыдущий статус, ждём согласования требований или изменение бэка

    // if (transportType === TransportTypes.PERSONAL && currentStatus.status) {
    //   if (AwaitingStatuses.includes(currentStatus.status) && ApprovedStatuses.includes(item.name)) {
    //     return true;
    //   }
    //   if (SharedRideDecline.includes(currentStatus.status) && AwaitingApprovalStatuses.includes(item.name)) {
    //     return true;
    //   }
    // };
    if (
      currentStatus.status
      && (TransportTypes.COURIER === transportType
      || TransportTypes.DEDICATED === transportType
      || TransportTypes.INTERREGIONAL === transportType
      || TransportTypes.DOMESTIC_COURIER === transportType
      || TransportTypes.INDIVIDUAL === transportType)
    ) {
      if (!TransportStatuses.cargo.includes(item.name)) {
        return false;
      }
      const currentIndex = TransportStatuses.cargo.indexOf(currentStatus.status);
      const itemIndex = TransportStatuses.cargo.indexOf(item.name);
      if (currentIndex > itemIndex) {
        return false;
      }
    }
    if (currentStatus.status && TransportStatuses.cargo.includes(currentStatus.status)) {
      if (TripFinishedStatuses.includes(currentStatus.status) && item.name === 'CARGO_CANCELED') {
        return false;
      }
    }

    if ((transportType === TransportTypes.TAXI || transportType === TransportTypes.GROUP_TRANSFER
      || transportType === TransportTypes.PUBLIC)
      && currentStatus.status) {
      if (CanceledStatuses.includes(item.name)) {
        return false;
      }
    }

    const transportTypes: Record<string, keyof typeof TransportStatuses> = {
      [TransportTypes.TAXI]: 'taxi',
      [TransportTypes.CARSHARING]: 'carsharing',
      [TransportTypes.PERSONAL]: 'personal',
      [TransportTypes.PUBLIC]: 'public',
      [TransportTypes.GROUP_TRANSFER]: 'group_transfer',
    };

    if (
      currentStatus.status
      && (transportType === TransportTypes.TAXI
      || transportType === TransportTypes.CARSHARING
      || transportType === TransportTypes.PERSONAL
      || transportType === TransportTypes.PUBLIC
      || transportType === TransportTypes.GROUP_TRANSFER)
    ) {
      if (TransportStatuses[transportTypes[transportType]].includes(item.name)) {
        if (transportType === TransportTypes.PERSONAL || transportType === TransportTypes.CARSHARING) {
          if (approvalSettings && approvalSettings.tripApprovalActive) {
            return item.index && (currentStatus.index || currentStatus.index === 0)
              ? (currentStatus.index <= 1 || currentStatus.index === 8) && item.index === 15 : false;
          } else {
            return item.index && (currentStatus.index || currentStatus.index === 0)
              ? (currentStatus.index === 0) && item.index === 15 : false;
          }
        }

        if (transportType === TransportTypes.PUBLIC) {
          return item.index && (currentStatus.index || currentStatus.index === 0)
            ? (currentStatus.index === 0) && item.index === 15 : false;
        }

        return item.index && currentStatus.index ? currentStatus.index <= 11 && item.index === 15 : false;
      }
      return false;
    }

    if (
      currentStatus.status
      && (transportType === TransportTypes.TAXI
      || transportType === TransportTypes.CARSHARING
      || transportType === TransportTypes.PERSONAL
      || transportType === TransportTypes.PUBLIC
      || transportType === TransportTypes.GROUP_TRANSFER)
    ) {
      if (!TransportStatuses[transportTypes[transportType]].includes(item.name)) {
        return false;
      }
    }

    if ((transportType === TransportTypes.PERSONAL || transportType === TransportTypes.PUBLIC)
      && currentStatus.status) {
      if (PaymentAwaitingStatuses.includes(currentStatus.status)) {
        return item.index && currentStatus.index
          ? (item.index > currentStatus.index && item.index < currentStatus.index + 3) : false;
      }
    }

    return item.index && currentStatus.index ? item.index > currentStatus.index : false;
  };

  const availableStatuses = indexedStatuses.filter(item => showedStatuses(item));

  return availableStatuses.reduce((acc: { label: string; value: string }[], { name, rusName }) => {
    const item = acc.find(el => el.label === rusName);
    item ? (item.value += `,${name}`) : acc.push({ label: rusName, value: name });
    return acc;
  }, []);
};

export const getRadioGroupOptions = (
  transportType: string | undefined
): LabeledValue[] => transportType === TransportTypes.TAXI
  ? cancelCodes.TaxiCancelCodes.map(item => ({ label: item.reason, value: `${item.reason}; ${item.code}` }))
  : cancelCodes.PersonalAndPublicCancelCodes.map(item => ({
    label: item.reason,
    value: `${item.reason};${item.code}`,
  }));

export const getTripStatus = ({
  statusList,
  status,
}: {
  statusList: TripStatus[];
  status: string | undefined;
}): string => statusList.find(tripStatus => tripStatus.name === status)?.rusName ?? '-';

export const getTransportType = (transportType: keyof typeof TransportTypes): string => ({
  TAXI: TransportTypeDescriptions.TAXI,
  PUBLIC: TransportTypeDescriptions.PUBLIC,
  PERSONAL: TransportTypeDescriptions.PERSONAL,
  CARSHARING: TransportTypeDescriptions.CARSHARING,
  BICYCLE: TransportTypeDescriptions.BICYCLE,
  WALK: TransportTypeDescriptions.WALK,
  SCOOTER: TransportTypeDescriptions.SCOOTER,
  DEDICATED: TransportTypeDescriptions.DEDICATED,
  INDIVIDUAL: TransportTypeDescriptions.INDIVIDUAL,
  COURIER: TransportTypeDescriptions.COURIER,
  INTERREGIONAL: TransportTypeDescriptions.INTERREGIONAL,
  DOMESTIC_COURIER: TransportTypeDescriptions.DOMESTIC_COURIER,
  GROUP_TRANSFER: TransportTypeDescriptions.GROUP_TRANSFER,
}[transportType]);

export const getEmployeesAttributes = (user: Employee | null, emptyValue = '-'): string => {
  if (!user || !user.attributes.length) {
    return emptyValue;
  }
  return user.attributes.map(attr => attr.name).join(', ');
};

export const getPositionName = (
  positionName: string | null | undefined,
  employee: Employee | null,
  emptyValue = '-'
): string => positionName || (employee && employee.positionName) || emptyValue;

export const getTariffHumanReadableId = (
  tariffHumanReadableId: string | null | undefined,
  tariff: TariffJson | null,
  emptyValue = '-'
): string => tariffHumanReadableId || (tariff && tariff.humanReadableId) || emptyValue;

export const getDepartmentName = (
  tripDepartment: Department | null | undefined,
  department: Department | null,
  emptyValue = '-'
): string => (tripDepartment && tripDepartment.departmentName) || department?.departmentName || emptyValue;

export const getContractorName = (
  contractor: { id: UUID; name?: string | null } | null | undefined,
  contractorById: Record<string, Contractor | undefined>,
  emptyValue = '-'
): string => {
  if (!contractor) {
    return emptyValue;
  }
  const currentContractor = contractorById[contractor.id];
  return currentContractor ? currentContractor.name : emptyValue;
};

export const getInitiatorTrip = (trips: TripRequestReport[]): TripRequestReport => {
  if (trips.length > 1) {
    const sortedTrips = [...trips].sort((prev, next) => {
      if (prev.creationTime && next.creationTime) {
        return prev.creationTime - next.creationTime;
      }
      return 0;
    });

    return sortedTrips[0];
  }
  return trips[0];
};

export const getAutoInfo = (coopTrips: TripRequestReport[], trip: FeedContent, emptyValue = '-'): string => {
  if (trip.transportType === TransportTypes.PERSONAL) {
    const initiatorTrip = getInitiatorTrip(coopTrips);

    const autoInfo = {
      brandName: initiatorTrip.personalCar?.brandName,
      model: initiatorTrip.personalCar?.model,
      registrationNumber: initiatorTrip.personalCar?.registrationNumber,
    };
    return initiatorTrip.personalCar ? fullVehicleInfo(autoInfo) : emptyValue;
  }

  if (trip.transportType === TransportTypes.TAXI) {
    const autoInfo = {
      brandName: trip.vehicle?.brandName,
      model: trip.vehicle?.model,
      registrationNumber: trip.vehicle?.registrationNumber,
      color: trip.vehicle?.color,
    };
    return trip.vehicle ? fullVehicleInfo(autoInfo) : emptyValue;
  }
  return emptyValue;
};

export const getDriverInfo = (coopTrips: TripRequestReport[], trip: FeedContent, emptyValue = '-'): string => {
  if (trip.transportType === TransportTypes.PERSONAL) {
    const initiatorTrip = getInitiatorTrip(coopTrips);

    const driverInfo = {
      firstName: initiatorTrip.passenger?.firstName,
      patronymic: initiatorTrip.passenger?.patronymic,
      lastName: initiatorTrip.passenger?.lastName,
      contactPhone: initiatorTrip.passenger?.phone,
    };
    return initiatorTrip.passenger ? fullNameWithPhoneNumber(driverInfo) : emptyValue;
  }

  if (trip.transportType === TransportTypes.TAXI) {
    return trip.driver ? fullNameWithPhoneNumber(trip.driver) : emptyValue;
  }
  return emptyValue;
};
