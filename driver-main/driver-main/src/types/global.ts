import { FC, PropsWithChildren } from 'react';
import 'react-query';

// eslint-disable-next-line @typescript-eslint/ban-types
export type FCC<P = {}> = FC<PropsWithChildren<P>>;
