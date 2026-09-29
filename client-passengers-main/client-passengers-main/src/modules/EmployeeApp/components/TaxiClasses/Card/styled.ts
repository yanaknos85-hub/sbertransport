import styled from 'styled-components';

interface LimitStatus {
  $value: number;
  $unit: string;
  $color: string;
}

const ClassHeader = styled.div`
  font-size: 14px;
  color: #000000;
  letter-spacing: -0.154px;
  line-height: 20px;
  font-family: 'SB Sans Text';
  margin: 0 0 10px 0;
  font-weight: 600;
  text-align: left;
`;

const LimitStatus = styled.div<LimitStatus>`
  background-color: ${props => props.$color};
  width: 8px;
  height: 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;

  &:after {
    content: '${props => `Лимит ${props.$value}${props.$unit}`}';
    margin-left: 14px;
    font-size: 12px;
    line-height: 14px;
    color: #a8abb3;
    white-space: nowrap;
  }
`;

const FlexItem = styled.div`
  flex: 1.5 1;
  font-family: 'SB Sans Text';
`;

const WaitingTime = styled.div`
  flex: 1;
  color: var(--jade);
  font-size: 12px;
  line-height: 14px;
  display: flex;
  justify-content: flex-end;
  padding: 0 12px;
`;

export {
  ClassHeader, LimitStatus, FlexItem, WaitingTime
};
