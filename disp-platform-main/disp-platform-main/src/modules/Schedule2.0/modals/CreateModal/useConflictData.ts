import { useEffect } from 'react';

import { FormInstance } from 'antd';
import moment from 'moment';

import { useSearchDriversMutation } from 'api/drivers/drivers.api';
import { useProfile } from 'api/profile/profile.api';
import { useSearchAllVehiclesMutation } from 'api/vehicles/vehicles.api';

import { ignore } from 'utils/utils';

import { useSelectedShift } from 'modules/Schedule2.0/context/selectedShift.context';

export const useConflictData = (form: FormInstance, isOpened: boolean) => {
  const { contractorId, autoparkId } = useProfile().data;

  const [fetchVehicles] = useSearchAllVehiclesMutation(contractorId);
  const [fetchDrivers] = useSearchDriversMutation(contractorId);

  const { conflictShift } = useSelectedShift();

  useEffect(() => {
    if (conflictShift && isOpened) {
      // Получаем id авто по госномеру
      fetchVehicles({
        page: 0,
        size: 1,
        stateNumber: conflictShift.stateNumber,
        isActive: true,
        inExploitation: true,
        autopark: autoparkId,
      })
        .then(result => {
          if (result && !!result.content?.[0]) {
            form.setFieldsValue({
              vehicleId: result.content[0].id,
            });
          }
        })
        .catch(ignore);

      // Получаем id водителя по табельному номеру
      fetchDrivers({
        page: 0,
        size: 1,
        personnelNumber: conflictShift.personnelNumber,
      })
        .then(result => {
          if (result && !!result.content?.[0]) {
            form.setFieldsValue({
              driverId: result.content[0].id,
            });
          }
        })
        .catch(ignore);

      // Устаавливаем выбранные даты
      form.setFieldsValue({
        startDate: moment.utc(conflictShift.startDate).local(),
        shiftEndDate: moment.utc(conflictShift.endDate).local(),
      });
    }
  }, [autoparkId, conflictShift, fetchDrivers, fetchVehicles, form, isOpened]);
};
