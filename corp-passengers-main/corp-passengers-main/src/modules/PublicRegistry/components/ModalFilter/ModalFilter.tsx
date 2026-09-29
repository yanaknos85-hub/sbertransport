/* eslint-disable @typescript-eslint/no-explicit-any */
import { Button } from 'antd';
import { useTranslation } from 'i18n';
import React, { Fragment, useEffect, useState } from 'react';
import type { Dispatch, FC, SetStateAction } from 'react';
import { FormInstance } from 'antd/es/form';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';

import {
  PaymentStateResponse,
  PublicRegistryFilters,
  PublicUIVisibilityDTO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import { ActiveStatusChangeButtons } from '../ActiveStatusChangeButtons/ActiveStatusChangeButtons';
import { CompensationModal } from 'modules/Registry/components/Modals/CompensationModal/CompensationModal';
import Filters from './Filters/Filters';
import { processRequestParamsOnXlsDownloadPublic, processSearchSubmitJson } from '../../utils/utils';
import { IResponseData, RequestBodyPublicParams, Filters as FilterParams } from '../../types/types';
import { UsersAttributes } from 'api/register-search';
import { useProfile } from 'api/profile';

interface SelectedRowData {
  requestId: string;
  payed: boolean;
}

export const ModalFilter: FC<{
  onFinish: Dispatch<any>;
  isOrgStructureRole: boolean;
  filters: PublicRegistryFilters;
  userColumnVisibilitySettings: UsersAttributes | undefined;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  selectedRowData: SelectedRowData[];
  savePaymentStatuses: (data: SelectedRowData[]) => Promise<PaymentStateResponse | undefined>;
  isSavingStatuses: boolean;
  handleChangeStatusClick: () => void;
  responseData: IResponseData;
  hashTariffs: Record<string, string>;
  setHashTariffs: Dispatch<SetStateAction<Record<string, string>>>;
}> = ({
  onFinish,
  isOrgStructureRole,
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

  const handleStatusUpdate = (data: PaymentStateResponse) => responseData.responseData.content?.forEach(item => {
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
          <CompensationModal<PublicRegistryFilters, Partial<RequestBodyPublicParams>, PublicUIVisibilityDTO>
            transportType="public"
            filterParams={processSearchSubmitJson(filters as FilterParams)}
            columnsVisibility={userColumnVisibilitySettings?.publicUIVisibility}
            mimeType={XLSX_MIME_TYPE}
            onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPublic}
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
