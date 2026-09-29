import React from 'react';

import { UUID } from 'utils/io-ts';

/**
 * Брокер кросс-микрофронтовой связи "редактирование смены".
 *
 * Структура дублирует контракт из хоста (disp-front/src/context/ShiftEdit.context.tsx).
 * Платформа регистрирует обработчик открытия CreateModal по id смены,
 * флит вызывает его через openShift(shiftId).
 */
export type OpenShiftHandler = (shiftId: UUID) => void;

export interface ShiftEditApi {
  register: (handler: OpenShiftHandler) => void;
  openShift: (shiftId: UUID) => void;
}

export const ShiftEditContext = React.createContext<ShiftEditApi>({} as ShiftEditApi);

export const useShiftEditApi = (): ShiftEditApi => React.useContext(ShiftEditContext);
