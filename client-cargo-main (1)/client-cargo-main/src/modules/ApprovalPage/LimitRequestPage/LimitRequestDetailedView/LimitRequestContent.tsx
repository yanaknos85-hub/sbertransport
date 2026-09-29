import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { LimitRequestInfo } from 'stores/Limits/Limit.interface';

import { LimitRequestGeneralInfo } from './LimitRequestDetailedComponents/LimitRequestGeneralInfo';
import { LimitRequestTables } from './LimitRequestDetailedComponents/LimitRequestTables/LimitRequestTables';

export const maxLimitRows = 100;

const LimitRequestContent: FC<{ request: LimitRequestInfo }> = observer(({ request }) => (
  <>
    <LimitRequestGeneralInfo request={request} />
    <LimitRequestTables request={request} />
  </>
));

export default LimitRequestContent;
