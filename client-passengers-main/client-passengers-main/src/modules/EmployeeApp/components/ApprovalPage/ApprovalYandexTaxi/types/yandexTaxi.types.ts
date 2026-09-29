export interface ApprovalSetting {
  status: string | string[];
  page?: number;
  size?: number;
}

export interface CommonParams {
  page: number;
  size: number;
}
