import styled from 'styled-components';

export const Container = styled.div<any>`
  padding: 2px 12px;
  border-radius: 15px;
  position: absolute;
  top: -11px;
  right: ${props => props.right};
  background-color: ${props => props.backgroundColor};
  color: ${props => props.color};
  font-size: 10px;
  text-transform: uppercase;
  box-sizing: border-box;
  border: ${props => (props.border ? `1px solid ${props.border}` : 'none')};
`;
