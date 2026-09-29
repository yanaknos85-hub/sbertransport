import React, { FC } from 'react';
import { NativeButtonProps } from 'antd/lib/button/button';

import { StyledBaseButton, StyledButton, StyledIconButton } from './Button.style';

export const BaseButton: FC<NativeButtonProps> = props => <StyledBaseButton {...props} />;

export const IconButton: FC<NativeButtonProps & { src: JSX.Element }> = ({ src, ...props }) => (
  <StyledIconButton {...props}>{src}</StyledIconButton>
);

const Button: FC<NativeButtonProps> = props => <StyledButton {...props} />;

export default Button;
