import React, { FC } from 'react';
import TTypography from 'shared/components/Cargo/TTypography';

import { Header } from './HeaderComponent.style';

interface Props {
  title: string;
  subTitle?: string;
}

export const HeaderComponent: FC<Props> = ({ title, subTitle }) => (
  <Header>
    <TTypography className="title">{title}</TTypography>
    <TTypography className="description">{subTitle}</TTypography>
  </Header>
);
