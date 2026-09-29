import styled from 'styled-components';

import { Button as AntButton } from 'antd';

export const AddressMainDiv = styled.div`
  display: flex;
`;

export const AddressInfo = styled.div`
  flex: 3 0 0;
`;

export const AddressTitle = styled.div`
  font-family: 'SB Sans Interface', serif, sans-serif;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 600;
  font-size: 16px;
  line-height: 1.5;
  margin-bottom: 18px;
`;

export const AddressMainDivDetailed = styled.div`
  display: flex;
`;

export const AddressDiv = styled.div`
  display: grid;
  position: relative;
  left: 16px;
  height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 88%;
  font-weight: 600;
`;

export const AddressString2 = styled.div`
  display: flex;
  flex-direction: column;
`;
export const AddressStringCollapsed = styled.div`
  flex-direction: row;
`;
export const buttonsBlock = styled.div`
  display: flex;
  margin-left: auto;
  margin-right: 0;
  margin-top: auto;
`;
export const Address = styled.div`
  width: 100%;
`;

export const ShowAdditionalAddressesButton = styled(AntButton)`
  position: relative;
  top: -4px;
  right: 4px;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 12px;
  line-height: 22px;
  align-items: center;
  text-align: right;
  letter-spacing: -0.243077px;
  color: #10bf6a;
`;

export const HideAdditionalAddressesButton = styled(AntButton)`
  position: relative;
  top: -4px;
  right: -70px;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 12px;
  line-height: 22px;
  align-items: center;
  text-align: right;
  letter-spacing: -0.243077px;
  color: #10bf6a;
`;
