import styled from 'styled-components';
import { Link } from 'react-router-dom';
import { List } from 'antd';

export const ListItemStyled = styled(List.Item)`
  width: 100%;
  display: flex;
  flex-flow: column nowrap;
  align-items: flex-start;
  overflow: hidden;
  margin: 0 auto;
  padding-right: 20px;
  background-color: var(--pure-white);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08), 0 0 1px rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 16px 0 20px;
  border-bottom: none !important;
`;

export const ItemHeader = styled.div`
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;

  @media (max-width: 500px) {
    flex-wrap: wrap;
  }
`;

export const StyledTitle = styled.h4`
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: -0.3px;
  font-family: SB Sans Text;
  color: var(--black-text);
  margin: 0;
`;

export const InfoWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;

  @media (max-width: 500px) {
    margin-top: 8px;
    width: 100%;
    justify-content: space-between;
  }
`;

export const TimeInfo = styled.div`
  font-size: 14px;
  font-weight: 400;
  line-height: 16px;
  font-family: SB Sans Interface;
  color: var(--gray-7);

  > time {
    color: var(--gray9);
    white-space: nowrap;
  }
`;

export const StyledIcon = styled.div<{ src: string }>`
  display: flex;
  width: 32px;
  height: 32px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
  background-position: center;
  margin-right: 12px;
`;

export const StyledBody = styled.div`
  margin-top: 8px;
  padding: 0 24px;
`;

export const RequestInfo = styled.div`
  font-size: 14px;
  font-weight: 600;
  line-height: 16px;
  font-family: SB Sans Text;
  letter-spacing: 0.15px;
  color: #a8abb3;
`;

export const TransportStyled = styled.div`
  display: flex;
  align-items: center;
  margin-top: 12px;
`;

export const ItemFooter = styled.div`
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-bottom: 10px;
  border-top: 1px solid rgba(38, 38, 38, 0.08);
  padding: 10px 24px 0;
`;

export const StyledSum = styled.div`
  display: flex;
  flex-direction: column;
  font-size: 12px;
  font-weight: 400;
  line-height: 16px;
  font-family: SB Sans Interface;
  letter-spacing: 0.2px;
  color: var(--gray-7);

  > span {
    font-size: 24px;
    font-weight: 700;
    line-height: 32px;
    color: rgba(0, 0, 0, 0.9);
    margin-top: 4px;
  }
`;

export const StyledLink = styled(Link)`
  height: 40px;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  font-family: SB Sans Text;
  letter-spacing: -0.3px;
  color: var(--black-text);
  padding: 9px 20px;
  border-radius: 8px;
  background-color: var(--solitude);
`;
