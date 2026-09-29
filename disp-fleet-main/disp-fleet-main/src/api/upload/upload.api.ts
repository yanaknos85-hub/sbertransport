import { QueryConfig } from 'react-query';
import * as t from 'io-ts';

import { DISPATCHER_ROOM } from 'api/vehicles/vehicles.constants';
import {
  useAPIMutation, useAPI, APIQueryResult
} from 'api';
import { ignore } from 'utils/utils';

import {
  ImportReport, LoadConfig, LoadEntity, LoadResponse
} from './upload.types';
import { importExportEndpointMap } from './upload.constants';

export const IMPORT_REPORT_KEY = 'import-report';

declare module 'api' {
  interface Cache {
    importReport: { key: [typeof IMPORT_REPORT_KEY, LoadEntity]; value: ImportReport[] };
  }
}

export const mkUseUploadEntity = (entity: LoadEntity, config: LoadConfig) => (): LoadResponse => (
  useAPIMutation<void, unknown, { file: File }, unknown>(({ http }, { file }) => {
    const formData = new FormData();
    formData.append('file', file);
    return http.post(`${DISPATCHER_ROOM}/files/${importExportEndpointMap[entity]}/`, formData).then(ignore);
  }, config)
);

export const useImportReport = (
  entity: LoadEntity,
  queryConfig?: QueryConfig<ImportReport[]>
): APIQueryResult<ImportReport[]> => useAPI(
  [IMPORT_REPORT_KEY, entity],
  ({ http, process }) => http
    .get<ImportReport[]>(`${DISPATCHER_ROOM}/files/${importExportEndpointMap[entity]}/result/`)
    .then(process.decodeResponseData(t.array(ImportReport))),
  {
    ...queryConfig,
  }
);
