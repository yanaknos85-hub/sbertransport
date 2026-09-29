import React, {
  Dispatch, FC, SetStateAction, useCallback
} from 'react';
import { Tooltip } from 'antd';
import cn from 'classnames';

import { Button } from 'components/Button';
import { Icon } from 'components/Icon/Icon';
import { SearchPanel } from 'components/SearchPanel/SearchPanel';
import { AutoAssignSwitcher } from 'components/AutoAssignSwitcher';
import { Switcher } from 'components/Switcher/Switcher';
import TableSettings from 'components/TableSettings/TableSettings';

import { useTripsSettings } from 'modules/Trips/context/TripsSettings.context';
import { defaultPagination } from 'hooks/useQuery';
import { SettingsColumnsType, TableSettingsType } from 'hooks/useTableSettings';
import { activeTripStatuses, tripStatuses } from 'constants/trips.constants';
import { useRoleMap } from 'hooks/useRoleMap';
import useFiltersCount from 'hooks/useFiltersCount';

import { Panel } from '../Panel';
import { useTripsModal } from '../../context/TripsModal';
import { FiltersModal } from './FiltersModal';
import { useTripsQuery } from '../../context/TripsQuery';
import { DownloadModal } from '../DownloadModal/DownloadModal';
import { Statistics } from '../Statistics/Statistics';
import { DurationFilter } from '../DurationFilter/DurationFilter';

import styles from './index.module.scss';

interface FiltersProps {
  isSelfTripsVisible: boolean;
  setIsSelfTripsVisible: Dispatch<SetStateAction<boolean>>;
  columns: SettingsColumnsType;
  tableSettings: TableSettingsType;
  onSaveTableSettings: (settings: TableSettingsType) => Promise<void>;
}

export const Filters: FC<FiltersProps> = ({
  isSelfTripsVisible,
  setIsSelfTripsVisible,
  columns,
  tableSettings,
  onSaveTableSettings,
}) => {
  const { isAdmin } = useRoleMap();

  const {
    settings, setIsTableVisible, setIsMapVisible,
  } = useTripsSettings();
  const { map, table } = settings;

  const {
    query, setQuery, setPagination,
  } = useTripsQuery();
  const { openFilters, openDownload } = useTripsModal();

  const { filtersCount } = useFiltersCount(query, ['expectedTime', 'desireDateEnd', 'requestHumanReadableId']);

  const handleSearch = useCallback(
    (value: string) => {
      setQuery({
        statuses: value ? tripStatuses : activeTripStatuses,
        requestHumanReadableId: value || undefined,
      });
    },
    [setQuery]
  );

  const handleSelfTripsVisible = (value: boolean) => {
    setPagination(defaultPagination);
    setIsSelfTripsVisible(value);
  };

  return (
    <div>
      <Panel className={styles.filtersPanel}>
        <div className={cn(styles.filtersCol, styles.leftCol)}>
          <SearchPanel
            placeholder="OT-0001-00000001"
            onSearch={handleSearch}
            value={query.requestHumanReadableId}
            isShort
          />
          <Button
            filter
            counter={filtersCount}
            className={styles.filterBtn}
            type="primary"
            onClick={openFilters}
          />
          <Switcher
            size="small"
            checked={map.isVisible}
            onChange={setIsMapVisible}
            title="Карта"
          />
          <Switcher
            size="small"
            checked={table.isVisible}
            onChange={setIsTableVisible}
            title="Заявки"
          />
          <Switcher
            size="small"
            checked={isSelfTripsVisible}
            onChange={handleSelfTripsVisible}
            title="Мои"
            disabled={isAdmin}
          />
        </div>
        <div className={styles.statistics}>
          <Statistics />
        </div>
        <DurationFilter />
        <div className={cn(styles.filtersCol, styles.rightCol)}>
          <AutoAssignSwitcher size="small" />
          <TableSettings
            columns={columns}
            tableSettings={tableSettings}
            onSaveTableSettings={onSaveTableSettings}
            iconType="bordered"
          />
          <Tooltip
            overlayClassName={styles.tooltip}
            placement="topLeft"
            title="Выгрузка реестра"
          >
            <Button
              type="primary"
              className={styles.buttonLoad}
              onClick={openDownload}
            >
              <Icon type="import" color="#909090" />
            </Button>
          </Tooltip>
        </div>
      </Panel>

      <FiltersModal />
      <DownloadModal />
    </div>
  );
};
