import React from 'react';
import { useRouteMatch } from 'react-router-dom';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import { MonthNames } from 'constants/calendar.constants';
import { LimitRequestStatusesTitlesEnum } from 'stores/Limits/Limit.interface';
import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import { toRubles, formatRublesWithoutPennies } from 'utils';
import { upFirst } from 'utils/formatName';
import { getTransportIcon } from 'utils/transport';
import { Status } from 'components/Limits';
import { ISpentActionsType } from '../../useDetailedLimitsMapper';
import {
  InfoWrapper,
  ItemFooter,
  ItemHeader,
  ListItemStyled,
  RequestInfo,
  StyledBody,
  StyledIcon,
  StyledLink,
  StyledSum,
  StyledTitle,
  TimeInfo,
  TransportStyled
} from './UserLimitRequestItem.styled';

export const UserLimitRequestItem: React.FC<{
  request: ISpentActionsType;
}> = ({ request }) => {
  const match = useRouteMatch();

  return (
    <ListItemStyled key={request.id}>
      <ItemHeader>
        <StyledTitle>
          Заявка на лимит
          {' '}
          {request.humanReadableId}
        </StyledTitle>
        <InfoWrapper>
          {request.creationTime && (
            <TimeInfo>
              Создано
              {' '}
              <time>{moment(request.creationTime).format(DATE_FORMAT.BASE_REVERTED_DOTS)}</time>
            </TimeInfo>
          )}
          <Status status={request.status}>{LimitRequestStatusesTitlesEnum[request.status]}</Status>
        </InfoWrapper>
      </ItemHeader>
      <StyledBody>
        <RequestInfo>
          {`Личный лимит • `}
          {upFirst(MonthNames[request.period])}
        </RequestInfo>
        <TransportStyled>
          <StyledIcon src={getTransportIcon(request.transportType)} />
          {TransportTypeTitles[request.transportType]}
        </TransportStyled>
      </StyledBody>
      <ItemFooter>
        <StyledSum>
          Запрашиваемая сумма
          {' '}
          <span>{request.sum ? formatRublesWithoutPennies(toRubles(request.sum)) : 0}</span>
        </StyledSum>
        <StyledLink to={`${match.url}/${request.id}`}>Подробнее</StyledLink>
      </ItemFooter>
    </ListItemStyled>
  );
};
