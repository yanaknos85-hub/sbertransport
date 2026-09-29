import styled from 'styled-components';

export const Header = styled.div<any>`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: ${props => (props.isAddToRoute ? '20px' : 0)};
  cursor: pointer;
`;
