import styled from 'styled-components';

export const RouteParams = styled.div`
  p {
    margin: 0;
    padding: 0;
  }
`;

export const SemiBoldText = styled.p<any>`
  margin: 0;
  padding: 0;

  font-size: 16px;
  font-weight: 600;

  font-family: ${props => props.fontFamily};
  color: ${props => props.color};
`;
