import styled from 'styled-components';

export const units = {
  weight: 'кг',
  meters: 'м',
  decimal: true,
};

export const StyledPanel = styled.div`
  padding: 9px 24px;
  border-radius: 16px;
  background-color: white;
  overflow: auto;
  line-height: 24px;
`;

export const TextSwitch = styled.span`
  margin: 0 16px 0 5px;
  font-family: 'SB Sans Text Regular', serif;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.3;

  &:nth-child(1) {
    /* margin-left: 0; */
  }
`;
