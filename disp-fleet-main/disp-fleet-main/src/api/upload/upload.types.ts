import * as t from 'io-ts';
import { MutationResultPair } from 'react-query';

import { APIMutationConfig } from 'api';
import { importExportEndpointMap } from './upload.constants';

export type LoadEntity = keyof typeof importExportEndpointMap;
export type LoadConfig = APIMutationConfig<void, unknown, { file: File }, unknown>;
export type LoadResponse = MutationResultPair<void, unknown, { file: File }, unknown>;

export const ImportReportRow = t.intersection([
  t.strict({
    rowNumber: t.number,
    violations: t.array(t.any),
    exceptions: t.array(t.string),
  }),
  t.partial({
    field: t.string,
  }),
]);

export type ImportReportRow = t.TypeOf<typeof ImportReportRow>;

export const ImportReportSinglePage = t.strict({
  exceptions: t.array(t.any),
  finished: t.boolean,
  name: t.string,
  row: t.array(ImportReportRow),
  rowCount: t.number,
  rowProcessed: t.number,
});

export type ImportReportSinglePage = t.TypeOf<typeof ImportReportSinglePage>;

export const ImportReportPerPage = t.strict({
  fileName: t.string,
  finished: t.boolean,
  pages: t.array(ImportReportSinglePage),
});

export type ImportReportPerPage = t.TypeOf<typeof ImportReportPerPage>;

export const ImportReport = t.union([ImportReportSinglePage, ImportReportPerPage]);

export type ImportReport = t.TypeOf<typeof ImportReport>;
