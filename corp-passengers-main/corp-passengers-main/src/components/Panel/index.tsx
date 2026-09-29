import * as React from 'react';
import styled from 'styled-components';
import { Toolbar } from 'components/Toolbar';

const StyledPanel = styled.div`
  margin: 8px;
  padding: 16px;
  padding-top: 32px;
  border-radius: 16px;
  background-color: white;
  box-shadow: 0 4px 12px 4px rgba(4, 0, 58, 0.04), 0 0 1px 0 rgba(3, 0, 27, 0.04);
  overflow: auto;
`;

const StyledPanelTitle = styled.h1`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
  margin: 0 -16px;
  height: 78px;
  line-height: 32px;
  color: inherit;
  font-weight: bold;
  padding-left: 32px;
  padding-right: 32px;
  font-size: 24px;
  margin-bottom: 16px;

  &:first-child {
    border-top: none;
    margin-top: -32px;
  }
`;

export const PanelTitle: React.FC<{ className?: string }> = ({ children, className }) => (
  <StyledPanelTitle className={className}>
    {children}
    <Toolbar />
  </StyledPanelTitle>
);

const Panel: React.FC<{ title?: string; className?: string }> = ({
  title, children, className,
}) => (
  <StyledPanel className={className}>
    {title ? <PanelTitle>{title}</PanelTitle> : null}
    {children}
  </StyledPanel>
);

export default Panel;
