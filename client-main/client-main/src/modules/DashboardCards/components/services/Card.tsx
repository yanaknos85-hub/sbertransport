import { Tooltip } from 'antd';
import * as React from 'react';
import { useHistory } from 'react-router-dom';
import TButton from 'shared/ui/Button/Button';

import {
  CardBody,
  Description,
  Header,
  Price,
  StyledCard,
  StyledColumnLeft,
  StyledColumnRight,
  StyledTitle
} from '../../styled';
import { Service } from './types';
import { ServiceTypeEnum } from 'constants/constants.app';

const accentColors = ['#27bea4', '#fda500', '#5d9aa7', '#7488fa', '#fd92a7'];

const Card: React.FC<Service & { index: number }> = ({
  index,
  serviceType,
  price,
  link,
  title,
  buttonIsDisabled,
  buttonText,
  icon,
  children,
  content,
  hidden,
}) => {
  const history = useHistory();

  // todo !!! удалить условие, когда будет готова многоточка !!!
  if (hidden) {
    return null;
  }

  return (
    <StyledCard accentColor={accentColors[index % accentColors.length]}>
      <Header>
        <StyledTitle margin="0px 0px 6px 0px">{title}</StyledTitle>
      </Header>
      {children && <CardBody>{children}</CardBody>}
      <Description>
        <StyledColumnLeft>
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
            <Price><strong /></Price>
          )}
          {content}
        </StyledColumnLeft>
        <StyledColumnRight disabled={buttonIsDisabled}>
          <Tooltip title={buttonIsDisabled ? 'Необходимо пополнить лимит' : undefined} placement="topLeft">
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
                fontSize: '18px',
                fontWeight: 600,
                lineHeight: '22px',
                letterSpacing: '-1.3px',
              }}
              onClick={() => {
                if (!buttonIsDisabled) {
                  history.push(link);
                }
              }}
            >
              {buttonText}
            </TButton>
          </Tooltip>
          {icon}
        </StyledColumnRight>
      </Description>
    </StyledCard>
  );
};

export default Card;
