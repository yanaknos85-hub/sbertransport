// TODO: Этот хук не используется - заменён на useGetFilters напрямую в List.tsx
// import { useEffect } from 'react';
//
// import { useGetFilters } from '../../api/filters';
//
// export const useExchangeSavedFilters = (
//   setDescriptionAddressFrom: (value: string) => void,
//   setDescriptionAddressTo: (value: string) => void
// ) => {
//   const { data: savedFilters, isLoading } = useGetFilters();
//
//   useEffect(() => {
//     if (!isLoading && savedFilters) {
//       if (savedFilters.addressFrom) {
//         setDescriptionAddressFrom(savedFilters.addressFrom);
//       }
//       if (savedFilters.addressTo) {
//         setDescriptionAddressTo(savedFilters.addressTo);
//       }
//     }
//   }, [savedFilters, isLoading, setDescriptionAddressFrom, setDescriptionAddressTo]);
//
//   return {
//     savedFilters,
//     isLoading,
//   };
// };
