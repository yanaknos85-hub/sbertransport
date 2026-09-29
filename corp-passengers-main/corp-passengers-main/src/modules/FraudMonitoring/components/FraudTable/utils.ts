import { VALUE_NOT_FOUND } from 'constants/constants.app';

import { prepareSubmitPurposes } from 'utils/purposesOptions';
import { isValue } from 'shared/components/DateInput/utils';

import { Value } from 'shared/components/DateInput/types';
import { TFraudMonitoringReportRequest } from 'modules/FraudMonitoring/fraudMonitoring.interface';
import { TransportType } from 'stores/TransportTypes/TransportTypes.interface';
import { FraudFilters } from './types';

import styles from './styles.module.scss';

export const getRowClassName = (_: unknown, index: number) => {
  return index % 2 === 0 ? styles.oddRow : styles.evenRow;
};

export const getTransportTypeName = (transportTypes: TransportType[]) => {
  const transportTypeNamesMap = transportTypes.reduce(
    (map, { name, rusName }) => map.set(name, rusName),
    new Map<string, string>()
  );

  return (transportType: string) => transportTypeNamesMap.get(transportType) ?? VALUE_NOT_FOUND;
};

export const processDate = (dateValue: Value | undefined): { start: string | undefined; end: string | undefined } => {
  const defaultReturnValue = { end: undefined, start: undefined };

  if (!dateValue) return defaultReturnValue;

  const { mode, value } = dateValue;

  if (mode === 'date') {
    const selectedDate = value[0];

    return {
      start: selectedDate?.startOf('date').toISOString(),
      end: selectedDate?.endOf('date').toISOString(),
    };
  }

  if (mode === 'year') {
    const selectedYear = value[0];

    return {
      start: selectedYear?.startOf('year').toISOString(),
      end: selectedYear?.endOf('year').toISOString(),
    };
  }

  if (mode === 'range') {
    return {
      start: value[0]?.startOf('date').toISOString(),
      end: value[1]?.endOf('date').toISOString(),
    };
  }

  return defaultReturnValue;
};

export const filterFalsyValues = <T extends object>(values: T) => {
  return Object.fromEntries(
    Object.entries(values).filter(([_, value]) => {
      if (value === undefined || value === '') return false;
      if (Array.isArray(value) && value.length === 0) return false;
      if (isValue(value) && value.value.every(item => item === null)) return false;

      return true;
    })
  );
};

export const getSelectedFiltersCount = (filters: FraudFilters) => {
  const tempFilters = { ...filters };
  delete tempFilters.humanReadableId;

  const count = Object.keys(tempFilters).length;

  return count;
};

export const prepareFiltersForRequest = (filters: FraudFilters): TFraudMonitoringReportRequest['filter'] => {
  const {
    humanReadableId, approveDate, approverName, passengerName, purposes, transportType, tripDate,
  } = filters;

  const { start: approveDateStart, end: approveDateEnd } = processDate(approveDate);
  const { start: tripDateStart, end: tripDateEnd } = processDate(tripDate);
  const purpose = prepareSubmitPurposes(purposes ?? []).map(({ id }) => id);

  const requestObject: TFraudMonitoringReportRequest['filter'] = {
    humanReadableId: humanReadableId?.trim(),
    approveDateStart,
    approveDateEnd,
    tripDateStart,
    tripDateEnd,
    purpose,
    transportType,
    approverName,
    passengerName,
  };

  return filterFalsyValues(requestObject);
};
