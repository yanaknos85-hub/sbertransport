import { TLoadingResult, TTicket, TUploadImportSummary } from '../Files.interface';

export class UploadImportSummaryModel implements TUploadImportSummary {
  ticket: TTicket;

  // parsingResult: TParsingResult;

  loadingResult: TLoadingResult;

  constructor(file: TUploadImportSummary) {
    this.ticket = file.ticket;
    // this.parsingResult = file.parsingResult;
    this.loadingResult = file.loadingResult;
  }
}
