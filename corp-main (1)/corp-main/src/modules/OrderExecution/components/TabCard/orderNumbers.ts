import { Tab } from '../../constants/tabs.constants';
import { TabCategory, TabInfo } from '../../types/tabs.interface';

const getAllOrders = (cards: TabCategory[]): TabInfo => {
  let result = {};

  for (let i = 0; cards.length > i; i += 1) {
    const category = cards[i];

    result = {
      ...result,
      [category.id]: {
        orders: category.orders,
        newOrders: category.newOrders,
      },
    };
  }

  return result;
};

// TODO Переделать, когда кол-во заявок будет приходить с бека
const orderNumbers = (tabKey: Tab): number => {
  const cards = [
    {
      id: Tab.passengers,
      orders: 54375,
      newOrders: 7,
    },
    {
      id: Tab.cargo,
      orders: 102453,
      newOrders: 2,
    },
    {
      id: Tab.carService,
      orders: 17204,
      newOrders: 3,
    },
  ];

  const result = getAllOrders(cards);

  return result[tabKey].orders;
};

export default orderNumbers;
