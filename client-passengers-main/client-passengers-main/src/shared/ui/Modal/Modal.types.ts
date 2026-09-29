import React, { DetailedHTMLProps, HTMLAttributes } from 'react';

export type TModalProps = {
  title: string;
  handleConfirm: () => Promise<number | void>;
  handleDecline?: (val: boolean) => void;
  initAfterConfirm?: () => void;
  style?: {
    height?: number;
    width?: number;
    justifyContent?: string;
    textAlign?: React.CSSProperties['textAlign'];
    margin?: React.CSSProperties['margin'];
    padding?: React.CSSProperties['padding'];
  };
  confirmButton?: {
    text: string;
  };
  declineButton?: {
    text: string;
    hide?: boolean;
  };
  withoutScrolls?: boolean;
} | null;

export type TModalType = 'confirm' | 'decline' | 'rate';

export type TModalTypes = {
  type: TModalType;
} | null;

export interface TModalMessageContent {
  title?: string;
  duration?: number;
  body?: (string | DetailedHTMLProps<HTMLAttributes<HTMLElement>, HTMLElement>)[] | string;
}
