import React, { FC } from 'react';
import { Tag as AnTag } from 'antd';
import { TagProps } from 'antd/lib/tag';
import styled from 'styled-components';

export enum StatusTagTypeEnum {
  SUCCESS = 'success',
  PROCESSING = 'processing',
  ERROR = 'error',
  WARNING = 'warning',
  DEFAULT = 'default',
}

interface StatusTagType {
  type?:
    | StatusTagTypeEnum.SUCCESS
    | StatusTagTypeEnum.PROCESSING
    | StatusTagTypeEnum.ERROR
    | StatusTagTypeEnum.WARNING
    | StatusTagTypeEnum.DEFAULT;
}

const setBackground = (type: any): string => {
  switch (type) {
    case StatusTagTypeEnum.SUCCESS:
      return 'rgba(23, 211, 91, 0.15)';
    case StatusTagTypeEnum.PROCESSING:
      return 'rgba(105, 121, 247, 0.15)';
    case StatusTagTypeEnum.ERROR:
      return 'rgba(254, 91, 59, 0.15)';
    case StatusTagTypeEnum.WARNING:
      return 'rgba(255, 180, 103, 0.15)';
    case StatusTagTypeEnum.DEFAULT:
    default:
      return 'rgba(204, 204, 204, 0.4)';
  }
};

const setColor = (type: any): string => {
  switch (type) {
    case StatusTagTypeEnum.SUCCESS:
      return '#19B150';
    case StatusTagTypeEnum.PROCESSING:
      return '#6979F7';
    case StatusTagTypeEnum.ERROR:
      return '#FE5B3B';
    case StatusTagTypeEnum.WARNING:
      return '#FF9A32';
    case StatusTagTypeEnum.DEFAULT:
    default:
      return '#00000080';
  }
};

const StatusDiv = styled(AnTag)<StatusTagType>`
  padding: 2px 8px;
  background: ${props => setBackground(props.type)};
  color: ${props => setColor(props.type)};
  border-radius: 16px;
  border-color: #f2f3f6;
  font-size: 10px;
  line-height: 14px;
  text-transform: uppercase;
  margin-right: ${props => (props.type ? 0 : '8px')} !important;
`;

/**
 * StatusTag - отображение тэга в виде стилизованного статуса в зависимости от выбранного типа
 * @param props тип статуса
 * @class
 */

const StatusTag: FC<StatusTagType & TagProps> = props => <StatusDiv {...props} />;

export default StatusTag;
