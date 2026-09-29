import { Form as AntForm } from 'antd';
import React, { FC } from 'react';
import styled from 'styled-components';

const StyledForm = styled(AntForm)`
  color: var(--black-text);

  .ant-input {
    height: 52px;
    padding: 4px 11px;
    border-radius: calc(3 * var(--indent-fourth));
  }

  .ant-form-item-explain.ant-form-item-explain-error {
    color: #fff;
    text-align: left;
    padding-left: 10px;
  }
`;

/**
 * TForm - кастомный компонент формы проекта Сбертранспорт
 * */

const TForm: FC<any> = props => <StyledForm {...props} />;

export default TForm;
