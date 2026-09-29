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
import { Department } from 'stores/Corporate/Corporate.interface';

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
  contractors: string[];
  additionalFeature: string;
}

interface ExecutorGroupRequest {
  active: boolean;
  authorId: UUID;
  customers: any[];
  departments: Department[];
  executors: any[];
  geoZones: any[];
  organizationId: UUID;
  organizations: any[];
  name: string;
  service: string;
  serviceLevel: string;
  fullData: ExecutorGroup;
  contractors: string[];
  additionalFeature: string;
}

/* TODO требуется рефакторинг всего раздала ExecutorGroup */
export const useModalForm = (executor: ExecutorGroupRequest | undefined) => {
  const [form] = useForm<Omit<ExecutorGroupRequest, 'id'>>();

  const { modalState, closeModal } = useModal();
  const { logger } = useAppStoreContext();

  const [execOrgId, setExecOrgId] = useState('');
  const [orgIds, setOrgIds] = useState<string[]>([]);
  // ПОЛЯ ФОРМЫ

  const initialValues = useMemo(
    () => ({
      name: executor?.name,
      organizationId: executor?.organizationId,
      executors: executor?.executors.map(el => el.employeeId),
      serviceLevel: executor?.serviceLevel,
      organizations: executor?.organizations.map(item => item.officialName),
      departments: executor?.departments.map(item => item.id),
      geoZones: executor?.geoZones.map(item => item.name),
      customers: executor?.customers[0]?.name ?? '',
      fullData: executor,
      contractors: executor?.contractors,
      additionalFeature: executor?.additionalFeature,
    }),
    [executor]
  );

  useEffect(() => {
    // Сбрасываем состояния при изменении типа модального окна
    setExecOrgId('');
    setOrgIds([]);

    if (modalState.type === 'edit' && executor) {
      // Устанавливаем organizationId для исполнителей
      if (executor.organizationId) {
        setExecOrgId(executor.organizationId);
      }
      // Устанавливаем organizationId для заказчика
      if (executor.organizations && executor.organizations.length > 0) {
        setOrgIds(executor.organizations.map(el => el?.id));
      }
    }
  }, [modalState.type, executor]);

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
        service: 'CARGO_TRANSPORTATION',
        organizations: values.organizations,
        customers: [],
      };

      const transformedData = {
        ...executorDetails,
        organizations: orgIds,
      };
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const saveExecutorGroup: (val: ExecutorGroupRequest) => Promise<any>
        = modalState.type === 'edit' ? editExecutorGroup : createExecutorGroup;
      saveExecutorGroup(transformedData).then(closeModal).catch(ignore);
    },
    [
      initialValues,
      modalState.type,
      editExecutorGroup,
      createExecutorGroup,
      closeModal,
      logger,
      orgIds,
    ]
  );

  return {
    form,
    saveForm,
    execOrgId,
    setExecOrgId,
    orgIds,
    setOrgIds,
    initialValues,
  };
};
