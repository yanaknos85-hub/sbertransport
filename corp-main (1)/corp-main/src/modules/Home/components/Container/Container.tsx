import React, { FC, ReactNode } from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';

import {
  REDESIGN_HOME
} from 'constants/constants.routes';

import styles from './Container.module.scss';

const BaseContainer = styled.div<{ margin?: string }>`
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  padding: 12px 22px 20px;
  background: #fff;
  box-shadow: 0 0 1px rgba($color: #000000, $alpha: 0.12), 0 4px 8px -2px rgba($color: #000000, $alpha: 0.12);
  border-radius: 12px;
  margin-top: 16px;
  margin: ${({ margin }) => margin};
`;

const WrapperTitle = styled.div<{ margin?: string }>`
  display: flex;
  justify-content: space-between;
`;

interface Props {
  title?: string;
  margin?: string;
  button?: ReactNode;
}

export const Container: FC<Props> = ({
  title,
  margin,
  button,
  children,
}) => (
  <BaseContainer margin={margin}>
    <WrapperTitle>
      <h3 className={styles.title}>{title}</h3>
      {button
      && (
      <Link to={REDESIGN_HOME}>
        {button}
      </Link>
      )}
    </WrapperTitle>
    {children}
  </BaseContainer>
);
