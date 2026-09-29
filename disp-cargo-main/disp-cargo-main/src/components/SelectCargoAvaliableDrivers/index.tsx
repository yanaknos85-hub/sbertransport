import { Spin, Select as AntdSelect } from 'antd';
import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import { getFullName } from 'utils/getFullName';
import { useTranslation } from 'i18n';
import { StarFilled } from '@ant-design/icons';
import styles from './index.module.scss';
import { Select } from '../Select';
import { UUID } from 'utils/io-ts';
import { DriverShort, DriversLocation } from 'api/trips-cargo/trips-cargo.types';
import { useProfile } from 'api/profile/profile.api';
import { useCargoDriversLocationsMutation } from 'api/trips-cargo/trips-cargo.api';

interface OptionProps {
  id: string;
  shiftId?: UUID | null;
  firstName?: string | null;
  lastName?: string | null;
  patronymic?: string | null;
  currentShift?: {
    driver?: { online: boolean } | null;
    vehicle: { stateNumber: string } | null;
  } | null;
  rating?: number;
  forPlanning?: boolean;
}

const Option: FC<OptionProps> = ({
  id,
  shiftId,
  firstName = '',
  lastName = '',
  patronymic,
  currentShift,
  rating,
  forPlanning,
}) => (
  <AntdSelect.Option value={forPlanning ? shiftId! : id}>
    <div className={styles.driverSelectOption}>
      <span>
        {getFullName({
          firstName, lastName, patronymic,
        })}
        {' '}
        <StarFilled />
        {' '}
        {(rating ?? 0) / 100}
      </span>
      <span>
        {currentShift?.driver?.online && <span className={styles.online}>online </span>}
        {currentShift?.vehicle?.stateNumber && (
        <span>
          [
          {currentShift?.vehicle.stateNumber}
          ]
        </span>
        )}
      </span>
    </div>
  </AntdSelect.Option>
);

interface SelectAvaliableDriversProps extends React.ComponentProps<typeof Select> {
  latitude: number;
  longitude: number;
  deadline?: string | null;
  defaultSelectedDriver?: DriverShort | null;
  forPlanning?: boolean;
}

export const SelectCargoAvaliableDrivers: FC<SelectAvaliableDriversProps> = ({
  latitude,
  longitude,
  deadline,
  defaultSelectedDriver,
  forPlanning,
  ...props
}) => {
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [allDrivers, setAllDrivers] = useState<DriversLocation[]>([]);
  const [fullSearch] = useState(true);

  const { contractorId } = useProfile().data;
  const { t } = useTranslation();

  const [getAvaliableDrivers, { isLoading }] = useCargoDriversLocationsMutation(contractorId);

  useEffect(() => {
    setPage(0);
  }, [contractorId]);

  const handleScroll = useCallback(
    e => {
      const target = e.target as HTMLDivElement;
      if (isLoading || page >= totalPages) {
        return;
      }

      if (target.scrollTop + target.offsetHeight + 200 < target.scrollHeight) {
        return;
      }

      setPage(p => p + 1);
    },
    [page, totalPages, isLoading]
  );

  useEffect(() => {
    if (!longitude && !latitude) {
      return;
    }

    getAvaliableDrivers({
      latitude,
      longitude,
      fullSearch,
      enableShiftFilter: true,
      page,
      size: 20,
      deadline: deadline ?? undefined,
      forPlanning,
    }).then(response => {
      setAllDrivers(prev => (response ? (page === 0 ? response.content : [...prev, ...response.content]) : prev));
      setTotalPages(response?.totalPages ?? Infinity);
    });
  }, [fullSearch, page, getAvaliableDrivers, latitude, longitude, deadline, forPlanning]);

  return (
    <Select
      {...props}
      onPopupScroll={handleScroll}
      placeholder={t.Drivers.chooseDriver}
    >
      {defaultSelectedDriver
      && !allDrivers.some(({ id }) => id === defaultSelectedDriver.id)
      && Option({ ...defaultSelectedDriver, forPlanning })}
      {allDrivers.map(driver => Option({ ...driver, forPlanning }))}
      {isLoading && (
        <AntdSelect.Option
          key="loading"
          disabled
          value=""
        >
          {t.global.load}
          {' '}
          ...
          <Spin />
        </AntdSelect.Option>
      )}
    </Select>
  );
};
