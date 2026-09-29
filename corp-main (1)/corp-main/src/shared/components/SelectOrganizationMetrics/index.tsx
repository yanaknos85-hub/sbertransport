import React, { useEffect, useRef, useState } from 'react';
import { Select as SelectAntd } from 'antd';
import debounce from 'lodash/debounce';
import { useOrganizationProjectionMetrics } from 'api/organizations/search';
import { UUID } from 'utils/io-ts';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { Select as NewSelect } from '../Select';

const handleFilterOrganizations = (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  input: string, option: any
): boolean => {
  return option.label && option.label.toLocaleLowerCase().indexOf(input.toLocaleLowerCase()) >= 0;
};

interface IOption {
  value: string;
  label: string;
}

interface ISelectOrganization extends React.ComponentProps<typeof SelectAntd> {
  groupId?: UUID;
  onListChange?: (list: Organization[]) => void;
  isNewDesign?: boolean;
  defaultOption: IOption | undefined;
}

export const SelectOrganizationMetrics: React.FC<ISelectOrganization> = React.memo(({
  groupId, onListChange, isNewDesign = false, defaultOption, ...props
}) => {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(300);
  const {
    data: organizations,
    isLoading,
    refetch,
  } = useOrganizationProjectionMetrics({ query: { groupId, size } }, { suspense: false });
  const selectRef = useRef<HTMLDivElement>(null);
  const scrollPositionRef = useRef(0);

  const options = [
    defaultOption,
    ...(organizations?.content ?? []).map(({ id, officialName }) => ({ value: id, label: officialName })),
  ].filter((current, index, array) => Boolean(current)
  && array.findIndex(elem => elem?.value === current?.value) === index
  ) as IOption[];

  useEffect(() => {
    if (!organizations) return;

    onListChange?.(organizations?.content);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizations]);

  const Select = isNewDesign ? NewSelect : SelectAntd;

  const loadOptions = async () => {
    if (selectRef.current) {
      const dropdown = selectRef.current.querySelector('.ant-select-dropdown');

      if (dropdown) {
        scrollPositionRef.current = dropdown.scrollTop;
      }
    }
    refetch();

    setTimeout(() => {
      if (selectRef.current) {
        const dropdown = selectRef.current.querySelector('.ant-select-dropdown');

        if (dropdown) {
          dropdown.scrollTop = scrollPositionRef.current;
        }
      }
    }, 0);
  };

  const handleSearch = debounce(() => {
    setPage(0);
    loadOptions();
  }, 300);

  const handlePopupScroll = e => {
    const { target } = e;

    if (target.scrollTop + target.offsetHeight === target.scrollHeight && organizations.totalPages > 1) {
      setPage(page + 1);
      setSize(size => size * 2);
      loadOptions();
    }
  };

  return (
    <div ref={selectRef}>
      <Select
        showSearch
        filterOption={handleFilterOrganizations}
        optionFilterProp="children"
        mode="multiple"
        allowClear
        showArrow
        maxTagCount="responsive"
        options={options}
        placeholder="Организации"
        onSearch={handleSearch}
        onPopupScroll={handlePopupScroll}
        loading={isLoading}
        {...props}
      />
    </div>
  );
});
