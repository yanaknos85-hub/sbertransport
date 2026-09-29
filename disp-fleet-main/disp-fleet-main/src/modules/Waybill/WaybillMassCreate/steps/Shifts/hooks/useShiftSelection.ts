import { useCallback } from 'react';
import { useAppStore } from 'ioc';
import { useTranslation } from 'i18n';
import type { Shift as ShiftType } from 'api/shifts/shifts.types';

interface UseShiftSelectionParams {
  errorShiftIds: Set<string>;
  maxSelectionLimit?: number;
  selectedShifts: ShiftType[];
  setSelectedShifts: React.Dispatch<React.SetStateAction<ShiftType[]>>;
}

export const useShiftSelection = ({
  errorShiftIds,
  maxSelectionLimit = 100,
  selectedShifts,
  setSelectedShifts,
}: UseShiftSelectionParams) => {
  const { logger } = useAppStore();
  const { createMass: i18n } = useTranslation().t.Waybill;

  const isShiftDisabled = useCallback(
    (shiftId: string): boolean => {
      // Сначала проверяем, есть ли ошибка у смены
      if (errorShiftIds.has(shiftId)) {
        return true;
      }

      // Затем проверяем логику ограничения выбора по лимиту
      const isSelectedLimitReached = selectedShifts.length >= maxSelectionLimit;
      // Если лимит достигнут, дезейблируем только те смены, которые еще не выбраны
      if (isSelectedLimitReached) {
        return !selectedShifts.some(shift => shift.id === shiftId);
      }

      return false;
    },
    [errorShiftIds, maxSelectionLimit, selectedShifts]
  );

  const handleRowSelectionChange = useCallback(
    (_selectedRowKeys: React.Key[], selectedRows: ShiftType[]) => {
      // Удаляем из выбранных смены с ошибками
      const validRows = selectedRows.filter(row => !errorShiftIds.has(row.id));

      if (validRows.length > maxSelectionLimit) {
        logger.toMessage('error', i18n.maxSelectionLimitText);
        setSelectedShifts(validRows.slice(0, maxSelectionLimit));
        return;
      }

      setSelectedShifts(validRows);
    },
    [errorShiftIds, maxSelectionLimit, setSelectedShifts, logger, i18n]
  );

  return {
    isShiftDisabled,
    handleRowSelectionChange,
  };
};
