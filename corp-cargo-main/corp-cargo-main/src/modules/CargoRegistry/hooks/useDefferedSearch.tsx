import React, { useState, useCallback } from 'react';
import { SearchResponse } from "stores/CargoRegistry/CargoRegistry.interface";

interface UseDeferredSearchParams<TQuery, TResponse> {
  // Хук для поиска по организации
  orgHook: (orgId: string | null | undefined) => any;
  // Хук для поиска по исполнителям
  executorHook: (execId?: string[] | undefined, emptyExecutorGroup?: boolean) => any;
  organizationId: string | null | undefined;
  executorGroupId?: string[] | undefined;
  emptyExecutorGroup?: boolean;
  allExecutorGroups?: boolean;
  isOrganization: boolean;
  initialData?: TResponse;
}

interface UseDeferredSearchResult<TQuery, TResponse> {
  // Выполнение поиска
  executeSearch: (query: TQuery) => Promise<void>;
  data: TResponse | undefined | SearchResponse;
  isLoadingOrg: boolean;
  isLoadingExec: boolean;
}

/**
 * Универсальный хук для выполнения отложенного поиска
 * с поддержкой двух типов запросов: по организации и по исполнителям
 */
export const useDeferredSearch = <TQuery, TResponse>({
   orgHook,
   executorHook,
   organizationId,
   executorGroupId,
   emptyExecutorGroup,
   allExecutorGroups,
   isOrganization,
   initialData,
 }: UseDeferredSearchParams<TQuery, TResponse>): UseDeferredSearchResult<TQuery, TResponse> => {

  const [searchOrg, { isLoading: isLoadingOrg }] = orgHook(organizationId);
  const [searchExec, { isLoading: isLoadingExec }] = executorHook(executorGroupId, emptyExecutorGroup);

  const [data, setData] = useState<TResponse | undefined | SearchResponse>(initialData);

  const executeSearch = useCallback(async (query: TQuery): Promise<void> => {
    try {
      if (isOrganization && organizationId) {
        const result = await searchOrg(query);
        setData(result?.responseData);
      } else if (executorGroupId?.length || emptyExecutorGroup || allExecutorGroups) {
        const result = await searchExec(query);
        setData(result?.responseData);
      } else {
        setData(undefined);
      }
    } catch (error) {
      console.error('Search error:', error);
      throw error;
    }
  }, [
    isOrganization,
    organizationId,
    executorGroupId,
    emptyExecutorGroup,
    allExecutorGroups,
    searchOrg,
    searchExec,
  ]);

  return {
    executeSearch,
    data,
    isLoadingOrg,
    isLoadingExec,
  };
}
