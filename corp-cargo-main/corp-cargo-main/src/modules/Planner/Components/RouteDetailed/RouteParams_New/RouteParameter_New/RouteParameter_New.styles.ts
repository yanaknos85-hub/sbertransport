import styled from 'styled-components';

export const Wrapper = styled.div`
  text-align: center;
  padding: 4px;

  p {
    margin: 0;
  }

  svg {
    width: 16px;
    height: 16px;
  }
`;

export const Block = styled.p<{ fontFamily: string; color: string; }>`
  margin: 0;
  padding: 0;
  letter-spacing: 0.1px;

  font-size: 12px;
  font-weight: 400;

  font-family: ${props => props.fontFamily};
  color: ${props => props.color};
`;

export const Text = styled.p`
  font-size: 12px;
`;
