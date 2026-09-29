import { useGetRequestSearchApproval } from 'api/approvals';

import { EmployeeAppLinks } from 'modules/EmployeeApp/EmployeeApp.constants';

import { LIMIT_REQUEST_STATUS } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { sortByTime } from 'utils';

import { activeSettings } from '../models/Approval.interface';
import { useAppStoreContext } from './useEmpContext';

export const useBadgeCounter = (): Record<string, number> => {
  const { [StoreNames.settingsStore]: settingsStore, [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  const activeSetting = {
    status: activeSettings,
  };
  const { data: activeApprovals } = useGetRequestSearchApproval(activeSetting);
  const getActiveLimitRequestsCount = () => sortByTime(limitsStore.limitRequests.filter(x => x.status === LIMIT_REQUEST_STATUS.INIT)).length ?? 0;

  return {
    ...settingsStore.menuCounterList,
    [EmployeeAppLinks.approvalRequestList]: activeApprovals?.totalElements ?? 0,
    [EmployeeAppLinks.approvementLimit]: getActiveLimitRequestsCount(),
  };
};
