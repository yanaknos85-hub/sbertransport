import React, { FC } from 'react';
import { Tooltip } from 'antd';
import { QuestionCircleFilled } from '@ant-design/icons';

export const HelperTooltip: FC<{ text: string }> = ({ text }): JSX.Element => (
  <Tooltip placement="top" title={text}>
    <QuestionCircleFilled style={{ color: '#ccc' }} />
  </Tooltip>
);
