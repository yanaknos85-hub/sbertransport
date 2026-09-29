import React, { FC } from 'react';
import styled from 'styled-components';

const Container = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin: 14px 0 12px;
`;

export const TableSettingsContainer: FC = ({ children }) => <Container>{children}</Container>;
