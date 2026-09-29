import React, { FC } from 'react';
import { InfoCircleOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { useTranslation } from 'i18n';
import Flex from 'components/Flex/Flex';

interface DriverTitleProps {
  isPlanned?: boolean;
}

const DriverTitle: FC<DriverTitleProps> = ({ isPlanned }) => {
  const { t } = useTranslation();

  return (
    <Flex gap={10} alignItems="center">
      <span>Водитель</span>
      {isPlanned && (
        <Tooltip title={t.Drivers.driverPlanned}>
          <InfoCircleOutlined />
        </Tooltip>
      )}
    </Flex>
  );
};

export default DriverTitle;
