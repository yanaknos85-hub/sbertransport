import React, { FC } from 'react';
import { Switch } from 'antd';
import { useTranslation } from 'i18n';
import { TextSwitch } from '../../../Planner.styles';

import * as S from '../../styles.common';

interface Props {
  isOrdersVisible?: boolean;
}
/* TODO Выглядит будто не используется, проверить */
export const Tools: FC<Props> = props => {
  const { isOrdersVisible = false } = props;

  const { t } = useTranslation();

  const { outSideOrders } = t.Planner;

  return (
    <S.Container>
      <TextSwitch>
        <Switch defaultChecked={isOrdersVisible} onClick={() => console.log('Показать заявки вне маршрута')} />
        <TextSwitch>{outSideOrders}</TextSwitch>
      </TextSwitch>
    </S.Container>
  );
};
