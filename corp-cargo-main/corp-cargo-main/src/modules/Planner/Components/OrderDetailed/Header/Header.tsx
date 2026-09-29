import React, { FC } from 'react';
import moment from 'moment';
import { useTranslation } from 'i18n';
import { useHistory } from '@sber-sbertransport/mf-core';
import { ReactComponent as Arrow } from '../../../images/arrowLeft.svg';
import { DATE_FORMAT } from 'constants/constants.app';
import CopyIdIcon from '../CopyId';

import * as S from './Header.styles';

interface Props {
  humanReadableId: string;
  desiredDate: number;
}

export const Header: FC<Props> = props => {
  const { humanReadableId, desiredDate } = props;

  const { t } = useTranslation();

  const history = useHistory();

  return (
    <S.Header>
      <S.TitleBlock>
        <S.TextBlock>
          <S.HeaderTitle>
            <Arrow onClick={history.goBack} />
            <span onClick={history.goBack}>{humanReadableId}</span>
            <CopyIdIcon id={humanReadableId} tooltipText={t.Planner.copyOrderId} />
          </S.HeaderTitle>
          <S.TextLight>
            {t.Planner.routeStartDate}
            {' '}
            {moment(desiredDate).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS)}
          </S.TextLight>
        </S.TextBlock>
      </S.TitleBlock>
    </S.Header>
  );
};
