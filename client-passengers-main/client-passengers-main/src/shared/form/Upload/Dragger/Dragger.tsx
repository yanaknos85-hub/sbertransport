import { DraggerProps } from 'antd/lib/upload';
import React, { FC } from 'react';

import { StyledDragger } from './Dragger.style';

const UploadDragger: FC<DraggerProps> = props => (
  <StyledDragger {...props}>
    {props.children}
  </StyledDragger>
);

export type Props = DraggerProps;

export default UploadDragger;
