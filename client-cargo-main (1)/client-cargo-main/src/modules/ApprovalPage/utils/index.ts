import {
  activeSettings, ApprovalSetting, ApprovalTypeEnum, closedSettings
} from 'shared/models/Approval.interface';

import { CommonParams } from '../ApprovalList';

export const getApprovalSettings = (
  filter: string,
  status: string | string[],
  commonParams: CommonParams
): ApprovalSetting => {
  if (filter === 'active' && !status) {
    return { status: activeSettings, ...commonParams };
  }
  if (status === ApprovalTypeEnum.SHARED_RIDE) {
    return {
      status: activeSettings, type: status, ...commonParams,
    };
  }
  if (status === ApprovalTypeEnum.TRIP) {
    return {
      status: 'NEW', type: status, ...commonParams,
    };
  }
  if (status) {
    return { status, ...commonParams };
  }
  return { status: closedSettings, ...commonParams };
};
