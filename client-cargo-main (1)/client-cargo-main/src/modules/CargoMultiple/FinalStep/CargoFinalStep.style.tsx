/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Col } from 'antd';
import { MapComponent } from 'shared/components/Map/MapComponent';
import styled from 'styled-components';

export const ColAddress = styled(Col)`
  width: 100%;

  @media (min-width: 1260px) {
    width: 50%;
  }
`;

export const Cargos = styled('div')`
  display: flex;
  flex-wrap: wrap;
  flex-direction: column;
  justify-content: flex-start;
  margin-bottom: 32px;
`;

export const CargosHeader = styled('div')`
  display: flex;
  align-items: flex-end;
  margin-bottom: 18px;
`;

export const CargosHeaderText = styled('div')`
  font-family: 'SB Sans Interface', serif, sans-serif;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 600;
  font-size: 16px;
  line-height: 1.5;
`;

export const CargosHeaderCount = styled('div')`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 20px;
  line-height: 24px;
  font-weight: 600;
  color: #c2c2c2;
  margin-left: 8px;
`;

export const Map = styled(MapComponent)`
  height: 214px;
  width: 100%;
  margin-bottom: var(--margin-base);
  margin-top: var(--margin-base);
  border-radius: calc(var(--border-radius-base) * 4);
`;
