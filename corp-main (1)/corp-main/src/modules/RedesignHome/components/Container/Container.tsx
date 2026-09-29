import React, { FC } from 'react';
import styled from 'styled-components';

import { IHotButtons } from 'modules/RedesignHome/types/Home.types';
import { IUiPreferences } from 'stores/Home/Home.interface';

import { ColumnVisibilitySettings } from '../ColumnVisibilitySettings/ColumnVisibilitySettings';

import styles from './Container.module.scss';

const BaseContainer = styled.div<{ margin?: string }>`
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  padding: 12px 22px 20px;
  background: #fff;
  box-shadow: 0 0 1px rgba($color: #000000, $alpha: 0.12), 0 4px 8px -2px rgba($color: #000000, $alpha: 0.12);
  margin: ${({ margin }) => margin};
`;

interface Props {
  title?: string;
  margin?: string;
  isPopularService?: boolean;
  hotButtons?: IHotButtons[];
  popularServices?: IUiPreferences;
}

export const Container: FC<Props> = ({
  title,
  margin,
  children,
  isPopularService,
  hotButtons,
  popularServices,
}) => (
  <BaseContainer margin={margin}>
    <div className={styles.title_wrapper}>
      <h3 className={styles.title}>{title}</h3>
      {isPopularService && (
        <ColumnVisibilitySettings popularServices={popularServices} hotButtons={hotButtons} />
      )}
    </div>
    {children}
  </BaseContainer>
);
