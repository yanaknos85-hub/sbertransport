import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import moment from 'moment';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ReactComponent as Arrow } from '../../../images/arrowLeft.svg';
import { RouteStatusEnum } from '../../../types';
import CopyIdIcon from '../../OrderDetailed/CopyId';
import { usePlanner } from '../../../context/PlannerContext';
import { DATE_FORMAT } from 'constants/constants.app';

import * as S from './Header.styles';

interface Props {
  humanReadableId: string;
  desiredDate: string;
  submitPlanning: () => void;
  formValues: any;
  status: string;
  changeStatus: () => void;
}

export const Header: FC<Props> = props => {
  const {
    humanReadableId, desiredDate, submitPlanning, status, changeStatus,
  } = props;

  const { plannerStore } = useAppStoreContext();
  const { t } = useTranslation();
  const { setRouteMode } = usePlanner()

  const handlerGoBack = () => {
    setRouteMode('list');
    plannerStore.clearRouteStore();
  };

  const isPlanningFinished = status === RouteStatusEnum.CARGO_PLANNING_FINISHED;

  return (
    <S.Header>
      <S.TitleBlock isPlanningFinished={isPlanningFinished}>
        <S.TextBlock isPlanningFinished={isPlanningFinished}>
          <S.HeaderTitle>
            <Arrow onClick={handlerGoBack} />
            <span className="route-id-text" onClick={handlerGoBack}>{humanReadableId}</span>
            <CopyIdIcon id={humanReadableId} tooltipText={t.Planner.copyRouteId} />
          </S.HeaderTitle>
          <S.TextLight>
            {t.Planner.routeStartDate}
            {' '}
            {moment(desiredDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
          </S.TextLight>
        </S.TextBlock>
        {status === RouteStatusEnum.CARGO_PLANNING ? (
          <S.Button onClick={submitPlanning}>{t.Planner.planingRoute}</S.Button>
        ) : (
          <S.Button isPlanningFinished={isPlanningFinished} onClick={changeStatus}>{t.Planner.changeStatus}</S.Button>
        )}
      </S.TitleBlock>
    </S.Header>
  );
};
