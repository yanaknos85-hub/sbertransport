import React, { FC, useMemo, useState } from 'react';

import { Form, Text } from '@sber-sbertransport/ui-kit/src';

import { useProfile } from 'api/profile/profile.api';
import { WaybillFilters } from 'api/waybill/waybill.types';

import useFiltersCount from 'hooks/useFiltersCount';

import { UUID } from 'utils/io-ts';
import { PaginationParams } from 'utils/io-ts/pagination';

import { Button } from 'components/Button';
import { FiltersButton } from 'components/FiltersButton/FiltersButton';
import Flex from 'components/Flex/Flex';
import Panel from 'components/Panel/Panel';
import { SearchPanel } from 'components/SearchPanel/SearchPanel';

import WaybillFiltersModal from './WaybillFiltersModal';

import { ReactComponent as CloseIcon } from 'assets/icons/close-filter.svg';

import styles from './WaybillFiltersModal.module.scss';

const restrictedFilters = ['contractorIds', 'autoparkIds'];

interface Props {
  query: WaybillFilters;
  setQuery: (query: Omit<WaybillFilters, keyof PaginationParams>) => void;
}

const WaybillFiltersComponent: FC<Props> = ({ query, setQuery }) => {
  const [visible, setVisible] = useState(false);

  const [form] = Form.useForm();

  const handleSearch = (value: string) => {
    if (value.length > 0 && value.length < 3) {
      return;
    }

    setQuery({ searchText: value || undefined } as Omit<WaybillFilters, keyof PaginationParams>);
  };

  const { filtersCount } = useFiltersCount(query, restrictedFilters);

  const { contractorId, autoparkId } = useProfile().data;

  const initialValues = useMemo(() => ({
    contractorIds: [contractorId],
    autoparkIds: autoparkId ? [autoparkId as UUID] : undefined,
  }), [autoparkId, contractorId]);

  return (
    <Panel>
      <Flex gap={0}>
        <SearchPanel
          searchValue={query.searchText}
          onSearch={handleSearch}
          placeholder="Поиск по Номеру путевого листа или Госномеру"
          minWidth={410}
        />
        <FiltersButton
          onClick={() => setVisible(true)}
          text="Фильтры"
        />
        {!!filtersCount && (
          <Button type="text" onClick={() => setQuery(initialValues)}>
            <Text type="secondary">Сбросить фильтры</Text>
            <CloseIcon className={styles.closeIcon} />
          </Button>
        )}
      </Flex>

      <WaybillFiltersModal
        visible={visible}
        onClose={() => setVisible(false)}
        query={query}
        setQuery={setQuery}
        form={form}
        initialValues={initialValues}
      />
    </Panel>
  );
};

export default WaybillFiltersComponent;
