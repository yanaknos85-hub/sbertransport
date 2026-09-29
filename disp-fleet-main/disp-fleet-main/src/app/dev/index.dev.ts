import { debug, error } from 'mf/debug';

debug();

import('./bootstrap.dev').catch(error);
export {};
