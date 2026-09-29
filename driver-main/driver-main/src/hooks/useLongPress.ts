/* eslint-disable @stylistic/max-len */
import {
  MouseEvent,
  MouseEventHandler,
  PointerEvent,
  PointerEventHandler,
  TouchEventHandler,
  useCallback,
  useEffect,
  useRef
} from 'react';
import {
  LongPressCallback,
  LongPressCallbackReason,
  LongPressDomEvents,
  LongPressEventType,
  LongPressHandlers,
  LongPressMouseHandlers,
  LongPressOptions,
  LongPressPointerHandlers,
  LongPressReactEvents,
  LongPressResult
} from 'types/longPressTypes';
import { createArtificialReactEvent, getCurrentPosition, isRecognisableEvent } from 'utils/longPress/longPressUtils';

export function useLongPress<
  Target extends Element = Element,
  Context = unknown,
  Callback extends LongPressCallback<Target, Context> = LongPressCallback<Target, Context>
>(
  callback: Callback | null,
  {
    threshold = 400,
    captureEvent = false,
    detect = LongPressEventType.Pointer,
    cancelOnMovement = false,
    cancelOutsideElement = true,
    filterEvents,
    onStart,
    onMove,
    onFinish,
    onCancel,
  }: LongPressOptions<Target, Context> = {}
): LongPressResult<LongPressHandlers<Target>, Context> {
  const isLongPressActive = useRef(false);
  const isPressed = useRef(false);
  const target = useRef<Target>();
  const timer = useRef<ReturnType<typeof setTimeout>>();
  const savedCallback = useRef(callback);
  const startPosition = useRef<{
    x: number;
    y: number;
  } | null>(null);

  const start = useCallback(
    (context?: Context) => (event: LongPressReactEvents<Target>) => {
      // Prevent multiple start triggers
      if (isPressed.current) {
        return;
      }

      // Ignore unrecognised events
      if (!isRecognisableEvent(event)) {
        return;
      }

      // If we don't want all events to trigger long press and provided event is filtered out
      if (filterEvents !== undefined && !filterEvents(event)) {
        return;
      }

      if (captureEvent) {
        event.persist();
      }

      // When touched trigger onStart and start timer
      onStart?.(event, { context });

      // Calculate position after calling 'onStart' so it can potentially change it
      startPosition.current = getCurrentPosition(event);
      isPressed.current = true;
      target.current = event.currentTarget;

      timer.current = setTimeout(() => {
        if (savedCallback.current) {
          savedCallback.current(event, { context });
          isLongPressActive.current = true;
        }
      }, threshold);
    },
    [captureEvent, filterEvents, onStart, threshold]
  );

  const cancel = useCallback(
    (context?: Context) => (event: LongPressReactEvents<Target>, reason?: LongPressCallbackReason) => {
      // Ignore unrecognised events
      if (!isRecognisableEvent(event)) {
        return;
      }

      // Ignore when element is not pressed anymore
      if (!isPressed.current) {
        return;
      }

      startPosition.current = null;

      if (captureEvent) {
        event.persist();
      }

      // Trigger onFinish callback only if timer was active
      if (isLongPressActive.current) {
        onFinish?.(event, { context });
      } else if (isPressed.current) {
        // If not active but pressed, trigger onCancel
        onCancel?.(event, { context, reason: reason ?? LongPressCallbackReason.CancelledByRelease });
      }

      isLongPressActive.current = false;
      isPressed.current = false;
      timer.current !== undefined && clearTimeout(timer.current);
    },
    [captureEvent, onFinish, onCancel]
  );

  const move = useCallback(
    (context?: Context) => (event: LongPressReactEvents<Target>) => {
      // Ignore unrecognised events
      if (!isRecognisableEvent(event)) {
        return;
      }

      // First call callback to allow modifying event position
      onMove?.(event, { context });

      if (cancelOnMovement !== false && startPosition.current) {
        const currentPosition = getCurrentPosition(event);

        if (currentPosition) {
          const moveThreshold = cancelOnMovement === true ? 25 : cancelOnMovement;
          const movedDistance = {
            x: Math.abs(currentPosition.x - startPosition.current.x),
            y: Math.abs(currentPosition.y - startPosition.current.y),
          };

          // If moved outside move tolerance box then cancel long press
          if (movedDistance.x > moveThreshold || movedDistance.y > moveThreshold) {
            cancel(context)(event, LongPressCallbackReason.CancelledByMovement);
          }
        }
      }
    },
    [cancel, cancelOnMovement, onMove]
  );

  const binder = useCallback<LongPressResult<LongPressHandlers<Target>, Context>>(
    (ctx?: Context) => {
      if (callback === null) {
        return {};
      }

      switch (detect) {
        case LongPressEventType.Mouse: {
          const result: LongPressMouseHandlers = {
            onMouseDown: start(ctx) as MouseEventHandler<Target>,
            onMouseMove: move(ctx) as MouseEventHandler<Target>,
            onMouseUp: cancel(ctx) as MouseEventHandler<Target>,
          };

          if (cancelOutsideElement) {
            result.onMouseLeave = (event: MouseEvent<Target>) => {
              cancel(ctx)(event, LongPressCallbackReason.CancelledOutsideElement);
            };
          }

          return result;
        }

        case LongPressEventType.Touch:
          return {
            onTouchStart: start(ctx) as TouchEventHandler<Target>,
            onTouchMove: move(ctx) as TouchEventHandler<Target>,
            onTouchEnd: cancel(ctx) as TouchEventHandler<Target>,
          };

        case LongPressEventType.Pointer: {
          const result: LongPressPointerHandlers = {
            onPointerDown: start(ctx) as PointerEventHandler<Target>,
            onPointerMove: move(ctx) as PointerEventHandler<Target>,
            onPointerUp: cancel(ctx) as PointerEventHandler<Target>,
          };

          if (cancelOutsideElement) {
            result.onPointerLeave = (event: PointerEvent<Target>) => cancel(ctx)(event, LongPressCallbackReason.CancelledOutsideElement);
          }

          return result;
        }
      }
    },
    [callback, cancel, cancelOutsideElement, detect, move, start]
  );

  // Listen to long press stop events on window
  useEffect(() => {
    function listener(event: LongPressDomEvents) {
      const reactEvent = createArtificialReactEvent<Target>(event);
      cancel()(reactEvent);
    }

    window.addEventListener('mouseup', listener);
    window.addEventListener('touchend', listener);
    window.addEventListener('pointerup', listener);

    // Unregister all listeners on unmount
    return () => {
      window.removeEventListener('mouseup', listener);
      window.removeEventListener('touchend', listener);
      window.removeEventListener('pointerup', listener);
    };
  }, [cancel]);

  // Clear timer on unmount
  useEffect(
    () => (): void => {
      timer.current !== undefined && clearTimeout(timer.current);
    },
    []
  );

  // Update callback handle when it changes
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  return binder;
}
