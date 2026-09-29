import {
  activeSettings, ApprovalSetting, closedSettings
} from 'shared/models/Approval.interface';

import { CommonParams } from '../ApprovalList';

export const getApprovalSettings = (
  filter: string,
  status: string,
  commonParams: CommonParams
): ApprovalSetting => {
  if (filter === 'active' && !status) {
    return { status: activeSettings, ...commonParams };
  }
  if (filter === 'active' && status) {
    return {
      status: activeSettings, type: status, ...commonParams,
    };
  }
  if (filter === 'closed' && status) {
    return {
      status: status, ...commonParams,
    };
  }
  return { status: closedSettings, ...commonParams };
};
