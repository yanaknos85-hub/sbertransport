import React, { FC } from 'react';
import { observer } from 'mobx-react';

import CargoDetailed from '../CargoDetailed/CargoDetailed';

const CargosApprovalDetailed: FC<{ isRegular?: boolean }> = observer(({ isRegular = false }) => (
  <CargoDetailed
    isApproval={true}
    isRegular={isRegular}
    isJournal={false}
  />
));

export default CargosApprovalDetailed;
