import { useState, useEffect } from 'react';
import { Shift as ShiftType } from 'api/shifts/shifts.types';

interface UseShiftsFilterParams {
  shiftsData: ShiftType[] | undefined;
}

export const useShiftsFilter = ({ shiftsData }: UseShiftsFilterParams) => {
  const [filteredShiftsData, setFilteredShiftsData] = useState<ShiftType[]>([]);
  const [searchValue, setSearchValue] = useState('');

  // Устанавливаем все данные при монтировании или изменении shiftsData
  useEffect(() => {
    if (shiftsData) {
      setFilteredShiftsData(shiftsData);
    }
  }, [shiftsData]);

  const handleSearch = (value: string) => {
    setSearchValue(value);

    if (!shiftsData) {
      setFilteredShiftsData([]);
      return;
    }

    if (!value.trim()) {
      setFilteredShiftsData(shiftsData);
      return;
    }

    const searchLower = value.toLowerCase();
    setFilteredShiftsData(shiftsData.filter(
      shift => shift.driverName.toLowerCase().includes(searchLower)
      || shift.vehicleStateNumber.toLowerCase().includes(searchLower)
    ));
  };

  return {
    filteredShiftsData,
    searchValue,
    handleSearch,
  };
};
