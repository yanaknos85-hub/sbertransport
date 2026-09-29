import React, { FC } from 'react';
import { observer } from 'mobx-react';

import CargoDetailed from './CargoDetailed/CargoDetailed';

const CargosRequestDetailed: FC<{ isRegular?: boolean }> = observer(({ isRegular = false }) => (
  <CargoDetailed
    isApproval={false}
    isRegular={!!isRegular}
    isJournal={true}
  />
));

export default CargosRequestDetailed;
