/* eslint no-use-before-define: 0 */
import {
  MouseEvent as ReactMouseEvent,
  MouseEventHandler,
  PointerEvent as ReactPointerEvent,
  PointerEventHandler,
  TouchEvent as ReactTouchEvent,
  TouchEventHandler
} from 'react';

export enum LongPressEventType {
  Mouse = 'mouse',
  Touch = 'touch',
  Pointer = 'pointer',
}

export enum LongPressCallbackReason {
  CancelledByMovement = 'cancelled-by-movement',
  CancelledByRelease = 'cancelled-by-release',
  CancelledOutsideElement = 'cancelled-outside-element',
}

export type LongPressReactEvents<Target extends Element = Element> =
  | ReactMouseEvent<Target>
  | ReactTouchEvent<Target>
  | ReactPointerEvent<Target>;

export interface LongPressCallbackMeta<Context = unknown> { context?: Context; reason?: LongPressCallbackReason }

export type LongPressCallback<Target extends Element = Element, Context = unknown> = (
  event: LongPressReactEvents<Target>,
  meta: LongPressCallbackMeta<Context>
) => void;

export type LongPressDomEvents = MouseEvent | TouchEvent | PointerEvent;

export interface LongPressOptions<
  Target extends Element = Element,
  Context = unknown,
  EventType extends LongPressEventType = LongPressEventType
> {
  threshold?: number;
  captureEvent?: boolean;
  detect?: EventType;
  filterEvents?: (event: LongPressReactEvents<Target>) => boolean;
  cancelOnMovement?: boolean | number;
  cancelOutsideElement?: boolean;
  onStart?: LongPressCallback<Target, Context>;
  onMove?: LongPressCallback<Target, Context>;
  onFinish?: LongPressCallback<Target, Context>;
  onCancel?: LongPressCallback<Target, Context>;
}

export interface LongPressMouseHandlers<Target extends Element = Element> {
  onMouseDown: MouseEventHandler<Target>;
  onMouseMove: MouseEventHandler<Target>;
  onMouseUp: MouseEventHandler<Target>;
  onMouseLeave?: MouseEventHandler<Target>;
}
export interface LongPressTouchHandlers<Target extends Element = Element> {
  onTouchStart: TouchEventHandler<Target>;
  onTouchMove: TouchEventHandler<Target>;
  onTouchEnd: TouchEventHandler<Target>;
}

export interface LongPressPointerHandlers<Target extends Element = Element> {
  onPointerDown: PointerEventHandler<Target>;
  onPointerMove: PointerEventHandler<Target>;
  onPointerUp: PointerEventHandler<Target>;
  onPointerLeave?: PointerEventHandler<Target>;
}

export type LongPressEmptyHandlers = Record<never, never>;

export type LongPressHandlers<Target extends Element = Element> =
  | LongPressMouseHandlers<Target>
  | LongPressTouchHandlers<Target>
  | LongPressPointerHandlers<Target>
  | LongPressEmptyHandlers;

export type LongPressResult<
  T extends LongPressHandlers<Target> | LongPressEmptyHandlers,
  Context = unknown,
  Target extends Element = Element
> = (context?: Context) => T;

export type LongPressWindowListener = (event: LongPressDomEvents) => void;
