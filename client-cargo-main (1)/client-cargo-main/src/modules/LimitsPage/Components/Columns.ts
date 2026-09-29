export const columns = (): any[] => [
  {
    title: 'ID заявки',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: 'Дата создания заявки',
    dataIndex: 'creationTime',
    key: 'creationTime',
  },
  {
    title: 'ФИО инициатора',
    dataIndex: 'fullName',
    key: 'fullName',
  },
  {
    title: 'Сумма',
    dataIndex: 'sum',
    key: 'sum',
  },
];
