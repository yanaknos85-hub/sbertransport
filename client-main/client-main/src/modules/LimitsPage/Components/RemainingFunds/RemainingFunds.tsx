import React, { FC } from 'react';
import NumberFormat from 'react-number-format';
import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { RUBLE_SIGN } from 'constants/constants.app';
import { toRubles } from 'utils';
import { getAvailablePercentage } from 'utils/trips';
import { chooseColorByPercent } from '../../utils';
import { LimitRequestModal } from 'modules/LimitsPage/LimitRequestModal/LimitRequestModal';
import { StoreNames } from 'stores/StoreNames.enum';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import {
  Container,
  HeadWrapper,
  StyledTitle,
  StyledProgress,
  Balance,
  StyledButton,
  PercentStyled
} from './RemainingFunds.styled';

interface BalanceProps {
  limit: LimitSharing;
  title: string;
  limitType: LIMIT_TYPE;
  isDepartmentHead?: boolean;
  isPersonal?: boolean;
  restrict?: string[];
  onClick?: () => void;
}

export const RemainingFunds: FC<BalanceProps> = ({
  title, limit, limitType, isDepartmentHead, isPersonal, restrict, onClick,
}) => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const percent = getAvailablePercentage(limit);
  const color = chooseColorByPercent(percent);
  const balance = toRubles(limit?.limitSharingPerPeriodDTO?.balance ?? 0);
  const sum = toRubles(limit?.limitSharingPerPeriodDTO?.sum ?? 0);
  const showRubles = isDepartmentHead || limitType === LIMIT_TYPE.EMPLOYEE;

  const handleSendRequest = () => {
    limitsStore.refreshLimits();
  };

  return (
    <Container>
      <HeadWrapper>
        <StyledTitle>{title}</StyledTitle>
        {!!isPersonal
        && (
        <LimitRequestModal
          requestButtonName="Запросить"
          restrict={restrict}
          onSendRequest={handleSendRequest}
        />
        )}
        {onClick && <StyledButton onClick={onClick}>Управлять</StyledButton>}
      </HeadWrapper>
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
    </Container>
  );
};
