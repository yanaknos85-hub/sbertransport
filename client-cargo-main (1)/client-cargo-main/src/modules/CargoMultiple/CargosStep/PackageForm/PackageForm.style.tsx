import styled from 'styled-components';

export const CheckboxWrapper = styled('div')`
  display: flex;
  margin-bottom: 16px;
`;

export const PackageItem = styled('div')`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  width: 100%;
`;

export const PackageName = styled('div')`
  width: 48%;;
`;

export const PackageCount = styled('div')`
  display: flex;
  align-items: center;
  width: 36%;
  position: relative;
`;

export const TrashIcon = styled('div')`
  width: 25px;
  height: 25px;
  position: absolute;
  top: -3px;
  right: -3px;
`;
