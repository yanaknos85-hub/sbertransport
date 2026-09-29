import React, { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { Button } from 'antd';
import classNames from 'classnames';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { CategoryItem } from '../../interfaces/Orders.types';
import categories from './categories';
import AccessControl from 'shared/components/AccessControl';
import { useRole } from 'utils/useRole';
import { useProfile } from 'api/profile';

import styles from './styles.module.scss';

const FilterCategory: FC = () => {
  const { passengerStore } = useAppStoreContext();
  const roles = useRole();
  const { path } = useRouteMatch();
  const { replace } = useHistory();
  const { service, category } = useParams<{ type: CategoryItem }>();
  const { data: profile } = useProfile();

  const clickHandler = (item: CategoryItem) => {
    if (item.available) {
      passengerStore.setPersonFilterQueryProps({ transportType: item.category.toUpperCase() });
      if (profile.isOrganization) {
        passengerStore.getPersonOrderList();
      } else {
        passengerStore.getPersonOrderListExec();
      }
    }
  };

  useEffect(() => {
    const item = categories[service].find(cat => cat.category === category);

    if (item && category !== passengerStore.category) {
      passengerStore.setCategory(category);
      clickHandler(item);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category]);

  const handleChangeTab = (value: string) => {
    replace(path.replace(':service', service).replace(':category', value));
    const item = categories[service].find(cat => cat.category === category);
    if (item) {
      passengerStore.setSavePersonFilterQueryProps({
        transportType: item.category.toUpperCase(), page: 0, pageSize: 0,
      });
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
