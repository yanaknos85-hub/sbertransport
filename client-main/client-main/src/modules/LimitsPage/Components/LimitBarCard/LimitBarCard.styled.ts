import styled from 'styled-components';
import { Progress } from 'antd';

export const StyledCard = styled.div`
  width: 270px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #F7F8FA;
  padding: 12px;
  border-radius: 8px;
  cursor pointer;

    @media (max-width: 500px) {
      width: 100%;
      padding: 8px;

      button {
        padding: 8px;
      }
    }

    @media (max-width: 400px) {
      width: 100%;
      flex-wrap: wrap;

      > div:nth-child(3) {
        margin-left: auto; 
      }
    }
`;

export const HeaderWrap = styled.div`
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const CardBody = styled.div`
  width: 100%;

  @media (max-width: 700px) {
    margin-right: auto;
  }
`;

export const StyledTitle = styled.h4`
  font-size: 16px;
  font-weight: 400;
  line-height: 24px;
  letter-spacing: -0.3px;
  font-family: SB Sans Text;
  color: var(--black-text);
  margin: 0;
`;

export const StyledProgress = styled(Progress)`
  width: 100%;
`;

export const IconWrapper = styled.div`
  padding: 18px 9px;
`;

export const StyledIcon = styled.div<{ src: string }>`
  display: flex;
  width: 24px;
  height: 24px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
  background-position: center;
`;

export const Balance = styled.div`
  margin-top: 10px;
  font-size: 12px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: #909090;
  display: flex;
  justify-content: space-between;
  align-items: baseline;

  > span:first-child {
    font-size: 16px;
    font-weight: 600;
    color: #262626;
    margin-right: 4px;
  }
`;

export const PercentStyled = styled.div`
  font-size: 16px;
  font-weight: 600;
  line-height: 22px;
  color: var(--black-text);
  letter-spacing: -0.3px;
`;
