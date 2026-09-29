import React, {
  useMemo, useState, useEffect, useRef, useCallback
} from 'react';
import { Form, FormInstance } from 'antd';
import { LabeledValue } from 'antd/lib/select';
import { useTranslation } from 'i18n';
import { useOrganizationProjection } from 'api/organizations/search';
import { useGeoZones } from 'api/geo-zones';
import { useContractors } from 'api/contractors';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { preventDefault } from 'utils';
import { UUID } from 'utils/io-ts';
import { SelectEmployee } from '../../SelectEmployee/SelectEmployee';
import { SelectDepartment } from '../../SelectDepartment/SelectDepartment';
import { Department } from 'stores/Corporate/Corporate.interface';

import styles from '../styles.module.scss';

interface DepartmentWithOrgInfo {
  id: UUID;
  name: string;
  organizationId: UUID;
}

export const useFields = (
  isEditable: boolean,
  orgExecId?: UUID,
  orgIds?: UUID[],
  isOpen?: boolean,
  executors?: UUID[] | undefined,
  departments?: UUID[] | undefined
) => {
  const [localExecutors, setLocalExecutors] = useState<UUID[]>([]);
  const [localDepartments, setLocalDepartments] = useState<UUID[]>([]);
  const [departmentsCache, setDepartmentsCache] = useState<DepartmentWithOrgInfo[]>([]);

  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const geoZones = useGeoZones({ suspense: true })?.data;
  const contractors = useContractors({ config: { suspense: false } }).data?.contractors.map(item => ({
    label: item.name,
    value: item.id,
  }));
  const organizationOptions: LabeledValue[]
    = organizations?.map(el => ({ label: el.officialName, value: el.id })) ?? [];
  const geoZoneOptions: LabeledValue[]
    = typeof geoZones !== 'string' ? geoZones.map(el => ({ label: el.name, value: el.id })) : [];
  const isEditMode = !isEditable;
  const { t } = useTranslation();

  // Синхронизация с формой при получении данных с бэкенда (только в режиме редактирования)
  useEffect(() => {
    if (!isEditable && executors && executors.length > 0) {
      setLocalExecutors(executors);
    } else if (isEditable) {
      // В режиме создания - сбрасываем
      setLocalExecutors([]);
    }
  }, [executors, isEditable]);

  // Сброс исполнителей при смене организации в режиме создания
  useEffect(() => {
    if (isEditable && orgExecId) { // режим создания и выбрана организация
      setLocalExecutors([]);
    }
  }, [orgExecId, isEditable]);

  // Сброс при смене режима (редактирование → создание)
  useEffect(() => {
    if (isEditable) { // переходим в режим создания
      setLocalExecutors([]);
    } else if (!isEditable && executors && executors.length > 0) { // переходим в режим редактирования
      setLocalExecutors(executors);
    }
  }, [isEditable, executors]);

  const prevOrgIdsRef = useRef<UUID[] | undefined>(undefined);

  // Эффект для очистки департаментов при удалении организации
  useEffect(() => {
    const prevOrgIds = prevOrgIdsRef.current || [];
    const currentOrgIds = orgIds || [];
    prevOrgIdsRef.current = currentOrgIds;

    if (isEditable && prevOrgIds.length > 0 && currentOrgIds.length < prevOrgIds.length) {
      const removedOrgIds = prevOrgIds.filter(orgId => !currentOrgIds.includes(orgId));

      if (removedOrgIds.length > 0) {
        const departmentsToRemove = departmentsCache
          .filter(dept => removedOrgIds.includes(dept.organizationId))
          .map(dept => dept.id);

        if (departmentsToRemove.length > 0) {
          setLocalDepartments(prev => prev.filter(deptId => !departmentsToRemove.includes(deptId))
          );
        }
      }
    }
  }, [orgIds, isEditable, departmentsCache]);

  useEffect(() => {
    if (!isEditable && departments && departments.length > 0) {
      setLocalDepartments(departments);
    } else if (isEditable) {
      if (!orgIds || orgIds.length === 0) {
        setLocalDepartments([]);
      }
    }
  }, [departments, isEditable, orgIds]);

  const updateDepartmentsCache = useCallback((departmentsData: Department[]) => {
    if (!departmentsData || departmentsData.length === 0) return;

    const newDepartmentsInfo: DepartmentWithOrgInfo[] = departmentsData.map(dept => ({
      id: dept.id,
      name: dept.departmentName,
      organizationId: dept.organizationId || (orgIds && orgIds.length > 0 ? orgIds[0] : '' as UUID),
    }));

    setDepartmentsCache(prev => {
      const existingIds = new Set(prev.map(dept => dept.id));
      const uniqueNew = newDepartmentsInfo.filter(dept => !existingIds.has(dept.id));
      return [...prev, ...uniqueNew];
    });
  }, [orgIds]);

  useEffect(() => {
    if (isEditable) {
      setDepartmentsCache([]);
    }
  }, [isEditable]);

  const parameterFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'name',
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        editable: true,
        label: t.ExecutorGroups.name,
        placeholder: 'Пример: ЦЧБ/Орловское отделение №0000/Пассажирские перевозки',
        maxLength: 128,
        rules: [
          {
            required: true,
            message: 'Пожалуйста, введите наименование группы',
          },
          {
            // @ts-ignore
            validator: (_, value): Promise<void> => value.match('^(?=.{1,150}$)[\\sа-яА-Я-]+/[\\sа-яА-Я0-9№-]+/[\\sа-яА-Я]+$')
              ? Promise.resolve()
              : Promise.reject('Введенный текст не соответствует маске шаблона'),
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'organizationId',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: t.ExecutorGroups.organizationExecutorGroups,
        placeholder: 'Выберите организацию',
        disabled: !isEditable,
        maxLength: 128,
        options: organizationOptions,
        rules: [
          {
            required: true,
            message: 'Пожалуйста, выберите организацию',
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'executors',
        fieldType: ModelFormFieldType.CUSTOM,
        editable: true,
        placeholder: 'Укажите исполнителей',
        isNewDesign: true,
        component: ({ form }: { form: FormInstance }) => {
          const onChange = (newValue: UUID[] | undefined) => {
            setLocalExecutors(newValue || []);
            form.setFieldsValue({ executors: newValue });
          };

          const isDisabled = !orgExecId;
          // В режиме создания всегда пропускаем начальную загрузку по ID
          const skipInitialRequest = isEditable;

          return (
            <Form.Item
              name="executors"
              style={{ marginBottom: 0 }}
              rules={[
                {
                  required: true,
                  message: 'Пожалуйста, выберите исполнителей из списка',
                },
              ]}
            >
              <div>
                <label htmlFor="executors-select" className={styles.labelStyle}>
                  {t.ExecutorGroups.executors}
                </label>
                <SelectEmployee
                  id="executors-select"
                  name="executors"
                  key={`${orgExecId}-${isEditable}`}
                  multiple
                  placeholder={isDisabled ? 'Сначала выберите организацию' : 'Укажите исполнителей'}
                  disabled={isDisabled}
                  value={localExecutors}
                  onChange={onChange}
                  extraParams={orgExecId ? { organizations: [orgExecId] } : undefined}
                  skipInitialRequest={skipInitialRequest}
                  isOpen={isOpen}
                  // Передаем начальные значения для отображения в режиме редактирования
                  initialEmployeeIds={!isEditable && executors ? executors : undefined}
                />
              </div>
            </Form.Item>
          );
        },
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        name: 'serviceLevel',
        editable: true,
        label: t.ExecutorGroups.serviceLevel,
        placeholder: 'Укажите уникальное значение',
        disabled: !isEditable,
        onPressEnter: preventDefault,
        maxLength: 128,
        isNewDesign: true,
      },
    ],
    [t, isEditable, organizationOptions, orgExecId, isOpen, executors]
  );

  const customerFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'organizations',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: t.ExecutorGroups.organization,
        placeholder: 'Выберите организацию',
        disabled: !isEditable,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: 'Пожалуйста, выберите организацию заказчика',
          },
        ],
        options: organizationOptions,
        isNewDesign: true,
        mode: 'multiple',
      },
      {
        name: 'departments',
        fieldType: ModelFormFieldType.CUSTOM,
        editable: true,
        mode: 'multiple',
        component: ({ form }: { form: FormInstance }) => {
          const onChange = (newValue: UUID[] | undefined) => {
            setLocalDepartments(newValue || []);
            form.setFieldsValue({ departments: newValue });
          };

          const isDisabled = !orgIds?.length;
          const skipInitialRequest = isEditable && (!localDepartments || localDepartments.length === 0);

          return (
            <Form.Item name="departments" style={{ marginBottom: 0 }}>
              <div>
                <label htmlFor="departments-select" className={styles.labelStyle}>
                  {t.ExecutorGroups.departments}
                </label>
                <SelectDepartment
                  id="departments-select"
                  name="departments"
                  key={`${orgIds?.join('-')}-${isEditable}`}
                  multiple
                  placeholder={isDisabled ? 'Сначала выберите организацию' : 'Введите от 9 символов для поиска'}
                  disabled={isDisabled}
                  value={localDepartments}
                  onChange={onChange}
                  extraParams={orgIds ? { organizations: orgIds } : undefined}
                  skipInitialRequest={skipInitialRequest}
                  isOpen={isOpen}
                  initialDepartmentIds={!isEditable && departments ? departments : undefined}
                  onDepartmentsLoad={updateDepartmentsCache}
                />
              </div>
            </Form.Item>
          );
        },
      },
      {
        name: 'geoZones',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: t.ExecutorGroups.geoZones,
        placeholder: 'Выберите геозоны',
        disabled: !isEditable,
        maxLength: 128,
        options: geoZoneOptions,
        isNewDesign: true,
        mode: 'multiple',
      },
      {
        name: 'customers',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        disabled: !isEditable,
        label: t.ExecutorGroups.customers,
        placeholder: 'Укажите ФИО или табельный номер',
        maxLength: 128,
        isNewDesign: true,
      },
      {
        name: 'contractors',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: t.ExecutorGroups.contractor,
        placeholder: 'Укажите контрагента',
        disabled: !isEditable && !isEditMode,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: 'Пожалуйста, выберите контрагента',
          },
        ],
        options: contractors,
        isNewDesign: true,
        mode: 'multiple',
      },
      {
        name: 'additionalFeature',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        disabled: !isEditable && !isEditMode,
        label: t.ExecutorGroups.additionalFeature,
        placeholder: 'Укажите дополнительный признак',
        maxLength: 64,
        isNewDesign: true,
      },
    ],
    [t, isEditable, organizationOptions, geoZoneOptions, contractors, orgIds, departments, updateDepartmentsCache]
  );

  return {
    parameterFields,
    customerFields,
    localDepartments,
  };
};
