import { PropsWithChildren } from 'react';

export type InformationBlockProps = PropsWithChildren<object>;
export type InformationBlockHeaderProps = PropsWithChildren<object>;
export interface InformationBlockTitleProps {
  children: string;
}
export interface InformationBlockDescriptionProps {
  children: string;
}
export type InformationBlockBodyProps = PropsWithChildren<object>;
export interface InformationBlockFieldProps {
  children: string;
  title: string;
}
