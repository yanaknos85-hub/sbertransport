import { useState } from 'react';
import { ISelfEmployeeStore } from '@sber-sbertransport/mf-core';

import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Store } from 'antd/lib/form/interface';
import { Dictionary, mapKeys } from 'lodash';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import { Delegate, DelegateModel, IDelegatesStore } from 'stores/Delegates/Delegates.interface';
import {
  ITransportTypesStore,
  TransportType,
  TransportTypeEnum,
  TransportTypesModel
} from 'stores/TransportTypes/TransportTypes.interface';
import { useModal } from '../context/modal.context';
import { DelegateFormValues } from '../components/DelegateAddEditForm/DelegateAddEditForm';

export interface IUseDelegate {
  onFinish(values: Store): void;
  onValuesChange(changedValues: unknown, allValues: DelegateFormValues): void;
  form: FormInstance<DelegateFormValues>;
  transportTypesByName: Dictionary<TransportTypesModel>;
  transportType: TransportTypeEnum;
  isLoading: boolean;
  isFormDirty: boolean;
  setFormDirty: React.Dispatch<React.SetStateAction<boolean>>;
}

interface Props {
  delegatesStore: IDelegatesStore;
  transportTypesStore: ITransportTypesStore;
  selfEmployee: ISelfEmployeeStore;
  refetch: () => void;
}

export const useDelegates = ({
  delegatesStore, transportTypesStore, selfEmployee, refetch,
}: Props): IUseDelegate => {
  const { closeModal } = useModal();
  const [isLoading, setLoading] = useState(false);
  const [isFormDirty, setFormDirty] = useState(false);

  const { addDelegate, updateDelegate } = delegatesStore;

  const {
    setActiveTransportType, availableTransportTypes, activeTransportType,
  } = transportTypesStore;

  const [form] = Form.useForm();

  const transportTypesByName = mapKeys(availableTransportTypes, 'name');

  const transportType = activeTransportType as TransportTypeEnum;

  const onFinish = async (delegate: Delegate): Promise<void> => {
    if (!delegate) return;

    setLoading(true);

    const model = new DelegateModel({
      id: delegate.id,
      supervisorId: selfEmployee.selfEmployee.id,
      delegateEmployee: delegate.delegateEmployee,
      delegateId: delegate.delegateEmployee.id,
      startDate: moment(delegate.startDate).format(DATE_FORMAT.BASE),
      endDate: moment(delegate.endDate).format(DATE_FORMAT.BASE),
      transportType: transportType as string,
    });

    return (model.id ? updateDelegate : addDelegate)(model)
      .then(() => {
        closeModal();
        form.resetFields();
        setFormDirty(false);
        transportTypesStore.clearActiveTransportType();
        !model.id && refetch();
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const onValuesChange = (_: unknown, allValues: DelegateFormValues): void => {
    setFormDirty(true);
    if (allValues.transportTypeId && availableTransportTypes) {
      setActiveTransportType(allValues.transportTypeId as TransportType);
    }
  };

  return {
    onFinish,
    form,
    onValuesChange,
    transportTypesByName,
    transportType,
    isLoading,
    isFormDirty,
    setFormDirty,
  };
};
