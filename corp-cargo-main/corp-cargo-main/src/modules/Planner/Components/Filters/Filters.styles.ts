import { Button as ButtonAnt, Form as FormAnt } from 'antd';
import { colors } from 'shared/styles/styles';

import styled from 'styled-components';

// @deprecated
export const FormWrapper = styled(FormAnt)`
  flex-grow: 2;

  & .ant-form-item {
    margin-bottom: 0;
  }
`;

export const Button = styled(ButtonAnt)`
  padding: 0;
  line-height: 0;
  border: none;
  outline: none;
  background-color: ${colors.bgDark};
  width: 50%;

  &:hover {
    background-color: ${colors.bgDark};
  }
`;
