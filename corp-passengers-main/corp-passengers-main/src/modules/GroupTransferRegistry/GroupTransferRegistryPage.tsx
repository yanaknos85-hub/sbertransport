import React, {
  Suspense,
  useState,
  useRef,
  useEffect
} from 'react';
import type { FC } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { FormInstance } from 'antd/lib/form/Form';
import { PageOptions, GroupTransferRegistryFilter } from './types/types';
import Filters from './Components/Filters/Filters';
import { GroupTransferRegistryTable } from './GroupTransferRegistryTable';
import { filter2searchQuery } from './utils';
import { ActionsRegistry } from '../Registry/components/ActionsRegistry/ActionsRegistry';
import { useTranslation } from 'i18n';
import { useProfile } from 'api/profile';

interface OwnProp {
  filterValues: GroupTransferRegistryFilter | undefined;
  setFilterValues: React.Dispatch<React.SetStateAction<GroupTransferRegistryFilter | undefined>>;
  pageOptions: PageOptions;
  resetPagination(): void;
}

export const GroupTransferRegistryPage: FC<OwnProp> = ({
  filterValues, setFilterValues, pageOptions, resetPagination,
}) => {
  const onSubmit = (values: GroupTransferRegistryFilter) => setFilterValues(values);
  const [form, setForm] = useState<FormInstance>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const { t } = useTranslation();
  const { userId } = useProfile().data;

  const handleSearchId = (id: string) => {
    form?.resetFields();
    form?.setFieldsValue({ requestHumanId: id });

    if (((id.length > 2) && (id.length < 20)) || !id) {
      onSubmit({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const [isVisible, setIsVisible] = useState(false);

  const buttonsRef = useRef<HTMLDivElement>(null);

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
      />
      <Suspense fallback={<SpinWrapped />}>
        {filterValues && (
          <GroupTransferRegistryTable
            filterValues={filter2searchQuery(filterValues)}
            pageOptions={pageOptions}
            buttonsRef={buttonsRef}
            userId={userId}
          />
        )}
      </Suspense>
    </>
  );
};
