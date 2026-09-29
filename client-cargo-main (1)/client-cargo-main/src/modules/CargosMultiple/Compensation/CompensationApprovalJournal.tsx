import React from 'react';
import { observer } from 'mobx-react';

import CompensationApprovalContent from './CompensationApprovalContent/CompensationApprovalContent';
import { useCompensationFetchConfig } from './hooks';

const CompensationApprovalJournal: React.FC = observer(() => {
  const fetchDataConfig = useCompensationFetchConfig();

  return (
    <CompensationApprovalContent fetchDataConfig={fetchDataConfig} />
  );
});

export default CompensationApprovalJournal;
