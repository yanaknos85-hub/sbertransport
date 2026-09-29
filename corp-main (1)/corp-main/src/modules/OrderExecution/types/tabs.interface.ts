import { Tab } from '../constants/tabs.constants';

interface TabOrderNumbers {
  orders: number;
  newOrders: number;
}

export interface TabCategory extends TabOrderNumbers {
  id: Tab;
}

export type TabInfo = Record<string, TabOrderNumbers>;

export interface ITabProps {
  activeTabKey: Tab;
  setActiveTabKey: (tabName: Tab) => void;
}
