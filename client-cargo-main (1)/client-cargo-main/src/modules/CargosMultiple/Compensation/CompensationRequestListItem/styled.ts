import { List } from 'antd';
import styled from 'styled-components';

export const StyledListItem = styled(List.Item)`
  width: 100%;
  display: flex;
  align-items: flex-start !important;
  overflow: hidden;
`;

export const ContentWrapper = styled.div<{ $isMobile?: boolean }>`
  display: flex;
  flex-direction: row;
  width: 100%;
  justify-content: space-between;

  @media (max-width: 767px) {
    flex-direction: column;
  }
`;

export const CargoTariffWrapper = styled.div`
  margin: 0 0 10px 20px;
  display: flex;
  gap: 8px;

  @media (max-width: 767px) {
    margin: 4px 0 10px 0;
  }
`;

export const CargoAddressBlockWrapper = styled.div`
  margin-top: 0;
  display: flex;
  justify-content: flex-end;
  width: 100%;

  @media (max-width: 767px) {
    flex-direction: column;
  }
`;

export const IdWrapper = styled.div`
  display: flex;
  flex-direction: row;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-style: normal;
  font-weight: 600;
  font-size: 16px;
  line-height: 24px;
  letter-spacing: -0.3px;
  color: #262626;
`;

export const CreationTime = styled.div`
  margin: 4px 0 0 12px;
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 14px;
  line-height: 16px;
  color: #909090;

  @media (max-width: 767px) {
    margin: 4px 0 0 0;
  }
`;

export const ApprovalDate = styled(CreationTime)`
  font-size: 12px;
  margin-left: 0;
  margin-top: 4px;
`;

export const DesiredDate = styled.span`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 14px;
  line-height: 16px;
  color: #909090;
`;

export const ButtonWrapper = styled.div`
  display: flex;
  align-items: flex-end;
  gap: 8px;

  @media (max-width: 767px) {
    flex-wrap: nowrap;
  }

  & > * {
    flex-grow: 1;
    min-width: 0;
  }
`;

export const RecipientInfo = styled.div`
  color: #909090;
`;
