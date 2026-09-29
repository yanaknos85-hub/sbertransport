import React, { FC } from 'react';
import Courier from 'shared/components/Images/cargo/Courier';
import Dedicated from 'shared/components/Images/cargo/Dedicated';
import DomesticCourier from 'shared/components/Images/cargo/DomesticCourier';
import Interregional from 'shared/components/Images/cargo/Interregional';
import Question from 'shared/components/Images/view/menu 2.0/Question';

import { TransportTypeEnum } from '../../../modules/Exchange/types';

interface Props {
  transportType: TransportTypeEnum;
}

export const Icon: FC<Props> = ({ transportType }) => {
  switch (transportType) {
    case TransportTypeEnum.COURIER:
      return <Courier />;
    case TransportTypeEnum.DEDICATED:
      return <Dedicated />;
    case TransportTypeEnum.INDIVIDUAL:
      return <Dedicated />;
    case TransportTypeEnum.INTERREGIONAL:
      return <Interregional />;
    case TransportTypeEnum.DOMESTIC_COURIER:
      return <DomesticCourier />;
    default:
      return <Question />;
  }
};
