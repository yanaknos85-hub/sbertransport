import styled from 'styled-components';

export const AddressSide = styled('div')`
  .addressPoint {
    margin: 0;
    font-size: 12px;
    color: #909090;
  }

  .address {
    margin: 4px 0 0;
    font-size: 16px;
  }
`;

export const DateSide = styled('div')`
  display: flex;
  flex-direction: column;
  text-align: right;

  div {
    font-size: 14px;
    color: #909090;
    font-family: 'SB Sans Interface', serif, sans-serif;

    span {
      color: #000;
      white-space: nowrap;
    }
  }

  p {
    margin: 0;
    font-size: 12px;
    font-family: 'SB Sans Interface SemiBold', serif, sans-serif;
    color: #10bf6a;
  }
`;

export const Content = styled('div')`
  background: rgba(242, 243, 246, 0.6);
  border-radius: 10px;
  padding: 16px;
  margin-top: 24px;
  @media (max-width: 767px) {
    flex-direction: column;
  }
`;

export const TopRow = styled('div')`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
`;

export const MiddleRow = styled('div')``;

export const BottomRow = styled('div')`
  text-align: right;
`;

export const CargoTariffDivFeedback = styled('div')`
  margin: 0;
`;
