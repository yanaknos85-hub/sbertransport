import { FloatingPanelRef } from 'antd-mobile';
import { injectable } from 'inversify';
import { action, observable } from 'mobx';
import { type RefObject } from 'react';
import { getSafeAreaHeight } from 'utils/getSafeAreaHeigt';

const DEFAULT_BOTTOM_ANCHOR = 65;

@injectable()
export class MainLayoutStore {
  private safeAreaHeight = 0;

  @observable needConfirmVehicle = false;
  @observable floatingPanel: RefObject<FloatingPanelRef | null> | null = null;
  @observable floatingPanelAnchors: number[] = [
    DEFAULT_BOTTOM_ANCHOR,
    window.innerHeight * 0.5,
    window.innerHeight * 0.95,
  ];

  @action.bound askConfirmVehicle() {
    this.needConfirmVehicle = true;
  }

  @action.bound confirmVehicle() {
    this.needConfirmVehicle = false;
  }

  @action.bound initPanel(floatingPanel: RefObject<FloatingPanelRef | null>) {
    this.floatingPanel = floatingPanel;
    this.safeAreaHeight = getSafeAreaHeight();
    this.setBottomAnchor(DEFAULT_BOTTOM_ANCHOR + this.safeAreaHeight);
  }

  @action.bound disableClosingPanel() {
    this.floatingPanelAnchors.shift();
  }

  @action.bound enableClosingPanel() {
    this.floatingPanelAnchors.unshift(DEFAULT_BOTTOM_ANCHOR + this.safeAreaHeight);
  }

  @action.bound setMiddleAnchor(anchor: number) {
    this.floatingPanelAnchors[1] = anchor + this.safeAreaHeight;
  }

  @action.bound setBottomAnchor(anchor: number) {
    this.floatingPanelAnchors[0] = anchor + this.safeAreaHeight;
  }
}
