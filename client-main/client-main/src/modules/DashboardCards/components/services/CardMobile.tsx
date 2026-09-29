import * as React from 'react';
import { useHistory } from 'react-router-dom';
import TButton from 'shared/ui/Button/Button';
import styled from 'styled-components';

import {
  Price,
  StyledCardMobile,
  StyledTitle
} from '../../styled';
import { Service } from './types';
import { ServiceTypeEnum } from 'constants/constants.app';

const StyledTitleMobile = styled(StyledTitle)`
  font-size: 16px;
`;

const IconMobileWrap = styled.div`
  width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const Root = styled.div``;

const accentColors = ['#27bea4', '#fda500', '#5d9aa7', '#7488fa', '#fd92a7'];

const CardMobile: React.FC<Service & { index: number }> = ({
  index,
  serviceType,
  price,
  link,
  title,
  buttonIsDisabled,
  buttonText,
  icon,
  hidden,
}) => {
  const history = useHistory();

  // todo !!! удалить условие, когда будет готова многоточка !!!
  if (hidden) {
    return null;
  }

  return (
    <Root
      onClick={() => {
        if (!buttonIsDisabled) {
          history.push(link);
        }
      }}
    >
      <StyledCardMobile accentColor={accentColors[index % accentColors.length]}>
        <div>
          <StyledTitleMobile margin="0px 0px 6px 0px">{title}</StyledTitleMobile>
          {price > 0 ? (
            <Price>
              от
              {' '}
              {price}
              {' '}
              ₽
              {serviceType === ServiceTypeEnum.PARKING && ' в минуту'}
            </Price>
          ) : (
            <Price>
              <strong />
            </Price>
          )}
          <TButton
            $size="small"
            style={{
              cursor: buttonIsDisabled ? 'not-allowed' : 'pointer',
              backgroundColor: '#F2F3F6',
              color: '#4D4D4D',
              borderColor: '#F2F3F6',
              width: '107px',
              padding: '0 20px',
              fontFamily: 'SB Sans Text',
              fontSize: '16px',
              fontWeight: 600,
              lineHeight: '18px',
              letterSpacing: '-1.3px',
            }}
          >
            {buttonText}
          </TButton>
        </div>
        <IconMobileWrap>{icon}</IconMobileWrap>
      </StyledCardMobile>
    </Root>
  );
};

export default CardMobile;
