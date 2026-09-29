import React, { useMemo, useState } from 'react';
import { FormInstance } from 'antd';
import moment from 'moment';
import { EmployeeModel, SelfEmployeeModel } from '@sber-sbertransport/mf-core';

import { StoreNames } from 'stores';

import { HttpStatus } from 'shared/constants/http';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { isAxiosError } from 'utils';

import { useCheckAbsenceMutation } from 'api/fraud/check-absence';

import Alert from 'modules/EmployeeApp/components/Alert/Alert';

import { ItemNewDesign } from '../../styles/styled';
import { FormValues } from '../../types/types';
import { getTimeZone } from '../../utils/utils';
import { ABSENCE_CHECK_ALERT_DESCRIPTION, ABSENCE_CHECK_ALERT_TITLE } from './constants';
import { useAbsenceWarningModal } from './useAbsenceWarningModal';

export const useCheckAbsence = (form: FormInstance<FormValues>, isExternal?: boolean) => {
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const [checkAbsenceMutation, { isLoading: isChecking }] = useCheckAbsenceMutation();
  const [isAbsenceConflict, setIsAbsenceConflict] = useState(false);
  const { ModalElement: AbsenceWarningModal, showModal: showAbsenceWarningModal } = useAbsenceWarningModal();

  const handleCheck = async () => {
    const {
      date: desiredDate = moment(), purpose, employee,
    } = form.getFieldsValue();
    const timeZone = getTimeZone();
    const personnelNumber = employee ? (employee as unknown as EmployeeModel | SelfEmployeeModel).personnelNumber : selfStore.selfEmployee.personnelNumber;
    const expectedDuration = geo.calculatedRoute?.time;

    if (!purpose || !personnelNumber || !timeZone) {
      return;
    }

    try {
      const response = await checkAbsenceMutation({
        purpose,
        desiredDate: desiredDate.valueOf(),
        personnelNumber,
        timeZone,
        expectedDuration,
      });

      if (!response || response.status !== HttpStatus.OK || response.data.absence === null) {
        return;
      }

      if (personnelNumber === selfStore.selfEmployee.personnelNumber) {
        setIsAbsenceConflict(true);

        throw Error;
      }

      try {
        await showAbsenceWarningModal();
      } catch {
        return;
      }

      throw Error;
    } catch (error) {
      if (isAxiosError(error)) {
        return;
      }

      throw error;
    }
  };

  const handleFormValuesChanged = (changedValues: object) => {
    if (['purpose', 'when', 'date', 'passenger', 'employee'].some(value => value in changedValues)) {
      setIsAbsenceConflict(false);
    }
  };

  const AbsenceConflictAlert = useMemo(() => {
    if (!isAbsenceConflict) {
      return null;
    }

    return (
      <ItemNewDesign>
        <Alert
          type="warning"
          description={ABSENCE_CHECK_ALERT_DESCRIPTION}
          showIcon
          message={ABSENCE_CHECK_ALERT_TITLE}
        />
      </ItemNewDesign>
    );
  }, [isAbsenceConflict]);

  if (isExternal && isAbsenceConflict) {
    setIsAbsenceConflict(false);
  }

  return {
    isChecking,
    AbsenceConflictAlert,
    AbsenceWarningModal,
    isAbsenceConflict,
    check: handleCheck,
    onFormValuesChange: handleFormValuesChanged,
  };
};
