import styled from 'styled-components';

export const CargoParams = styled.div`
  text-align: center;

  p {
    margin-bottom: 0;
  }
`;

export const SemiBoldText = styled.p<{ fontFamily: string; color: string}>`
  margin: 0;
  padding: 0;

  font-size: 14px;
  font-weight: 600;

  font-family: ${props => props.fontFamily};
  color: ${props => props.color};
`;

export const Text = styled.p`
  font-size: 12px;
  color: rgb(173, 173, 173);
`;
