import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useOrganizationProjection } from 'api/organizations/search';
import { useGetAvailableTransportTypes, useTransportServiceTypes } from 'api/transport-types';
import { useSelectContractors } from 'api/contractors';
import { LabeledValue } from 'antd/lib/select';
import { useProfile } from 'api/profile';
import { useGeoZones } from 'api/geo-zones';
import { ServiceTypeEnum } from 'modules/Departments/DelegatesPage/constants/EmployeeApp.constants';
import { TariffsHanbooksNames, TariffsHanbookTitles } from 'modules/NewTariffs/constants/Tariffs.constants';
import { tariffStatusOptions, taxiClassOptions } from '../../constants/constants';

export const useFilterFields = () => {
  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const contractors = useSelectContractors({ suspense: false }).data?.contractors;
  const { organizationId } = useProfile().data;
  const transportTypes = useGetAvailableTransportTypes(organizationId, { suspense: false }).data;
  const filteredTransportTypes = transportTypes;

  const transportServiceTypes = useTransportServiceTypes({ suspense: false }).data;

  const contractorOptions: LabeledValue[] = contractors?.map(el => ({ label: el.name, value: el.id })) ?? [];
  const transportTypeOptions: LabeledValue[]
    = filteredTransportTypes?.map(el => ({ label: el.rusName, value: el.name })) ?? [];
  const organizationOptions: LabeledValue[]
    = organizations?.map(el => ({ label: el.officialName, value: el.id })) ?? [];
  const transportServiceTypeOptions: LabeledValue[]
    = transportServiceTypes?.map(el => ({
      label: el.rusName,
      value: el.name,
    })) ?? [];

  // временное решение пока не уберут лишние типы транспорта на бэке
  const filterTransportTypesOptions = transportTypeOptions
    .filter(item => item.value !== 'DEDICATED'
    && item.value !== 'COURIER'
    && item.value !== 'DOMESTIC_COURIER'
    && item.value !== 'INTERREGIONAL'
    && item.value !== 'INDIVIDUAL'
    && item.value !== 'OFFICIAL'
    && item.value !== 'PRIVATE'
    && item.value !== 'SPECIAL'
    )
    .filter((item, index, self) => index === self.findIndex(t => t.value === item.value)
    );

  const filterTransportServiceTypeOptions = transportServiceTypeOptions.filter(item => item.value !== 'CARGO_TRANSPORTATION'
    && item.value !== 'REPAIR'
  );

  const nightTariffOptions: LabeledValue[] = [{ label: 'Ночной тариф', value: 'true' }];

  const { data: geoZones } = useGeoZones({ suspense: false });

  const filterFields: ModelFormFieldProps[] = [
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.organizationId,
      label: TariffsHanbookTitles.organizationId,
      editable: true,
      allowClear: true,
      options: organizationOptions,
      isNewDesign: true,
      placeholder: 'Выберите заказчика',
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.contractorId,
      label: TariffsHanbookTitles.contractorId,
      editable: true,
      allowClear: true,
      options: contractorOptions,
      isNewDesign: true,
      placeholder: 'Выберите контрагента',
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: TariffsHanbooksNames.contractNumber,
      label: TariffsHanbookTitles.contractNumber,
      editable: true,
      allowClear: true,
      isNewDesign: true,
      placeholder: 'Введите номер',
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.serviceType,
      label: TariffsHanbookTitles.serviceType,
      editable: true,
      options: filterTransportServiceTypeOptions,
      isNewDesign: true,
      placeholder: 'Выберите услугу',
      defaultValue: ServiceTypeEnum.EMPLOYEE_TRANSPORTATION,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.transportType,
      label: TariffsHanbookTitles.transportType,
      editable: true,
      allowClear: true,
      options: filterTransportTypesOptions,
      isNewDesign: true,
      placeholder: 'Выберите вид',
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.transportClass,
      label: TariffsHanbookTitles.transportClass,
      editable: true,
      allowClear: true,
      options: taxiClassOptions,
      isNewDesign: true,
      placeholder: 'Выберите класс',
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.regionId,
      label: TariffsHanbookTitles.regionId,
      editable: true,
      options: geoZones?.map(item => ({ label: item.name, value: item.id })) ?? [],
      isNewDesign: true,
      allowClear: true,
      placeholder: 'Выберите услугу',
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.active,
      label: TariffsHanbookTitles.active,
      editable: true,
      allowClear: true,
      options: tariffStatusOptions,
      isNewDesign: true,
      placeholder: 'Выберите статус',
    },
    {
      fieldType: ModelFormFieldType.CHECKBOX_GROUP,
      name: TariffsHanbooksNames.isNightTariff,
      label: '',
      editable: true,
      options: nightTariffOptions,
      isNewDesign: true,
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: TariffsHanbooksNames.humanReadableId,
      label: TariffsHanbookTitles.humanReadableId,
      editable: true,
      allowClear: true,
      isNewDesign: true,
      placeholder: 'Введите ID',
    },
  ];

  return filterFields;
};
