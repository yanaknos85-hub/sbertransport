import {
  FilterResponse, FilterValues, useGetFilters, useSaveFilters
} from '../api/filters';

export const useExchangeFilters = (
  form?: any,
  _setDescriptionAddressFrom?: (value: string) => void,
  _setDescriptionAddressTo?: (value: string) => void
) => {
  const getFiltersResult = useGetFilters();
  const [saveFilters, saveFiltersResult] = useSaveFilters();

  const savedFilters = getFiltersResult.data;

  const handleSave = (addressFrom?: string, addressTo?: string) => {
    const filteredValues: FilterValues = {
      addressFrom: addressFrom,
      addressTo: addressTo,
    };

    saveFilters(filteredValues, {
      onSuccess: (data: FilterResponse) => {
        // Если передана форма, заполняем её сохранёнными данными
        if (form) {
          form.setFieldsValue(data);
        }
        // При сохранении адреса НЕ обновляются — они обновляются только при применении фильтров
      },
    });
  };

  return {
    savedFilters,
    handleSave,
    saveFiltersResult,
    isLoading: getFiltersResult.isLoading || saveFiltersResult.isLoading,
  };
};
