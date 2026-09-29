import React, {
  Dispatch, SetStateAction, Suspense, useEffect, useState
} from 'react';
import type { FC } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { ToolbarElement } from 'components/Toolbar';
import { FormInstance } from 'antd/lib/form/Form';
import { PageOptions, TaxiRegistryFilter } from './types/types';
import { TaxiRegistryTable } from './TaxiRegistryTable';
import Filters from './Components/Filters/Filters';
import { UploadRegistry } from './Components/UploadRegistry/UploadRegistry';
import { filter2searchQuery } from './utils';
import { ActionsRegistry } from '../Registry/components/ActionsRegistry/ActionsRegistry';
import { useTranslation } from 'i18n';

interface OwnProp {
  filterValues: TaxiRegistryFilter | undefined;
  setFilterValues: React.Dispatch<React.SetStateAction<TaxiRegistryFilter | undefined>>;
  pageOptions: PageOptions;
  resetPagination(): void;
  hashTariffs: Record<string, string>;
  setHashTariffs: Dispatch<SetStateAction<Record<string, string>>>;
}

export const TaxiRegistryPage: FC<OwnProp> = ({
  filterValues,
  setFilterValues,
  pageOptions,
  resetPagination,
  hashTariffs,
  setHashTariffs,
}) => {
  const onSubmit = (values: TaxiRegistryFilter) => setFilterValues(values);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const { t } = useTranslation();

  const handleSearchId = (id: string) => {
    form?.resetFields();
    form?.setFieldsValue({ requestHumanId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
      onSubmit({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const [isVisible, setIsVisible] = useState(false);

  const buttonsRef = React.useRef<HTMLDivElement>(null);

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
      <ToolbarElement>
        <UploadRegistry />
      </ToolbarElement>

      <ActionsRegistry
        isNewDesign
        idValue={idValue}
        handleChangeId={handleChangeId}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        buttonsRef={buttonsRef}
        filters={isVisible && (
          <Filters
            submitBtnName={t.global.confirm}
            form={form}
            setForm={setForm}
            onApplyFilters={onSubmit}
            filterValues={filterValues}
            resetPagination={resetPagination}
            setIsVisible={setIsVisible}
            hashTariffs={hashTariffs}
            setHashTariffs={setHashTariffs}
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
