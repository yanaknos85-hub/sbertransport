import React, {
  Suspense,
  useEffect,
  useState,
  useRef
} from 'react';
import type { FC } from 'react';
import { observer } from 'mobx-react';
import { FormInstance } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PageOptions, TaxiYandexRegistryFilter } from './types/types';
import Filters from './components/Filters/Filters';
import { TaxiYandexRegistryTable } from './TaxiYandexRegistryTable';
import { ActionsRegistry } from '../Registry/components/ActionsRegistry/ActionsRegistry';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

interface OwnProp {
  filterValues: TaxiYandexRegistryFilter;
  setFilterValues: React.Dispatch<React.SetStateAction<TaxiYandexRegistryFilter>>;
  pageOptions: PageOptions;
  resetPagination(): void;
}

export const TaxiYandexRegistryPage: FC<OwnProp> = observer(({
  filterValues, setFilterValues, pageOptions, resetPagination,
}) => {
  const onSubmit = (values: TaxiYandexRegistryFilter) => {
    setFilterValues(values);
    pageOptions.onPaginationChange(1, 20);
  };

  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  const [isStatusChangeActive, setStatusChangeActive] = useState(false);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('humanReadableId'));
  const { t } = useTranslation();

  const handleSearchId = (id: string) => {
    form?.resetFields();
    form?.setFieldsValue({ humanReadableId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
      onSubmit({ ...form?.getFieldsValue(true), humanReadableId: id });
    }
  };

  const [isVisible, setIsVisible] = useState(false);

  const buttonsRef = useRef<HTMLDivElement>(null);
  const actionButtonsRef = useRef<HTMLDivElement>(null);

  const handleChangeId = (id: string) => {
    if (id.length <= 16) {
      setIdValue(id);
    }
  };

  useEffect(() => {
    setIdValue(form?.getFieldValue('humanReadableId'));
  }, [form?.getFieldValue('humanReadableId')]);

  useEffect(() => {
    !isStatusChangeActive && register.clearSelectedRowData();
  }, [isStatusChangeActive]);

  return (
    <>
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
          />
        )}
        buttons={[
          <div
            style={{
              gap: '12px', display: 'flex', justifyContent: 'flex-end',
            }}
            ref={actionButtonsRef}
          />,
        ]}
      />

      <Suspense fallback={<SpinWrapped />}>
        <TaxiYandexRegistryTable
          onSubmit={onSubmit}
          filters={filterValues}
          pageOptions={pageOptions}
          buttonsRef={buttonsRef}
          actionButtonsRef={actionButtonsRef}
          isStatusChangeActive={isStatusChangeActive}
          setStatusChangeActive={setStatusChangeActive}
          form={form}
        />
      </Suspense>
    </>
  );
});
