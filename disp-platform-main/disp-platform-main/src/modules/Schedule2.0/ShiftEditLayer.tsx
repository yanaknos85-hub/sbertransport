import React, {
  FC, Suspense, useEffect, useState
} from 'react';

import { UUID } from 'utils/io-ts';

import { useShiftEditApi } from 'context/ShiftEdit.context';

import { SimpleEditModal } from './modals/SimpleEditModal/SimpleEditModal';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import ErrorBoundary from 'components/ErrorBoundary';

/**
 * Мост из другого микрофронта в платформенную модалку редактирования смены.
 * Регистрирует обработчик в общем брокере хоста; при openShift(shiftId)
 * грузит смену по id и открывает упрощённую модалку редактирования.
 *
 * Обязательно монтируется на уровне платформенного AppProvider,
 * чтобы быть зарегистрированным на любом экране хоста (в т.ч. экранах флота).
 */
const ShiftEditBridge: FC = () => {
  const shiftEditApi = useShiftEditApi();

  const [shiftId, setShiftId] = useState<UUID | null>(null);

  useEffect(() => {
    shiftEditApi.register(setShiftId);
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    return () => shiftEditApi.register(() => { });
  }, [shiftEditApi]);

  if (!shiftId) return null;

  return (
    <SimpleEditModal
      shiftId={shiftId}
      visible
      onClose={() => setShiftId(null)}
    />
  );
};

/**
 * Общий слой редактирования смены для всей платформы.
 * Рендерится на уровне AppProvider и остаётся активным на любом экране хоста.
 */
export const ShiftEditLayer: FC = ({ children }) => (
  <>
    {children}
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <ShiftEditBridge />
      </Suspense>
    </ErrorBoundary>
  </>
);
