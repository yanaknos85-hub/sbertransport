/* eslint-disable @typescript-eslint/no-explicit-any */
import { useForm } from 'antd/lib/form/Form';
import { useCreateExecutorGroup, useUpdateExecutorGroup } from 'api/executor-group';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { UUID } from 'utils/io-ts';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ignore } from 'utils';
import { useModal } from '../../context/modal.context';

interface ExecutorGroup {
  active: boolean;
  authorId: UUID;
  customers: any[];
  departments: any[];
  executors: any[];
  geoZones: any[];
  organizationId: UUID;
  organizations: any[];
  name: string;
  service: string;
  serviceLevel: string;
}

interface ExecutorGroupRequest {
  active: boolean;
  authorId: UUID;
  customers: any[];
  departments: any[];
  executors: any[];
  geoZones: any[];
  organizationId: UUID;
  organizations: any[];
  name: string;
  service: string;
  serviceLevel: string;
  fullData: ExecutorGroup;
}

export const useModalForm = (executor: ExecutorGroupRequest | undefined) => {
  const [form] = useForm();

  const { modalState, closeModal } = useModal();
  const { logger } = useAppStoreContext();

  const [execOrgId, setExecOrgId] = useState('');
  const [orgId, setOrgId] = useState([]);
  const [depsId, setDepsId] = useState([]);

  // ПОЛЯ ФОРМЫ

  const initialValues = useMemo(
    () => ({
      name: executor?.name,
      organizationId: executor?.organizationId,
      executors: executor?.executors.map(el => (`${el.employeeName} (${el.employeePersonnelNumber})`)),
      serviceLevel: executor?.serviceLevel,
      organizations: executor?.organizations.map(item => item.officialName),
      departments: executor?.departments.map(item => item.humanReadableId),
      geoZones: executor?.geoZones.map(item => item.name),
      customers: executor?.customers[0]?.name,
      fullData: executor,
    }),
    [executor]
  );

  useEffect(() => {
    form.resetFields();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialValues, form.resetFields, modalState.type]);

  // СОХРАНЕНИЕ ФОРМЫ

  const [createExecutorGroup] = useCreateExecutorGroup();
  const [editExecutorGroup] = useUpdateExecutorGroup();

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (values: any) => {
      const executorDetails = {
        ...initialValues,
        ...values,
      };

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const saveExecutorGroup: (val: ExecutorGroupRequest) => Promise<any>
        = modalState.type === 'edit' ? editExecutorGroup : createExecutorGroup;
      saveExecutorGroup(executorDetails).then(closeModal).catch(ignore);
    },
    [
      initialValues,
      modalState.type,
      editExecutorGroup,
      createExecutorGroup,
      closeModal,
      logger,
    ]
  );

  return {
    form,
    saveForm,
    execOrgId,
    setExecOrgId,
    orgId,
    setOrgId,
    depsId,
    setDepsId,
    initialValues,
  };
};
