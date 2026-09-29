import { useEffect, useState } from 'react';

import { createCallableCtx } from 'utils/createCallableContext';

import { WORKING_DAY_KEY, WORKING_DAY_LENGTH } from '../../../constants/schedule.constants';

const useHook = () => {
  const [estimatedHours, setEstimatedHours] = useState(WORKING_DAY_LENGTH);

  useEffect(() => {
    const workingDayLength = localStorage.getItem(WORKING_DAY_KEY);

    workingDayLength && setEstimatedHours(Number(workingDayLength));
  }, []);

  const onEstimatedHours = (value: number) => {
    localStorage.setItem(WORKING_DAY_KEY, `${value}`);
    setEstimatedHours(value);
  };

  return {
    estimatedHours,
    setEstimatedHours: onEstimatedHours,
  };
};

export const [useEstimatedHours, EstimatedHoursProvider] = createCallableCtx(useHook, {
  name: 'EstimatedHoursProvider',
});
