/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import React from 'react';
import { Divider as AntdDevider } from 'antd';
import { BaseButton } from 'shared/form/Button/Button';
import styled from 'styled-components';

import { ReactComponent as DeleteIcon } from './images/deleteIcon.svg';
import { ReactComponent as EditIcon } from './images/editButton.svg';

export const Borderline = styled('div')`
  margin: 21px 0;
  width: 100%;
  height: 1px;
  background-color: #ebebeb;
`;

export const ButtonAddCargo = styled(BaseButton)`
  width: 100%;
  opacity: 0.44;
  color: #262626;
  font-weight: 600;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
  border: none;
  box-shadow: none;

  &:hover {
    color: #262626;
    opacity: 1;
  }
`;

export const Image = styled('img')`
  margin-right: 12px;
`;

export const Container = styled('div')`
  padding: 16px 8px 16px 16px;
  background: #fff;
`;

export const PackageContainer = styled('div')`
  padding: 16px 8px 16px 16px;
  margin-top: 12px;
  background: #fff;
  max-height: 400px;
  border-radius: 8px;
  overflow-y: auto;
`;

export const PackageTitle = styled('div')`
  margin-bottom: 16px;
  color: #000;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 16px;
  font-weight: 600;
`;

export const PackageItemControls = styled('div')`
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 16px;
`;

export const CargoHeader = styled('h3')`
  font-weight: 600;
  margin-bottom: 20px;
`;

// --- Cargo Item

export const CargoItem = styled('div')`
  margin-bottom: 2px;
`;

export const CargoItemInner = styled('div')`
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08), 0 0 1px rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 16px 0;
`;

export const CargoItemContentTop = styled('div')`
  display: flex;
  justify-content: space-between;
  padding: 0 16px;
`;

export const CargoItemContentBottom = styled('div')`
  display: flex;
  justify-content: flex-start;
  padding: 0 16px;
`;

export const CargoItemDescription = styled('div')`
  display: flex;
  flex-direction: column;
`;

export const CargoItemName = styled('div')`
  margin-bottom: 4px;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export const CargoItemType = styled('div')`
  display: flex;
  flex-direction: row;
  align-items: center;
`;

export const CargoItemTypeIcon = styled('div')``;

export const CargoItemTypeName = styled('div')`
  margin-bottom: 0;
  font-size: 12px;
  color: #909090;
  margin-left: var(--margin-twelve);
`;

export const CargoItemButtons = styled('div')`
  display: flex;
  justify-content: space-between;
`;

export const CargoItemBtnEdit = (props: any) => (
  <BaseButton {...props}>
    <EditIcon />
  </BaseButton>
);

const _CargoItemBtnDelete = (props: any) => (
  <BaseButton {...props}>
    <DeleteIcon />
  </BaseButton>
);

export const CargoItemBtnDelete = styled(_CargoItemBtnDelete)`
  margin-left: 8px;
`;

export const CargoItemOptions = styled('div')`
  display: flex;
  justify-content: space-between;
  width: 100%;
`;

export const CargoItemOption = styled('div')``;

export const CargoItemOptionTitle = styled('div')`
  margin-bottom: 5px;
  font-size: 12px;
  color: #909090;
`;

export const CargoItemOptionTitleForNumber = styled('div')`
  margin-bottom: 0;
  font-size: 12px;
  color: #909090;
`;

export const CargoItemOptionValue = styled('div')``;

export const Divider = styled(AntdDevider)`
  margin: 12px 0;
`;

// --- Cargo Form

export const CargoFormBtnCancel = styled(BaseButton)`
  height: 32px;
  padding: 5px 16px;

  &:hover {
    opacity: 0.7;
    color: #4d4d4d;
  }
`;

export const CargoFormBtnDelete = styled(BaseButton)`
  height: 32px;
  padding: 5px 16px;
  color: #ff5743;

  &:hover {
    opacity: 0.7;
  }
`;

export const CargoFormBtnSave = styled(BaseButton)`
  height: 32px;
  background: #f2f3f6;
  border-radius: 8px;
  color: #4d4d4d;
  padding: 5px 16px;
  margin-left: 4px;

  :hover {
    opacity: 0.8;
    background: #e5e6e9;
    color: #4d4d4d;
  }
`;
