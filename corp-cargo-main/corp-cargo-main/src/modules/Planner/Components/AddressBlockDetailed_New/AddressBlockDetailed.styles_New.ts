import styled from 'styled-components';

import { Button as AntButton } from 'antd';

export const AddressMainDiv = styled.div`
  display: flex;
`;

export const AddressInfo = styled.div`
  width: 77%;
`;

export const AddressMainDivDetailed = styled.div`
  display: flex;
  height: 20px;
`;

export const AddressDiv = styled.div`
  position: relative;
  left: 16px;

  height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 88%;
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
