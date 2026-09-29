export interface IRequestBase {
  authorId?: string;

  status?: string;

  approvalState?: string;

  id: string;

  humanReadableId: string;

  creationTime: string | number;
}
