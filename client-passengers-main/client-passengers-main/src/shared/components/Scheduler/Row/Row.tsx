import React, { FC } from 'react';
import styled from 'styled-components';

const Row = styled('div')<any>`
  display: flex;
  margin-bottom: 16px;
`;

export const RowItem = styled('div')<any>`
  margin-left: 8px;

  &:first-child {
    margin-left: 0;
  }
`;

const Component: FC = ({ children }) => <Row>{children}</Row>;

export default Component;
