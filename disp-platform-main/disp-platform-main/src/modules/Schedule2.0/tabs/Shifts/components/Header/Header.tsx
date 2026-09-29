import React, { FC } from 'react';

import { ZoomInOutlined, ZoomOutOutlined } from '@ant-design/icons';
import { Button } from 'antd';

import { useTranslation } from 'i18n';

import { useShiftsQuery } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';

import { Icon } from 'components/Icon/Icon';
import { SearchPanel } from 'components/SearchPanel/SearchPanel';

import { useModals } from '../../../../context/modal.context';
import { useTableZoom } from '../../context/tableZoom.context';
import { FiltersButton } from './FiltersButton/FiltersButton';
import { PeriodFilter } from './PeriodFilter/PeriodFilter';

import styles from './Header.module.scss';

export const Header: FC = () => {
  const { t } = useTranslation();
  const {
    handleOpenFilters, handleOpenCreate, handleOpenSettings,
  } = useModals();

  const { zoomIn, zoomOut } = useTableZoom();

  const { query, setQuery } = useShiftsQuery();

  const search = (value: string) => {
    if (!value || value.length >= 3) {
      setQuery({
        startDate: query.startDate,
        endDate: query.endDate,
        search: value || undefined,
      });
    }
  };

  return (
    <div className={styles.header}>
      <div className={styles.filters}>
        <SearchPanel
          value={query.search}
          placeholder={t.Shifts.searchTitle}
          onSearch={search}
        />
        <FiltersButton onClick={handleOpenFilters} />
        <PeriodFilter />
      </div>

      <div className={styles.buttonGroup}>
        <ZoomOutOutlined className={styles.zoomButton} onClick={zoomOut} />
        <ZoomInOutlined className={styles.zoomButton} onClick={zoomIn} />

        <Button
          type="text"
          className={styles.settings}
          onClick={handleOpenSettings}
        >
          <Icon className={styles.settings} type="settings" />
        </Button>
        <Button
          className={styles.headerButton}
          type="primary"
          onClick={handleOpenCreate}
          data-name="platform_disp_createShift"
        >
          {t.Shifts.buttonCreate}
        </Button>
      </div>
    </div>
  );
};
