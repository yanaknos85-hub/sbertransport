import { useState, useEffect, useMemo } from 'react';
import { useTranslation } from 'i18n';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { preventDefault } from 'utils';
import { useOrganizationProjection } from 'api/organizations/search';
import { LabeledValue } from 'antd/lib/select';
import { useGetAllEmployees } from 'api/executor-group';
import { useSelectDepartmentsSearch } from 'api/departments';
import { useGeoZones } from 'api/geo-zones';
import { useProfile } from 'api/profile';
import { UUID } from 'utils/io-ts';

export const useFilterFields = (orgExecId: UUID, orgId?: UUID) => {
  const [employeeOptions, setEmployeeOptions] = useState<LabeledValue[]>([]);
  const [departmentOptions, setDepartmentOptions] = useState<LabeledValue[]>([]);
  const { organizationId } = useProfile().data;
  const { t } = useTranslation();
  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const [getDepartments] = useSelectDepartmentsSearch(orgId || organizationId);
  const [getEmployees, { isLoading }] = useGetAllEmployees(orgExecId || organizationId);
  const geoZones = useGeoZones({ suspense: true })?.data;

  const organizationOptions: LabeledValue[]
    = organizations?.map(el => ({ label: el.officialName, value: el.id })) ?? [];
  const geoZoneOptions: LabeledValue[] = typeof geoZones !== 'string' ? geoZones.map(el => ({ label: el.name, value: el.id })) : [];

  useEffect(() => {
    if (orgExecId) {
      getEmployees({
        departmentName: undefined,
      }).then(resp => {
        setEmployeeOptions(resp?.content ? resp?.content.map(el => ({ label: `${el.firstName} ${el.lastName} ${el.patronymic} (${el.personnelNumber})`, value: el.lastName })) : []);
      });
    }
  }, [orgExecId]);

  useEffect(() => {
    if (orgId) {
      getDepartments({ page: 0, departmentName: undefined }).then(resp => {
        setDepartmentOptions(resp?.content
          ? resp?.content.map(el => ({ label: el.humanReadableId, value: el.id }))
          : []);
      });
    }
  }, [orgId]);

  const parameterFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'executorGroupName',
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        editable: true,
        label: 'Наименование группы исполнителей',
        placeholder: 'Пример: ЦЧБ/Орловское отделение №0000/Пассажирские перевозки',
        maxLength: 128,
        isNewDesign: true,
      },
      {
        name: 'executorOrganizations',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Организация группы исполнителей',
        placeholder: 'Выберите организацию',
        maxLength: 128,
        options: organizationOptions,
        isNewDesign: true,
      },
      {
        name: 'executorFIO',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'ФИО исполнителей',
        placeholder: 'Укажите исполнителей',
        mode: 'multiple',
        maxLength: 128,
        options: !isLoading ? employeeOptions : [],
        isNewDesign: true,
      },
    ],
    [t, organizationOptions, employeeOptions]
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
        noHeaderPadding: true,
        maxLength: 128,
        options: organizationOptions,
        isNewDesign: true,
      },
      {
        name: 'executorDepartments',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Подразделения',
        placeholder: 'Выберите подразделения',
        maxLength: 128,
        options: departmentOptions,
        isNewDesign: true,
        mode: 'multiple',
      },
      {
        name: 'executorGeoZones',
        fieldType: ModelFormFieldType.SELECT,
        editable: true,
        label: 'Геозона',
        placeholder: 'Выберите геозоны',
        maxLength: 128,
        options: geoZoneOptions,
        isNewDesign: true,
        mode: 'multiple',
      },
    ],
    [t, organizationOptions, departmentOptions]
  );

  return {
    parameterFields,
    customerFields,
  };
};
