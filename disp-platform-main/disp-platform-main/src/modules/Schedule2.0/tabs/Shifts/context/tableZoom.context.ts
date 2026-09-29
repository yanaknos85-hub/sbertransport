import { useCallback, useState } from 'react';

import { createCallableCtx } from 'utils/createCallableContext';

const ZOOM_STEP = 30;
const CELL_WIDTH = 30 * 4;
const MIN_STEP = 2;
const MAX_STEP = 8;

const useHook = () => {
  const [cellWidth, setCellWidth] = useState(CELL_WIDTH);

  const zoomIn = useCallback(() => {
    setCellWidth(prev => Math.min(ZOOM_STEP * MAX_STEP, prev + ZOOM_STEP));
  }, []);

  const zoomOut = useCallback(() => {
    setCellWidth(prev => Math.max(ZOOM_STEP * MIN_STEP, prev - ZOOM_STEP));
  }, []);

  return {
    cellWidth,
    zoomIn,
    zoomOut,
  };
};

export const [useTableZoom, TableZoomProvider] = createCallableCtx(useHook, { name: 'TableZoomProvider' });
