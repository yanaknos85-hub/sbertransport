import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { useDebounce } from 'use-debounce';
import { useOrganizationProjection, useOrganizationSearch } from 'api/organizations/search';
import { useProfile } from 'api/profile';

export const useOrganizationFiltersSearch = (delayTimeout = 1000) => {
  const {
    organizationId,
  } = useProfile().data;

  const [search, setSearch] = useState('');
  const [debouncedValue] = useDebounce(search, delayTimeout);

  const {
    data, refetch, isFetched,
  } = useOrganizationSearch(debouncedValue, {
    suspense: false, enabled: debouncedValue && debouncedValue.length < 0,
  }
  );

  const handleOnSearch = useCallback((value: string) => {
    setSearch(value);
  }, []);

  const projectionOrganizations = useOrganizationProjection({}, { suspense: false }).data;

  const initialOrganizationOptions = useMemo(() => {
    const foundEntry = projectionOrganizations?.find(entry => entry.id === organizationId);

    return foundEntry
      ? [{
        value: foundEntry.id,
        label: foundEntry.officialName,
      }]
      : [];
  }, [projectionOrganizations, organizationId]);

  const organizations = useMemo(() => {
    return data?.organizations?.length ? data.organizations.map(entry => ({
      value: entry.id,
      label: entry.officialName,
    })) : [];
  }, [data, initialOrganizationOptions]);

  useEffect(() => {
    if (!debouncedValue && !isFetched) return;

    refetch();
  }, [debouncedValue]);

  return {
    organizations,
    handleOnSearch,
  };
};
