import React, { ReactElement } from 'react';
import Courier from 'shared/components/Images/cargo/Courier';
import Dedicated from 'shared/components/Images/cargo/Dedicated';
import Interregional from 'shared/components/Images/cargo/Interregional';
import Question from 'shared/components/Images/view/menu 2.0/Question';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

/**
 * По полученному типу транспорта выбираем отображаемую иконку для элемента CargoTariffTag
 * @param transportType полученный тип транспорта
 */
export const setIcon = (transportType?: TransportTypeEnum): ReactElement => {
  switch (transportType) {
    case TransportTypeEnum.COURIER:
      return <Courier />;
    case TransportTypeEnum.DEDICATED:
      return <Dedicated />;
    case TransportTypeEnum.INDIVIDUAL:
      return <Dedicated />;
    case TransportTypeEnum.INTERREGIONAL:
      return <Interregional />;
    default:
      return <Question />;
  }
};
