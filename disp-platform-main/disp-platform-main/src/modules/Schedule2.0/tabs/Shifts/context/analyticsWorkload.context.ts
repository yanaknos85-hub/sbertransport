import {
  useState, useEffect, useCallback, useRef
} from 'react';

import { useAnalyticsWorkload } from 'api/schedule2.0/schedule.api';
import { Workload } from 'api/schedule2.0/schedule.types';

import { createCallableCtx } from 'utils/createCallableContext';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import { useShiftsQuery } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';

import { useWorkloadGeneral } from '../../../context/workload.context';
import { useEstimatedHours } from './estimatedHours.context';

const VISIBLE_VEHICLE_WORK_LOAD = 'VISIBLE_VEHICLE_WORK_LOAD';

const useHook = () => {
  const [workload, setWorkload] = useState<Workload>();
  const [vehicleIds, setVehicleIds] = useState<UUID[]>([]);
  const [visibleVehicleWorkload, setVisibleVehicleWorkload] = useState(
    JSON.parse(localStorage.getItem(VISIBLE_VEHICLE_WORK_LOAD) ?? 'null') ?? true
  );

  const { setWorkloadGeneral } = useWorkloadGeneral();

  useEffect(() => {
    setWorkloadGeneral(workload?.general);
  }, [setWorkloadGeneral, workload]);

  const workloadDayRef = useRef('');

  const { query } = useShiftsQuery();

  const { estimatedHours } = useEstimatedHours();

  const [getAnalyticsWorkload] = useAnalyticsWorkload();

  const getWorkload = useCallback(
    () => getAnalyticsWorkload({
      startTime: query.startDate,
      endTime: query.endDate,
      estimatedHours,
      vehicleIds,
    })
      .then(data => {
        if (data) {
          setWorkload(data);
        }
      })
      .catch(ignore),
    [getAnalyticsWorkload, query.startDate, query.endDate, vehicleIds, estimatedHours]
  );

  useEffect(() => {
    localStorage.setItem(VISIBLE_VEHICLE_WORK_LOAD, JSON.stringify(visibleVehicleWorkload));
  }, [visibleVehicleWorkload]);

  useEffect(() => {
    if (vehicleIds.length) {
      getWorkload();
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vehicleIds, estimatedHours]);

  const onVehicleId = useCallback(
    (values: UUID[]) => {
      if (vehicleIds.join() !== values.join()) {
        setVehicleIds(values);
      } else {
        const dateStr = `${query.startDate}-${query.endDate}`;

        if (workloadDayRef.current !== dateStr) {
          workloadDayRef.current = dateStr;
          setVehicleIds(values);
        }
      }

      if (!values.length) {
        setWorkload(undefined);
      }
    },
    [query.endDate, query.startDate, vehicleIds]
  );

  return {
    workload,
    visibleVehicleWorkload,
    setVisibleVehicleWorkload,
    setVehicleIds: onVehicleId,
  };
};

export const [useWorkload, AnalyticsWorkloadProvider] = createCallableCtx(useHook, {
  name: 'AnalyticsWorkloadProvider',
});
