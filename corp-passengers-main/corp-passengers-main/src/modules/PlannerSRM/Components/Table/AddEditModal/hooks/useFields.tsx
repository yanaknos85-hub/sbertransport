import { useTranslation } from 'i18n';
import { useOrganizationProjection } from 'api/organizations/search';
import { useSelectDepartmentsSearch } from 'api/departments';
import { useProfile } from 'api/profile';
import { useGeoZones } from 'api/geo-zones';
import { LabeledValue } from 'antd/lib/select';
import { useGetEmployeesByOrgAndDep } from 'api/executor-group';

import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { preventDefault } from 'utils';
import { useMemo, useState, useEffect } from 'react';
import { UUID } from 'utils/io-ts';

export const useFields = (isEditable: boolean, orgId: UUID[], depsId: UUID[], orgExecId: UUID) => {
  const [departmentOptions, setDepartmentOptions] = useState<LabeledValue[]>([]);
  const [employeeOptions, setEmployeeOptions] = useState<LabeledValue[]>([]);
  const [customerOptions, setCustomerOptions] = useState<LabeledValue[]>([]);
  const { organizationId } = useProfile().data;
  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const [getDepartments] = useSelectDepartmentsSearch(orgId[orgId.length - 1] || organizationId);
  const geoZones = useGeoZones({ suspense: true })?.data;
  const [getEmployeesByOrgAndDep, { isLoading }] = useGetEmployeesByOrgAndDep();

  const organizationOptions: LabeledValue[]
    = organizations?.map(el => ({ label: el.officialName, value: el.id })) ?? [];
  const geoZoneOptions: LabeledValue[] = typeof geoZones !== 'string' ? geoZones.map(el => ({ label: el.name, value: el.id })) : [];
  const { t } = useTranslation();

  const [departments, setDepartments] = useState(new Map());

  useEffect(() => {
    if (orgExecId && isEditable) {
      getEmployeesByOrgAndDep({
        organizations: [orgExecId],
      }).then(resp => {
        setEmployeeOptions(resp ? resp?.map(el => ({ label: `${el.firstName} ${el.lastName} ${el.patronymic} (${el.personnelNumber})`, value: el.id })) : []);
      }).catch(() => setEmployeeOptions(() => []));
    }
  }, [orgExecId, isEditable]);

  useEffect(() => {
    if (orgId.length && isEditable) {
      const department = new Map();
      getDepartments({ page: 0, departmentName: undefined }).then(resp => {
        if (resp?.content) {
          department.set(orgId[orgId.length - 1], resp?.content.map(
            el => ({ label: el.humanReadableId, value: el.id }))
          );
        }

        orgId.forEach(item => {
          if (departments.has(item)) {
            department.set(item, departments.get(item));
          }
        });
        setDepartments(new Map(department));

        setDepartmentOptions(Array.from(department.values()).flat());
        setDepartments(new Map(department));
      });
    } else if (!orgId.length) {
      setDepartments(new Map());
      setDepartmentOptions([]);
    }
  }, [orgId.length, isEditable]);

  useEffect(() => {
    if ((orgId.length || depsId.length) && isEditable) {
      getEmployeesByOrgAndDep({ organizations: orgId, departments: depsId }).then(resp => {
        setCustomerOptions(resp ? resp.map(el => ({ label: `${el.firstName} ${el.lastName} ${el.patronymic} (${el.personnelNumber})`, value: el.id })) : []);
      }).catch(() => setCustomerOptions(() => []));
    }
  }, [orgId.length, depsId.length, isEditable]);

  const parameterFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'name',
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        editable: true,
        label: 'Наименование группы',
        placeholder: 'Пример: ЦЧБ/Орловское отделение №0000/Пассажирские перевозки',
        maxLength: 128,
        rules: [
          {
            required: true,
            message: 'Пожалуйста, введите наименование группы',
          },
          {
            // @ts-ignore
            validator: (_, value): Promise<void> => value.match('^(?=.{1,150}$)[\\sа-яА-Я-]+/[\\sа-яА-Я0-9№-]+/[\\sа-яА-Я]+$') ? Promise.resolve() : Promise.reject('Введенный текст не соответствует маске шаблона: ЦЧБ/Орловское отделение №0000/Пассажирские перевозки'),
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'organizationId',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Организация группы исполнителей',
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
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'ФИО исполнителей',
        placeholder: 'Укажите исполнителей',
        disabled: !isEditable,
        mode: 'multiple',
        maxLength: 128,
        options: !isLoading ? employeeOptions : [],
        rules: [
          {
            required: true,
            message: 'Пожалуйста, выберите исполнителей из списка',
          },
        ],
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        name: 'serviceLevel',
        editable: true,
        label: 'Уровень сервиса',
        placeholder: 'Укажите уникальное значение',
        disabled: !isEditable,
        onPressEnter: preventDefault,
        maxLength: 128,
        isNewDesign: true,
      },
    ],
    [t, isEditable, organizationOptions, employeeOptions]
  );

  const customerFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'organizations',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Организация',
        placeholder: 'Выберите организацию',
        header: t.contractors.dispatcherTitle,
        disabled: !isEditable,
        noHeaderPadding: true,
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
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Подразделения',
        placeholder: 'Выберите подразделения',
        disabled: !isEditable,
        maxLength: 128,
        options: departmentOptions,
        isNewDesign: true,
        mode: 'multiple',
      },
      {
        name: 'geoZones',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Геозона',
        placeholder: 'Выберите геозоны',
        disabled: !isEditable,
        maxLength: 128,
        options: geoZoneOptions,
        isNewDesign: true,
        mode: 'multiple',
      },
      {
        name: 'customers',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'ФИО заказчика',
        placeholder: 'Укажите ФИО или табельный номер',
        disabled: !isEditable,
        options: !isLoading ? customerOptions : [],
        maxLength: 128,
        isNewDesign: true,
      },
    ],
    [t, isEditable, organizationOptions, departmentOptions, geoZoneOptions, customerOptions]
  );

  return {
    parameterFields,
    customerFields,
  };
};
