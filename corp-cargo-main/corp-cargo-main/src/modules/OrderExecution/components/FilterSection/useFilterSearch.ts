import { ChangeEvent, useState, useRef, useEffect, useCallback } from 'react';

const MIN_SEARCH_LENGTH = 3;

interface CargoSearchStore {
  cargoQueryFilters?: { id?: string };
  setCargoFilterQueryProps: (props: { id?: string }) => void;
  getCargoSchedulerList: () => void;
  getCargoOrderListDeferredPost: () => void;
}

interface SearchActions {
  searchValue: string;
  handleInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
  handleSearchByInitialsName: (searchString: string) => void;
}

export const useFilterSearch = (
  cargoStore: CargoSearchStore,
  category: string | undefined,
): SearchActions => {
  const [searchValue, setSearchValue] = useState(cargoStore.cargoQueryFilters?.id || '');
  const hasActiveSearch = useRef(false);

  // После изменения ID из модалки фильтров — синхронизируем значение инпута
  useEffect(() => {
    const storeId = cargoStore.cargoQueryFilters?.id || '';
    if (searchValue !== storeId) {
      setSearchValue(storeId);
    }
  }, [cargoStore.cargoQueryFilters?.id]);

  const getList = useCallback(async () => {
    if (category === 'template') {
      await cargoStore.getCargoSchedulerList();
    } else {
      await cargoStore.getCargoOrderListDeferredPost();
    }
  }, [category, cargoStore.getCargoSchedulerList, cargoStore.getCargoOrderListDeferredPost]);

  const handleInputChange = useCallback(
    async (e: ChangeEvent<HTMLInputElement>) => {
      setSearchValue(e.target.value);

      if (e.target.value === '') {
        hasActiveSearch.current = false;
        cargoStore.setCargoFilterQueryProps({
          id: undefined,
        });
        await getList();
      }
    },
    [cargoStore, getList],
  );

  const handleSearchByInitialsName = useCallback(
    async (searchString: string) => {
      const targetString = searchString.trim();
      if (targetString.length >= MIN_SEARCH_LENGTH) {
        hasActiveSearch.current = true;
        cargoStore.setCargoFilterQueryProps({
          id: targetString,
        });
        await getList();
      }
    },
    [cargoStore, getList],
  );

  return {
    searchValue,
    handleInputChange,
    handleSearchByInitialsName,
  };
};

export default useFilterSearch;