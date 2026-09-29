import React, { FC } from 'react';
import { WaypointType, WaypointWithType } from 'types/waypoint';
import { getAddress } from 'utils/getAddress';
import { Collapse } from 'antd';
import Flex from 'components/Flex/Flex';
import { getFullName } from 'utils/getFullName';
import styles from './waypointInfo.module.scss';
import Text from 'antd/lib/typography/Text';
import Card from '../Card/Card';
import cn from 'classnames';
import styled from 'styled-components';
import { colorMap } from 'constants/waypoint.constants';
import { bgColorMap, waypointTypeTitle } from './waypointInfo.constants';
import { getWaypointActionText } from './waypointInfo.utils';
import moment from 'moment';
import { EmployeeShort } from 'api/trips/trips.types';
import { DATE_FORMAT } from 'constants/app.constants';
import { CheckinType } from 'constants/trips.constants';

const WaypointAction = styled.div<{ type: WaypointType }>`
  background: ${({ type }) => bgColorMap[type]};
  color: ${({ type }) => colorMap[type]};
  padding: 2px 10px;
  font-size: 10px;
  border-radius: 16px;
  white-space: nowrap;
`;

interface HeaderProps {
  waypoint: WaypointWithType;
  index: number;
  length: number;
  passengerCount?: number;
}

const Header: FC<HeaderProps> = ({
  waypoint,
  index,
  length,
  passengerCount,
}) => {
  const addressTitle = index === 0
    ? 'Адрес подачи'
    : index === length - 1
      ? 'Адрес назначения'
      : `Остановка №${index}`;

  const waypointActionText = getWaypointActionText(waypoint.type, index, passengerCount);

  return (
    <div className={styles.mainContent}>
      <div>
        <div className={styles.addressTitle}>{addressTitle}</div>

        <Text
          strong
          className={cn({ [styles.noCheckin]: waypoint.type === 'PASSED_NO_CHECKIN' })}
        >
          {waypoint.fullAddress ?? getAddress(waypoint)}
        </Text>

        <Flex gap={10}>
          {waypoint.arriveTime && (
            <Card>
              {moment(waypoint.arriveTime).format(DATE_FORMAT.TIME)}
              {' '}
              {waypoint.leaveTime && (
              <span>
                -
                {moment(waypoint.leaveTime).format(DATE_FORMAT.TIME)}
              </span>
              )}
            </Card>
          )}
          {!!passengerCount && (
          <Card>
            Мест
            {passengerCount}
          </Card>
          )}
        </Flex>

        <div className={cn(styles.waypointTypeTitle, {
          [styles.noCheckin]: waypoint.type === 'PASSED_NO_CHECKIN',
          [styles.autoCheckin]: waypoint.checkinType === CheckinType.AUTO,
          [styles.manualCheckin]: waypoint.checkinType === CheckinType.MANUAL,
        })}
        >
          {waypointTypeTitle[waypoint.type]}
        </div>
      </div>

      <div>
        {waypointActionText && <WaypointAction type={waypoint.type}>{waypointActionText}</WaypointAction>}
      </div>
    </div>
  );
};

interface WaypointInfoProps {
  waypoint: WaypointWithType;
  index: number;
  length: number;
  passengerCount?: number;
  passengers?: EmployeeShort[] | WaypointWithType['contact'][];
}

const WaypointInfo: FC<WaypointInfoProps> = ({
  waypoint,
  index,
  length,
  passengerCount,
  passengers,
}) => {
  return (
    <Collapse ghost>
      <Collapse.Panel
        key="waypoint"
        className={styles.waypointInfo}
        header={(
          <Header
            waypoint={waypoint}
            index={index}
            length={length}
            passengerCount={passengerCount}
          />
)}
      >
        {passengers?.map((pass, passIndex) => (
          <div key={passIndex}>
            <div>{pass.name ?? getFullName(pass)}</div>
            <div className={styles.mobilePhone}>{pass.mobilePhone ?? pass.phone}</div>
            <div>{pass.organization}</div>
          </div>
        ))}
      </Collapse.Panel>
    </Collapse>
  );
};

export default WaypointInfo;
