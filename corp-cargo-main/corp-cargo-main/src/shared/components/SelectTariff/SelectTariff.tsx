import React, {
  useCallback, useEffect, useState, useMemo
} from 'react';

import { debounce } from '@material-ui/core';
import { Select, Spin } from 'antd';
import { useTranslation } from 'i18n';

import { useSelectTariffsSearch } from 'api/tariffs';
import { useProfile } from 'api/profile';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import { UUID } from 'utils/io-ts';
import { Tariff } from 'stores/Tariffs/Tariffs.interface';

enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
}

export type Props = React.ComponentProps<typeof Select> & {
  isRedistribution?: boolean;
  orgId?: UUID;
  isNewDesign?: boolean;
  transportType: keyof typeof TransportTypesPassenger;
} & {
  defaultValue?: string;
};

export const SelectTariff = ({
  defaultValue,
  isRedistribution,
  transportType,
  isNewDesign = false,
  orgId,
  onChange,
  ...selectProps
}: Props): JSX.Element => {
  const { t } = useTranslation();
  const [tariffs, setTariffs] = useState<Tariff[]>([]);
  const [isFetching, setIsFetching] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [totalPages, setTotalPages] = useState(Infinity);
  const [page, setPage] = useState(0);

  const { organizationId } = useProfile().data;
  // @ts-ignore
  const [fetchTariffs] = useSelectTariffsSearch(orgId || organizationId);

  useEffect(() => {
    if (!searchValue) {
      setIsFetching(false);
      return;
    }

    // @ts-ignore
    fetchTariffs({
      page, humanReadableId: searchValue, transportType,
    }).then(responseTariff => {
      setTariffs([...tariffs, ...(responseTariff?.content as unknown as Tariff[])]);
      setIsFetching(false);
      setTotalPages(responseTariff?.totalPages ?? Infinity);
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, fetchTariffs, searchValue]);

  const handleScroll = useCallback(
    e => {
      const target = e.target as HTMLDivElement;
      if (isFetching || page >= totalPages) {
        return;
      }

      if (target.scrollTop + target.offsetHeight + 200 < target.scrollHeight) {
        return;
      }

      setPage(p => p + 1);
      setIsFetching(true);
    },
    [page, totalPages, isFetching]
  );

  const iconProps = useMemo(() => (isNewDesign ? { suffixIcon: <DownArrowIcon /> } : {}), [isNewDesign]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const handleSearch = useCallback(
    debounce((name: string) => {
      setTariffs([]);
      setTotalPages(Infinity);
      setPage(0);
      setSearchValue(name);
      setIsFetching(true);
    }, 500),
    []
  );

  return (
    <Select
      {...selectProps}
      {...iconProps}
      allowClear
      autoClearSearchValue
      optionFilterProp="children"
      value={selectProps.value}
      defaultValue={defaultValue}
      onSearch={handleSearch}
      onPopupScroll={handleScroll}
      showSearch
      filterOption={false}
      onChange={onChange}
      getPopupContainer={trigger => trigger.parentNode}
    >
      {tariffs.map(({ id, humanReadableId }) => (
        <Select.Option key={id} value={id}>
          <span>
            {humanReadableId}
            {' '}
          </span>
        </Select.Option>
      ))}
      {isFetching && (
        <Select.Option
          key="loading"
          disabled
          value=""
        >
          {t.global.load}
          {' '}
          ...
          <Spin />
        </Select.Option>
      )}
    </Select>
  );
};

SelectTariff.FormItem = SelectTariff as FormItem<typeof SelectTariff>;
