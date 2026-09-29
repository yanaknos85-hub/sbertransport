import React from 'react';
import { useRouteMatch } from 'react-router-dom';
import moment from 'moment';
import { observer } from 'mobx-react';

import { DATE_FORMAT } from 'constants/constants.app';
import { MonthNames } from 'constants/calendar.constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import {
  LimitRequestInfo,
  LimitRequestStatusesTitlesEnum
} from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores';
import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import { formatRubles, formatRublesWithoutPennies, toRubles } from 'utils';
import { getTransportIcon } from 'utils/transport';
import { formatName, upFirst } from 'utils/formatName';
import { Status } from 'components/Limits';
import { LimitRequestButtons } from '../../components/LimitRequestButtons/LimitRequestButtons';
import {
  Actions,
  AuthorInfo,
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
} from './LimitRequestItem.styled';

export const LimitRequestListItem: React.FC<{
  request: LimitRequestInfo;
  viewDisabled: boolean;
  refresh: () => void;
}> = observer(({
  request, viewDisabled, refresh,
}) => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const match = useRouteMatch();

  const depLimitBalance = limitsStore.currentDepartmentSharing.find(x => (
    x.transportType === request.transportType
  ));

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
              <time>{moment(request.creationTime).utcOffset('GMT+06').format(DATE_FORMAT.DATE_WITH_TIME)}</time>
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
        <AuthorInfo>
          <span>{formatName(request.author)}</span>
          <span>{`(${request.author.humanReadableId})`}</span>
        </AuthorInfo>
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
        <StyledSum>
          Фактический остаток (на месяц)
          {' '}
          <span>
            {depLimitBalance
              ? formatRubles(toRubles(depLimitBalance.limitSharingPerPeriodDTO?.balance || 0, true))
              : '-'}
          </span>
        </StyledSum>
        <Actions>
          <LimitRequestButtons
            request={request}
            viewDisabled={viewDisabled}
            refresh={refresh}
          />
          <StyledLink to={`${match.url}/${request.id}`}>Подробнее</StyledLink>
        </Actions>
      </ItemFooter>
    </ListItemStyled>
  );
});
