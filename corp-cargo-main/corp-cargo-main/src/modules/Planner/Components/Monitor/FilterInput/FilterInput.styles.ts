import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';

import { colors } from 'shared/styles/styles';

export const Button = styled(ButtonAnt)`
  padding: 0;
  line-height: 0;
  border: none;
  outline: none;
  background-color: ${colors.bgDark};

  &:hover {
    background-color: ${colors.bgDark};
  }
`;

export const FormWrapper = styled.div`
  flex-grow: 1;

  & .ant-form-item {
    margin-bottom: 0;
    .ant-input-affix-wrapper {
      height: 40px;
    }
  }

  & .ant-input {
    background-color: ${colors.bgDark} !important;
  }
`;
