import { RequestBaseModel } from 'shared/models/RequestBase.model';

export interface ICompensationRecipient {
  fio: string;
  phone: string;
  personnelNumber: string;
}

export interface ICompensationRequestDto {
  id: string;
  humanReadableId: string;
  transportType: string;
  recipient: ICompensationRecipient;
  creationTime: string;
  status: string;
  cost: number;
  desiredDate: string;
  approvalDate?: string;
}

export class CompensationRequestModel extends RequestBaseModel<string, string> {
  transportType: string;
  recipient: ICompensationRecipient;
  cost: number;
  desiredDate: string;
  approvalDate?: string;

  constructor(data: ICompensationRequestDto) {
    super(data);
    this.transportType = data.transportType;
    this.recipient = { ...data.recipient };
    this.cost = data.cost;
    this.desiredDate = data.desiredDate;
    this.approvalDate = data.approvalDate;
  }
}
