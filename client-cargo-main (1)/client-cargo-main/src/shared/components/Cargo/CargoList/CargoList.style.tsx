/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const List = styled.div`
  display: flex;
  flex-wrap: wrap;
  margin: -8px;
`;

export const Item = styled.div`
  display: flex;
  flex-direction: column;
  margin: 8px;
  border-radius: 12px;
  width: calc(33.33% - 16px);
  min-width: 300px;
  flex-wrap: nowrap;
`;

export const ItemBody = styled.div`
  display: flex;
  flex-direction: column;
  box-shadow: 0px 4px 20px rgba(0, 0, 0, 0.08), 0px 0px 1px rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 16px 0;
`;

export const ItemSection = styled.div`
  padding: 0 16px;
`;

export const ItemDescription = styled.div`
  display: flex;
  flex-direction: column;
`;

export const ItemName = styled.div``;

export const ItemType = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
`;

export const ItemTypeName = styled.div`
  margin-bottom: 0;
  margin-left: var(--margin-twelve);
  font-size: var(--fz-12);
  color: var(--gray-7);
`;

export const ItemParams = styled.div`
  display: flex;
  justify-content: space-between;
  width: 100%;
`;

export const ItemParam = styled.div``;

export const ItemParamLabel = styled.div`
  margin-bottom: 0;
  font-size: var(--fz-12);
  color: var(--gray-7);
`;

export const ItemParamValue = styled.div``;
