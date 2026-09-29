import React, {
  FC, useEffect, memo, MutableRefObject
} from 'react';
import { createPortal } from 'react-dom';
import { FormInstance } from 'antd';

import { RegisterSearchQuery } from 'api/register-search';
import { ExportXLSModal } from 'shared/components/ModalsExportXLSBusinessReports/ExportXLSModal/ExportXLSModal';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';

import { FilterValues, RequestBodyParamsTaxi } from './types/types';
import { processTaxiRequestBodyOnXlsDownload } from './utils/data';
import { useUserAttributes } from './hooks/useUserAttributes';

export const TaxiBusinessReportsTable: FC<{
  filterValues: FilterValues;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
  isLoading: boolean;
  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  form?: FormInstance<any>;
}> = memo(({
  filterValues, buttonsRef, isLoading, setIsLoading, form,
}) => {
  const {
    usersAttributes, setSettingAttributes,
  } = useUserAttributes();

  useEffect(() => {
    setSettingAttributes(usersAttributes);
  }, [usersAttributes, setSettingAttributes]);

  return (
    <>
      {buttonsRef
      && buttonsRef.current
      && createPortal(
        // @ts-ignore
        <ExportXLSModal<RegisterSearchQuery, RequestBodyParamsTaxi>
          transportType="taxi"
          filterParams={filterValues}
          mimeType={XLSX_MIME_TYPE}
          onRequestParamsOnXlsDownload={processTaxiRequestBodyOnXlsDownload}
          isLoading={isLoading}
          setIsLoading={setIsLoading}
          form={form}
        />,
        buttonsRef.current
      )}
    </>
  );
});
