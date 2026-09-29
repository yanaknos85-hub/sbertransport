/* eslint-disable @typescript-eslint/no-explicit-any */
import { Button, type FormInstance } from 'antd';
import { useTranslation } from 'i18n';
import React, { Dispatch, Fragment, SetStateAction } from 'react';
import { ActiveStatusChangeButtons } from '../ActiveStatusChangeButtons/ActiveStatusChangeButtons';

import type { SelectedRowData } from '../ActiveStatusChangeButtons/ActiveStatusChangeButtons';
import type { PaymentStateResponse, YandexTaxiUpdateStatusesParams } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import { UsersAttributes } from 'api/register-search';
import { CompensationModal } from 'modules/Registry/components/Modals/CompensationModal/CompensationModal';
import { PublicUIVisibilityDTO } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { RequestBodyPublicParams } from 'modules/PublicRegistry/types/types';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { processPaymentYandexTaxiRequestQueryOnXlsDownload } from 'modules/TaxiYandexRegistry/utils/data';
import type { TableRecord, TaxiYandexRegistryFilter } from '../../types/types';

export const RegistryUpload = ({
  isStatusChangeActive,
  setStatusChangeActive,
  isOrgStructureRole,
  handleChangeStatusClick,
  isOrganization,
  selectedRowData,
  form,
  savePaymentStatuses,
  userColumnVisibilitySettings,
  isSavingStatuses,
  responseData,
  onFinish,
  filters,
  applyFilters,
}: {
  isOrgStructureRole: boolean;
  onFinish: () => void;
  applyFilters: Dispatch<any>;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  savePaymentStatuses: (data: YandexTaxiUpdateStatusesParams) => Promise<unknown>;
  isSavingStatuses: boolean;
  isOrganization: boolean;
  handleChangeStatusClick: () => void;
  selectedRowData: SelectedRowData[];
  form?: FormInstance<any>;
  responseData: TableRecord[];
  filters: TaxiYandexRegistryFilter;
  userColumnVisibilitySettings: UsersAttributes | undefined;
}) => {
  const { t } = useTranslation();

  const handleStatusUpdate = (data: PaymentStateResponse) => responseData?.forEach(item => {
    const row = data.find(el => el.requestId === item.id);
    if (row) {
      item.requestStatus = row.status;
    }
  });

  return (
    <>
      <Fragment key="isOrgStructure">
        {isOrgStructureRole && !isStatusChangeActive && (
        <CompensationModal<TaxiYandexRegistryFilter, Partial<RequestBodyPublicParams>, PublicUIVisibilityDTO>
          transportType="external"
          filterParams={filters}
          columnsVisibility={userColumnVisibilitySettings?.publicUIVisibility}
          mimeType={XLSX_MIME_TYPE}
          // @ts-ignore
          onRequestParamsOnXlsDownload={processPaymentYandexTaxiRequestQueryOnXlsDownload}
          onFinish={applyFilters}
          form={form}
        />
        )}
      </Fragment>
      {isOrgStructureRole && !isStatusChangeActive && (
        <Button
          type="primary"
          danger={isStatusChangeActive}
          onClick={handleChangeStatusClick}
          disabled={!isOrganization}
        >
          {t.Forms.registryFilterFields.paidStatusButton}
        </Button>
      )}
      {isStatusChangeActive && (
        <ActiveStatusChangeButtons
          setStatusChangeActive={setStatusChangeActive}
          selectedRowData={selectedRowData}
          savePaymentStatuses={savePaymentStatuses}
          isSavingStatuses={isSavingStatuses}
          handleStatusUpdate={handleStatusUpdate}
          onFinish={onFinish}
        />
      )}
    </>
  );
};
