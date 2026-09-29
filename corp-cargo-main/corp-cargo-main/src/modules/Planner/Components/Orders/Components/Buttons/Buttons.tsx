import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { Tooltip } from 'antd';
import { usePlanner } from 'modules/Planner/context/PlannerContext';

import * as S from './Buttons.styles';

interface Props {
  handleCreateRoute: () => void;
}

export const Buttons: FC<Props> = props => {
  const { handleCreateRoute } = props;
  const { setRouteMode } = usePlanner()
  const { t } = useTranslation();

  return (
    <S.ButtonsBlock>
      <S.ButtonRouteStyled onClick={() => setRouteMode('choose')}>
        <Tooltip title={t.Planner.toRoute}>
            В Маршрут
        </Tooltip>
      </S.ButtonRouteStyled>
      <S.ButtonStyled
        onClick={handleCreateRoute}>
        {t.Planner.newPlanning}
      </S.ButtonStyled>
    </S.ButtonsBlock>
  );
};
