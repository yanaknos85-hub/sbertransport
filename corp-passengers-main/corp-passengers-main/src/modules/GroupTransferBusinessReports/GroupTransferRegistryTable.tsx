import React, {
  FC, memo, MutableRefObject
} from 'react';
import { createPortal } from 'react-dom';
import { FormInstance } from 'antd';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { ExportXLSModal } from 'shared/components/ModalsExportXLSBusinessReports/ExportXLSModal/ExportXLSModal';
import { RegistrySearchQuery } from 'stores/GroupTransferRegistry/GroupTransferRegistry';

import { RequestBodyParamsGroupTransfer, TransportTypes } from './types/types';
import { processGroupTransferRequestBodyOnXlsDownload } from './utils/data';

export const GroupTransferRegistryTable: FC<{
  filterValues: RegistrySearchQuery;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
  isLoading: boolean;
  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  form?: FormInstance<any>;
}> = memo(({
  filterValues, buttonsRef, isLoading, setIsLoading, form,
}) => {
  return (
    <>
      {buttonsRef
      && buttonsRef.current
      && createPortal(
        <ExportXLSModal<RegistrySearchQuery, Partial<RequestBodyParamsGroupTransfer>>
          transportType={TransportTypes.GROUP_TRANSFER.toLowerCase()}
          filterParams={filterValues}
          mimeType={XLSX_MIME_TYPE}
          onRequestParamsOnXlsDownload={processGroupTransferRequestBodyOnXlsDownload}
          isLoading={isLoading}
          setIsLoading={setIsLoading}
          form={form}
        />,
        buttonsRef.current
      )}
    </>
  );
});
