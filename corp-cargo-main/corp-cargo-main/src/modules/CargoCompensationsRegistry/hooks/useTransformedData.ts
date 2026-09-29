import { useMemo } from 'react';
import { formatBaseDate } from 'utils/formatTime';
import { convertToRubles } from 'utils/convertToRubles';
import { CompensationType, Row } from '../types';
import { VisibleFields } from '../constants';
import { emptySign } from 'constants/constants.app';

export const useTransformedData = (data: CompensationType[]): Row[] => {
  return useMemo(
    () => (data || []).map(item => {
      return {
        id: item.id || emptySign,
        [VisibleFields.humanReadableId]: item.humanReadableId || emptySign,
        [VisibleFields.routeNumber]: item.routeNumber || emptySign,
        [VisibleFields.courier]: item.courier || emptySign,
        [VisibleFields.department]: item.department || emptySign,
        [VisibleFields.mvz]: item.mvz || emptySign,
        [VisibleFields.cost]: convertToRubles(item.cost) || emptySign,
        [VisibleFields.status]: item.status || emptySign,
        [VisibleFields.deadline]: formatBaseDate(item.deadline),
        [VisibleFields.approvedBy]: item.approvedBy || emptySign,
        [VisibleFields.approvalDate]: formatBaseDate(item.approvalDate),
        [VisibleFields.formationDate]: formatBaseDate(item.formationDate),
      };
    }),
    [data]
  );
};
