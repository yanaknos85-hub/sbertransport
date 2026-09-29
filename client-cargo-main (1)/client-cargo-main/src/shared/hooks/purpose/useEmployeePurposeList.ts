import { useGetEmployeePurposes } from 'api/purposes';

import usePurposeListBase, { PurposeListBase } from './usePurposeListBase';

/**
 * Хук для получения списка целей по пользователю
 */
const useEmployeePurposeList = (): PurposeListBase => usePurposeListBase(useGetEmployeePurposes);

export default useEmployeePurposeList;
