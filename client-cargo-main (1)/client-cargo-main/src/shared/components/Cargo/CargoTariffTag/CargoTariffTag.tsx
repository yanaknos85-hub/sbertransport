import React, { FC } from 'react';
import { Tag as AnTag } from 'antd';
import { TagProps } from 'antd/lib/tag';
import styled from 'styled-components';

interface CargoTariffTagType {
  transportType?: 'courier' | 'dedicated' | 'interregional';
  express?: 'true' | 'false';
  $isMobile?: boolean;
}

const CargoTariffTagDiv = styled(AnTag)<CargoTariffTagType>`
  display: flex;
  height: 28px;
  width: fit-content;
  border: 1px solid rgba(38, 38, 38, 0.08);
  box-sizing: border-box;
  border-radius: 20px;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: ${props => (props.$isMobile ? '10px' : '12px')};
  line-height: 20px;
  letter-spacing: ${props => (props.$isMobile ? '-0.2px' : '-0.154px')};
  color: #000000;
  background-size: contain;
  padding: 0 14px;
  align-content: center;
  align-items: center;
`;

/**
 * CargoTariffTag - отображение тэга в виде комбинации иконки типа транспорта, названия типа транспорта и типе тарифа
 * @param props
 * @class
 */

const CargoTariffTag: FC<CargoTariffTagType & TagProps> = props => <CargoTariffTagDiv {...props} />;

export default CargoTariffTag;
