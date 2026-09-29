import React, { FC, useCallback } from 'react';
import { MutateFunction } from 'react-query';
import { Select } from '@sber-sbertransport/ui-kit/src';

import { useAutoparkBranch, useAutoparkBranches } from 'api/branches/branches.api';
import { AutoParkBranch, AutoParkBranches, AutoParkBranchesFilters } from 'api/branches/branches.types';
import { SelectEndlessScroll } from 'components/SelectEndlessScroll';

interface BranchSelectProps extends React.ComponentProps<typeof Select> {
  autoparkId?: string;
}

const BranchSelect: FC<BranchSelectProps> = ({ autoparkId = '', ...props }) => {
  const [_fetchAutoparkBranch] = useAutoparkBranch(autoparkId);
  const [_fetchAutoparkBranches] = useAutoparkBranches();

  const fetchAutoparkBranch: MutateFunction<AutoParkBranch, unknown, string, unknown> = useCallback(
    async (branchId?: string) => {
      const result = await _fetchAutoparkBranch(branchId);

      return result;
    },
    [_fetchAutoparkBranch]
  );

  const fetchAutoparkBranches: MutateFunction<AutoParkBranches, unknown, AutoParkBranchesFilters, unknown>
    = useCallback(
      async (filters?: AutoParkBranchesFilters) => {
        const result = await _fetchAutoparkBranches({ autoparkId, ...filters } as AutoParkBranchesFilters);

        return result;
      },
      [_fetchAutoparkBranches, autoparkId]
    );

  return (
    <SelectEndlessScroll
      fetch={fetchAutoparkBranches}
      fetchOne={fetchAutoparkBranch}
      searchField="name"
      labelField="name"
      enabled={!!autoparkId}
      showSearch
      showDivider
      size="small"
      {...props}
    />
  );
};

export default BranchSelect;
