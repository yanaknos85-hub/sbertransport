import { injectable } from 'inversify';

import { IFilesService, TUploadImportSummary, TUploadParsingSummary } from './Files.interface';

@injectable()
export class FilesServiceMocked implements IFilesService {
  uploadFile = async (): Promise<TUploadImportSummary> => (await import('mock/stores/Files/uploadFile.json')).default as any;

  preloadFile = async (): Promise<TUploadParsingSummary> => (await import('mock/stores/Files/preloadFile.json')).default as any;
}
