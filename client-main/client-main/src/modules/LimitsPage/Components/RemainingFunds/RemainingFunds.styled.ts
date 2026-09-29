import { Progress, Button } from 'antd';
import styled from 'styled-components';

export const Container = styled.div`
  width: 50%;
  max-width: 548px;
  padding: 16px;
  border-radius: 8px;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
  gap: 8px;

  @media (max-width: 800px) {
    width: 100%;
    max-width: 100%;
  }
`;

export const HeadWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const StyledTitle = styled.h3`
  font-size: 18px;
  font-weight: 600;
  line-height: 24px;
  font-family: SB Sans Text;
  color: var(--black-text);
  margin: 0;
`;

export const Balance = styled.div`
  min-width: 300px;
  font-size: 16px;
  font-weight: 400;
  line-height: 24px;
  letter-spacing: -0.3px;
  color: var(--black-shading-text);
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  flex-wrap: wrap;

  > span:first-child {
    font-size: 30px;
    font-weight: 700;
    line-height: 38px;
    color: var(--black-text);
    margin-right: 8px;

    @media (max-width: 500px) {
      font-size: 24px;
    }
  }

  @media (max-width: 500px) {
    flex-direction: column;
    gap: 4px;
  }
`;

export const StyledProgress = styled(Progress)`
  font-size: 0;
`;

export const StyledButton = styled(Button)`
  padding: 5px 16px;
  border-radius: 8px;
  border-color: #c2c2c2;
  background-color: transparent;
  outline: none;

  > span {
    font-size: 14px;
    font-weight: 600;
    line-height: 22px;
    letter-spacing: -0.3px;
    font-family: SB Sans Text;
    color: var(--black-text);
  }

  &:hover,
  &:focus {
    border-color: #0ba85d;
  }

  &:active {
    border-color: #0d9954;
  }
`;

export const PercentStyled = styled.div`
  font-size: 30px;
  font-weight: 700;
  line-height: 38px;
  color: var(--black-text);
`;
