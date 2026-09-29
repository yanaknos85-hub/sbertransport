import React, { useState, Suspense, useEffect } from 'react';
import type { FC } from 'react';
import type { FormInstance } from 'antd/lib/form/Form';

import { useTranslation } from 'i18n';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { usePagination } from 'shared/hooks/usePagination';
import { CarSharingReportFilters, CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { CarSharingRegistryTable } from './components/CarSharingRegistryTable';
import Filters from './components/Filters/Filters';
import { ActionsRegistry } from '../Registry/components/ActionsRegistry/ActionsRegistry';
import { useRegistryFilters } from '../Registry/RegistryFilterContext';
import { useProfile } from 'api/profile';

const CarSharingRegistry: FC = () => {
  const { t } = useTranslation();
  const { filterValues, setFilterValues } = useRegistryFilters<CarSharingReportFilters>();
  const [hashTariffs, setHashTariffs] = useState<Record<string, string>>({});
  const {
    pageSetting, onPaginationChange, resetPagination,
  } = usePagination(true);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const [isVisible, setIsVisible] = useState(false);
  const { userId } = useProfile().data;

  const pageOptions = {
    pageSetting,
    onPaginationChange,
  };

  const onFinish = (data: CarSharingSearchQuery) => setFilterValues(data as CarSharingReportFilters);

  const handleSearchId = (id: string) => {
    form?.resetFields();
    form?.setFieldsValue({ requestHumanId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
      resetPagination();
      onFinish({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const handleChangeId = (id: string) => {
    if (id.length <= 16) {
      setIdValue(id);
    }
  };

  useEffect(() => {
    setIdValue(form?.getFieldValue('requestHumanId'));
  }, [form?.getFieldValue('requestHumanId')]);

  return (
    <>
      <ActionsRegistry
        isNewDesign
        idValue={idValue}
        handleChangeId={handleChangeId}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        filters={isVisible && (
          <Filters
            submitBtnName={t.global.confirm}
            form={form}
            setForm={setForm}
            onApplyFilters={onFinish}
            resetPagination={resetPagination}
            setIsVisible={setIsVisible}
            filterValues={filterValues}
            hashTariffs={hashTariffs}
            setHashTariffs={setHashTariffs}
          />
        )}
      />
      <Suspense fallback={<SpinWrapped />}>
        {filterValues && (
          <CarSharingRegistryTable
            pageOptions={pageOptions}
            userId={userId}
          />
        )}
      </Suspense>
    </>
  );
};

export default CarSharingRegistry;
