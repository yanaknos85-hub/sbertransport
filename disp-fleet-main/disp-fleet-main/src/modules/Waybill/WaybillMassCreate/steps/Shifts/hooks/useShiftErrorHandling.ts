import { useEffect, useMemo } from 'react';
import type { Shift as ShiftType } from 'api/shifts/shifts.types';
import type { TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';

interface UseShiftErrorHandlingParams {
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  setSelectedShifts: React.Dispatch<React.SetStateAction<ShiftType[]>>;
}

export const useShiftErrorHandling = ({
  firstTitleResult,
  setSelectedShifts,
}: UseShiftErrorHandlingParams) => {
  // Идентификаторы смен, по которым есть ошибки в firstTitleResult
  const errorShiftIds = useMemo(() => {
    if (!firstTitleResult) return new Set<string>();
    const errorIds = firstTitleResult
      .filter(item => item.errorText !== '')
      .map(item => item.shiftId);
    return new Set(errorIds);
  }, [firstTitleResult]);

  const getRowClassName = (record: ShiftType): string => {
    return errorShiftIds.has(record.id) ? 'errorRow' : '';
  };

  // Удаляем из selectedShifts смены с ошибками
  useEffect(() => {
    if (!firstTitleResult) return;

    const errorIds = Array.from(errorShiftIds);

    if (errorIds.length > 0) {
      setSelectedShifts(prevShifts => {
        const filteredSelected = prevShifts.filter(shift => !errorIds.includes(shift.id));
        return filteredSelected.length !== prevShifts.length ? filteredSelected : prevShifts;
      });
    }
  }, [firstTitleResult, errorShiftIds, setSelectedShifts]);

  return {
    errorShiftIds,
    getRowClassName,
  };
};
