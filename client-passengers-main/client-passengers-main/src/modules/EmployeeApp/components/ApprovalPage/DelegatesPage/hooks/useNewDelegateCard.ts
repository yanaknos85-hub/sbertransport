import { ISelfEmployeeStore, EmployeeModel } from '@sber-sbertransport/mf-core';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Store } from 'antd/lib/form/interface';
import { Dictionary, mapKeys } from 'lodash';
import moment from 'moment';
import { useCallback, useState } from 'react';

import { DATE_FORMAT } from 'constants/constants.app';

import { Delegate, DelegateModel, IDelegatesStore } from 'stores/Delegates/Delegates.interface';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import {
  ITransportTypesStore,
  TransportTypeEnum,
  TransportTypesModel
} from 'stores/TransportTypes/TransportTypes.interface';

interface IUseDelegate {
  onFinish(values: Store): void;
  goDelegatesList(): void;
  onValuesChange(changedValues: any, allValues: any): void;
  form: FormInstance;
  transportTypesByName: Dictionary<TransportTypesModel>;
  transportType: TransportTypeEnum;
  isStartDateActive: boolean;
  getDelegates: (date: string) => Promise<void>;
  selfCandidatesToDelegates: EmployeeModel[];
}

export const useDelegates = (
  delegatesStore: IDelegatesStore,
  transportTypesStore: ITransportTypesStore,
  selfEmployee: ISelfEmployeeStore
): IUseDelegate => {
  const { configStore } = useAppStoreContext();

  const {
    addDelegate, getSelfCandidatesToDelegates, selfCandidatesToDelegates,
  } = delegatesStore;
  const {
    setActiveTransportType,
    availableTransportTypes,
    activeTransportType,
    clearActiveTransportType,
  } = transportTypesStore;

  const [form] = Form.useForm();

  const goDelegatesList = useCallback((): void => {
    configStore.history.push('./');
    clearActiveTransportType();
  }, [history, clearActiveTransportType]);

  const transportTypesByName = mapKeys(availableTransportTypes, 'name');

  const transportType = activeTransportType as TransportTypeEnum;

  function onFinish(delegate: Omit<Delegate, 'delegateId'> & { employee: EmployeeModel }): void {
    if (!delegate) {
      return;
    }

    const model = new DelegateModel({
      id: delegate.id,
      supervisorId: selfEmployee.selfEmployee.id,
      delegateId: delegate.employee.id,
      startDate: moment(delegate.startDate).format(DATE_FORMAT.BASE),
      endDate: moment(delegate.endDate).format(DATE_FORMAT.BASE),
      transportType: transportType as string,
      delegateEmployee: delegate.delegateEmployee,
    });

    addDelegate({
      supervisorId: model.supervisorId,
      delegateId: model.delegateId,
      startDate: model.startDate,
      endDate: model.endDate,
      transportType: model.transportType,
    });

    goDelegatesList();
  }

  const onValuesChange = (changedValues: any, allValues: any): void => {
    if (allValues.transportTypeId && availableTransportTypes) {
      setActiveTransportType(allValues.transportTypeId);
    }
  };

  const [isStartDateActive, setIsStartDateActive] = useState(false);

  const getDelegates = async (date: string): Promise<void> => {
    setIsStartDateActive(true);
    await getSelfCandidatesToDelegates(transportType, date);
  };

  return {
    onFinish,
    form,
    goDelegatesList,
    onValuesChange,
    transportTypesByName,
    transportType,
    isStartDateActive,
    getDelegates,
    selfCandidatesToDelegates,
  };
};
