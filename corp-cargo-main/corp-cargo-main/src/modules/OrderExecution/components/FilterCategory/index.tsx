import React, { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { Button } from 'antd';
import classNames from 'classnames';
import { useParams, useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { CategoryItem } from '../../interfaces/Orders.types';
import categories from './categories';
import AccessControl from 'shared/components/AccessControl';
import { useRole } from 'utils/useRole';

import styles from './styles.module.scss';

const FilterCategory: FC = () => {
  const { cargoStore } = useAppStoreContext();
  const roles = useRole();
  const { path } = useRouteMatch();
  const { replace } = useHistory();
  const { service, category } = useParams<{ type: CategoryItem }>();

  useEffect(() => {
    cargoStore.initializeFilters();
  }, []);

  const clickHandler = (item: CategoryItem) => {
    if (item.category === cargoStore.activeCategory || !item.available) {
      return;
    }

    // Этот метод теперь сам загрузит нужные фильтры из sessionStorage
    cargoStore.setActiveCategory(item.category);
  };

  useEffect(() => {
    const item = categories[service].find(cat => cat.category === category);

    if (item) {
      clickHandler(item);
    }
  }, [category]);

  /* Так как все табы задействуют category: transportType = Тип доставки, а template не имеет к этому отношения, так как другой запрос, приходится
  так управлять изменением URL в зависимости от состояния текущего пути. */

  const handleChangeTab = (value: string) => {
    if(!path.includes(':category') && path.includes('template')) {
      replace(path.replace(':service', service).replace('template', value));
    } else {
      replace(path.replace(':service', service).replace(':category', value));
    }
  };

  return (
    <div className={styles.categoryFilter}>
      <ul className={styles.categoryFilter__list}>
        {categories[service].map(item => (
          <AccessControl
            key={item.name}
            userPermissions={roles}
            allowedPermissions={item.allowedRoles || []}
          >
            <li
              key={item.category}
              className={classNames(
                styles.categoryFilter__item,
                item.category === category && styles.categoryFilter__item_active
              )}
            >
              <Button
                type="text"
                disabled={!item.available}
                className={styles.categoryFilter__button}
                onClick={() => handleChangeTab(item.category)}
              >
                {item.name}
              </Button>
            </li>
          </AccessControl>
        ))}
      </ul>
    </div>
  );
};

export default observer(FilterCategory);
