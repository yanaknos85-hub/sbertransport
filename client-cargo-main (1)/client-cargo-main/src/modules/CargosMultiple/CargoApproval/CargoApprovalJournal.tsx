import React from 'react';
import { observer } from 'mobx-react';

import CargoApprovalContent from './CargoApprovalContent';
import { useCargoApprovalFetchConfig } from './hooks';

const CargoApprovalJournal: React.FC<{ isRegular?: boolean }> = observer(({ isRegular = false }) => {
  const fetchDataConfig = useCargoApprovalFetchConfig(isRegular);

  return (
    <CargoApprovalContent
      isRegular={isRegular}
      fetchDataConfig={fetchDataConfig}
    />
  );
});

export default CargoApprovalJournal;
