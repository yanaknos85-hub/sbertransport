import * as t from 'io-ts';

import { RequestModel } from './models/SRMMassRequestMultiple.model';
import { UUID } from 'utils/io-ts';

export enum RequestStatus {
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR',
  IN_PROGRESS = 'IN_PROGRESS',
}

export interface RequestServer {
  id: string;
  count: number;
  countAll: number;
  date: string;
  status: RequestStatus;
  data: RequestListItem[];
  description?: string;
}

export interface Request {
  id: string;
  count: number;
  countAll: number;
  date: string;
  status: RequestStatus;
  data: RequestModel[];
  description?: string;
}

export interface ISRMMassStoreMultiple {
  uploadRequestId: string;
  request: Request | undefined;
  requestList: any[];
  saveFile(file: any, userId: UUID): Promise<void>;
  validateFile(file: any, userId: UUID): Promise<void>;
}

export interface ISRMMassServiceMultiple {
  saveFile(file: File, userId: UUID): Promise<SavedFileInfo | void>;
  validateFile(file: File, userId: UUID): Promise<SavedFileInfo | void>;
}

export enum UsedFileFormatEnum {
  JPG = 'JPG',
  JPEG = 'JPEG',
  PNG = 'PNG',
  TIFF = 'TIFF',
  PDF = 'PDF',
  DOC = 'DOC',
  DOCX = 'DOCX',
  HEIC = 'HEIC',
}

export const SavedFileInfo = t.strict({
  requestId: t.string,
  started: t.boolean,
});

export type SavedFileInfo = t.TypeOf<typeof SavedFileInfo>;

const ErrorStructure = t.type({
  value: t.string,
  error: t.boolean,
});

export type ErrorStructure = t.TypeOf<typeof ErrorStructure>;

const RequestListItem = t.type({
  personnelNumber: ErrorStructure,
  fullName: ErrorStructure,
  addressFrom: ErrorStructure,
  addressTo: ErrorStructure,
  orderTime: ErrorStructure,
  orderDate: ErrorStructure,
  transportClass: ErrorStructure,
  transportType: ErrorStructure,
  tripType: ErrorStructure,
  addressFromLatitude: t.number,
  addressFromLongitude: t.number,
  addressToLatitude: t.number,
  addressToLongitude: t.number,
});

export type RequestListItem = t.TypeOf<typeof RequestListItem>;
