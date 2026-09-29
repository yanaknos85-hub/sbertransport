import React, { FC } from 'react';
import { Button, Empty } from 'antd';

import EmptyFolder from 'shared/images/EmptyFolder.png';
import { EmptyDataListStyled } from './EmptyFactory.styled';

interface IEmptyFactory {
  back(): void;
  message: string;
  action: string;
}

type TEmptyAction = Pick<IEmptyFactory, 'back'>;
// type TEmptyNoAction = Omit<IEmptyFactory, 'back' | 'action'>;

interface DistribEmptyDataProps {
  title?: string;
  message?: string;
}

const EmptyDataList: FC<DistribEmptyDataProps> = ({ title, message }) => (
  <EmptyDataListStyled>
    <img src={EmptyFolder} alt="" />
    <h3>{title || 'Нет данных для отображения'}</h3>
    {message && <i>{message}</i>}
  </EmptyDataListStyled>
);

const EmptyFactory: FC<Partial<IEmptyFactory>> = ({
  back, message, action,
}) => (
  <Empty
    description={(
      <span>
        {message}
        <br />
        {back && action && (
          <Button style={{ marginTop: 16 }} onClick={back}>
            {action}
          </Button>
        )}
      </span>
    )}
  />
);

const EmptyItem: FC<IEmptyFactory> = ({
  back, message, action,
}) => (
  <EmptyFactory
    back={back}
    message={message}
    action={action}
  />
);

const EmptyRequest: FC<TEmptyAction> = ({ back }) => (
  <EmptyFactory
    back={back}
    message="Заявка с таким номером отсутствует в системе"
    action="Вернуться к списку заявок"
  />
);

const EmptyRequestList: FC = () => <EmptyFactory message="Список заявок пуст" />;
const EmptyApprovementList: FC = () => <EmptyFactory message="У вас нет заявок для согласования" />;
const EmptyFavoriteList: FC = () => <EmptyFactory message="У вас нет любимых адресоа" />;

export {
  EmptyItem as default,
  EmptyRequest,
  EmptyRequestList,
  EmptyApprovementList,
  EmptyFactory,
  EmptyFavoriteList,
  EmptyDataList
};
