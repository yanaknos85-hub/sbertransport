import React, {
  Dispatch, FC, SetStateAction, useCallback
} from 'react';
import { Menu, Dropdown, Tooltip } from 'antd';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import { Button } from 'components/Button';
import { Icon } from 'components/Icon/Icon';
import { SearchPanel } from 'components/SearchPanel/SearchPanel';
import { AutoAssignSwitcher } from 'components/AutoAssignSwitcher';
import { Switcher } from 'components/Switcher/Switcher';
import { Panel } from '../Panel';
import { defaultPagination } from 'hooks/useQuery';
import useFiltersCount from 'hooks/useFiltersCount';

import { useTripsModal } from '../../context/TripsModal';
import { FiltersModal } from './FiltersModal';
import { useTripsQuery } from '../../context/TripsQuery';
import { DownloadModal } from '../DownloadModal/DownloadModal';
import { MutualSettlementsModal as CargoDownloadModal } from '../MutualSettlementsModal/MutualSettlementsModal';
import { Statistics } from '../Statistics/Statistics';
import styles from './index.module.scss';
import { useTripsSettings } from 'modules/Trips/context/TripsSettings.context';
import { activeTripStatuses, tripStatuses } from 'constants/trips.constants';

interface FiltersProps {
  isSelfTripsVisible: boolean;
  setIsSelfTripsVisible: Dispatch<SetStateAction<boolean>>;
}

export const Filters: FC<FiltersProps> = ({
  isSelfTripsVisible,
  setIsSelfTripsVisible,
}) => {
  const { t } = useTranslation();

  const {
    settings, setIsTableVisible, setIsMapVisible,
  } = useTripsSettings();
  const { map, table } = settings;

  const {
    query, setQuery, setPagination,
  } = useTripsQuery();
  const {
    openFilters, openDownload, openMutualSettlements,
  } = useTripsModal();

  const { filtersCount } = useFiltersCount(query, ['desireDateEnd']);

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

  const dropdownMenu = (
    <Menu>
      <Menu.Item className="Item" onClick={openDownload}>{t.Requests.detailed}</Menu.Item>
      <Menu.Item className="Item" onClick={openMutualSettlements}>{t.Requests.mutualSettlements}</Menu.Item>
    </Menu>
  );

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
          />
        </div>
        <div className={styles.statistics}>
          <Statistics />
        </div>
        <div className={cn(styles.filtersCol, styles.rightCol)}>
          <AutoAssignSwitcher size="small" />
          <Tooltip
            overlayClassName={styles.tooltip}
            placement="topLeft"
            title="Выгрузка реестра"
          >
            <Dropdown overlay={dropdownMenu}>
              <Button
                type="primary"
                className={styles.buttonLoad}
              >
                <Icon type="import" color="#909090" />
              </Button>
            </Dropdown>
          </Tooltip>
        </div>
      </Panel>

      <FiltersModal />
      <DownloadModal />
      <CargoDownloadModal />
    </div>
  );
};
