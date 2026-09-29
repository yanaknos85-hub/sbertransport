import { UploadFile } from 'antd/lib/upload/interface';
import * as t from 'io-ts';

import { EmployeeStatus } from 'constants/constants.app';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export interface IFilesStore {
  parsingSummary: TUploadParsingSummary | undefined;
  loadingSummary: TUploadImportSummary | undefined;
  isUploading: boolean;
  clearSummary(): void;
  preloadHandbook(values: TUploadHandbookParams): Promise<void>;
  loadHandbook(values: TUploadHandbookParams): Promise<void>;
  initStore(): void;
}

export interface IFilesService {
  uploadFile(args: getFilesArgs): Promise<TUploadImportSummary>;
  preloadFile(args: getFilesArgs): Promise<TUploadParsingSummary>;
}

export interface IUploadConfig {
  decSeparator: FileDecSeparator;
  nsi: FileNsi;
  orgId: string;
  separator: FileSeparator;
  strategyMode: StrategyMode;
  upload: UploadFile[];
}

export enum StrategyMode {
  UPDATE_OR_INSERT = 'UPDATE_OR_INSERT',
  UPDATE = 'UPDATE',
  INSERT = 'INSERT',
}

export enum UploadStatus {
  SUCCESS = 'SUCCESS',
  FAIL = 'FAIL',
  IN_PROGRESS = 'IN_PROGRESS',
}

export enum FileEmployeeStatus {
  OK = 'OK',
  ERROR = 'ERROR',
}

export enum FileDecSeparator {
  DOT = 'DOT',
  COMMA = 'COMMA',
}

export enum FileSeparator {
  COMMA = 'COMMA',
  SEMICOLON = 'SEMICOLON',
  WHITESPACE = 'WHITESPACE',
  TAB = 'TAB',
  OTHER = 'OTHER',
}

export enum FileNsi {
  EMPLOYEE = 'EMPLOYEE',
}

export enum FileHeadersPresence {
  WITH = 'WITH',
  WITHOUT = 'WITHOUT',
}

export enum FileUploadMode {
  load = 'load',
  preload = 'preload',
}

export interface getFilesArgs {
  orgId: string;
  strategyMode: StrategyMode;
  nsi: FileNsi;
  decSeparator: FileDecSeparator;
  separator: FileSeparator;
  data: FormData;
}

export interface TUploadHandbookParams {
  decSeparator: FileDecSeparator;
  separator: FileSeparator;
  nsi: FileNsi;
  strategyMode: StrategyMode;
  upload: UploadFile<any>[];
  headers: string;
}

export const IOFileEmployee = t.intersection([
  t.type({}),
  t.partial({
    id: t.string,
    userId: t.string,
    availableTransportTypes: t.UnknownArray,
    personalCars: t.UnknownArray,
    organizationId: t.string,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
    email: t.string,
    personnelNumber: t.string,
    mobilePhone: t.string,
    position: t.string,
    department: t.string,
    delegatedById: t.string,
    supervisorId: t.string,
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
  }),
]);

export type TFileEmployee = t.TypeOf<typeof IOFileEmployee>;

export const IOTicket = t.type({
  operationId: t.string,
  dtOperation: t.string,
  login: t.string,
});

export type TTicket = t.TypeOf<typeof IOTicket>;

export const IOValidationResult = t.type({
  field: t.string,
  badValue: t.string,
  description: t.string,
});

export const IOEmployeeFileItem = t.intersection([
  t.type({
    item: IOFileEmployee,
    parseStatus: ioTypeFromEnum<FileEmployeeStatus>('FileEmployeeStatus', FileEmployeeStatus),
    validationResults: t.array(IOValidationResult),
  }),
  t.partial({
    description: t.string,
  }),
]);

export const IOParsingResult = t.type({
  parseStatus: ioTypeFromEnum<FileEmployeeStatus>('FileEmployeeStatus', FileEmployeeStatus),
  items: t.array(IOEmployeeFileItem),
  totalRead: t.number,
  totalBadRecords: t.number,
  totalWithOutError: t.number,
});

export type TParsingResult = t.TypeOf<typeof IOParsingResult>;

export const IOLoadingResult = t.intersection([
  t.type({
    resultStatus: ioTypeFromEnum<UploadStatus>('UploadStatus', UploadStatus),
    inserted: t.number,
    replaced: t.number,
    updated: t.number,
    deleted: t.number,
    unchanged: t.number,
    totalBadRecords: t.number,
    totalProcessed: t.number,
    totalRecordsBeforeLoading: t.number,
    validationResults: t.array(IOValidationResult),
  }),
  t.partial({
    description: t.string,
  }),
]);

export type TLoadingResult = t.TypeOf<typeof IOLoadingResult>;

export const IOUploadImportSummary = t.intersection([
  t.type({
    ticket: IOTicket,
    loadingResult: IOLoadingResult,
  }),
  t.partial({
    parsingResult: IOParsingResult,
  }),
]);

export const IOUploadParsingSummary = t.type({
  parsingResult: IOParsingResult,
});

export type TUploadParsingSummary = t.TypeOf<typeof IOUploadParsingSummary>;
export type TUploadImportSummary = t.TypeOf<typeof IOUploadImportSummary>;
