import { useState } from 'react';

import { ShiftConflict } from 'api/shift-conflicts/shift-conflicts.types';

import { createCallableCtx } from 'utils/createCallableContext';

import { ShiftWithCar } from '../Schedule.types';

const useHook = () => {
  const [selectedShift, setSelectedShift] = useState<ShiftWithCar | null>(null);
  const [conflictShift, setConflictShift] = useState<ShiftConflict | null>(null);

  return {
    selectedShift,
    setSelectedShift,
    conflictShift,
    setConflictShift,
  };
};

export const [useSelectedShift, SelectedShiftProvider] = createCallableCtx(useHook, { name: 'SelectedShiftProvider' });
