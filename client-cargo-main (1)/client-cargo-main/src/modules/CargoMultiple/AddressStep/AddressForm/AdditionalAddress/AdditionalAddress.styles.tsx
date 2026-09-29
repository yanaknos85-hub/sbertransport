import styled from 'styled-components';

export const Section = styled('div')`
  margin-top: 24px;
  margin-bottom: 24px;

  &:first-child {
    border-bottom: 1px solid rgba(38, 38, 38, 0.08);
  }

  &:first-child + & {
    margin-top: 24px;
`;

export const Header = styled('div')`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;

  & p {
    margin-left: 0;
    font-weight: 600;
  }
`;

export const HeaderTitle = styled('div')`
  margin-left: 0;
  font-weight: 600;
`;

export const Trash = styled('div')`
  display: flex;
  justify-content: flex-end;
`;

export const AddressInfo = styled('div')`
  display: flex;
  gap: 12px;
`;

export const RelocationSwitch = styled('span')`
  margin-left: 8px;
`;

export const ContactHeader = styled('div')`
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  cursor: pointer;
`;

export const AddContactButton = styled('div')`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-weight: bold;
  margin-bottom: 12px;
  cursor: pointer;
`;
