import { useParams } from 'react-router-dom';
import { LabeledValue } from 'antd/lib/select';

import { tariffStatusOptions } from '../../constants/constants';
import { TariffTypes } from 'constants/constants.app';
import { useOrganizationProjection } from 'api/organizations/search';
import { useGetAvailableTransportTypes, useTransportServiceTypes } from 'api/transport-types';
import { useSelectContractors } from 'api/contractors';
import { useProfile } from 'api/profile';
import { useGeoZones } from 'api/geo-zones';

import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TariffsHanbooksNames, TariffsHanbookTitles } from 'modules/NewTariffs/constants/Tariffs.constants';
import { ServiceTypeEnum } from 'modules/Departments/DelegatesPage/constants/EmployeeApp.constants';

export const useFilterFields = () => {
  const { tariffType } = useParams<{ tariffType: TariffTypes }>();
  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const contractors = useSelectContractors({ suspense: false }).data?.contractors;
  const { organizationId } = useProfile().data;
  const transportTypes = useGetAvailableTransportTypes(organizationId, { suspense: false }).data;
  const filteredTransportTypes = transportTypes?.filter(({ name }) => tariffType ? name === TransportTypes.TAXI : true);

  const transportServiceTypes = useTransportServiceTypes({ suspense: false }).data;

  const isInCome = tariffType === TariffTypes.INCOME;

  const contractorOptions: LabeledValue[] = contractors?.map(el => ({ label: el.name, value: el.id })) ?? [];
  const transportTypeOptions: LabeledValue[]
    = filteredTransportTypes?.map(el => ({ label: el.rusName, value: el.name })) ?? [];
  const organizationOptions: LabeledValue[]
    = organizations?.map(el => ({ label: el.officialName, value: el.id })) ?? [];
  const transportServiceTypeOptions: LabeledValue[]
    = transportServiceTypes
      ?.filter(({ name }) => name === ServiceTypeEnum.EMPLOYEE_TRANSPORTATION)
      .map(el => ({
        label: el.rusName,
        value: el.name,
      })) ?? [];

  const nightTariffOptions: LabeledValue[] = [{ label: 'Ночной тариф', value: 'true' }];

  const { data: geoZones } = useGeoZones({ suspense: false });

  const filterFields: ModelFormFieldProps[] = [
    {
      fieldType: ModelFormFieldType.SELECT,
      name: isInCome ? TariffsHanbooksNames.organizationId : TariffsHanbooksNames.contractorId,
      label: isInCome ? TariffsHanbookTitles.clientOrganization : TariffsHanbookTitles.contractor,
      editable: true,
      options: isInCome ? organizationOptions : contractorOptions,
      isNewDesign: true,
      allowClear: true,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.serviceType,
      label: TariffsHanbookTitles.serviceType,
      editable: true,
      allowClear: true,
      options: transportServiceTypeOptions,
      isNewDesign: true,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.transportType,
      label: TariffsHanbookTitles.transportType,
      editable: true,
      options: transportTypeOptions,
      isNewDesign: true,
      allowClear: true,
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: TariffsHanbooksNames.humanReadableId,
      label: TariffsHanbookTitles.humanReadableId,
      editable: true,
      isNewDesign: true,
      allowClear: true,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.active,
      label: TariffsHanbookTitles.active,
      editable: true,
      options: tariffStatusOptions,
      isNewDesign: true,
      allowClear: true,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      name: TariffsHanbooksNames.regionId,
      label: TariffsHanbookTitles.regionTariffId,
      editable: true,
      options: geoZones?.map(item => ({ label: item.name, value: item.id })) ?? [],
      isNewDesign: true,
      allowClear: true,
    },
    {
      fieldType: ModelFormFieldType.CHECKBOX_GROUP,
      name: TariffsHanbooksNames.isNightTariff,
      label: '',
      editable: true,
      options: nightTariffOptions,
      isNewDesign: true,
    },
  ];

  return filterFields;
};
