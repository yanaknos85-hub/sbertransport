import React, { FC } from 'react';
import { NativeButtonProps } from 'antd/lib/button/button';

import { StyledBaseButton, StyledButton, StyledIconButton } from './Button.style';

type ButtonProps = NativeButtonProps & {
  disabled?: boolean;
};

export const BaseButton: FC<NativeButtonProps> = props => <StyledBaseButton {...props} />;

export const IconButton: FC<ButtonProps & { src: JSX.Element }> = ({
  disabled, src, ...props
}) => (
  <StyledIconButton disabled={disabled} {...props}>{src}</StyledIconButton>
);

const Button: FC<NativeButtonProps> = props => <StyledButton {...props} />;

export default Button;
