import React, { FC } from 'react';
import NumberFormat from 'react-number-format';

import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import { RUBLE_SIGN } from 'constants/constants.app';
import { getTransportIcon } from 'utils/transport';
import { chooseColorByPercent } from '../../utils';
import {
  StyledCard,
  HeaderWrap,
  CardBody,
  StyledIcon,
  IconWrapper,
  StyledTitle,
  StyledProgress,
  Balance,
  PercentStyled
} from './LimitBarCard.styled';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';

interface LimitBarCardProps {
  percent: number;
  balance: number;
  sum: number;
  transportType: string;
  limitType: LIMIT_TYPE;
  isDepartmentHead?: boolean;
  restrict?: string[];
  onSendRequest?: () => void;
  onClick?: () => void;
}

export const LimitBarCard: FC<LimitBarCardProps> = ({
  percent,
  balance,
  sum,
  transportType,
  limitType,
  isDepartmentHead,
  onClick,
}) => {
  const color = chooseColorByPercent(percent);
  const showRubles = isDepartmentHead || limitType === LIMIT_TYPE.EMPLOYEE;

  return (
    <StyledCard>
      <HeaderWrap>
        <StyledTitle>{TransportTypeTitles[transportType] ?? transportType}</StyledTitle>
        <IconWrapper>
          <StyledIcon src={getTransportIcon(transportType)} />
        </IconWrapper>
      </HeaderWrap>
      <CardBody onClick={onClick}>
        <StyledProgress
          trailColor="#efefef"
          strokeColor={{ '0%': color, '100%': color }}
          percent={percent ?? 0}
          status="normal"
          showInfo={false}
        />
        {showRubles ? (
          <Balance>
            <NumberFormat
              value={balance}
              displayType="text"
              thousandSeparator="&thinsp;"
              suffix={` ${RUBLE_SIGN}`}
            />
            <div>
              <span>из </span>
              <NumberFormat
                value={`из ${sum}`}
                displayType="text"
                thousandSeparator="&thinsp;"
                suffix={` ${RUBLE_SIGN}`}
              />
            </div>
          </Balance>
        ) : (
          <PercentStyled>
            {percent}
            &#37;
          </PercentStyled>
        )}
      </CardBody>
    </StyledCard>
  );
};
