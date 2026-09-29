import React, { useState } from 'react';
import type { FC, Dispatch, SetStateAction } from 'react';
import type { FormInstance } from 'antd/es/form';

import { clearButtonActive } from 'utils/reportsUtils';
import { FilterPanel } from 'shared/components/FilterPanel';
import type { FilterPanelProps } from 'shared/components/FilterPanel/types';
import { useFilterFields } from 'modules/CarSharingRegistry/hooks/useFilterFields';

type Props = Pick<FilterPanelProps, 'submitBtnName' | 'onApplyFilters' | 'resetPagination' | 'filterValues'
  | 'setForm' | 'setIsVisible'>
  & {
    form: FormInstance | undefined;
    hashTariffs: Record<string, string>;
    setHashTariffs: Dispatch<SetStateAction<Record<string, string>>>;
  };

const Filters: FC<Props> = props => {
  const [isStatusChangeActiveFilter, setStatusChangeActiveFilter] = useState(true);
  const onClearButtonActivator = () => {
    const values = props.form?.getFieldsValue(true);

    setStatusChangeActiveFilter(clearButtonActive(values));
  };
  const fields = useFilterFields(onClearButtonActivator, props.hashTariffs, props.setHashTariffs);

  return (
    <FilterPanel
      {...props}
      isNewDesign
      withFormSection
      initialSearch
      filterValues={props.filterValues}
      fields={fields}
      isStatusChangeActive={isStatusChangeActiveFilter}
      setStatusChangeActive={setStatusChangeActiveFilter}
      onClearButtonActivator={onClearButtonActivator}
    />
  );
};

export default Filters;
