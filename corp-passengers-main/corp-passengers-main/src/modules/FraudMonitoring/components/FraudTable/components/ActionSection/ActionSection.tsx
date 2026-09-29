import React, { useMemo } from 'react';
import { Form } from 'antd';
import { useForm } from 'antd/es/form/Form';
import { useTranslation } from 'i18n';

import Input from 'shared/components/Inputs/Input/Input';
import { useModalState } from 'shared/hooks/useModal';

import FilterButton from 'components/FilterButton/FilterButton';
import { FilterModal } from './components/FilterModal/FilterModal';

import { formValues } from './constants';

import { ActionSectionProps, FilterFormValues, SearchFormValues } from './types';

import styles from './styles.module.scss';

const { humanReadableId } = formValues;

export const ActionSection = ({
  filtersCount, filters, setFilters, resetFilters,
}: ActionSectionProps) => {
  const [searchForm] = useForm<SearchFormValues>();
  const [isFilterModalOpened, { show: showFilterModal, hide: hideFilterModal }] = useModalState();
  const { t } = useTranslation();

  const handleSearchFormSubmit = (searchFormValues: SearchFormValues) => {
    setFilters({
      ...searchFormValues,
    });
  };

  const handleFilterFormSubmit = (filterFormValues: FilterFormValues) => {
    setFilters({ ...filterFormValues });
  };

  const handleResetFilters = () => {
    resetFilters();
    hideFilterModal();
  };

  const handleCloseFilterModal = () => {
    hideFilterModal();
  };

  const filterModalInitialValues = useMemo<FilterFormValues>(
    () => ({
      ...filters,
    }),
    [filters]
  );

  return (
    <div className={styles.container}>
      <Form form={searchForm} onFinish={handleSearchFormSubmit}>
        <Form.Item name={humanReadableId}>
          <Input.Search
            onPressEnter={searchForm.submit}
            placeholder={t.Forms.fraudFilterFields.searchByOrderNumber}
            onSearch={searchForm.submit}
          />
        </Form.Item>
      </Form>

      <FilterButton
        title={t.global.filters}
        onClick={showFilterModal}
        filterCount={filtersCount}
      />

      {isFilterModalOpened && (
        <FilterModal
          initialValues={filterModalInitialValues}
          isOpened={isFilterModalOpened}
          onClose={handleCloseFilterModal}
          onReset={handleResetFilters}
          onSubmit={handleFilterFormSubmit}
        />
      )}
    </div>
  );
};
