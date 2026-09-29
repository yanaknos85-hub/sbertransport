import React, { FC, useState } from 'react';
import locale from 'antd/es/date-picker/locale/ru_RU';
import moment from 'moment';

import {
  TransportTypeEnum,
  TransportTypeTitles
} from 'stores/TransportTypes/TransportTypes.interface';
import { RequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { ReactComponent as Icon } from 'shared/form/DatePicker/images/calendar.svg';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';
import { getTransportIcon } from 'utils/transport';

import { ISpentActionsType } from '../../useDetailedLimitsMapper';
import { getRequestsTransportTypes } from '../../limitRequests/limitRequestUtils';
import {
  FilterContainer,
  FilterContent,
  StyledDatePicker,
  StyledIcon,
  TransportItem,
  TransportList
} from './LimitRequestFilter.styled';

interface LimitRequestFilterProps extends RequestFilterProps {
  limitsRequests: ISpentActionsType[];
}

export const LimitRequestFilter: FC<LimitRequestFilterProps> = ({
  limitsRequests,
  transportType,
  dateRange,
  setTransportType,
  setDateRange,
}) => {
  const transportTypes = getRequestsTransportTypes(limitsRequests);
  const { isMobile } = usePlatformDetect();
  const [isOpenFilters, setOpenFilters] = useState(true);

  const onSetTransportType = (type?: string) => () => setTransportType(type as TransportTypeEnum);

  return (
    <FilterContainer>
      <TransportList>
        <TransportItem isActive={!transportType} onClick={onSetTransportType()}>
          <span>
            Все
            {' '}
            {limitsRequests.length}
          </span>
        </TransportItem>
        {isMobile && (
          <FilterContent onClick={() => setOpenFilters(!isOpenFilters)}>
            <span>{isOpenFilters ? 'Cкрыть' : 'Раскрыть'}</span>
            {isOpenFilters ? <ArrowUp /> : <ArrowDown />}
          </FilterContent>
        )}
        {isOpenFilters && transportTypes.map(([type, count]) => (
          <TransportItem
            key={type}
            isActive={type === transportType}
            onClick={onSetTransportType(type)}
          >
            <StyledIcon src={getTransportIcon(type)} />
            <span>
              {TransportTypeTitles[type]}
              {' '}
              {count}
            </span>
          </TransportItem>
        ))}
      </TransportList>
      <StyledDatePicker
        locale={locale}
        value={dateRange}
        placeholder={['Начальная', 'Конечная']}
        separator="-"
        suffixIcon={<Icon />}
        onChange={(dates): void => setDateRange(dates && [dates[0]?.startOf('day') || null, dates[1]?.endOf('day') || null])}
        getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        ranges={{
          вчера: [moment().subtract(1, 'day'), moment().subtract(1, 'day')],
          позавчера: [moment().subtract(2, 'day'), moment().subtract(2, 'day')],
          неделя: [moment().subtract(1, 'weeks'), moment()],
          месяц: [moment().subtract(1, 'month'), moment()],
        }}
      />
    </FilterContainer>
  );
};
