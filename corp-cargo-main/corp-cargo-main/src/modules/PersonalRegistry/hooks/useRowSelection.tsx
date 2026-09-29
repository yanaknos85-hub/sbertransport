import React, { useState } from 'react';
import { useTranslation } from 'i18n';
import { useSavePaymentStatuses } from 'api/public-register-search';
import { TableRecord } from '../types/types';

export interface SelectedRowData {
  requestId: string;
  payed: boolean;
}

export const useRowSelection = (data: TableRecord[]) => {
  const { t } = useTranslation();
  const [savePaymentStatuses, { isLoading: isSavingStatuses }] = useSavePaymentStatuses();

  const [selectedTripIds, setSelectedTripIds] = useState<string[]>([]);
  const [selectedRowData, setSelectedRowData] = useState<SelectedRowData[]>([]);

  const rowSelection = {
    columnTitle: t.Forms.registryFilterFields.isPaid,
    selectedRowKeys: selectedTripIds,
    getCheckboxProps: (record: TableRecord) => ({
      disabled: record.requestStatusVisible !== t.Forms.registryFilterFields.changeableStatus,
    }),
    onChange: (selectedRowKeys: React.Key[], selectedRows: TableRecord[]) => {
      const freshRowData = data.reduce((acc: SelectedRowData[], { requestStatusVisible, id }) => {
        if (requestStatusVisible === t.Forms.registryFilterFields.changeableStatus) {
          acc.push({
            requestId: id,
            payed: selectedRows.some(row => row.id === id),
          });
        }
        return acc;
      }, []);

      setSelectedRowData(freshRowData);
      setSelectedTripIds(selectedRows.map(el => el.id));
    },
  };

  return {
    selectedRowsState: selectedTripIds,
    selectedRowData,
    setSelectedTripIds,
    rowSelection,
    setSelectedRowData,
    savePaymentStatuses,
    isSavingStatuses,
  };
};
