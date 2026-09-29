import { routes } from 'constants/routes.constants';
import { inject, injectable } from 'inversify';
import { computed } from 'mobx';
import type { IConfigStore } from 'stores/Config/Config.interface';
import { TYPES } from 'stores/stores.types';

import {
  clear as clearStorage,
  getSudirReAuthAmt,
  removeRefreshToken,
  removeSudirReAuthAmt,
  setSudirReAuthAmt
} from 'utils/storage/storage';

type NavSudirReAuth = ({ reAuth, fail }: { reAuth: string; fail: string }) => void;

export interface INavigator {
  readonly navRoot: () => void;
  readonly navLogout: () => void;
  readonly navSudirReAuth: NavSudirReAuth;
}

@injectable()
export class Navigator implements INavigator {
  @inject(TYPES.IConfigStore)
  private configStore!: IConfigStore;

  @computed
  get history() {
    return this.configStore.history || null;
  }

  readonly navRoot = (): void => {
    removeRefreshToken();

    if (this.history) {
      // this.history.push('/');
      window.location.href = routes.Home;
    }
  };

  readonly navLogout = (): void => {
    if (this.history) {
      // this.history.push('/oauth/logout');
      window.location.href = routes.Logout;
    }
  };

  readonly navSudirReAuth: NavSudirReAuth = ({ reAuth, fail }): void => {
    const locationContext: Location = (window as Window).location;
    const reAuthAmt: number = getSudirReAuthAmt();

    clearStorage();

    if (reAuthAmt < 3) {
      setSudirReAuthAmt(reAuthAmt + 1);
      locationContext.assign(reAuth);
    } else {
      removeSudirReAuthAmt();
      locationContext.assign(fail);
    }
  };
}
