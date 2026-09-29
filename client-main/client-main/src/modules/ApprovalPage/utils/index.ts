import { AxiosError } from 'axios';
import {
  ApprovalSetting, ApprovalTypeEnum, activeSettings, closedSettings
} from 'shared/models/Approval.interface';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
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
  if (status === ApprovalTypeEnum.TRIP || status === ApprovalTypeEnum.REQUEST) {
    return {
      status: 'NEW', type: status, ...commonParams,
    };
  }
  if (status) {
    return { status, ...commonParams };
  }
  return { status: closedSettings, ...commonParams };
};

export const getDelegateErrorMessage = (error: AxiosError) => {
  let errorMessage: string = SYSTEM_MESSAGES.generalSaveError;
  const problems = error.response?.data?.problems;

  if (problems?.length) {
    const fields = problems.map((problem: { field: string }) => problem.field);

    if (fields.includes('startDate') || fields.includes('endDate')) {
      errorMessage = SYSTEM_MESSAGES.delegatePeriodError;
    }
  }

  return errorMessage;
};
