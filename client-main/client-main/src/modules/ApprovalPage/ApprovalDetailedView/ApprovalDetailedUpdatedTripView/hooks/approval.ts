import type { RefetchOptions } from 'react-query/types/core/query.d';

import { useGetApprovalByRequestId } from 'api/approvals';

import { ApprovalModel } from 'shared/models/Approval.model';
import { TripRequestModel } from 'stores/Trip/models';

/**
 * Применимо только для согласованной заявки
 */
export const useApprovalId = (
  request: TripRequestModel
): {
    approvalId: string | undefined;
    approvalIdIsLoading: boolean;
    refetchApprovalId: (options?: RefetchOptions | undefined) => Promise<ApprovalModel | null>;
  } => {
  const {
    data: approvalData,
    isFetching,
    isLoading,
    refetch: refetchApprovalId,
  } = useGetApprovalByRequestId(request.id);
  const approvalId = approvalData?.id;

  const approvalIdIsLoading = isFetching || isLoading;

  return {
    approvalId, approvalIdIsLoading, refetchApprovalId,
  };
};
