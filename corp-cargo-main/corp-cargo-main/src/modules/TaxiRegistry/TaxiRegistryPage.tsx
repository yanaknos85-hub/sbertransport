import React, { FC, Suspense, useState } from 'react';
import { FilterPanel } from 'shared/components/FilterPanel';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { ToolbarElement } from 'components/Toolbar';
import { FormInstance } from 'antd/lib/form/Form';
import { clearButtonActive } from 'utils/reportsUtils';
import { useFilterFields } from './hooks/useFilterFields';
import { PageOptions, TaxiRegistryFilter } from './types/types';
import { TaxiRegistryTable } from './TaxiRegistryTable';
import { UploadRegistry } from './Components/UploadRegistry/UploadRegistry';
import { filter2searchQuery } from './utils';
import { ActionsRegistry } from '../Registry/components/ActionsRegistry/ActionsRegistry';
import { useTranslation } from 'i18n';

interface OwnProp {
  filterValues: TaxiRegistryFilter | undefined;
  setFilterValues: React.Dispatch<React.SetStateAction<TaxiRegistryFilter | undefined>>;
  pageOptions: PageOptions;
  resetPagination(): void;
}

export const TaxiRegistryPage: FC<OwnProp> = ({
  filterValues, setFilterValues, pageOptions, resetPagination,
}) => {
  const onSubmit = (values: TaxiRegistryFilter) => setFilterValues(values);
  const [isStatusChangeActive, setStatusChangeActive] = useState(true);
  const [form, setForm] = useState<FormInstance<any>>();
  const { t } = useTranslation();

  const onClearButtonActivator = (e?: any) => {
    const values = form?.getFieldsValue(true);
    setStatusChangeActive(clearButtonActive(values));
  };

  const fields = useFilterFields(onClearButtonActivator);

  const handleSearchId = (id: string) => {
    form?.setFieldsValue({ requestHumanId: id });

    if (id.length > 2 && id.length < 20 || !id) {
      onSubmit({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const [isVisible, setIsVisible] = useState(false);

  const buttonsRef = React.useRef<HTMLDivElement>(null);

  return (
    <>
      <ToolbarElement>
        <UploadRegistry />
      </ToolbarElement>

      <ActionsRegistry
        isNewDesign
        idValue={form?.getFieldValue('requestHumanId')}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        buttonsRef={buttonsRef}
        filters={(
          <FilterPanel
            withFormSection
            isNewDesign
            submitBtnName={t.global.confirm}
            fields={fields}
            setForm={setForm}
            onApplyFilters={onSubmit}
            filterValues={filterValues}
            resetPagination={resetPagination}
            isStatusChangeActive={isStatusChangeActive}
            setStatusChangeActive={setStatusChangeActive}
            onClearButtonActivator={onClearButtonActivator}
            initialSearch
            setIsVisible={setIsVisible}
          />
        )}
      />
      <Suspense fallback={<SpinWrapped />}>
        {filterValues && (
          <TaxiRegistryTable
            filterValues={filter2searchQuery(filterValues)}
            pageOptions={pageOptions}
            buttonsRef={buttonsRef}
          />
        )}
      </Suspense>
    </>
  );
};
