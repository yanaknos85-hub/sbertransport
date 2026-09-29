import { MutationResultPair, QueryConfig } from 'react-query';
import {
  useAPIMutation, APIMutationConfig, useAPI, APIQueryResult
} from 'api';
import { ignore } from 'utils';
import * as t from 'io-ts';
import { importExportEndpointMap } from 'modules/UploadButton';

const Violation = t.any;

export const ImportReportRow = t.intersection([
  t.strict({
    rowNumber: t.number,
    violations: t.array(Violation),
    exceptions: t.array(t.string),
  }),
  t.partial({
    field: t.string,
  }),
]);

export type ImportReportRow = t.TypeOf<typeof ImportReportRow>;

export const ImportReportException = t.any;

export const ImportReportSinglePage = t.strict({
  finished: t.boolean,
  name: t.string,
  row: t.array(ImportReportRow),
  exceptions: t.array(ImportReportException),
});

export type ImportReportSinglePage = t.TypeOf<typeof ImportReportSinglePage>;

export const ImportReportPerPage = t.strict({
  finished: t.boolean,
  pages: t.array(ImportReportSinglePage),
});

export type ImportReportPerPage = t.TypeOf<typeof ImportReportPerPage>;

export const ImportReport = t.union([ImportReportSinglePage, ImportReportPerPage]);

export type ImportReport = t.TypeOf<typeof ImportReport>;

declare module 'api' {
  interface Cache {
    importReport: { key: ['import-report', keyof typeof importExportEndpointMap]; value: ImportReport[] };
  }
}

const getSuffix = (entity: keyof typeof importExportEndpointMap) => {
  if (entity === 'tariffCargo') {
    return 'tariff';
  }
  if (entity === 'contractCargo') {
    return 'contract';
  }
  return entity;
};

export const mkUseUploadEntity = (
  entity: keyof typeof importExportEndpointMap,
  config: APIMutationConfig<void, unknown, { file: File }, unknown>
// eslint-disable-next-line @stylistic/max-len
) => (): MutationResultPair<void, unknown, { file: File }, unknown> => useAPIMutation<void, unknown, { file: File }, unknown>(({ http }, { file }) => {
  const formData = new FormData();
  formData.append('file', file);
  return http.post(`${importExportEndpointMap[entity]}/files/${getSuffix(entity)}/`, formData).then(ignore);
}, config);

export const mkUseUploadCargoEntity = (
  entity: keyof typeof importExportEndpointMap,
  config: APIMutationConfig<void, unknown, { file: File }, unknown>
// eslint-disable-next-line @stylistic/max-len
) => (): MutationResultPair<void, unknown, { file: File }, unknown> => useAPIMutation<void, unknown, { file: File }, unknown>(({ http }, { file }) => {
  const formData = new FormData();
  formData.append('file', file);
  return http.post(`${importExportEndpointMap[entity]}/files/tariff/`, formData).then(ignore);
}, config);

export const useImportReport = (
  entity: keyof typeof importExportEndpointMap,
  queryConfig?: QueryConfig<ImportReport[]>
): APIQueryResult<ImportReport[]> => useAPI(
  ['import-report', entity],
  ({ http, process }) => http
    .get<ImportReport[]>(`${importExportEndpointMap[entity]}/files/${getSuffix(entity)}/result/`)
    .then(process.decodeResponseData(t.array(ImportReport))),
  {
    ...queryConfig,
  }
);

export const mkUseUploadVSPEntity = (
  entity: keyof typeof importExportEndpointMap,
  config: APIMutationConfig<void, unknown, { file: File }, unknown>
) => (): MutationResultPair<void, unknown, { file: File }, unknown> => (
  useAPIMutation<void, unknown, { file: File }, unknown>(({ http }, { file }) => {
    const formData = new FormData();
    formData.append('file', file);
    return http.post(`${importExportEndpointMap[entity]}/files/${getSuffix(entity)}`, formData).then(ignore);
  }, config));
