import dayjs from 'dayjs';
import type { Duration } from 'dayjs/plugin/duration';
import { injectable } from 'inversify';
import { action, observable } from 'mobx';

@injectable()
export class ActiveTripStore {
  private timeout: NodeJS.Timeout | null = null;
  @observable waitTimer: Duration = dayjs.duration(0);

  @action.bound startWaitTimer() {
    this.timeout = setTimeout(() => {
      this.waitTimer = this.waitTimer.clone().add(1, 's');
      this.startWaitTimer();
    }, 1000);
  }

  @action.bound stopWaitTimer() {
    if (this.timeout) {
      clearTimeout(this.timeout);
      this.timeout = null;
    }
    this.waitTimer = dayjs.duration(0);
  }
}
