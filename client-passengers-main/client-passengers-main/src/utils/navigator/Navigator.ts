import { useHistory as history } from '@sber-sbertransport/mf-core';
import { injectable } from 'inversify';

import {
  clear as clearStorage,
  getSudirReAuthAmt,
  removeRefreshToken,
  removeSudirReAuthAmt,
  setSudirReAuthAmt
} from '../storage';

type NavSudirReAuth = ({ reAuth, fail }: { reAuth: string; fail: string }) => void;

export interface INavigator {
  readonly navRoot: () => void;
  readonly navLogout: () => void;
  readonly navSudirReAuth: NavSudirReAuth;
}

@injectable()
export class Navigator implements INavigator {
  readonly navRoot = (): void => {
    removeRefreshToken();
    history().push('/');
  };

  readonly navLogout = (): void => {
    history().push('/logout');
  };

  readonly navSudirReAuth: NavSudirReAuth = ({ reAuth, fail }): void => {
    const locationContext: Location = (window as Window).location;
    const reAuthAmt: number = getSudirReAuthAmt();

    if (reAuthAmt < 3) {
      setSudirReAuthAmt(reAuthAmt + 1);
      locationContext.assign(reAuth);
    } else {
      removeSudirReAuthAmt();
      locationContext.assign(fail);
    }

    clearStorage();
  };
}
