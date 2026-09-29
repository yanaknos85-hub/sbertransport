import React, { FC } from 'react';

import { Tooltip } from 'antd';
import * as S from './Icon.styles';

interface Props {
  styles?: Record<string, string>;
  onClick?: () => void;
  icon: any;
  tooltip?: string;
}

export const Icon: FC<Props> = ({
  icon, styles, onClick, tooltip,
}) => (
  <Tooltip title={tooltip}>
    <S.Icon onClick={onClick} style={styles}>
      {icon}
    </S.Icon>
  </Tooltip>
);
