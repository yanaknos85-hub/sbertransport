import { useGetAllPurposes } from 'api/purposes';

import usePurposeListBase, { PurposeListBase } from './usePurposeListBase';

/**
 * Хук для получения списка целей
 */
const useAllPurposeList = (): PurposeListBase => usePurposeListBase(useGetAllPurposes);

export default useAllPurposeList;
