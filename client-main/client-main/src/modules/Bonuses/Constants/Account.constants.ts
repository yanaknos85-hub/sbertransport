export const CARDS_CONTENT = {
  header: 'Бонусный счет \n от СберТранспорта',
  headerDesc: 'Копи бонусы для более комфортных поездок',
  grids: [
    {
      title: 'Как копить',
      desc:
        'Бонусный счет, является частью мотивационной '
        + 'програмы Сбертранспорта, руководитель может отметить Вас за '
        + 'результативность и пополнить Ваш баланс.',
      svg: 'FirstGrid',
    },
    {
      title: 'Куда потратить',
      desc:
        'При помощи бонусных баллов вы можете воспользоваться повышенным '
        + 'классом такси comfort, comfort+, vip, доступных в вашем регионе.',
      svg: 'CarSvg',
    },
    {
      title: 'Как использовать',
      desc:
        'Для того что бы воспользоваться доступными Вам бонусами, необходимо при выборе '
        + 'класса такси, указать использование бонусного счета.',
    },
  ],
};

export const ACCOUNT_TABLE_TEXTS = {
  bonusText: 'Бонусный счет',
  submit: 'Пополнить',
  devExtra: 'Функционал находится в разработке',
  tableTitle: 'История пополнения и списания с бонусного счета',
  reason: 'Расходы и пополнения',
  date: 'Дата',
  sum: 'Сумма',
};

export const ACCOUNT_TABLE_ROWS = [
  {
    name: '№',
    style: 'tableColumn1',
  },
  {
    name: ACCOUNT_TABLE_TEXTS.reason,
    style: 'tableColumn2',
    type: 'reason',
  },
  {
    name: ACCOUNT_TABLE_TEXTS.date,
    style: 'tableColumn3',
    type: 'updateTime',
  },
  {
    name: ACCOUNT_TABLE_TEXTS.sum,
    style: 'tableColumn3',
    type: 'sum',
  },
];
