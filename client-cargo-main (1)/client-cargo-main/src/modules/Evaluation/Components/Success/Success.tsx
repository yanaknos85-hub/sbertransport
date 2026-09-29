import React, { FC } from 'react';

import { ReactComponent as SuccessIcon } from '../../static/icons/successfulIcon.svg';
import { SuccessBlock } from '../../static/styles';

interface Props {
  title: string;
  text: string;
}

export const Success: FC<Props> = ({ title, text }) => (
  <SuccessBlock>
    <SuccessIcon />
    <h3 style={{ marginTop: '30px', fontWeight: 600 }}>{title}</h3>
    <p>{text}</p>
  </SuccessBlock>
);
