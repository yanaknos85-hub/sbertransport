import { LabeledValue } from 'antd/lib/select';
import { useGetListRegions } from 'api/tariffs';
import React from 'react';
import { FilterPanel } from 'shared/components/FilterPanel';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { searchSymbol } from 'utils/searchSymbol';

import { ExpandedFiltersNames, ExpandedFiltersTitles } from '../Departments.constants';

const useFilterFields: () => ModelFormFieldProps[] = () => {
  const geoZonesList = useGetListRegions().data;

  const geoZonesOptions: LabeledValue[] = Array.from(new Set(geoZonesList.map(({ name }) => name))).map(name => ({
    label: name,
    value: name,
  }));

  const filterFields: ModelFormFieldProps[] = [
    {
      fieldType: ModelFormFieldType.TEXT,
      name: ExpandedFiltersNames.humanReadableId,
      label: ExpandedFiltersTitles.humanReadableId,
      editable: true,
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: ExpandedFiltersNames.code,
      label: ExpandedFiltersTitles.code,
      editable: true,
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: ExpandedFiltersNames.departmentName,
      label: ExpandedFiltersTitles.departmentName,
      editable: true,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      options: [
        { label: 'Активно', value: 'ACTIVE' },
        { label: 'Удалено', value: 'INACTIVE' },
      ],
      name: ExpandedFiltersNames.status,
      label: ExpandedFiltersTitles.status,
      editable: true,
    },
    {
      fieldType: ModelFormFieldType.SELECT,
      options: geoZonesOptions,
      name: ExpandedFiltersNames.location,
      label: ExpandedFiltersTitles.location,
      showSearch: true,
      filterOption: searchSymbol,
      editable: true,
    },
  ];
  return filterFields;
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const Filters: React.FC<{ acceptFilters: React.Dispatch<any> }> = ({ acceptFilters }) => (
  <FilterPanel
    fields={useFilterFields()}
    onApplyFilters={acceptFilters}
    isStatusChangeActive
  />
);
