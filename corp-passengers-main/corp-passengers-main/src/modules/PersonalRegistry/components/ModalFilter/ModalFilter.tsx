/* eslint-disable @typescript-eslint/no-explicit-any */
import { Button } from 'antd';
import { useTranslation } from 'i18n';
import React, {
  Dispatch,
  FC, Fragment, SetStateAction, useEffect, useState
} from 'react';
import { FormInstance } from 'antd/es/form';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';

import {
  PersonalRegistryFilters,
  PersonalSearchResponse,
  PersonalUIVisibilityDTO,
  PersonalSearchQuery
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import Filters from './Filters/Filters';
import { ActiveStatusChangeButtons } from '../ActiveStatusChangeButtons/ActiveStatusChangeButtons';
import { CompensationModal } from 'modules/Registry/components/Modals/CompensationModal/CompensationModal';
import { processRequestParamsOnXlsDownloadPersonal, processSearchSubmitJson } from '../../utils';
import { RequestBodyPersonalParams } from '../../types/types';
import { UsersAttributes } from 'api/register-search';
import { PaymentStateResponse } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { useProfile } from 'api/profile';

interface SelectedRowData {
  requestId: string;
  payed: boolean;
}

export const ModalFilter: FC<{
  onFinish: Dispatch<any>;
  isOrgStructureRole: boolean;
  transportType: 'personal' | 'public' | 'taxi' | 'cargo';
  filters: PersonalRegistryFilters;
  userColumnVisibilitySettings: UsersAttributes | undefined;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  selectedRowData: SelectedRowData[];
  savePaymentStatuses: (data: SelectedRowData[]) => Promise<PaymentStateResponse | undefined>;
  isSavingStatuses: boolean;
  handleChangeStatusClick: () => void;
  responseData: PersonalSearchResponse;
  hashTariffs: Record<string, string>;
  setHashTariffs: Dispatch<SetStateAction<Record<string, string>>>;
}> = ({
  onFinish,
  isOrgStructureRole,
  transportType,
  filters,
  userColumnVisibilitySettings,
  isStatusChangeActive,
  setStatusChangeActive,
  selectedRowData,
  savePaymentStatuses,
  isSavingStatuses,
  handleChangeStatusClick,
  responseData,
  hashTariffs,
  setHashTariffs,
}) => {
  const { t } = useTranslation();
  const [form, setForm] = useState<FormInstance<any>>();
  const [isVisible, setIsVisible] = useState(false);
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const { isOrganization } = useProfile().data;

  const handleChangeId = (id: string) => {
    if (id.length <= 16) {
      setIdValue(id);
    }
  };

  useEffect(() => {
    setIdValue(form?.getFieldValue('requestHumanId'));
  }, [form?.getFieldValue('requestHumanId')]);

  const handleSearchId = (id: string) => {
    form?.resetFields();
    form?.setFieldsValue({ requestHumanId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
      onFinish({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const handleStatusUpdate = (data: PaymentStateResponse) => responseData.content?.forEach(item => {
    const row = data.find(el => el.requestId === item.id);
    if (row) {
      item.status = row.status;
    }
  });

  const renderActiveStatusChangeButtons = (): JSX.Element | boolean => isStatusChangeActive && (
    <ActiveStatusChangeButtons
      setStatusChangeActive={setStatusChangeActive}
      selectedRowData={selectedRowData}
      savePaymentStatuses={savePaymentStatuses}
      isSavingStatuses={isSavingStatuses}
      handleStatusUpdate={handleStatusUpdate}
      onFinish={onFinish}
      form={form}
    />
  );

  const renderChangeStatusButton = (): JSX.Element | boolean => isOrgStructureRole
    && !isStatusChangeActive && (
      <Button
        type="primary"
        danger={isStatusChangeActive}
        onClick={handleChangeStatusClick}
        disabled={!isOrganization}
      >
        {t.Forms.registryFilterFields.paidStatusButton}
      </Button>
  );

  return (
    <ActionsRegistry
      isNewDesign
      idValue={idValue}
      handleSearchId={handleSearchId}
      handleChangeId={handleChangeId}
      isVisible={isVisible}
      setIsVisible={setIsVisible}
      filters={isVisible && (
        <Filters
          submitBtnName={t.global.confirm}
          onApplyFilters={onFinish}
          form={form}
          setForm={setForm}
          setIsVisible={setIsVisible}
          filterValues={filters}
          hashTariffs={hashTariffs}
          setHashTariffs={setHashTariffs}
        />
      )}
      buttons={[
        <Fragment key="isOrgStructureRole">
          {isOrgStructureRole && !isStatusChangeActive && (
          <CompensationModal<PersonalRegistryFilters, Partial<RequestBodyPersonalParams>, PersonalUIVisibilityDTO>
            transportType={transportType}
            filterParams={processSearchSubmitJson(filters as PersonalSearchQuery)}
            columnsVisibility={userColumnVisibilitySettings?.personalUIVisibility}
            mimeType={XLSX_MIME_TYPE}
            onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPersonal}
            onFinish={onFinish}
            form={form}
          />
          )}
        </Fragment>,
        renderActiveStatusChangeButtons(),
        renderChangeStatusButton(),
      ]}
    />
  );
};
