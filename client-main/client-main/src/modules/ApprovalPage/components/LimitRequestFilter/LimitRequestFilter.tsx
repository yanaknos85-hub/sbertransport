import React, { useState, useEffect, FC } from 'react';

import { useGetLimitRequestStats } from 'api/limits';
import { TransportTypeEnum, TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import { RequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { PageSetting } from 'shared/hooks/usePagination';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';
import { getTransportIcon } from 'utils/transport';
import {
  FilterContainer, FilterContent, StyledIcon, TransportItem, TransportList
} from './LimitRequestFilter.styled';

interface LimitRequestFilterProps extends RequestFilterProps {
  totalElements?: number;
  isActiveFilter: boolean;
  setPageSetting(setting: PageSetting): void;
}

export const LimitRequestFilter: FC<LimitRequestFilterProps> = ({
  totalElements,
  isActiveFilter,
  transportType,
  setTransportType,
  setPageSetting,
}) => {
  const { data: statistics } = useGetLimitRequestStats(isActiveFilter);
  const { isMobile } = usePlatformDetect();
  const [totalCount, setTotalCount] = useState(0);
  const [isOpenFilters, setOpenFilters] = useState(true);

  useEffect(() => {
    if (!transportType) {
      setTotalCount(totalElements);
    }
  }, [totalElements]);

  const onSetTransportType = (type?: string) => () => {
    setTransportType(type as TransportTypeEnum);
    setPageSetting({ page: 0, size: 10 });
  };

  return (
    <FilterContainer>
      <TransportList>
        <TransportItem isActive={!transportType} onClick={onSetTransportType()}>
          <span>
            Все
            {' '}
            {totalCount || ''}
          </span>
        </TransportItem>
        {isMobile && (
          <FilterContent onClick={() => setOpenFilters(!isOpenFilters)}>
            <span>{isOpenFilters ? 'Cкрыть' : 'Раскрыть'}</span>
            {isOpenFilters ? <ArrowUp /> : <ArrowDown />}
          </FilterContent>
        )}
        {isOpenFilters && statistics.map(({ transportType: type, count }) => (
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
    </FilterContainer>
  );
};
